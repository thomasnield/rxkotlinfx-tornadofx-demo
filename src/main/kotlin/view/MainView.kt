package view

import javafx.geometry.Orientation
import javafx.scene.control.MenuBar
import javafx.scene.control.SplitPane
import javafx.scene.layout.BorderPane
import rx.javafx.kt.actionEvents
import rx.javafx.kt.addTo
import rx.javafx.kt.plusAssign
import tornadofx.*

class MainView : View() {
    override val root = BorderPane()

    private val salesPeopleView: SalesPeopleView by inject()
    private val companyClientView: CustomerView by inject()
    private val appliedCustomerView: AppliedCustomerView by inject()

    private val controller: EventController by inject()

    init {
        title = "Client/Salesperson Assignments"

        with(root) {
            setPrefSize(940.0,610.0)
            top = menubar {
                menu("File") {
                    menuitem("Refresh").apply {
                        controller.refreshCustomers += actionEvents().map { Unit }
                        controller.refreshSalesPeople += actionEvents().map { Unit }
                    }
                }
                menu("Edit") {
                    menuitem("Create Customer").actionEvents().map { Unit }.addTo(controller.createNewCustomer)
                }
            }
            center = splitpane {
                orientation = Orientation.HORIZONTAL
                splitpane {
                    orientation = Orientation.VERTICAL
                    this += salesPeopleView
                    this += appliedCustomerView
                }
                this += companyClientView
            }
        }
    }
}