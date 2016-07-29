package domain

import app.flatCollect
import app.toSet
import javafx.beans.binding.Binding
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.paint.Color
import javafx.scene.text.Text
import rx.Observable
import rx.javafx.kt.addTo
import rx.javafx.kt.onChangedObservable
import rx.javafx.kt.toBinding
import rx.lang.kotlin.addTo
import rx.lang.kotlin.subscribeWith
import rx.lang.kotlin.toObservable
import rx.lang.kotlin.withIndex
import rx.subscriptions.CompositeBinding
import rx.subscriptions.CompositeSubscription
import java.util.*

class SalesPerson(val id: Int, val firstName: String, val lastName: String) {

    //We maintain a collection of bindings and subscriptions to unsubscribe them later
    private val bindings = CompositeBinding()
    private val subscriptions = CompositeSubscription()

    //hold original customer assignments for dirty validation
    val originalAssignments: List<Int> by lazy {
        ArrayList<Int>().apply {
            assignmentsFor(id)
                    .map { it.customerId }
                    .subscribeWith {
                        onNext { add(it) }
                        onError { throw RuntimeException(it) }
                    }.addTo(subscriptions)
        }
    }

    //The staged Customer ID's for this SalesPerson
    val customerAssignments: ObservableList<Int> by lazy { FXCollections.observableArrayList(originalAssignments) }


    //A Binding holding formatted concatenations of the CompanyClient ID's for this SalesPerson
    val customerAssignmentsConcat: Binding<Text> by lazy {
        customerAssignments.onChangedObservable().flatMap { list ->
            list.toObservable().map { it.toString() }.reduce("") { x, y -> if (x == "") y else "$x|$y" }
                .map {
                    Text(it).apply {
                        if (!originalAssignments.equals(list)) fill = Color.RED
                    }
                }
        }.toBinding()
         .addTo(bindings)
    }

    //Compares original and new Customer ID assignments and writes them to database
    fun saveAssignments(): Observable<Int> {

        val newItems = customerAssignments.toObservable()
                .withIndex()
                .map { indexed ->  Assignment(-1,id,indexed.value,indexed.index)}
                .toSet()

        val previousItems = assignmentsFor(id).toSet()

        //zip old and new assignments together, compare them, and write changes
        return Observable.zip(newItems, previousItems) { new,old -> new to old }
            .flatMap { newAndOld ->
                val new = newAndOld.first
                val old = newAndOld.second

                Observable.merge(
                        new.toObservable().filter { !old.contains(it) }.flatMap { writeAssignment(it) },
                        old.toObservable().filter { !new.contains(it) }.flatMap { removeAssignment(it.id) }
                )
            }.count()
    }

    fun delete() = db.update("DELETE FROM SALES_PERSON WHERE ID = ?")
        .parameter(id)
        .count()
        .map { id } //return id of deleted SalesPerson

    /**Releases any reactive subscriptions associated with this SalesPerson.
     * This is very critical to prevent memory leaks with infinite hot Observables
     * because they do not know when they are complete
     */
    fun dispose() {
        bindings.dispose()
        subscriptions.unsubscribe()
    }
    companion object {

        //Retrieves all SalesPerson instances from database
        val all: Observable<SalesPerson> = db.select("SELECT * FROM SALES_PERSON")
            .get { SalesPerson(it.getInt("ID"),it.getString("FIRST_NAME"),it.getString("LAST_NAME")) }
            .flatCollect()


        fun forId(id: Int) = db.select("SELECT * FROM SALES_PERSON WHERE ID = ?")
            .parameter(id)
            .get { SalesPerson(it.getInt("ID"),it.getString("FIRST_NAME"),it.getString("LAST_NAME")) }
            .flatCollect()

        //Retrieves all assigned CompanyClient ID's for a given SalesPerson
        fun assignmentsFor(salesPersonId: Int): Observable<Assignment> =
            db.select("SELECT * FROM ASSIGNMENT WHERE SALES_PERSON_ID = ? ORDER BY APPLY_ORDER")
                .parameter(salesPersonId)
                .get { Assignment(it.getInt("ID"), it.getInt("SALES_PERSON_ID"), it.getInt("CUSTOMER_ID"), it.getInt("APPLY_ORDER")) }
                .flatCollect()

        fun createNew(firstName: String, lastName: String) =
                db.update("INSERT INTO SALES_PERSON (FIRST_NAME,LAST_NAME) VALUES (?,?)")
                    .parameters(firstName,lastName)
                    .returnGeneratedKeys()
                    .getAs(Int::class.java)
                    .flatCollect()

        //commits assignments
        private fun writeAssignment(assignment: Assignment) =
                db.update("INSERT INTO ASSIGNMENT (SALES_PERSON_ID, CUSTOMER_ID, APPLY_ORDER) VALUES (?,?,?)")
                    .parameters(assignment.salesPersonId,assignment.customerId,assignment.order)
                    .returnGeneratedKeys()
                    .getAs(Int::class.java)
                    .flatCollect()

        //deletes assignments
        private fun removeAssignment(assignmentId: Int) =
                db.update("DELETE FROM ASSIGNMENT WHERE ID = ?")
                    .parameter(assignmentId)
                    .count()
    }
}