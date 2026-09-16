package com.example.blike.ui.screens.forget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blike.di.ServiceLocator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ForgetUiState(
    val step: Int = 1,              // 1 = 验证邮箱，2 = 设置新密码
    val email: String = "",
    val code: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val loading: Boolean = false,
    val sendingCode: Boolean = false,
    val countdown: Int = 0,
    val error: String? = null,
    val success: Boolean = false
)

class ForgetViewModel : ViewModel() {

    private val _state = MutableStateFlow(ForgetUiState())
    val state = _state.asStateFlow()

    private var countdownJob: Job? = null

    fun onEmailChange(v: String)    = _state.update { it.copy(email = v, error = null) }
    fun onCodeChange(v: String)     = _state.update { it.copy(code = v, error = null) }
    fun onPasswordChange(v: String) = _state.update { it.copy(password = v, error = null) }
    fun onConfirmChange(v: String)  = _state.update { it.copy(confirmPassword = v, error = null) }

    fun consumeError()   = _state.update { it.copy(error = null) }
    fun consumeSuccess() = _state.update { it.copy(success = false) }

    fun backToStep1() = _state.update { it.copy(step = 1, code = "", error = null) }

    /** 发送重置密码验证码 */
    fun sendResetCode() {
        val email = _state.value.email.trim()
        if (email.isBlank()) {
            _state.update { it.copy(error = "请先输入邮箱") }
            return
        }
        if (_state.value.countdown > 0 || _state.value.sendingCode) return

        viewModelScope.launch {
            _state.update { it.copy(sendingCode = true, error = null) }
            try {
                val res = ServiceLocator.apiService.sendResetEmail(email)
                if (res.effectiveStatus == 200) {
                    _state.update { it.copy(sendingCode = false) }
                    startCountdown()
                } else {
                    _state.update {
                        it.copy(
                            sendingCode = false,
                            error = res.effectiveMessage ?: "验证码发送失败（${res.effectiveStatus}）"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(sendingCode = false, error = e.message ?: "网络异常")
                }
            }
        }
    }

    private fun startCountdown(seconds: Int = 60) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (i in seconds downTo 1) {
                _state.update { it.copy(countdown = i) }
                delay(1000L)
            }
            _state.update { it.copy(countdown = 0) }
        }
    }

    /** 第 1 步：校验邮箱验证码，通过后进入第 2 步 */
    fun verifyCode() {
        val s = _state.value
        if (s.email.isBlank()) {
            _state.update { it.copy(error = "请输入邮箱") }; return
        }
        if (s.code.isBlank()) {
            _state.update { it.copy(error = "请输入验证码") }; return
        }

        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val res = ServiceLocator.apiService.startReset(
                    email = s.email.trim(),
                    code  = s.code.trim()
                )
                if (res.effectiveStatus == 200) {
                    _state.update { it.copy(loading = false, step = 2) }
                } else {
                    _state.update {
                        it.copy(
                            loading = false,
                            error = res.effectiveMessage ?: "验证码校验失败（${res.effectiveStatus}）"
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

    /** 第 2 步：提交新密码 */
    fun submitNewPassword() {
        val s = _state.value
        when {
            s.password.length < 6 -> {
                _state.update { it.copy(error = "密码至少 6 位") }; return
            }
            s.password != s.confirmPassword -> {
                _state.update { it.copy(error = "两次输入的密码不一致") }; return
            }
        }

        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val res = ServiceLocator.apiService.doPassword(
                    email    = s.email.trim(),
                    password = s.password
                )
                if (res.effectiveStatus == 200) {
                    _state.update { it.copy(loading = false, success = true) }
                } else {
                    _state.update {
                        it.copy(
                            loading = false,
                            error = res.effectiveMessage ?: "重置失败（${res.effectiveStatus}）"
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

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}