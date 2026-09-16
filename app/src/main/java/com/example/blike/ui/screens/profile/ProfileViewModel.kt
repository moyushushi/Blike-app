package com.example.blike.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blike.data.model.ChangePasswordRequest
import com.example.blike.data.model.UpdateBioRequest
import com.example.blike.data.model.User
import com.example.blike.di.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File


data class ProfileUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val user: User? = null,

    // 编辑 bio
    val editingBio: Boolean = false,
    val bioInput: String = "",
    val savingBio: Boolean = false,

    // 上传头像
    val uploadingAvatar: Boolean = false,

    val changingPassword: Boolean = false,   // ← 新增

    // 退出登录
    val loggedOut: Boolean = false,


)


class ProfileViewModel : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state = _state.asStateFlow()

    init {
        loadUser()
    }

    fun loadUser() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val res = ServiceLocator.apiService.getMe()
                val user = res.effectiveData
                _state.update {
                    it.copy(
                        loading = false,
                        user = user,
                        bioInput = user?.bio ?: ""
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(loading = false, error = e.message ?: "网络异常")
                }
            }
        }
    }

    // ==================== 编辑 bio ====================

    fun startEditBio() {
        _state.update {
            it.copy(editingBio = true, bioInput = it.user?.bio ?: "")
        }
    }

    fun cancelEditBio() {
        _state.update { it.copy(editingBio = false) }
    }

    fun onBioChange(v: String) {
        _state.update { it.copy(bioInput = v) }
    }

    fun saveBio() {
        val bio = _state.value.bioInput.trim()
        viewModelScope.launch {
            _state.update { it.copy(savingBio = true, error = null) }
            try {
                val res = ServiceLocator.apiService.updateBio(UpdateBioRequest(bio))
                if (res.success) {
                    _state.update {
                        it.copy(
                            savingBio = false,
                            editingBio = false,
                            user = it.user?.copy(bio = bio)
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            savingBio = false,
                            error = res.effectiveMessage ?: "保存失败"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(savingBio = false, error = e.message ?: "网络异常")
                }
            }
        }
    }

    // ==================== 上传头像 ====================

    fun uploadAvatar(file: File) {
        viewModelScope.launch {
            _state.update { it.copy(uploadingAvatar = true, error = null) }
            try {
                val part = MultipartBody.Part.createFormData(
                    "avatar",
                    file.name,
                    file.asRequestBody("image/*".toMediaType())
                )
                val res = ServiceLocator.apiService.uploadAvatar(part)
                val newUrl = res.effectiveData
                if (res.success && newUrl != null) {
                    _state.update {
                        it.copy(
                            uploadingAvatar = false,
                            user = it.user?.copy(avatar = newUrl)
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            uploadingAvatar = false,
                            error = res.effectiveMessage ?: "上传失败"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(uploadingAvatar = false, error = e.message ?: "网络异常")
                }
            }
        }
    }

    // ==================== 退出登录 ====================

    fun logout() {
        // 根据你 TokenManager 的实际方法名改，常见的有 clearToken / clear / removeToken
        ServiceLocator.tokenManager.logout()
        _state.update { it.copy(loggedOut = true) }
    }
    // ==================== 修改密码 ====================

    fun changePassword(old: String, new: String, onResult: (Boolean) -> Unit) {
        if (old.isBlank() || new.isBlank()) {
            _state.update { it.copy(error = "请输入旧密码和新密码") }
            onResult(false)
            return
        }
        if (new.length < 6) {
            _state.update { it.copy(error = "新密码至少 6 位") }
            onResult(false)
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(changingPassword = true, error = null) }
            try {
                val res = ServiceLocator.apiService.changePassword(
                    ChangePasswordRequest(old, new)
                )
                if (res.success) {
                    _state.update { it.copy(changingPassword = false) }
                    onResult(true)
                } else {
                    _state.update {
                        it.copy(
                            changingPassword = false,
                            error = res.effectiveMessage ?: "修改失败"
                        )
                    }
                    onResult(false)
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(changingPassword = false, error = e.message ?: "网络异常")
                }
                onResult(false)
            }
        }
    }

    fun consumeError() = _state.update { it.copy(error = null) }
}