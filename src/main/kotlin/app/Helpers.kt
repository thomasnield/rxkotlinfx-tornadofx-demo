package app

import javafx.collections.ObservableList
import javafx.scene.control.Alert
import rx.Observable
import rx.lang.kotlin.FunctionSubscriberModifier
import rx.lang.kotlin.toObservable
import tornadofx.alert
import java.util.*

/**
 * Standardized `onError()` notification that uses a JavaFX `Alert`
 */
fun <T> FunctionSubscriberModifier<T>.alertError() {
    onError {
        it.printStackTrace()
        alert(Alert.AlertType.ERROR,"PROBLEM!",it.message?:"").show()
    }
}

/**
 * Workaround for SQLite locking error.
 * Collecting items and then emitting them again allows query to close for other queries
 */
fun <T> Observable<T>.flatCollect(): Observable<T> = toList().flatMap { it.toObservable() }

/**
 * Adds the item to an `Observablelist<T>` if it is not present
 */
fun <T> ObservableList<T>.addIfAbsent(item: T): Boolean {
    if (!contains(item)) {
        add(item)
        return true
    } else {
        return false
    }
}

/**
 * Adds each item to an `Observablelist<T>` if it is not present
 */
fun <T> ObservableList<T>.addIfAbsent(vararg items: T) {
    items.forEach { addIfAbsent(it) }
}

fun <T> ObservableList<T>.moveUp(item: T) {
    val index = indexOf(item)
    if (index > 0) {
        remove(item)
        add(index - 1,item)
    }
}

fun <T> ObservableList<T>.moveDown(item: T) {
    val index = indexOf(item)
    if (index >= 0 && index < (size - 1)) {
        remove(item)
        add(index + 1,item)
    }
}

@Suppress("USELESS_CAST")
fun <T> Observable<T>.toSet(): Observable<Set<T>> = collect({ HashSet<T>() },{ set, t -> set.add(t)}).map { it as Set<T> }

