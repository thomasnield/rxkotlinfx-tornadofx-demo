package app

import tornadofx.App
import tornadofx.importStylesheet
import view.MainView

class MyApp : App() {
    override val primaryView = MainView::class

    init {
        importStylesheet(Styles::class)
    }
}