package app

import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.Stylesheet
import tornadofx.box
import tornadofx.cssclass
import tornadofx.px

class Styles : Stylesheet() {
    companion object {
        val heading by cssclass()
    }

    init {
        label and heading {
            padding = box(0.px,0.px,0.px,5.px)
            fontSize = 12.px
            fontWeight = FontWeight.BOLD
            textFill = Color.RED
        }
    }
}