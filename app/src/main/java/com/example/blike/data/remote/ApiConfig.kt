package com.example.blike.data.remote


object ApiConfig {

    // 电脑局域网 IP，和 RetrofitClient 里保持一致
    const val BASE_URL = "http://10.201.186.178:8080/"

    // 给 Coil 拼完整 URL 用，不带末尾斜杠
    const val ASSET_BASE = "http://10.201.186.178:8080"

    fun fullUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null
        return if (path.startsWith("http")) path
        else "$ASSET_BASE$path"
    }
}