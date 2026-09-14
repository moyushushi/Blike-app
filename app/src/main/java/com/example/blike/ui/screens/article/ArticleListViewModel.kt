package com.example.blike.ui.screens.article

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blike.data.model.Article
import com.example.blike.di.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ArticleListUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val articles: List<Article> = emptyList()
)

class ArticleListViewModel : ViewModel() {

    private val _state = MutableStateFlow(ArticleListUiState())
    val state = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val res = ServiceLocator.apiService.getArticleList()
                val data = res.effectiveData.orEmpty()
                _state.update { it.copy(loading = false, articles = data) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(loading = false, error = e.message ?: "网络错误")
                }
            }
        }
    }
}