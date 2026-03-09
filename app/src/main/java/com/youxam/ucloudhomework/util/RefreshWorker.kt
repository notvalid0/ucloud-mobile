package com.youxam.ucloudhomework.util

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.youxam.ucloudhomework.model.UserCredentials
import com.youxam.ucloudhomework.network.RetrofitClient
import com.youxam.ucloudhomework.repository.HomeworkRepository
import com.youxam.ucloudhomework.repository.UserPreferencesRepository
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * 自动刷新作业列表的 Worker
 */
class RefreshWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    companion object {
        private const val TAG = "RefreshWorker"
        private const val WORK_NAME = "homework_refresh_work"
        
        /**
         * 安排定时刷新任务
         * 在每日 8:00, 12:00, 16:00 执行
         */
        fun schedulePeriodicRefresh(context: Context) {
            val workManager = WorkManager.getInstance(context)
            
            // 计算到下一个刷新时间点的延迟
            val initialDelay = calculateInitialDelay()
            
            // 创建周期性任务（每4小时执行一次）
            val refreshWork = PeriodicWorkRequestBuilder<RefreshWorker>(
                repeatInterval = 4,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .addTag(WORK_NAME)
                .build()
            
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                refreshWork
            )
            
            Log.d(TAG, "已安排定时刷新任务，初始延迟: $initialDelay ms")
        }
        
        /**
         * 取消定时刷新任务
         */
        fun cancelPeriodicRefresh(context: Context) {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelUniqueWork(WORK_NAME)
        }
        
        /**
         * 计算到下一个刷新时间点的延迟
         * 目标时间点：8:00, 12:00, 16:00
         */
        private fun calculateInitialDelay(): Long {
            val now = Calendar.getInstance()
            val targetTimes = listOf(8, 12, 16)
            
            var nextTarget: Calendar? = null
            
            for (hour in targetTimes) {
                val target = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                
                if (target.after(now)) {
                    nextTarget = target
                    break
                }
            }
            
            // 如果今天的时间点都过了，设置为明天的第一个时间点（8:00）
            if (nextTarget == null) {
                nextTarget = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 8)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
            }
            
            return nextTarget!!.timeInMillis - now.timeInMillis
        }
    }
    
    override suspend fun doWork(): Result {
        Log.d(TAG, "开始执行定时刷新任务")
        
        return try {
            val userPrefsRepo = UserPreferencesRepository.getInstance(applicationContext)
            val homeworkRepo = HomeworkRepository.getInstance()
            
            // 检查是否有保存的凭证
            val credentials = userPrefsRepo.getCredentials()
            if (credentials == null) {
                Log.d(TAG, "未找到保存的凭证，跳过刷新")
                return Result.success()
            }
            
            // 检查网络
            if (!NetworkUtils.isNetworkAvailable(applicationContext)) {
                Log.d(TAG, "无网络连接，跳过刷新")
                return Result.retry()
            }
            
            // 设置凭证
            RetrofitClient.setCredentials(credentials.studentId, credentials.password)
            
            // 执行刷新
            val result = homeworkRepo.getUndoneList(forceRefresh = true)
            
            when (result) {
                is com.youxam.ucloudhomework.network.NetworkResult.Success -> {
                    userPrefsRepo.updateLastRefreshTime()
                    Log.d(TAG, "刷新成功，共 ${result.data.size} 项作业")
                    // TODO: 发送通知
                    Result.success()
                }
                is com.youxam.ucloudhomework.network.NetworkResult.Error -> {
                    Log.e(TAG, "刷新失败: ${result.message}")
                    Result.retry()
                }
                is com.youxam.ucloudhomework.network.NetworkResult.Loading -> {
                    Result.success()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "刷新任务执行出错", e)
            Result.retry()
        }
    }
}
