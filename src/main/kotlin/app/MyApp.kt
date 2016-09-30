package app

import javafx.scene.image.Image
import tornadofx.App
import tornadofx.addStageIcon
import view.MainView

class MyApp : App(MainView::class, Styles::class) {
    init {
        addStageIcon(Image("/app/tornado-fx-logo.png"))
    }
}
