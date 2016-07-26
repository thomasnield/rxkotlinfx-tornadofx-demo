package view

import domain.SalesPerson
import javafx.scene.control.Label
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableView
import javafx.scene.layout.BorderPane
import rx.javafx.kt.onChangedObservable
import rx.javafx.kt.plusAssign
import rx.lang.kotlin.filterNotNull
import rx.lang.kotlin.subscribeWith
import rx.lang.kotlin.toObservable
import tornadofx.View
import tornadofx.center
import tornadofx.column
import tornadofx.top

class SalesPeopleView: View() {

    override val root = BorderPane()
    private val controller: EventController by inject()

    init {
        with(root) {

            top(Label("SALES PEOPLE"))

            center(TableView<SalesPerson>()) {
                column("ID",SalesPerson::id)
                column("First Name",SalesPerson::firstName)
                column("Last Name",SalesPerson::lastName)
                column("Assigned Clients",SalesPerson::assignmentsBinding)

                selectionModel.selectionMode = SelectionMode.MULTIPLE

                //broadcast selections
                controller.selectedSalesPeople += selectionModel.selectedItems.onChangedObservable()
                        .flatMap { it.toObservable().filterNotNull().toList() }

                //handle refresh events and import data
                controller.refreshSalesPeople.toObservable().startWith(Unit)
                    .flatMap {
                        SalesPerson.all.toList()
                    }.subscribeWith {
                        onNext { items.setAll(it) }
                        onError { it.printStackTrace() }
                    }
            }

        }
        connect()
    }

    private fun connect() {
        with(controller) {
            searchClientUsages.toObservable().subscribeWith {
                onNext {  }
            }
        }
    }
}