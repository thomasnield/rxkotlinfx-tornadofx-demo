package view

import app.Styles
import app.currentSelections
import app.toSet
import com.github.thomasnield.rxkotlinfx.actionEvents
import com.github.thomasnield.rxkotlinfx.events
import com.github.thomasnield.rxkotlinfx.onChangedObservable
import com.github.thomasnield.rxkotlinfx.toMaybe
import domain.Customer
import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toObservable
import javafx.geometry.Orientation
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.paint.Color
import tornadofx.*

class CustomerView : View() {
    private val controller: EventController by inject()
    private var table: TableView<Customer> by singleAssign()

    override val root = borderpane {
        top = label("CUSTOMER").addClass(Styles.heading)

        center = tableview<Customer> {
            readonlyColumn("ID", Customer::id)
            readonlyColumn("NAME", Customer::name)

            selectionModel.selectionMode = SelectionMode.MULTIPLE

            //broadcast selections
            selectionModel.selectedItems.onChangedObservable()
                    .map { it.filterNotNull().toSet() }
                    .subscribe(controller.selectedCustomers )

            //Import data and refresh event handling
            controller.refreshCustomers.startWith(Unit)
                    .flatMapSingle {
                        Customer.all.toList()
                    }.subscribeBy(
                        onNext = { items.setAll(it) },
                        onError = { alert(Alert.AlertType.ERROR, "PROBLEM!", it.message ?: "").show() }
                    )

            //handle search request
            controller.searchCustomers
                .subscribeBy(
                    onNext = { ids ->
                        moveToTopWhere { it.id in ids }
                        requestFocus()
                    },
                    onError  ={ it.printStackTrace() }
                )

            table = this
        }
        left = toolbar {
            orientation = Orientation.VERTICAL

            // search selected customers on AppliedCustomerView
            button("⇇\uD83D\uDD0E") {

                actionEvents().flatMapSingle {
                    controller.selectedCustomers.take(1)
                            .flatMap { it.toObservable() }
                            .map { it.id }
                            .toSet()
                }.subscribe(controller.searchCustomerUsages)
            }
            // search selected applied
            button("⇉\uD83D\uDD0E") {

                actionEvents().flatMapSingle {
                    controller.selectedSalesPeople.take(1)
                            .flatMap { it.toObservable() }
                            .flatMap { it.customerAssignments.toObservable() }
                            .distinct()
                            .toSet()
                }.subscribe(controller.searchCustomers)
            }
            button("⇇") {
                tooltip("Apply selected Customers to Selected Sales Persons (CTRL + ←)")

                useMaxWidth = true
                textFill = Color.GREEN

                val keyEvents = table.events(KeyEvent.KEY_PRESSED).filter { it.isControlDown && it.code == KeyCode.LEFT }
                val buttonEvents = actionEvents()

                Observable.merge(keyEvents, buttonEvents).flatMapSingle {
                    controller.selectedCustomers.take(1)
                            .flatMap { it.toObservable() }
                            .map { it.id }
                            .toSet()
                }.subscribe(controller.applyCustomers)
            }
            //remove selected customers
            button("⇉") {
                tooltip("Remove selected Customers from Selected Sales Persons (CTRL + →)")

                useMaxWidth = true
                textFill = Color.RED

                val keyEvents = table.events(KeyEvent.KEY_PRESSED).filter { it.isControlDown && it.code == KeyCode.RIGHT }
                val buttonEvents = actionEvents()

                Observable.merge(keyEvents,buttonEvents).flatMapSingle {
                    controller.selectedCustomers.take(1)
                            .flatMap { it.toObservable() }
                            .map { it.id }
                            .toSet()
                }.subscribe(controller.removeCustomerUsages)
            }
            //add customer button
            button("\u2795") {
                tooltip("Create a new Customer")
                useMaxWidth = true
                textFill = Color.BLUE

                actionEvents().map { Unit }.subscribe(controller.createNewCustomer)
            }

            //remove customer button
            button("✘") {
                tooltip("Remove selected Customers")
                useMaxWidth = true
                textFill = Color.RED

                actionEvents().map {
                    table.selectionModel.selectedItems
                            .asSequence()
                            .filterNotNull()
                            .map { it.id }
                            .toSet()
                }.subscribe(controller.deleteCustomers)
            }
        }

        //create new Customer requests
        controller.createNewCustomer
            .flatMapMaybe { NewCustomerDialog().toMaybe() }
            .flatMapMaybe { it }
            .flatMapSingle { Customer.forId(it) }
            .subscribe {
                    table.items.add(it)
                    table.selectionModel.clearSelection()
                    table.selectionModel.select(it)
                    table.requestFocus()
                }


        //handle Customer deletions
        val deletions = controller.deleteCustomers
            .flatMapSingle {
                table.currentSelections.toList()
            }.flatMapSingle { deleteItems ->
                Alert(Alert.AlertType.WARNING, "Are you sure you want to delete these ${deleteItems.size} customers?", ButtonType.YES, ButtonType.NO)
                    .toMaybe().filter { it == ButtonType.YES }
                    .map { deleteItems }
                    .flatMapObservable { it.toObservable() }
                    .flatMapSingle { it.delete() }
                    .toSet()
            }.publish() //publish() to prevent multiple subscriptions triggering alert multiple times

        deletions.subscribe(controller.deletedCustomers)

        //refresh on deletion
        controller.deletedCustomers
                .map { Unit }
                .subscribe(controller.refreshCustomers) //push this refresh customers

        //trigger the publish
        deletions.connect()
    }
}