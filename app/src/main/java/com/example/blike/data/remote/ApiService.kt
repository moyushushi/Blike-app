package com.example.blike.data.remote

import com.example.blike.data.model.CommentRequest
import com.example.blike.data.model.LoginData
import com.example.blike.data.model.LoginRequest
import com.example.blike.data.model.RestBean
import com.example.blike.data.model.User
import com.example.blike.data.model.Video
import com.example.blike.data.model.VideoComment
import com.example.blike.data.model.VideoDetail
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface  ApiService{

    @POST("api/login")
    suspend fun login(@Body body: LoginRequest): RestBean<LoginData>

    @GET("api/user/me")
    suspend fun getMe(): RestBean<User>

    @GET("api/video/list")
    suspend fun getVideoList(): RestBean<List<Video>>

    @GET("api/video/{id}")
    suspend fun getVideoDetail(@Path("id") id: Long): RestBean<VideoDetail>

    @POST("api/video/{id}/play")
    suspend fun addPlayCount(@Path("id") id: Long): RestBean<Unit>

    @GET("api/comment/video/{id}")
    suspend fun getVideoComments(@Path("id") id: Long): RestBean<List<VideoComment>>

    @POST("api/comment/video")
    suspend fun postVideoComment(@Body body: CommentRequest): RestBean<Unit>

    @GET("api/user/is-following")
    suspend fun isFollowing(@Query("userId") userId: Long): RestBean<Boolean>

    @POST("api/user/follow/{userId}")
    suspend fun follow(@Path("userId") userId: Long): RestBean<Unit>

    @DELETE("api/user/follow/{userId}")
    suspend fun unfollow(@Path("userId") userId: Long): RestBean<Unit>

    @GET("api/user/is-liked")
    suspend fun isLiked(@Query("videoId") videoId: Long): RestBean<Boolean>

    @POST("api/user/like/{videoId}")
    suspend fun like(@Path("videoId") videoId: Long): RestBean<Unit>

    @DELETE("api/user/like/{videoId}")
    suspend fun unlike(@Path("videoId") videoId: Long): RestBean<Unit>
}
