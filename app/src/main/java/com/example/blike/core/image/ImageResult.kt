package com.example.blike.core.image

import android.graphics.Bitmap

sealed interface ImageResult {
    data class Success(val bitmap: Bitmap) : ImageResult
    data class Error(val throwable: Throwable) : ImageResult
}