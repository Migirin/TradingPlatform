package com.example.tradingplatform.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradingplatform.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Error(val message: String) : AuthUiState
    data object Success : AuthUiState
    data class VerificationEmailSent(val email: String) : AuthUiState // 验证邮件已发送
    data object EmailVerified : AuthUiState // 邮箱已验证
}

class AuthViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val repo: AuthRepository = AuthRepository(application.applicationContext)
    private val _state = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val state: StateFlow<AuthUiState> = _state

    /**
     * 注册新用户
     */
    fun register(email: String, password: String) {
        if (!email.endsWith("@ucdconnect.ie", ignoreCase = true)) {
            _state.value = AuthUiState.Error("请使用 @ucdconnect.ie 校内邮箱")
            return
        }
        if (password.length < 6) {
            _state.value = AuthUiState.Error("密码至少需要6个字符")
            return
        }
        _state.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = repo.register(email, password)
            result.onSuccess {
                // 注册成功，验证邮件已发送
                _state.value = AuthUiState.VerificationEmailSent(email)
            }.onFailure { exception ->
                val errorMessage = exception.message ?: "注册失败"
                _state.value = AuthUiState.Error(errorMessage)
            }
        }
    }

    /**
     * 验证邮箱
     */
    fun verifyEmail(email: String, code: String) {
        _state.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = repo.verifyEmail(email, code)
            result.onSuccess {
                _state.value = AuthUiState.EmailVerified
            }.onFailure { exception ->
                val errorMessage = exception.message ?: "验证失败"
                _state.value = AuthUiState.Error(errorMessage)
            }
        }
    }

    /**
     * 登录
     */
    fun login(email: String, password: String) {
        if (!email.endsWith("@ucdconnect.ie", ignoreCase = true)) {
            _state.value = AuthUiState.Error("请使用 @ucdconnect.ie 校内邮箱")
            return
        }
        _state.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = repo.login(email, password)
            result.onSuccess {
                _state.value = AuthUiState.Success
            }.onFailure { exception ->
                val errorMessage = exception.message ?: "登录失败"
                _state.value = AuthUiState.Error(errorMessage)
            }
        }
    }

    /**
     * 重新发送验证邮件
     */
    fun resendVerificationEmail(email: String) {
        _state.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = repo.resendVerificationEmail(email)
            result.onSuccess {
                _state.value = AuthUiState.VerificationEmailSent(email)
            }.onFailure { exception ->
                val errorMessage = exception.message ?: "发送失败"
                _state.value = AuthUiState.Error(errorMessage)
            }
        }
    }

    /**
     * 删除用户（用于测试/清理数据）
     */
    fun deleteUser(email: String) {
        _state.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = repo.deleteUser(email)
            result.onSuccess {
                _state.value = AuthUiState.Idle
            }.onFailure { exception ->
                val errorMessage = exception.message ?: "删除失败"
                _state.value = AuthUiState.Error(errorMessage)
            }
        }
    }
}


