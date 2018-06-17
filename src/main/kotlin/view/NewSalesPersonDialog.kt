package view

import domain.SalesPerson
import io.reactivex.Observable
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.stage.Stage
import tornadofx.*

class NewSalesPersonDialog: Dialog<Int>() {
    private val root = Form()
    private var first: TextField by singleAssign()
    private var last: TextField by singleAssign()

    init {
        title = "Create New Sales Person"

        with(root) {
            fieldset("Name") {
                field("First") {
                    first = textfield()
                }
                field("Last") {
                    last = textfield()
                }

            }
        }

        setResultConverter {
            if (it == ButtonType.OK)
                SalesPerson.createNew(first.text,last.text) //returns ID for new Customer
            else
                0
        }

        dialogPane.content = root
        dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)
        graphic = ImageView(Image("/app/tornado-fx-logo.png"))
        (dialogPane.scene.window as Stage).icons += Image("/app/tornado-fx-logo.png")
    }
}