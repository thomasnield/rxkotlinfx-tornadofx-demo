package view

import domain.Customer
import javafx.scene.control.TableView
import javafx.scene.layout.BorderPane
import rx.javafx.kt.actionEvents
import rx.javafx.kt.onChangedObservable
import rx.javafx.kt.plusAssign
import rx.lang.kotlin.filterNotNull
import rx.lang.kotlin.subscribeWith
import rx.lang.kotlin.toObservable
import tornadofx.*

class AppliedCustomerView : View() {
    override val root = BorderPane()

    private val controller: EventController by inject()

    init {
        with(root) {

            center(TableView<Customer>()) {
                column("ID", Customer::id)
                column("Name", Customer::name)

                //broadcast selections
                controller.selectedApplications += selectionModel.selectedItems.onChangedObservable()
                    .flatMap { it.toObservable().filterNotNull().map { it.id }.toSet() }

                //subscribe to selections in SalesPeopleView
                controller.selectedSalesPeople.toObservable()
                    .flatMap {
                        it.toObservable().flatMap { it.customerAssignments.toObservable() }
                            .distinct()
                            .flatMap { Customer.forId(it) }
                            .toList()
                    }.subscribeWith {
                        onNext { items.setAll(it) }
                        alertError()
                    }
            }
            left = toolbar {
                button("\uD83D\uDD0E⇉") {
                    controller.searchClients += actionEvents().flatMap {
                        controller.selectedApplications.toObservable().take(1)
                    }
                }
            }
        }
    }
}