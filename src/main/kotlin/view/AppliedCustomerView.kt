package view

import app.Styles
import app.alertError
import app.toSet
import domain.Customer
import javafx.geometry.Orientation
import javafx.scene.control.TableView
import javafx.scene.layout.BorderPane
import rx.javafx.kt.actionEvents
import rx.javafx.kt.addTo
import rx.javafx.kt.onChangedObservable
import rx.javafx.kt.toBinding
import rx.lang.kotlin.filterNotNull
import rx.lang.kotlin.subscribeWith
import rx.lang.kotlin.toObservable
import tornadofx.*

class AppliedCustomerView : View() {
    override val root = BorderPane()

    private val controller: EventController by inject()
    private var table: TableView<Customer> by singleAssign()

    init {
        with(root) {

            top = label("ASSIGNED CUSTOMERS").addClass(Styles.heading)

            center = tableview<Customer> {
                column("ID", Customer::id)
                column("Name", Customer::name)

                //broadcast selections
                selectionModel.selectedItems.onChangedObservable()
                    .flatMap { it.toObservable().filterNotNull().map { it.id }.toSet() }
                    .addTo(controller.selectedApplications)

                //subscribe to selections in SalesPeopleView extract a list of customers
                val selectedIds = selectionModel.selectedItems.onChangedObservable().filterNotNull()
                        .filter { it[0] != null }
                        .flatMap { it.toObservable().map { it.id }.distinct().toSet() }
                        .filterNotNull()
                        .toBinding()

                controller.selectedSalesPeople.toObservable()
                    .switchMap { selectedPeople ->

                        if (selectedPeople.size == 1) {
                            selectedPeople.toObservable().flatMap {
                                it.customerAssignments.onChangedObservable()
                                    .switchMap {
                                        it.toObservable().flatMap { Customer.forId(it) }.toList()
                                    }
                            }
                        } else {
                            selectedPeople.toObservable().flatMap { it.customerAssignments.toObservable() }
                                    .distinct()
                                    .flatMap { Customer.forId(it) }
                                    .toList()
                        }
                    }.filterNotNull().subscribeWith {
                        onNext {
                            items.setAll(it)
                            selectWhere { it.id in selectedIds.value?:setOf() }
                            requestFocus()
                            resizeColumnsToFitContent()
                        }
                        alertError()
                    }

                table = this
            }
            left = toolbar {
                orientation = Orientation.VERTICAL
                button("▲") {

                    //disable when multiple salespeople selected
                    controller.selectedSalesPeople.toObservable().map { it.size > 1 }.subscribe { isDisable = it }

                    //broadcast move up requests
                    actionEvents().map { table.selectedItem?.id }.filterNotNull().addTo(controller.moveSelectedCustomerUp)

                    useMaxWidth = true
                }
                button("▼") {

                    //disable when multiple salespeople selected
                    controller.selectedSalesPeople.toObservable().map { it.size > 1 }.subscribe { isDisable = it }

                    //broadcast move down requests
                    actionEvents().map { table.selectedItem?.id }.filterNotNull().addTo(controller.moveSelectedCustomerDown)

                    useMaxWidth = true
                }
                button("\uD83D\uDD0E⇉") {
                    actionEvents().flatMap {
                        controller.selectedApplications.toObservable().take(1)
                    }.addTo(controller.searchCustomers)
                }
            }
        }
    }
}