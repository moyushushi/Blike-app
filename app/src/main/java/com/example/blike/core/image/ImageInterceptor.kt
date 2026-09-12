package com.example.blike.core.image

fun interface ImageInterceptor{
    suspend fun intercept(chain: ImageChain): ImageResult
}