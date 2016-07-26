package view

import javafx.scene.control.Alert
import rx.Observable
import rx.lang.kotlin.FunctionSubscriberModifier
import tornadofx.alert
import java.util.HashSet

fun <T> FunctionSubscriberModifier<T>.alertError() {
    onError { alert(Alert.AlertType.ERROR,"PROBLEM!",it.message?:"").show() }
}

@Suppress("USELESS_CAST")
fun <T> Observable<T>.toSet() = collect({HashSet<T>()},{ set, t -> set.add(t)}).map { it as Set<T> }