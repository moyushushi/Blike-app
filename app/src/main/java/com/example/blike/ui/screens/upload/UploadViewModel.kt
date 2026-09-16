package com.example.blike.ui.screens.upload

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.blike.di.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

enum class PublishMode { VIDEO, ARTICLE }

val CATEGORIES = listOf("生活", "游戏", "音乐", "影视", "知识", "科技", "其他")

data class PublishUiState(
    val mode: PublishMode = PublishMode.VIDEO,
    val title: String = "",
    val desc: String = "",         // 视频简介
    val content: String = "",      // 图文正文
    val category: String = CATEGORIES.first(),
    val videoUri: Uri? = null,
    val coverUri: Uri? = null,
    val publishing: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class PublishViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(PublishUiState())
    val state = _state.asStateFlow()

    fun switchMode(mode: PublishMode) =
        _state.update { it.copy(mode = mode, error = null) }

    fun onTitleChange(v: String) = _state.update { it.copy(title = v, error = null) }
    fun onDescChange(v: String) = _state.update { it.copy(desc = v, error = null) }
    fun onContentChange(v: String) = _state.update { it.copy(content = v, error = null) }
    fun onCategoryChange(v: String) = _state.update { it.copy(category = v) }
    fun onVideoSelected(uri: Uri?) = _state.update { it.copy(videoUri = uri, error = null) }
    fun onCoverSelected(uri: Uri?) = _state.update { it.copy(coverUri = uri, error = null) }

    fun consumeError() = _state.update { it.copy(error = null) }
    fun consumeSuccess() = _state.update { it.copy(success = false) }

    fun publish() {
        val s = _state.value
        when (s.mode) {
            PublishMode.VIDEO -> {
                if (s.videoUri == null) {
                    _state.update { it.copy(error = "请选择视频文件") }; return
                }
                if (s.title.isBlank()) {
                    _state.update { it.copy(error = "请输入标题") }; return
                }
            }
            PublishMode.ARTICLE -> {
                if (s.title.isBlank()) {
                    _state.update { it.copy(error = "请输入标题") }; return
                }
                if (s.content.isBlank()) {
                    _state.update { it.copy(error = "请输入正文") }; return
                }
            }
        }

        viewModelScope.launch {
            _state.update { it.copy(publishing = true, error = null) }
            try {
                val res = when (s.mode) {
                    PublishMode.VIDEO -> uploadVideo(s)
                    PublishMode.ARTICLE -> uploadArticle(s)
                }
                if (res.success) {
                    _state.update { it.copy(publishing = false, success = true) }
                } else {
                    _state.update {
                        it.copy(
                            publishing = false,
                            error = res.effectiveMessage
                                ?: "发布失败（${res.effectiveStatus}）"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(publishing = false, error = e.message ?: "网络异常")
                }
            }
        }
    }

    private suspend fun uploadVideo(s: PublishUiState) = withContext(Dispatchers.IO) {
        val videoFile = uriToCacheFile(s.videoUri!!, "video_${System.currentTimeMillis()}.mp4")
        val videoPart = MultipartBody.Part.createFormData(
            "video", videoFile.name, videoFile.asRequestBody("video/*".toMediaType())
        )
        val coverPart = s.coverUri?.let {
            val f = uriToCacheFile(it, "cover_${System.currentTimeMillis()}.jpg")
            MultipartBody.Part.createFormData(
                "cover", f.name, f.asRequestBody("image/*".toMediaType())
            )
        }
        val titleBody = s.title.trim().toRequestBody("text/plain".toMediaType())
        val descBody = s.desc.trim().toRequestBody("text/plain".toMediaType())
        val categoryBody = s.category.toRequestBody("text/plain".toMediaType())

        ServiceLocator.apiService.uploadVideo(
            videoPart, coverPart, titleBody, descBody, categoryBody
        )
    }

    private suspend fun uploadArticle(s: PublishUiState) = withContext(Dispatchers.IO) {
        val coverPart = s.coverUri?.let {
            val f = uriToCacheFile(it, "cover_${System.currentTimeMillis()}.jpg")
            MultipartBody.Part.createFormData(
                "cover", f.name, f.asRequestBody("image/*".toMediaType())
            )
        }
        val titleBody = s.title.trim().toRequestBody("text/plain".toMediaType())
        val contentBody = s.content.trim().toRequestBody("text/plain".toMediaType())
        val categoryBody = s.category.toRequestBody("text/plain".toMediaType())

        ServiceLocator.apiService.uploadArticle(
            titleBody, contentBody, categoryBody, coverPart
        )
    }

    private fun uriToCacheFile(uri: Uri, name: String): File {
        val ctx = getApplication<Application>()
        val file = File(ctx.cacheDir, name)
        ctx.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        return file
    }
}