package view

import app.Styles
import domain.Customer
import javafx.geometry.Orientation
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import rx.javafx.kt.actionEvents
import rx.javafx.kt.onChangedObservable
import rx.javafx.kt.plusAssign
import rx.lang.kotlin.filterNotNull
import rx.lang.kotlin.subscribeWith
import rx.lang.kotlin.toObservable
import tornadofx.*

class CustomerView : View() {
    override val root = BorderPane()
    private val controller: EventController by inject()

    init {
        with(root) {
            top(Label("CUSTOMER").addClass(Styles.heading))

            center(TableView<Customer>()) {
                column("ID", Customer::id)
                column("NAME", Customer::name)

                selectionModel.selectionMode = SelectionMode.MULTIPLE

                //broadcast selections
                controller.selectedCustomers += selectionModel.selectedItems.onChangedObservable()
                        .flatMap { it.toObservable().filterNotNull().toSet() }

                //Import data and refresh event handling
                controller.refreshCustomers.toObservable().startWith(Unit)
                    .flatMap {
                        Customer.all.toList()
                    }.subscribeWith {
                        onNext { items.setAll(it) }
                        onError { alert(Alert.AlertType.ERROR,"PROBLEM!",it.message?:"").show() }
                    }
            }
            left(ToolBar()) {
                orientation = Orientation.VERTICAL
                button("⇇\uD83D\uDD0E") {
                    controller.searchClientUsages += actionEvents().flatMap {
                        controller.selectedCustomers.toObservable().take(1)
                            .flatMap { it.toObservable() }
                            .map { it.id }
                            .toSet()
                    }
                }
                button("⇉\uD83D\uDD0E") {
                    controller.searchClients += actionEvents().flatMap {
                        controller.selectedSalesPeople.toObservable().take(1)
                            .flatMap { it.toObservable() }
                            .flatMap { it.customerAssignments.toObservable() }
                            .distinct()
                            .toSet()
                    }
                }
                button("⇇") {
                    useMaxWidth = true
                    controller.applyCustomers += actionEvents().flatMap {
                        controller.selectedCustomers.toObservable().take(1)
                                .flatMap { it.toObservable() }
                                .map { it.id }
                                .toSet()
                    }
                }
                //remove selected customers
                button("⇉")  {
                    useMaxWidth = true
                    controller.removeCustomers += actionEvents().flatMap {
                        controller.selectedCustomers.toObservable().take(1)
                                .flatMap { it.toObservable() }
                                .map { it.id }
                                .toSet()
                    }
                }
            }
        }
    }
}