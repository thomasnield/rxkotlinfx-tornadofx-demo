package view

import javafx.geometry.Orientation
import javafx.scene.layout.BorderPane
import rx.javafx.kt.actionEvents
import rx.javafx.kt.plusAssign
import tornadofx.*

class MainView : View() {
    override val root = BorderPane()

    private val salesPeopleView: SalesPeopleView by inject()
    private val companyClientView: CompanyClientView by inject()

    private val controller: EventController by inject()

    init {
        title = "Client/Salesperson Assignments"

        with(root) {
            top = menubar {
                menu("File") {
                    menuitem("Refresh").apply {
                        controller.refreshCompanyClients += actionEvents().map { Unit }
                        controller.refreshSalesPeople += actionEvents().map { Unit }
                    }
                }
            }
            center = splitpane {
                orientation = Orientation.HORIZONTAL
                this += salesPeopleView
                this += companyClientView
            }
        }
    }
}