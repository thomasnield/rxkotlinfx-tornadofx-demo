package domain

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxjavafx.subscriptions.CompositeBinding
import org.nield.dirtyfx.collections.DirtyObservableList
import org.nield.dirtyfx.extensions.addTo
import org.nield.dirtyfx.tracking.CompositeDirtyProperty
import org.nield.rxkotlinjdbc.execute
import org.nield.rxkotlinjdbc.insert
import org.nield.rxkotlinjdbc.select

class SalesPerson(val id: Int,
                  val firstName: String,
                  val lastName: String) {

    //We maintain a collection of bindings and disposables to unsubscribe them later
    private val bindings = CompositeBinding()
    private val disposables = CompositeDisposable()
    private val dirtyStates = CompositeDirtyProperty()

    // The staged Customer ID's for this SalesPerson
    val customerAssignments = DirtyObservableList(
            assignmentsFor(id).toSequence().map { it.id }.toList()
    ).addTo(dirtyStates)

    //Compares original and new Customer ID assignments and writes them to database
    fun saveAssignments() {

        val new = customerAssignments.asSequence()
                .mapIndexed { index, customerId ->  Assignment(id=-1, salesPersonId = id, customerId=customerId, order=index) }
                .toSet()

        val old = assignmentsFor(id).toSequence().toSet()

        new.asSequence().filter { !old.contains(it) }.forEach { writeAssignment(it) }
        old.asSequence().filter { !new.contains(it) }.forEach { removeAssignment(it.id) }
    }

    fun delete() = db.execute("DELETE FROM SALES_PERSON WHERE ID = ?")
        .parameter(id)
        .toSingle()


    /**Releases any reactive disposables associated with this SalesPerson.
     * This is very critical to prevent memory leaks with infinite hot Observables
     * because they do not know when they are complete
     */
    fun dispose() {
        bindings.dispose()
        disposables.dispose()
    }

    companion object {

        //Retrieves all SalesPerson instances from database
        val all = db.select("SELECT * FROM SALES_PERSON")
                .toPipeline { SalesPerson(it.getInt("ID"), it.getString("FIRST_NAME"), it.getString("LAST_NAME")) }


        fun forId(id: Int) = db.select("SELECT * FROM SALES_PERSON WHERE ID = ?")
                .parameter(id)
                .toPipeline { SalesPerson(it.getInt("ID"), it.getString("FIRST_NAME"), it.getString("LAST_NAME")) }

        // Retrieves all assigned CompanyClient ID's for a given SalesPerson
        fun assignmentsFor(salesPersonId: Int) =
                db.select("SELECT * FROM ASSIGNMENT WHERE SALES_PERSON_ID = :salesPersonId ORDER BY APPLY_ORDER")
                        .parameter("salesPersonId", salesPersonId)
                        .toPipeline { Assignment(it.getInt("ID"), it.getInt("SALES_PERSON_ID"), it.getInt("CUSTOMER_ID"), it.getInt("APPLY_ORDER")) }


        // Creates a new SalesPerson
        fun createNew(firstName: String, lastName: String) =
                db.insert("INSERT INTO SALES_PERSON (FIRST_NAME,LAST_NAME) VALUES (:firstName,:lastName)")
                        .parameter("firstName", firstName)
                        .parameter("lastName", lastName)
                        .blockingFirst { it.getInt(1) }

        //commits assignments
        private fun writeAssignment(assignment: Assignment) =
                db.insert("INSERT INTO ASSIGNMENT (SALES_PERSON_ID, CUSTOMER_ID, APPLY_ORDER) VALUES (:salesPersonId, :customerId, :applyOrder)")
                        .parameter("salesPersonId", assignment.salesPersonId)
                        .parameter("customerId", assignment.customerId)
                        .parameter("applyOrder", assignment.order)
                        .blockingFirst { it.getInt(1) }

        //deletes assignments
        private fun removeAssignment(assignmentId: Int) =
                db.execute("DELETE FROM ASSIGNMENT WHERE ID = :id")
                        .parameter("id",assignmentId)
                        .toSingle()
    }
}