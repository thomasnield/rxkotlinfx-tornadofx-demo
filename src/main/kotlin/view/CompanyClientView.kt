package view

import app.Styles
import domain.ClientCompany
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

class CompanyClientView: View() {
    override val root = BorderPane()
    private val controller: EventController by inject()

    init {
        with(root) {
            top(Label("CLIENT COMPANIES").addClass(Styles.heading))

            center(TableView<ClientCompany>()) {
                column("ID",ClientCompany::id)
                column("NAME",ClientCompany::name)

                selectionModel.selectionMode = SelectionMode.MULTIPLE

                //broadcast selections
                controller.selectedClients += selectionModel.selectedItems.onChangedObservable()
                        .flatMap { it.toObservable().filterNotNull().toSet() }

                //Import data and refresh event handling
                controller.refreshCompanyClients.toObservable().startWith(Unit)
                    .flatMap {
                        ClientCompany.all.toList()
                    }.subscribeWith {
                        onNext { items.setAll(it) }
                        onError { alert(Alert.AlertType.ERROR,"PROBLEM!",it.message?:"").show() }
                    }
            }
            left(ToolBar()) {
                orientation = Orientation.VERTICAL
                button("⇇\uD83D\uDD0E") {
                    controller.searchClientUsages += actionEvents().flatMap {
                        controller.selectedClients.toObservable().take(1)
                            .flatMap { it.toObservable() }
                            .map { it.id }
                            .toSet()
                    }
                }
                button("⇉\uD83D\uDD0E") {
                    controller.searchClients += actionEvents().flatMap {
                        controller.selectedSalesPeople.toObservable().take(1)
                            .flatMap { it.toObservable() }
                            .flatMap { it.assignments.toObservable() }
                            .distinct()
                            .toSet()
                    }
                }
                button("⇇") {
                    useMaxWidth = true
                    controller.applyClients += actionEvents().flatMap {
                        controller.selectedClients.toObservable().take(1)
                                .flatMap { it.toObservable() }
                                .map { it.id }
                                .toSet()
                    }
                }
                button("⇉")  {
                    useMaxWidth = true
                }
            }
        }
    }
}