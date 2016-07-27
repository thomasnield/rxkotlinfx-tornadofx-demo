package view

import app.*
import domain.SalesPerson
import javafx.geometry.Orientation
import javafx.scene.control.SelectionMode
import javafx.scene.layout.BorderPane
import javafx.scene.paint.Color
import rx.javafx.kt.actionEvents
import rx.javafx.kt.addTo
import rx.javafx.kt.onChangedObservable
import rx.javafx.kt.plusAssign
import rx.lang.kotlin.filterNotNull
import rx.lang.kotlin.subscribeWith
import rx.lang.kotlin.toObservable
import tornadofx.*

class SalesPeopleView: View() {
    override val root = BorderPane()
    private val controller: EventController by inject()

    init {
        with(root) {

            left = toolbar {
                orientation = Orientation.VERTICAL

                //save button
                button("\uD83D\uDCBE") {
                    useMaxWidth = true
                    actionEvents()
                        .map { Unit }
                        .addTo(controller.saveAssignments)
                }

                //refresh button
                button("↺") {
                    textFill = Color.BLUE
                    useMaxWidth = true
                    actionEvents()
                        .map { Unit }
                        .addTo(controller.refreshSalesPeople)
                }
            }

            top = label("SALES PEOPLE").addClass(Styles.heading)

            center = tableview<SalesPerson> {
                column("ID",SalesPerson::id)
                column("First Name",SalesPerson::firstName)
                column("Last Name",SalesPerson::lastName)
                column("Assigned Clients",SalesPerson::customerAssignmentsConcat)

                selectionModel.selectionMode = SelectionMode.MULTIPLE

                //broadcast selections
                 selectionModel.selectedItems.onChangedObservable()
                        .flatMap { it.toObservable().filterNotNull().toSet() }
                        .addTo(controller.selectedSalesPeople)

                //handle search requests
                controller.searchCustomerUsages.toObservable().subscribeWith {
                    onNext { ids ->
                        moveToTopWhere { it.customerAssignments.any { it in ids } }
                        requestFocus()
                    }
                    alertError()
                }

                //handle adds
                controller.applyCustomers.toObservable().subscribeWith {
                    onNext { ids ->
                        selectionModel.selectedItems.asSequence().filterNotNull().forEach {
                            it.customerAssignments.addIfAbsent(*ids.toTypedArray())
                        }
                    }
                    alertError()
                }

                //handle removals
                controller.removeCustomers.toObservable().subscribeWith {
                    onNext { ids ->
                        selectionModel.selectedItems.asSequence().filterNotNull().forEach {
                            it.customerAssignments.removeAll(ids)
                        }
                    }
                    alertError()
                }

                //handle commits
                controller.saveAssignments.toObservable().flatMap {
                    items.toObservable().flatMap { it.saveAssignments() }
                        .reduce { x,y -> x + y}
                        .doOnNext { println("Committed $it changes") }
                }.map { Unit }
                .addTo(controller.refreshSalesPeople)

                //handle refresh events and import data
                controller.refreshSalesPeople.toObservable()
                        .doOnNext { items.forEach { it.dispose() } } //important to kill subscriptions on each SalesPerson
                        .startWith(Unit)
                        .flatMap {
                            SalesPerson.all.toList()
                        }.subscribeWith {
                    onNext { items.setAll(it) }
                    alertError()
                }

                //handle move up and move down requests
                controller.moveCustomerUp.toObservable()
                        .map { it to selectedItem?.customerAssignments }
                        .filter { it.second != null }
                        .subscribe { it.second!!.moveUp(it.first) }

                //handle move up and move down requests
                controller.moveCustomerDown.toObservable()
                        .map { it to selectedItem?.customerAssignments }
                        .filter { it.second != null }
                        .subscribe { it.second!!.moveDown(it.first) }
            }

        }
    }
}