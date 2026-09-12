package com.example.blike.data.remote

import com.example.blike.data.model.LoginData
import com.example.blike.data.model.LoginRequest
import com.example.blike.data.model.RestBean
import com.example.blike.data.model.User
import com.example.blike.data.model.Video
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface  ApiService{

    @POST("api/login")
    suspend fun login(@Body body: LoginRequest): RestBean<LoginData>

    @GET("api/user/me")
    suspend fun getMe(): RestBean<User>

    @GET("api/video/list")
    suspend fun getVideoList(): RestBean<List<Video>>
}
