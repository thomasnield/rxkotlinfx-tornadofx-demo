package domain

import org.nield.rxkotlinjdbc.execute
import org.nield.rxkotlinjdbc.insert
import org.nield.rxkotlinjdbc.select


class Customer(val id: Int, val name: String) {

    companion object {
        val all = db.select("SELECT * FROM CUSTOMER")
                    .toObservable { Customer(it.getInt("ID"), it.getString("NAME")) }

        fun forId(id: Int) = db.select("SELECT * FROM CUSTOMER WHERE ID = ?")
                .parameter(id)
                .toSingle { Customer(it.getInt("ID"), it.getString("NAME")) }

        fun createNew(name: String) = db.insert("INSERT INTO CUSTOMER (NAME) VALUES (?)")
            .parameter(name)
            .toSingle { it.getInt(1) }
    }

    fun delete() = db.execute("DELETE FROM CUSTOMER WHERE ID = ?")
        .parameter(id)
        .toSingle()

}