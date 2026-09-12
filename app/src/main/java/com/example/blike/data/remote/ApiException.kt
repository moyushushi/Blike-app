package com.example.blike.data.remote

class ApiException(
    val status: Int,
    override val message: String
): Exception(message)