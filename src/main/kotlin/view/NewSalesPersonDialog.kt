package view

import domain.SalesPerson
import io.reactivex.Observable
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import javafx.scene.control.TextField
import javafx.scene.image.ImageView
import javafx.stage.Stage
import tornadofx.*

class NewSalesPersonDialog: Dialog<Observable<Int>>() {
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
                Observable.empty()
        }

        dialogPane.content = root
        dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)
        graphic = ImageView(FX.primaryStage.icons[0])
        (dialogPane.scene.window as Stage).icons += FX.primaryStage.icons[0]
    }
}