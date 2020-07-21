package com.app.service.manager

import com.app.service.KeyValueStorage
import com.app.service.KeyValueStorageKey
import javax.inject.Inject

class UserManager @Inject constructor(private val keyValueStorage: KeyValueStorage) {

    val isUserLoggedIn: Boolean
        get() = id != null && authToken != null

    fun logout() {
        id = null
        authToken = null
    }

    var id: Int?
        get() = keyValueStorage.integer(KeyValueStorageKey.LOGGED_IN_USER_ID)
        set(value) {
            keyValueStorage.setInt(value, KeyValueStorageKey.LOGGED_IN_USER_ID)
        }

    var authToken: String?
        get() = keyValueStorage.string(KeyValueStorageKey.LOGGED_IN_USER_AUTH_TOKEN)
        set(value) {
            keyValueStorage.setString(value, KeyValueStorageKey.LOGGED_IN_USER_AUTH_TOKEN)
        }
}
