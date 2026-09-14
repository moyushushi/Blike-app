package com.example.blike.data.remote

import com.example.blike.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

    interface ApiService {

        // 认证

        @POST("api/login")
        suspend fun login(@Body body: LoginRequest): RestBean<LoginData>

        @FormUrlEncoded
        @POST("api/register")
        suspend fun register(
            @Field("username") username: String,
            @Field("password") password: String,
            @Field("email") email: String,
            @Field("code") code: String
        ): RestBean<String>

        @FormUrlEncoded
        @POST("api/vali-register-email")
        suspend fun sendRegisterEmail(@Field("email") email: String): RestBean<String>

        @FormUrlEncoded
        @POST("api/vali-reset-email")
        suspend fun sendResetEmail(@Field("email") email: String): RestBean<String>

        @FormUrlEncoded
        @POST("api/start-reset")
        suspend fun startReset(
            @Field("email") email: String,
            @Field("code") code: String
        ): RestBean<String>

        @FormUrlEncoded
        @POST("api/do-password")
        suspend fun doPassword(
            @Field("email") email: String,
            @Field("password") password: String
        ): RestBean<String>

        // 用户

        @GET("api/user/me")
        suspend fun getMe(): RestBean<User>

        @GET("api/user/info/{id}")
        suspend fun getUserInfo(@Path("id") id: Long): RestBean<User>

        @POST("api/user/update-bio")
        suspend fun updateBio(@Body body: UpdateBioRequest): RestBean<String>

        @Multipart
        @POST("api/user/upload-avatar")
        suspend fun uploadAvatar(@Part avatar: MultipartBody.Part): RestBean<String>

        @POST("api/user/change-password")
        suspend fun changePassword(@Body body: ChangePasswordRequest): RestBean<String>

        @GET("api/user/following")
        suspend fun getFollowing(): RestBean<List<User>>

        @GET("api/user/liked-videos")
        suspend fun getLikedVideos(@Query("limit") limit: Int = 10): RestBean<List<Video>>

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

        // 视频

        @GET("api/video/list")
        suspend fun getVideoList(): RestBean<List<Video>>

        @GET("api/video/{id}")
        suspend fun getVideoDetail(@Path("id") id: Long): RestBean<VideoDetail>

        @GET("api/video/user/{userId}")
        suspend fun getUserVideos(@Path("userId") userId: Long): RestBean<List<Video>>

        @Multipart
        @POST("api/video/upload")
        suspend fun uploadVideo(
            @Part video: MultipartBody.Part,
            @Part cover: MultipartBody.Part?,
            @Part("title") title: RequestBody,
            @Part("desc") desc: RequestBody?,
            @Part("category") category: RequestBody?
        ): RestBean<String>

        @POST("api/video/{id}/play")
        suspend fun addPlayCount(@Path("id") id: Long): RestBean<Unit>

        //  文章

        @GET("api/article/list")
        suspend fun getArticleList(): RestBean<List<Article>>

        @GET("api/article/{id}")
        suspend fun getArticleDetail(@Path("id") id: Long): RestBean<Article>

        @GET("api/article/user/{userId}")
        suspend fun getUserArticles(@Path("userId") userId: Long): RestBean<List<Article>>

        @GET("api/article/liked")
        suspend fun getLikedArticles(@Query("limit") limit: Int = 10): RestBean<List<Article>>

        @Multipart
        @POST("api/article/upload")
        suspend fun uploadArticle(
            @Part("title") title: RequestBody,
            @Part("content") content: RequestBody,
            @Part("category") category: RequestBody?,
            @Part cover: MultipartBody.Part?
        ): RestBean<Article>

        // 评论

        @GET("api/comment/video/{id}")
        suspend fun getVideoComments(@Path("id") id: Long): RestBean<List<VideoComment>>

        @POST("api/comment/video")
        suspend fun postVideoComment(@Body body: CommentRequest): RestBean<Unit>

        @GET("api/comment/article/{articleId}")
        suspend fun getArticleComments(@Path("articleId") articleId: Long): RestBean<List<ArticleComment>>

        @POST("api/comment/article")
        suspend fun postArticleComment(@Body body: ArticleCommentRequest): RestBean<Unit>
    }