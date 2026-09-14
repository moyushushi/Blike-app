package com.example.blike.ui.screens.video

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blike.data.model.CommentRequest
import com.example.blike.data.model.VideoComment
import com.example.blike.data.model.VideoDetail
import com.example.blike.di.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VideoDetailUiState(
    val loading: Boolean = true,
    val video: VideoDetail? = null,
    val comments: List<VideoComment> = emptyList(),
    val error: String? = null,
    val isFollowing: Boolean = false,
    val hasLiked: Boolean = false,
    val submittingComment: Boolean = false,
    val commentText: String = ""
)

class VideoDetailViewModel : ViewModel() {

    private val _state = MutableStateFlow(VideoDetailUiState())
    val state = _state.asStateFlow()

    private var loadedId: Long = -1
    private var playAdded = false

    fun load(videoId: Long) {
        if (loadedId == videoId) return
        loadedId = videoId
        playAdded = false

        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val detailRes = ServiceLocator.apiService.getVideoDetail(videoId)
                val video = detailRes.effectiveData
                _state.update { it.copy(loading = false, video = video) }

                if (!playAdded) {
                    playAdded = true
                    runCatching { ServiceLocator.apiService.addPlayCount(videoId) }
                }

                loadComments(videoId)
                checkFollowStatus(video?.userId ?: 0L)
                checkLikeStatus(videoId)
            } catch (e: Exception) {
                _state.update {
                    it.copy(loading = false, error = e.message ?: "网络错误")
                }
            }
        }
    }

    private suspend fun loadComments(videoId: Long) {
        runCatching {
            val res = ServiceLocator.apiService.getVideoComments(videoId)
            _state.update { it.copy(comments = res.effectiveData.orEmpty()) }
        }
    }

    private suspend fun checkFollowStatus(authorId: Long) {
        if (authorId == 0L) return
        if (ServiceLocator.tokenManager.token.isNullOrBlank()) return
        runCatching {
            val res = ServiceLocator.apiService.isFollowing(authorId)
            _state.update { it.copy(isFollowing = res.effectiveData == true) }
        }
    }

    private suspend fun checkLikeStatus(videoId: Long) {
        if (ServiceLocator.tokenManager.token.isNullOrBlank()) return
        runCatching {
            val res = ServiceLocator.apiService.isLiked(videoId)
            _state.update { it.copy(hasLiked = res.effectiveData == true) }
        }
    }

    fun onCommentChange(text: String) =
        _state.update { it.copy(commentText = text) }

    fun toggleFollow() {
        val video = _state.value.video ?: return
        if (ServiceLocator.tokenManager.token.isNullOrBlank()) return
        viewModelScope.launch {
            val target = !_state.value.isFollowing
            runCatching {
                if (target) {
                    ServiceLocator.apiService.follow(video.userId)
                } else {
                    ServiceLocator.apiService.unfollow(video.userId)
                }
                _state.update { it.copy(isFollowing = target) }
            }
        }
    }

    fun toggleLike() {
        val video = _state.value.video ?: return
        if (ServiceLocator.tokenManager.token.isNullOrBlank()) return
        viewModelScope.launch {
            val target = !_state.value.hasLiked
            runCatching {
                if (target) {
                    ServiceLocator.apiService.like(video.id)
                } else {
                    ServiceLocator.apiService.unlike(video.id)
                }
                val newCount = (video.likeCount ?: 0L)
                    .let { if (target) it + 1 else (it - 1).coerceAtLeast(0L) }
                _state.update {
                    it.copy(
                        hasLiked = target,
                        video = it.video?.copy(likeCount = newCount)
                    )
                }
            }
        }
    }

    fun submitComment() {
        val text = _state.value.commentText.trim()
        val video = _state.value.video ?: return
        if (text.isBlank()) return
        if (ServiceLocator.tokenManager.token.isNullOrBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(submittingComment = true) }
            try {
                val res = ServiceLocator.apiService.postVideoComment(
                    CommentRequest(videoId = video.id, content = text)
                )
                if (res.effectiveStatus == 200) {
                    _state.update { it.copy(commentText = "") }
                    loadComments(video.id)
                    val newCount = (video.commentCount ?: 0L) + 1
                    _state.update {
                        it.copy(video = it.video?.copy(commentCount = newCount))
                    }
                }
            } catch (_: Exception) {
            } finally {
                _state.update { it.copy(submittingComment = false) }
            }
        }
    }
}