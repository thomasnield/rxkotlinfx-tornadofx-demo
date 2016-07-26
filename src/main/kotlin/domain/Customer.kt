package domain

class Customer(val id: Int, val name: String) {

    companion object {
        val all = db.select("SELECT * FROM CLIENT_COMPANY")
            .get { Customer(it.getInt("ID"), it.getString("NAME")) }

        fun forId(id: Int) = db.select("SELECT * FROM CLIENT_COMPANY WHERE ID = ?")
            .get { Customer(it.getInt("ID"), it.getString("NAME")) }
    }
}