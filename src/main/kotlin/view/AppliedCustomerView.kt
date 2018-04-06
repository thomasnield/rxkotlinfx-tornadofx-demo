package view

import app.Styles
import com.github.thomasnield.rxkotlinfx.actionEvents
import com.github.thomasnield.rxkotlinfx.events
import com.github.thomasnield.rxkotlinfx.onChangedObservable
import com.github.thomasnield.rxkotlinfx.toBinding
import domain.Customer
import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toObservable
import javafx.geometry.Orientation
import javafx.scene.control.TableView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.BorderPane
import tornadofx.*

class AppliedCustomerView : View() {
    override val root = BorderPane()

    private val controller: EventController by inject()
    private var table: TableView<Customer> by singleAssign()

    init {
        with(root) {

            top = label("ASSIGNED CUSTOMERS").addClass(Styles.heading)

            center = tableview<Customer> {
                readonlyColumn("ID", Customer::id)
                readonlyColumn("Name", Customer::name)

                //broadcast selections
                selectionModel.selectedItems.onChangedObservable()
                    .map { it.asSequence().filterNotNull().map { it.id }.toSet() }
                    .subscribe { controller.selectedApplications.onNext(it) }

                //subscribe to selections in SalesPeopleView extract a list of customers
                val selectedIds = selectionModel.selectedItems.onChangedObservable()
                        .map { it.asSequence().filterNotNull().map { it.id }.toSet() }
                        .toBinding()

                //if multiple SalesPeople are selected, we consolidate their customers distinctly.
                //Otherwise we will push out a hot list of Customers for that one SalesPerson.
                //It will update automatically and the switchMap() will kill it when the selection changes
                controller.selectedSalesPeople
                    .switchMap { selectedPeople ->
                        //the switchMap() is raw power! it unsubscribes the previous emission when a new one comes in

                        if (selectedPeople.size == 1) {
                            selectedPeople.toObservable().flatMap {
                                it.customerAssignments.onChangedObservable()
                                    .switchMapSingle {
                                        it.toObservable().flatMapSingle { Customer.forId(it) }.toList()
                                    }
                            }
                        } else {
                            selectedPeople.toObservable()
                                    .flatMap { it.customerAssignments.toObservable() }
                                    .distinct()
                                    .flatMapSingle { Customer.forId(it) }
                                    .toSortedList { x,y -> x.id.compareTo(y.id) }
                                    .toObservable()
                        }
                    }.subscribeBy(
                            onNext = {
                                items.setAll(it)
                                selectWhere { it.id in selectedIds.value?:setOf() }
                                requestFocus()
                                resizeColumnsToFitContent()
                            }
                        )

                table = this
            }
            left = toolbar {
                orientation = Orientation.VERTICAL
                button("▲") {
                    tooltip("Move customer up (CTRL + ↑)")

                    //disable when multiple salespeople selected
                    controller.selectedSalesPeople.map { it.size > 1 }.subscribe { isDisable = it }

                    //broadcast move up requests

                    val keyEvents =  table.events(KeyEvent.KEY_PRESSED).filter { it.isControlDown && it.code == KeyCode.UP }
                    val buttonEvents = actionEvents()

                    Observable.merge(keyEvents, buttonEvents)
                            .filter { table.selectedItem?.id != null }
                            .map { table.selectedItem?.id }
                            .subscribe(controller.moveCustomerUp)

                    useMaxWidth = true
                }
                button("▼") {
                    tooltip("Move customer down (CTRL + ↓)")

                    //disable when multiple salespeople selected
                    controller.selectedSalesPeople.map { it.size > 1 }.subscribe { isDisable = it }

                    //broadcast move down requests
                    val keyEvents =  table.events(KeyEvent.KEY_PRESSED).filter { it.isControlDown && it.code == KeyCode.DOWN }
                    val buttonEvents = actionEvents()

                    Observable.merge(keyEvents, buttonEvents)
                        .filter { table.selectedItem != null }
                        .map { table.selectedItem!!.id }
                        .subscribe { controller.moveCustomerDown.onNext(it) }

                    useMaxWidth = true
                }
                button("\uD83D\uDD0E⇉") {
                    actionEvents().flatMap {
                        controller.selectedApplications.take(1)
                    }.subscribe(controller.searchCustomers)
                }
            }
        }
    }
}