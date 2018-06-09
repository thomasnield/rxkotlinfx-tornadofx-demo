package domain

import org.nield.rxkotlinjdbc.execute
import org.nield.rxkotlinjdbc.insert
import org.nield.rxkotlinjdbc.select


class Customer(val id: Int, val name: String) {

    /**
     * Deletes this `Customer` instance
     */
    fun delete() = db.execute("DELETE FROM CUSTOMER WHERE ID = ?")
            .parameter(id)
            .toSingle()


    companion object {

        /**
         * Retrieves all Customers
         */
        val all = db.select("SELECT * FROM CUSTOMER")
                    .toObservable { Customer(it.getInt("ID"), it.getString("NAME")) }

        /**
         * Retrieves `Customer` for a given `id`
         */
        fun forId(id: Int) = db.select("SELECT * FROM CUSTOMER WHERE ID = ?")
                .parameter(id)
                .toPipeline { Customer(it.getInt("ID"), it.getString("NAME")) }

        /**
         * Creates a new `Customer` with the given `name`
         */
        fun createNew(name: String) = db.insert("INSERT INTO CUSTOMER (NAME) VALUES (:name)")
            .parameter("name",name)
            .toSingle { it.getInt(1) }
    }
}