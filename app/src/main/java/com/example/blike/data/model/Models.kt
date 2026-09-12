package com.example.blike.data.model

import com.google.gson.annotations.SerializedName

data class RestBean<T>(
    @SerializedName("status") val status: Int,
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: T? = null,
    @SerializedName("code") val code: Int? = null,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("data") val data: T? = null
) {
    val effectiveStatus: Int
        get() = code ?: status

    val effectiveData: T?
        get() = data ?: message

    val effectiveMessage: String?
        get() = msg ?: (message as? String)
}

data class LoginRequest(
    val username: String,
    val password: String,
    val remember: Boolean = false
)

data class LoginData(
    val token: String,
    val user: User
)

data class User(
    val id: Long,
    val username: String,
    val email: String,
    val avatar: String? = null,
    val bio: String? = null
)

data class Video(
    val id: Long,
    val title: String,
    val cover: String? = null,
    val author: String? = null,
    val avatar: String? = null,
    val playCount: String? = null,
    val time: String? = null
)