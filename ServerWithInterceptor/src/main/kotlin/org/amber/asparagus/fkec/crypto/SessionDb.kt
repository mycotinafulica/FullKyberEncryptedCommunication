package org.amber.asparagus.fkec.crypto

/*
* This is just a simplified session database. For production environment, use a proper session database such as redis.
* Unless you are just planning to spawn one server instance, even then, you still need to add mechanism for session
* expiry and that kind of stuffs as well.
* */
class SessionDb {
    companion object {
        // Pair Session ID to the session encryption key.
        val sessions: HashMap<String, ByteArray> = HashMap()
    }
}