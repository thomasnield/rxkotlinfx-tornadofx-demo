package view

import app.Styles
import app.currentSelections
import app.toSet
import domain.Customer
import javafx.geometry.Orientation
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.paint.Color
import rx.Observable
import rx.javafx.kt.*
import rx.lang.kotlin.filterNotNull
import rx.lang.kotlin.subscribeWith
import rx.lang.kotlin.toObservable
import tornadofx.*

class CustomerView : View() {
    private val controller: EventController by inject()
    private var table: TableView<Customer> by singleAssign()

    override val root = borderpane {
        top = label("CUSTOMER").addClass(Styles.heading)

        center = tableview<Customer> {
            column("ID", Customer::id)
            column("NAME", Customer::name)

            selectionModel.selectionMode = SelectionMode.MULTIPLE

            //broadcast selections
            selectionModel.selectedItems.onChangedObservable()
                    .flatMap { it.toObservable().filterNotNull().toSet() }
                    .addTo(controller.selectedCustomers )

            //Import data and refresh event handling
            controller.refreshCustomers.toObservable().startWith(Unit)
                    .flatMap {
                        Customer.all.toList()
                    }.subscribeWith {
                        onNext { items.setAll(it) }
                        onError { alert(Alert.AlertType.ERROR, "PROBLEM!", it.message ?: "").show() }
                    }

            //handle search request
            controller.searchCustomers.toObservable()
                .subscribeWith {
                    onNext { ids -> moveToTopWhere { it.id in ids } }
                    onError { it.printStackTrace() }
                }

            table = this
        }
        left = toolbar {
            orientation = Orientation.VERTICAL
            button("⇇\uD83D\uDD0E") {

                actionEvents().flatMap {
                    controller.selectedCustomers.toObservable().take(1)
                            .flatMap { it.toObservable() }
                            .map { it.id }
                            .toSet()
                }.addTo(controller.searchCustomerUsages)
            }
            button("⇉\uD83D\uDD0E") {

                actionEvents().flatMap {
                    controller.selectedSalesPeople.toObservable().take(1)
                            .flatMap { it.toObservable() }
                            .flatMap { it.customerAssignments.toObservable() }
                            .distinct()
                            .toSet()
                }.addTo(controller.searchCustomers)
            }
            button("⇇") {
                tooltip("Apply selected Customers to Selected Sales Persons (CTRL + ←)")

                useMaxWidth = true
                textFill = Color.GREEN

                val keyEvents = table.events(KeyEvent.KEY_PRESSED).filter { it.isControlDown && it.code == KeyCode.LEFT }
                val buttonEvents = actionEvents()

                Observable.merge(keyEvents, buttonEvents).flatMap {
                    controller.selectedCustomers.toObservable().take(1)
                            .flatMap { it.toObservable() }
                            .map { it.id }
                            .toSet()
                }.addTo(controller.applyCustomers)
            }
            //remove selected customers
            button("⇉") {
                tooltip("Remove selected Customers from Selected Sales Persons (CTRL + →)")

                useMaxWidth = true
                textFill = Color.RED

                val keyEvents = table.events(KeyEvent.KEY_PRESSED).filter { it.isControlDown && it.code == KeyCode.RIGHT }
                val buttonEvents = actionEvents()

                Observable.merge(keyEvents,buttonEvents).flatMap {
                    controller.selectedCustomers.toObservable().take(1)
                            .flatMap { it.toObservable() }
                            .map { it.id }
                            .toSet()
                }.addTo(controller.removeCustomerUsages)
            }
            //add customer button
            button("\u2795") {
                tooltip("Create a new Customer")
                useMaxWidth = true
                textFill = Color.BLUE

                actionEvents().map { Unit }.addTo(controller.createNewCustomer)
            }

            //remove customer button
            button("✘") {
                tooltip("Remove selected Customers")
                useMaxWidth = true
                textFill = Color.RED

                actionEvents().flatMap {
                    table.selectionModel.selectedItems.toObservable()
                        .filterNotNull()
                        .map { it.id }
                        .toSet()
                }.addTo(controller.deleteCustomers)
            }
        }

        //create new Customer requests
        controller.createNewCustomer.toObservable()
            .flatMap { NewCustomerDialog().toObservable() }
            .flatMap { it }
            .flatMap { Customer.forId(it) }
            .subscribeWith {
                onNext {
                    table.items.add(it)
                    table.selectionModel.clearSelection()
                    table.selectionModel.select(it)
                    table.requestFocus()
                }
            }

        //handle Customer deletions
        val deletions = controller.deleteCustomers.toObservable()
            .flatMap {
                table.currentSelections().toList()
            }.flatMap { deleteItems ->
                Alert(Alert.AlertType.WARNING, "Are you sure you want to delete these ${deleteItems.size} customers?", ButtonType.YES, ButtonType.NO)
                    .toObservable().filter { it == ButtonType.YES }
                    .map { deleteItems }
                    .flatMap { it.toObservable() }
                    .flatMap { it.delete() }
                    .toSet()
            }.publish() //publish() to prevent multiple subscriptions triggering alert multiple times

        deletions.addTo(controller.deletedCustomers)

        //refresh on deletion
        controller.deletedCustomers.toObservable().map { Unit }.addTo(controller.refreshCustomers) //push this refresh customers

        //trigger the publish
        deletions.connect()
    }
}