package view

import app.*
import domain.SalesPerson
import javafx.geometry.Orientation
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableView
import javafx.scene.paint.Color
import org.controlsfx.glyphfont.FontAwesome
import org.controlsfx.glyphfont.GlyphFontRegistry
import rx.javafx.kt.actionEvents
import rx.javafx.kt.addTo
import rx.javafx.kt.onChangedObservable
import rx.javafx.kt.toObservable
import rx.lang.kotlin.filterNotNull
import rx.lang.kotlin.subscribeWith
import rx.lang.kotlin.toObservable
import tornadofx.*

class SalesPeopleView: View() {
    private val controller: EventController by inject()
    private var table: TableView<SalesPerson> by singleAssign()

    private val fontAwesome = GlyphFontRegistry.font("FontAwesome")
    private val saveGlyph = fontAwesome.create(FontAwesome.Glyph.SAVE)
    private val refreshGlyph = fontAwesome.create(FontAwesome.Glyph.UNDO).color(Color.PURPLE)
    private val addGlyph = fontAwesome.create(FontAwesome.Glyph.PLUS).color(Color.BLUE)
    private val removeGlyph = fontAwesome.create(FontAwesome.Glyph.TIMES).color(Color.RED)

    override val root = borderpane {


        left = toolbar {
            orientation = Orientation.VERTICAL

            //save button
            button("",saveGlyph) {
                useMaxWidth = true
                actionEvents()
                        .map { Unit }
                        .addTo(controller.saveAssignments)
            }

            //refresh button
            button("",refreshGlyph) {
                useMaxWidth = true
                actionEvents()
                        .map { Unit }
                        .addTo(controller.refreshSalesPeople)
            }

            //add button
            button("", addGlyph) {
                tooltip("Create a new Sales Person")
                useMaxWidth = true
                actionEvents().map { Unit }.addTo(controller.createNewSalesPerson)
            }

            //remove button
            //remove customer button
            button("",removeGlyph) {
                tooltip("Remove selected Customers")
                useMaxWidth = true
                actionEvents().flatMap {
                    table.selectionModel.selectedItems.toObservable()
                            .filterNotNull()
                            .map { it.id }
                            .toSet()
                }.addTo(controller.deleteSalesPerson)
            }
        }

        top = label("SALES PEOPLE").addClass(Styles.heading)

        center = tableview<SalesPerson> {
            table = this
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
            controller.removeCustomerUsages.toObservable().subscribeWith {
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
    init {
        //when customers are deleted, remove their usages
        controller.deletedCustomers.toObservable().flatMap { deleteIds ->
            table.items.toObservable().doOnNext { it.customerAssignments.removeAll(deleteIds) }
        }.subscribeWith {
            onNext {  }
            alertError()
        }

        //handle new Sales Person request
        controller.createNewSalesPerson.toObservable().flatMap {
            NewSalesPersonDialog().toObservable()
                    .flatMap { it }
                    .flatMap { SalesPerson.forId(it) }
        }.subscribeWith {
            onNext {
                table.selectionModel.clearSelection()
                table.items.add(it)
                table.selectionModel.select(it)
                table.requestFocus()
            }
            alertError()
        }

        //handle sales person deletions
        controller.deleteSalesPerson.toObservable().flatMap {
            table.currentSelections.toList().flatMap { deleteItems ->
                Alert(Alert.AlertType.WARNING, "Are you sure you want to delete these ${deleteItems.size} sales people?", ButtonType.YES, ButtonType.NO).toObservable()
                    .filter { it == ButtonType.YES }
                    .flatMap {  deleteItems.toObservable() }
                    .flatMap { it.delete() }
                    .toSet()
            }
        }.subscribeWith {
            onNext { deletedIds ->
                table.items.deleteWhere { it.id in deletedIds }
            }
            alertError()
        }
    }
}