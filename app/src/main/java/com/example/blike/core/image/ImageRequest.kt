package com.example.blike.core.image

data class ImageRequest(
    val url: String,
    val width: Int = 0,
    val height: Int = 0
)