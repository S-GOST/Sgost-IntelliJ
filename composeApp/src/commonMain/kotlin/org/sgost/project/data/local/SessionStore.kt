package org.sgost.project.data.local

class SessionStore {
    private var token: String? = null

    fun saveToken(value: String) {
        token = value
    }

    fun getToken(): String? = token

    fun clear() {
        token = null
    }
}
