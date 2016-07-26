package domain

class ClientCompany(val id: Int, val name: String) {

    companion object {
        val all = db.select("SELECT * FROM CLIENT_COMPANY")
            .get { ClientCompany(it.getInt("ID"), it.getString("NAME")) }

        fun forId(id: Int) = db.select("SELECT * FROM CLIENT_COMPANY WHERE ID = ?")
            .get { ClientCompany(it.getInt("ID"), it.getString("NAME")) }
    }
}