package com.example.blike.data.remote

import android.content.Context

class TokenManager(context: Context) {

    private val sp = context.applicationContext
        .getSharedPreferences("blike_auth", Context.MODE_PRIVATE)

    //  Token 读写

    var token: String?
        get() = sp.getString(KEY_TOKEN, null)
        set(value) {
            sp.edit().putString(KEY_TOKEN, value).apply()
        }

    fun saveToken(token: String) {
        sp.edit().putString(KEY_TOKEN, token).apply()
    }

    // 清空 / 退出登录

    /**
     * 仅清空登录状态（token + 用户信息），不清其他缓存。
     * 退出登录、401 自动登出都调这个。
     */
    fun clear() {
        sp.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_USERNAME)
            .remove(KEY_EMAIL)
            .remove(KEY_AVATAR)
            .apply()
    }

    /**
     * 退出登录 = 清空 + 跳登录页（跳转由 UI 层做，这里只负责清数据）。
     * 保留 logout() 这个名字，让调用方语义清晰。
     */
    fun logout() {
        clear()
    }

    // 用户信息缓存（可选，方便首屏秒显）

    var userId: Long
        get() = sp.getLong(KEY_USER_ID, 0L)
        set(value) {
            sp.edit().putLong(KEY_USER_ID, value).apply()
        }

    var username: String?
        get() = sp.getString(KEY_USERNAME, null)
        set(value) {
            sp.edit().putString(KEY_USERNAME, value).apply()
        }

    var email: String?
        get() = sp.getString(KEY_EMAIL, null)
        set(value) {
            sp.edit().putString(KEY_EMAIL, value).apply()
        }

    var avatar: String?
        get() = sp.getString(KEY_AVATAR, null)
        set(value) {
            sp.edit().putString(KEY_AVATAR, value).apply()
        }

    /**
     * 登录成功后一次性存好 token + 用户基本信息。
     */
    fun saveLogin(token: String, userId: Long, username: String, email: String?, avatar: String?) {
        sp.edit()
            .putString(KEY_TOKEN, token)
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_USERNAME, username)
            .putString(KEY_EMAIL, email)
            .putString(KEY_AVATAR, avatar)
            .apply()
    }

    // 状态查询

    val isLoggedIn: Boolean
        get() = !token.isNullOrBlank()

    val authorizationHeader: String?
        get() = token?.takeIf { it.isNotBlank() }?.let { "Bearer $it" }

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
        private const val KEY_AVATAR = "avatar"
    }
}