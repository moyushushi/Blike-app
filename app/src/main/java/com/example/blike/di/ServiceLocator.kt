package com.example.blike.di

import android.content.Context
import com.example.blike.data.remote.ApiService
import com.example.blike.data.remote.RetrofitClient
import com.example.blike.data.remote.TokenManager

object ServiceLocator {

    lateinit var tokenManager: TokenManager
        private set

    lateinit var apiService: ApiService
        private set

    fun init(context: Context) {
        if (::tokenManager.isInitialized) return

        tokenManager = TokenManager(context)
        apiService = RetrofitClient.create(tokenManager)
    }
}