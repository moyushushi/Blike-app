package com.example.blike.data.remote

import android.content.Context

class TokenManager(context: Context) {
    private val sp = context.applicationContext
        .getSharedPreferences("blike_auth", Context.MODE_PRIVATE)

    var token: String?
        get() = sp.getString(KEY_TOKEN, null)
        set(value) {
            sp.edit().putString(KEY_TOKEN, value).apply()
        }

    fun saveToken(token: String) {
        sp.edit().putString(KEY_TOKEN, token).apply()
    }

    fun clear() {
        sp.edit().clear().apply()
    }

    val isLoggedIn: Boolean
        get() = !token.isNullOrBlank()

    companion object {
        private const val KEY_TOKEN = "token"
    }
}
