package com.example.blike.ui.screens.video


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blike.data.model.Video
import com.example.blike.di.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VideoListUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val videos: List<Video> =emptyList()
)

class VideoListViewModel: ViewModel(){

    private val _state = MutableStateFlow(VideoListUiState())
    val state = _state.asStateFlow()

    init {
        load()
    }

    fun load(){
        val launch = viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val res = ServiceLocator.apiService.getVideoList()
                val data = res.effectiveData.orEmpty()
                _state.update { it.copy(loading = false, videos = data) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(loading = false, error = e.message ?: "网络错误")
                }
            }
        }
    }
}
