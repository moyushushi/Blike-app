package com.example.blike.core.image

interface ImageChain{
    val request: ImageRequest
    suspend fun proceed(request: ImageRequest): ImageResult
}