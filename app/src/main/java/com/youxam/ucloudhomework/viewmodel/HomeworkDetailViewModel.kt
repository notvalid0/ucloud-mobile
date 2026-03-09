package com.youxam.ucloudhomework.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.youxam.ucloudhomework.model.HomeworkDetail
import com.youxam.ucloudhomework.network.NetworkResult
import com.youxam.ucloudhomework.repository.HomeworkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 作业详情页面 ViewModel
 */
class HomeworkDetailViewModel(application: Application) : AndroidViewModel(application) {
    
    private val homeworkRepo = HomeworkRepository.getInstance()
    
    private val _detailState = MutableStateFlow<NetworkResult<HomeworkDetail>>(NetworkResult.Loading)
    val detailState: StateFlow<NetworkResult<HomeworkDetail>> = _detailState.asStateFlow()
    
    /**
     * 加载作业详情
     */
    fun loadHomeworkDetail(activityId: String) {
        viewModelScope.launch {
            _detailState.value = NetworkResult.Loading
            
            val result = homeworkRepo.getHomeworkDetail(activityId)
            _detailState.value = result
        }
    }
    
    /**
     * Factory
     */
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeworkDetailViewModel::class.java)) {
                return HomeworkDetailViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

