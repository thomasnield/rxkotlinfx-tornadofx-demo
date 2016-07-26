package domain

class Customer(val id: Int, val name: String) {

    companion object {
        val all = db.select("SELECT * FROM CUSTOMER")
                    .get { Customer(it.getInt("ID"), it.getString("NAME")) }

        fun forId(id: Int) = db.select("SELECT * FROM CUSTOMER WHERE ID = ?")
                .parameter(id)
                .get { Customer(it.getInt("ID"), it.getString("NAME")) }
    }
}