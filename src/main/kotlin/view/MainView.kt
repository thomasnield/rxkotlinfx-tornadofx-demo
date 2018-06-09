package view

import com.github.thomasnield.rxkotlinfx.actionEvents
import javafx.geometry.Orientation
import javafx.scene.layout.BorderPane
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
                    item("Refresh").apply {
                        actionEvents().map { Unit }.subscribe(controller.refreshCustomers)
                        actionEvents().map { Unit }.subscribe(controller.refreshSalesPeople)
                    }
                }
                menu("Edit") {
                    item("Create Customer").actionEvents().map { Unit }.subscribe(controller.createNewCustomer)
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