package app

import io.reactivex.Observable
import javafx.collections.ObservableList
import javafx.scene.control.TableView
import io.reactivex.rxkotlin.toObservable

/**
 * Workaround for [SQLite locking error](https://github.com/davidmoten/rxjava-jdbc#note-for-sqlite-users).
 * Collecting items and then emitting them again allows query
 * to close and open connection for other queries
 */
fun <T: Any> Observable<T>.flatCollect() = toList().flatMapObservable { it.toObservable() }

/**
 * Adds the item to an `Observablelist<T>` if it is not present
 */
fun <T> ObservableList<T>.addIfAbsent(item: T): Boolean {
    return if (!contains(item)) {
        add(item)
        true
    } else {
        false
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
fun <T> ObservableList<T>.deleteWhere(predicate: (T) -> Boolean) {
    asSequence().toList().asSequence().filter(predicate).forEach { remove(it) }
}

fun <T> Observable<T>.toSet() = collect({ HashSet<T>() },{ set, t -> set.add(t)}).map { it as Set<T> }

val <T: Any> TableView<T>.currentSelections get() = selectionModel.selectedItems.toObservable()