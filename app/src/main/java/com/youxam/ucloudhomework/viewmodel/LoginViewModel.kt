package com.youxam.ucloudhomework.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.youxam.ucloudhomework.model.UserCredentials
import com.youxam.ucloudhomework.network.NetworkResult
import com.youxam.ucloudhomework.repository.HomeworkRepository
import com.youxam.ucloudhomework.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 登录页面 ViewModel
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {
    
    private val userPrefsRepo = UserPreferencesRepository.getInstance(application)
    private val homeworkRepo = HomeworkRepository.getInstance()
    
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()
    
    private val _savedStudentId = MutableStateFlow<String?>(null)
    val savedStudentId: StateFlow<String?> = _savedStudentId.asStateFlow()
    
    private val _rememberMe = MutableStateFlow(false)
    val rememberMe: StateFlow<Boolean> = _rememberMe.asStateFlow()
    
    init {
        loadSavedCredentials()
    }
    
    /**
     * 加载保存的凭证
     */
    private fun loadSavedCredentials() {
        val credentials = userPrefsRepo.getCredentials()
        if (credentials != null && credentials.rememberMe) {
            _savedStudentId.value = credentials.studentId
            _rememberMe.value = true
        }
    }
    
    /**
     * 登录
     * @param studentId 学号
     * @param password 密码
     * @param rememberMe 是否记住账号
     */
    fun login(studentId: String, password: String, rememberMe: Boolean) {
        if (studentId.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("请输入学号和密码")
            return
        }
        
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            
            val credentials = UserCredentials(studentId, password, rememberMe)
            val result = homeworkRepo.validateCredentials(credentials)
            
            when (result) {
                is NetworkResult.Success -> {
                    // 保存凭证
                    userPrefsRepo.saveCredentials(credentials)
                    homeworkRepo.setCredentials(credentials)
                    _loginState.value = LoginState.Success
                }
                is NetworkResult.Error -> {
                    _loginState.value = LoginState.Error(result.message)
                }
                is NetworkResult.Loading -> {
                    // 不应该到达这里
                }
            }
        }
    }
    
    /**
     * 重置状态
     */
    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}

/**
 * 登录状态
 */
sealed class LoginState {
    data object Idle : LoginState()
    data object Loading : LoginState()
    data object Success : LoginState()
    data class Error(val message: String) : LoginState()
}
