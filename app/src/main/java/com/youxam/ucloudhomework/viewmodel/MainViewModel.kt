package com.youxam.ucloudhomework.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.youxam.ucloudhomework.model.HomeworkItem
import com.youxam.ucloudhomework.network.NetworkResult
import com.youxam.ucloudhomework.repository.HomeworkRepository
import com.youxam.ucloudhomework.repository.UserPreferencesRepository
import com.youxam.ucloudhomework.util.NetworkUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 主页面 ViewModel
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val context = application.applicationContext
    private val homeworkRepo = HomeworkRepository.getInstance()
    private val userPrefsRepo = UserPreferencesRepository.getInstance(application)
    
    private val _homeworkState = MutableStateFlow<HomeworkState>(HomeworkState.Idle)
    val homeworkState: StateFlow<HomeworkState> = _homeworkState.asStateFlow()
    
    private val _homeworkList = MutableStateFlow<List<HomeworkItem>>(emptyList())
    val homeworkList: StateFlow<List<HomeworkItem>> = _homeworkList.asStateFlow()
    
    private val _lastUpdateTime = MutableStateFlow<String?>(null)
    val lastUpdateTime: StateFlow<String?> = _lastUpdateTime.asStateFlow()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    init {
        loadLastUpdateTime()
    }
    
    /**
     * 加载作业列表
     * @param forceRefresh 是否强制刷新
     */
    fun loadHomeworkList(forceRefresh: Boolean = false) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _homeworkState.value = HomeworkState.Error("无网络连接，请检查网络设置")
            return
        }
        
        viewModelScope.launch {
            _isRefreshing.value = true
            _homeworkState.value = HomeworkState.Loading
            
            val result = homeworkRepo.getUndoneList(forceRefresh)
            
            when (result) {
                is NetworkResult.Success -> {
                    _homeworkList.value = result.data
                    _homeworkState.value = if (result.data.isEmpty()) {
                        HomeworkState.Empty
                    } else {
                        HomeworkState.Success
                    }
                    updateRefreshTime()
                }
                is NetworkResult.Error -> {
                    _homeworkState.value = HomeworkState.Error(result.message)
                }
                is NetworkResult.Loading -> {
                    // 不应该到达这里
                }
            }
            
            _isRefreshing.value = false
        }
    }
    
    /**
     * 下拉刷新
     */
    fun refresh() {
        loadHomeworkList(forceRefresh = true)
    }
    
    /**
     * 退出登录
     */
    fun logout() {
        userPrefsRepo.clearCredentials()
        homeworkRepo.clearCredentials()
        homeworkRepo.clearCache()
        _homeworkList.value = emptyList()
        _homeworkState.value = HomeworkState.Idle
    }
    
    /**
     * 检查登录状态
     */
    fun checkLoginStatus(): Boolean {
        val hasCreds = userPrefsRepo.hasCredentials()
        if (hasCreds) {
            // 从存储中加载凭证并设置到 Repository
            val credentials = userPrefsRepo.getCredentials()
            credentials?.let {
                homeworkRepo.setCredentials(it)
            }
        }
        return hasCreds
    }
    
    /**
     * 更新刷新时间
     */
    private fun updateRefreshTime() {
        val currentTime = System.currentTimeMillis()
        userPrefsRepo.updateLastRefreshTime()
        
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
        _lastUpdateTime.value = format.format(Date(currentTime))
    }
    
    /**
     * 加载最后刷新时间
     */
    private fun loadLastUpdateTime() {
        val lastTime = userPrefsRepo.getLastRefreshTime()
        if (lastTime > 0) {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
            _lastUpdateTime.value = format.format(Date(lastTime))
        }
    }
}

/**
 * 作业列表状态
 */
sealed class HomeworkState {
    data object Idle : HomeworkState()
    data object Loading : HomeworkState()
    data object Success : HomeworkState()
    data object Empty : HomeworkState()
    data class Error(val message: String) : HomeworkState()
}

