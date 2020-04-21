package app

import tornadofx.App
import tornadofx.launch
import view.MainView

class MyApp : App(MainView::class, Styles::class) {
    fun main(args: Array<String>) {
        launch<MyApp>(args)
    }
}
