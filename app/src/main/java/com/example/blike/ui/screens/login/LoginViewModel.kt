package com.example.blike.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blike.data.model.LoginRequest
import com.example.blike.di.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val remember: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class LoginViewModel : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state = _state.asStateFlow()

    fun onUsernameChange(v: String) =
        _state.update { it.copy(username = v, error = null) }

    fun onPasswordChange(v: String) =
        _state.update { it.copy(password = v, error = null) }

    fun onRememberChange(v: Boolean) =
        _state.update { it.copy(remember = v) }

    fun consumeError() = _state.update { it.copy(error = null) }
    fun consumeSuccess() = _state.update { it.copy(success = false) }

    fun login() {
        val s = _state.value
        if (s.username.isBlank() || s.password.isBlank()) {
            _state.update { it.copy(error = "请输入账号和密码") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val res = ServiceLocator.apiService.login(
                    LoginRequest(s.username.trim(), s.password, s.remember)
                )
                val data = res.effectiveData
                if (res.effectiveStatus == 200 && data != null) {
                    ServiceLocator.tokenManager.saveLogin(
                        token = data.token,
                        userId = data.user.id,
                        username = data.user.username,
                        email = data.user.email,
                        avatar = data.user.avatar
                    )
                    _state.update { it.copy(loading = false, success = true) }
                } else {
                    _state.update {
                        it.copy(
                            loading = false,
                            error = res.effectiveMessage ?: "登录失败（${res.effectiveStatus}）"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(loading = false, error = e.message ?: "网络异常")
                }
            }
        }
    }

}