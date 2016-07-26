package app

import javafx.scene.image.Image
import tornadofx.App
import tornadofx.FX
import tornadofx.addStageIcon
import tornadofx.importStylesheet
import view.MainView

class MyApp : App() {
    override val primaryView = MainView::class

    init {
        importStylesheet(Styles::class)
        addStageIcon(Image("/app/tornado-fx-logo.png"))
    }
}