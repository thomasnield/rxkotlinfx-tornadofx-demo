package domain

import com.github.davidmoten.rx.jdbc.ConnectionProviderFromUrl
import com.github.davidmoten.rx.jdbc.Database
import rx.lang.kotlin.subscribeWith
import rx.lang.kotlin.toObservable

/**
 * An in-memory `Database` using SQLite holding three tables. This `Database` performs reactive querying
 * and writing via [RxJava-JDBC](https://github.com/davidmoten/rxjava-jdbc)
 */
val db: Database = Database.from(ConnectionProviderFromUrl("jdbc:sqlite::memory:").get()).apply {

    //create CLIENT_COMPANY TABLE
    val create1 = update("CREATE TABLE CUSTOMER (ID INTEGER PRIMARY KEY, NAME VARCHAR)").count()

    val clientCompanyValues = listOf(
            "Alpha Analytics",
            "Rexon Solutions",
            "Travis Technologies",
            "Anex Applications",
            "Edvin Enterprises",
            "T-Boom Consulting",
            "Nield Industrial",
            "Dash Inc"
    ).toObservable()

    update("INSERT INTO CUSTOMER (NAME) VALUES (?)")
        .parameters(clientCompanyValues)
        .dependsOn(create1)
        .returnGeneratedKeys()
        .getAs(Int::class.java)
        .toList()
        .subscribeWith {
            onNext { println("CUSTOMER table created, KEYS: $it")}
            onError { throw RuntimeException(it) }
        }

    //create SALES_PERSON TABLE
    val create2 = update("CREATE TABLE SALES_PERSON (ID INTEGER PRIMARY KEY, FIRST_NAME VARCHAR, LAST_NAME VARCHAR)").count()

    val salesPersonValues = listOf(
            "Joe","McManey",
            "Heidi","Howell",
            "Eric","Wentz",
            "Jonathon","Smith",
            "Samantha","Stewart",
            "Jillian","Michelle"
    ).toObservable()

    update("INSERT INTO SALES_PERSON (FIRST_NAME,LAST_NAME) VALUES (?,?)")
        .dependsOn(create2)
        .parameters(salesPersonValues)
        .returnGeneratedKeys()
        .getAs(Int::class.java)
        .toList()
        .subscribeWith {
            onNext { println("SALES_PERSON table created, KEYS: $it") }
            onError { throw RuntimeException(it) }
        }

    //CREATE ASSIGNMENTS TABLE

     update("CREATE TABLE ASSIGNMENT (ID INTEGER PRIMARY KEY, " +
            "CUSTOMER_ID INTEGER, SALES_PERSON_ID INTEGER, APPLY_ORDER INTEGER)")
        .count()
        .subscribeWith {
            onNext { println("ASSIGNMENT table created") }
            onError { throw RuntimeException(it) }
        }
}


