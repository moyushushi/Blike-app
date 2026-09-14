package com.example.blike.ui.screens.article

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.blike.data.model.Article
import com.example.blike.data.model.ArticleComment
import com.example.blike.data.model.ArticleCommentRequest
import com.example.blike.di.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class ArticleDetailUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val article: Article? = null,
    val comments: List<ArticleComment> = emptyList(),
    val sending: Boolean = false
)


class ArticleDetailViewModel(private val articleId: Long) : ViewModel() {

    private val _state = MutableStateFlow(ArticleDetailUiState())
    val state = _state.asStateFlow()

    init {
        loadArticle()
        loadComments()
    }

    fun loadArticle() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val res = ServiceLocator.apiService.getArticleDetail(articleId)
                _state.update { it.copy(loading = false, article = res.effectiveData) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(loading = false, error = e.message ?: "网络错误")
                }
            }
        }
    }

    fun loadComments() {
        viewModelScope.launch {
            try {
                val res = ServiceLocator.apiService.getArticleComments(articleId)
                _state.update { it.copy(comments = res.effectiveData.orEmpty()) }
            } catch (_: Exception) {
                // 评论加载失败不阻塞正文
            }
        }
    }

    fun postComment(content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(sending = true) }
            try {
                ServiceLocator.apiService.postArticleComment(
                    ArticleCommentRequest(articleId = articleId, content = content)
                )
                _state.update { it.copy(sending = false) }
                loadComments()
            } catch (e: Exception) {
                _state.update {
                    it.copy(sending = false, error = e.message ?: "评论失败")
                }
            }
        }
    }
}


class ArticleDetailViewModelFactory(
    private val articleId: Long
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ArticleDetailViewModel(articleId) as T
    }
}