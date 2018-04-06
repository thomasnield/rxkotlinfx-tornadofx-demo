package domain

import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toObservable
import org.nield.rxkotlinjdbc.execute
import org.nield.rxkotlinjdbc.insert
import java.sql.DriverManager


/**
 * An in-memory `Database` using SQLite holding three tables. This `Database` performs reactive querying
 * and writing via [RxJava-JDBC](https://github.com/davidmoten/rxjava-jdbc)
 */
val db = DriverManager.getConnection("jdbc:sqlite::memory:").apply {

    //create CLIENT_COMPANY TABLE
   execute("CREATE TABLE CUSTOMER (ID INTEGER PRIMARY KEY, NAME VARCHAR)").toSingle().subscribe()

    listOf(
            "Alpha Analytics",
            "Rexon Solutions",
            "Travis Technologies",
            "Anex Applications",
            "Edvin Enterprises",
            "T-Boom Consulting",
            "Nield Industrial",
            "Dash Inc"
    ).toObservable()
    .flatMap {
        insert("INSERT INTO CUSTOMER (NAME) VALUES (?)")
                .parameter(it)
                .toObservable { it.getInt(0) }
    }
    .toList()
    .subscribeBy(
        onSuccess = { println("CUSTOMER table created, KEYS: $it")},
        onError = { throw RuntimeException(it) }
    )

    //create SALES_PERSON TABLE
    execute("CREATE TABLE SALES_PERSON (ID INTEGER PRIMARY KEY, FIRST_NAME VARCHAR, LAST_NAME VARCHAR)").toSingle().subscribe()

    val salesPersonValues = listOf(
            "Joe" to "McManey",
            "Heidi" to "Howell",
            "Eric" to "Wentz",
            "Jonathon" to "Smith",
            "Samantha" to "Stewart",
            "Jillian" to "Michelle"
    ).toObservable()

    salesPersonValues.flatMapSingle {
        insert("INSERT INTO SALES_PERSON (FIRST_NAME,LAST_NAME) VALUES (?,?)")
                .parameters(it.first, it.second)
                .toSingle { it.getInt(1) }
    }.subscribeBy(
            onNext = { println("SALES_PERSON table created, KEYS: $it") },
            onError = { throw RuntimeException(it) }
        )

    //CREATE ASSIGNMENTS TABLE
     execute("CREATE TABLE ASSIGNMENT (ID INTEGER PRIMARY KEY, " +
            "CUSTOMER_ID INTEGER, SALES_PERSON_ID INTEGER, APPLY_ORDER INTEGER)")
        .toSingle()
        .subscribeBy(
            onSuccess  = { println("ASSIGNMENT table created") },
            onError =  { throw RuntimeException(it) }
        )
}


