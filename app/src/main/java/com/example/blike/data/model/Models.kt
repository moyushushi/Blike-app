package com.example.blike.data.model

import com.google.gson.annotations.SerializedName

data class RestBean<T>(
    @SerializedName("status") val status: Int,
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: T? = null,
    @SerializedName("code") val code: Int? = null,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("data") val data: T? = null
) {
    val effectiveStatus: Int
        get() = code ?: status

    val effectiveData: T?
        get() = data ?: message

    val effectiveMessage: String?
        get() = msg ?: (message as? String)
}

// ==================== 认证 ====================

data class LoginRequest(
    val username: String,
    val password: String,
    val remember: Boolean = false
)

data class LoginData(
    val token: String,
    val user: User
)

// ==================== 用户 ====================

data class User(
    val id: Long,
    val username: String,
    val email: String? = null,   // /user/info、/user/following 不返回 email
    val avatar: String? = null,
    val bio: String? = null
)

data class UpdateBioRequest(
    val bio: String
)

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)

// ==================== 视频 ====================

data class Video(
    val id: Long,
    val title: String,
    val cover: String? = null,
    val author: String? = null,
    val avatar: String? = null,
    val playCount: Long? = null,
    val time: String? = null,
    val publishTime: String? = null,   // /video/user/{userId} 返回
    val likeTime: String? = null       // /user/liked-videos 返回
)

data class VideoDetail(
    val id: Long,
    val userId: Long = 0,
    val title: String,
    val url: String,
    val cover: String? = null,
    val author: String? = null,
    val avatar: String? = null,
    val playCount: Long? = null,
    val time: String? = null,
    val desc: String? = null,
    val publishTime: String? = null,
    val likeCount: Long? = null,
    val commentCount: Long? = null
)

data class VideoComment(
    val id: Long,
    val videoId: Long = 0,
    val userId: Long = 0,
    val content: String,
    val createTime: String? = null,
    val username: String? = null,
    val avatar: String? = null
)

data class CommentRequest(
    val videoId: Long,
    val content: String
)

// ==================== 文章 ====================

data class Article(
    val id: Long,
    val title: String,
    val content: String? = null,   // 列表接口不返回 content，必须可空
    val cover: String? = null,
    val category: String? = null,
    val viewCount: Long? = null,
    val likeCount: Long? = null,
    val commentCount: Long? = null,
    val publishTime: String? = null,
    val userId: Long = 0,
    val status: Int? = null,
    val author: String? = null,
    val avatar: String? = null,
    val likeTime: String? = null    // /article/liked 返回
)

data class ArticleComment(
    val id: Long,
    val articleId: Long = 0,
    val userId: Long = 0,
    val content: String,
    val likeCount: Long? = null,
    val createTime: String? = null,
    val username: String? = null,
    val avatar: String? = null
)

data class ArticleCommentRequest(
    val articleId: Long,
    val content: String
)