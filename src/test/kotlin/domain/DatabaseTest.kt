package domain

import org.junit.Test
import rx.lang.kotlin.subscribeWith

class DatabaseTest {
    @Test
    fun testSqliteConnection() {
        db.select("SELECT * FROM SALES_PERSON")
            .count()
            .subscribeWith {
                onError { throw RuntimeException(it) }
            }
    }
}