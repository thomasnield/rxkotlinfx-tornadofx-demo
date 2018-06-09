package app

import javafx.application.Application
import javafx.scene.image.Image
import tornadofx.App
import tornadofx.addStageIcon
import view.MainView


fun main(args: Array<String>) = Application.launch(MyApp::class.java, *args)

class MyApp : App(MainView::class, Styles::class) {
    init {
        addStageIcon(Image("/app/tornado-fx-logo.png"))
    }
}
