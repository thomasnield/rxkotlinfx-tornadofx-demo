package domain

import app.flatCollect
import rx.Observable

class Customer(val id: Int, val name: String) {

    companion object {
        val all: Observable<Customer> = db.select("SELECT * FROM CUSTOMER")
                    .get { Customer(it.getInt("ID"), it.getString("NAME")) }
                    .flatCollect()

        fun forId(id: Int): Observable<Customer> = db.select("SELECT * FROM CUSTOMER WHERE ID = ?")
                .parameter(id)
                .get { Customer(it.getInt("ID"), it.getString("NAME")) }
                .flatCollect()
    }
}