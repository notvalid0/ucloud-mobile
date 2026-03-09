package com.youxam.ucloudhomework.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.youxam.ucloudhomework.model.HomeworkDetail
import com.youxam.ucloudhomework.model.HomeworkItem
import com.youxam.ucloudhomework.model.SearchResult
import com.youxam.ucloudhomework.model.UndoneListResponse
import com.youxam.ucloudhomework.model.UserCredentials
import com.youxam.ucloudhomework.network.NetworkResult
import com.youxam.ucloudhomework.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException

/**
 * 作业数据仓库
 * 负责管理作业相关的数据获取和缓存
 */
class HomeworkRepository private constructor() {
    
    private val apiService = RetrofitClient.getApiService()
    private val gson = Gson()
    
    // 内存缓存
    private var cachedHomeworkList: List<HomeworkItem>? = null
    private var cacheTimestamp: Long = 0
    
    // 缓存有效期：5分钟
    private val cacheValidDuration = 5 * 60 * 1000L
    
    /**
     * 设置用户凭证
     */
    fun setCredentials(credentials: UserCredentials) {
        RetrofitClient.setCredentials(credentials.studentId, credentials.password)
    }
    
    /**
     * 清除用户凭证
     */
    fun clearCredentials() {
        RetrofitClient.clearCredentials()
        cachedHomeworkList = null
        cacheTimestamp = 0
    }
    
    /**
     * 检查是否已设置凭证
     */
    fun hasCredentials(): Boolean = RetrofitClient.hasCredentials()
    
    /**
     * 获取未完成作业列表
     * @param forceRefresh 是否强制刷新（忽略缓存）
     */
    suspend fun getUndoneList(forceRefresh: Boolean = false): NetworkResult<List<HomeworkItem>> {
        return withContext(Dispatchers.IO) {
            // 检查缓存
            if (!forceRefresh && isCacheValid()) {
                cachedHomeworkList?.let {
                    return@withContext NetworkResult.Success(it)
                }
            }
            
            try {
                val response = apiService.getUndoneList()
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.isSuccess()) {
                        val homeworkList = body.getHomeworkList()
                        
                        // 更新缓存
                        cachedHomeworkList = homeworkList
                        cacheTimestamp = System.currentTimeMillis()
                        
                        NetworkResult.Success(homeworkList)
                    } else {
                        val errorMsg = body?.getErrorMessage() ?: "响应数据为空"
                        Log.e(TAG, "API 返回错误：$errorMsg")
                        NetworkResult.Error(errorMsg)
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: ""
                    Log.e(TAG, "请求失败: code=${response.code()}, error=$errorBody")
                    
                    val errorMsg = when (response.code()) {
                        400 -> "请求参数错误"
                        401 -> "未授权，请重新登录"
                        403 -> "无权限访问"
                        404 -> "资源不存在"
                        500 -> "服务器错误"
                        else -> "请求失败：${response.code()}"
                    }
                    NetworkResult.Error(errorMsg, response.code())
                }
            } catch (e: IOException) {
                Log.e(TAG, "网络连接失败", e)
                val errorMessage = when (e) {
                    is java.net.UnknownHostException -> "无法连接服务器，请检查网络或域名是否正确"
                    else -> "网络连接失败，请检查网络设置"
                }
                NetworkResult.Error(errorMessage)
            } catch (e: Exception) {
                Log.e(TAG, "获取作业列表失败：${e.javaClass.simpleName} - ${e.message}", e)
                val errorMessage = when (e) {
                    is java.net.UnknownHostException -> "无法连接服务器，请检查网络"
                    is java.net.SocketTimeoutException -> "连接超时，请重试"
                    is javax.net.ssl.SSLException -> "SSL 连接失败，请检查网络设置"
                    else -> e.message ?: "未知错误 (${e.javaClass.simpleName})"
                }
                NetworkResult.Error(errorMessage)
            }
        }
    }
    
    /**
     * 解析未完成作业列表响应
     * API 返回格式：{ "code": 200, "data": { "undoneList": [...] } }
     */
    private fun parseUndoneListResponse(rawJson: String): List<HomeworkItem> {
        val jsonElement = JsonParser.parseString(rawJson)
        
        return when {
            // 如果是数组，直接解析
            jsonElement.isJsonArray -> {
                val type = object : TypeToken<List<HomeworkItem>>() {}.type
                gson.fromJson(rawJson, type)
            }
            // 如果是对象，解析完整响应结构
            jsonElement.isJsonObject -> {
                val response = gson.fromJson(rawJson, UndoneListResponse::class.java)
                
                if (response.isSuccess()) {
                    response.getHomeworkList()
                } else {
                    Log.w(TAG, "API 返回错误：${response.getErrorMessage()}")
                    emptyList()
                }
            }
            else -> {
                Log.w(TAG, "无法解析响应格式：$rawJson")
                emptyList()
            }
        }
    }
    
    /**
     * 获取作业详情
     * @param activityId 作业活动 ID
     */
    suspend fun getHomeworkDetail(activityId: String): NetworkResult<HomeworkDetail> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "请求作业详情：activityId=$activityId")
                val response = apiService.getHomeworkDetail(activityId)
                Log.d(TAG, "作业详情响应：code=${response.code()}")
                    
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Log.d(TAG, "作业详情获取成功：${body.assignmentTitle}")
                        NetworkResult.Success(body)
                    } else {
                        Log.e(TAG, "作业详情响应为空")
                        NetworkResult.Error("响应数据为空")
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: ""
                    Log.e(TAG, "获取作业详情失败：code=${response.code()}, error=$errorBody")
                    handleResponse(response) { it }
                }
            } catch (e: IOException) {
                Log.e(TAG, "网络连接失败", e)
                val errorMessage = when (e) {
                    is java.net.UnknownHostException -> "无法连接服务器，请检查网络或域名是否正确"
                    else -> "网络连接失败，请检查网络设置"
                }
                NetworkResult.Error(errorMessage)
            } catch (e: Exception) {
                Log.e(TAG, "获取作业详情失败：${e.javaClass.simpleName} - ${e.message}", e)
                val errorMessage = when (e) {
                    is java.net.UnknownHostException -> "无法连接服务器，请检查网络"
                    is java.net.SocketTimeoutException -> "连接超时，请重试"
                    is javax.net.ssl.SSLException -> "SSL 连接失败，请检查网络设置"
                    else -> e.message ?: "未知错误 (${e.javaClass.simpleName})"
                }
                NetworkResult.Error(errorMessage)
            }
        }
    }
    
    /**
     * 搜索课程信息
     * @param activityId 作业活动ID
     * @param keyword 搜索关键词
     */
    suspend fun searchCourse(activityId: String, keyword: String): NetworkResult<List<SearchResult>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.searchCourse(activityId, keyword)
                handleResponse(response) { it }
            } catch (e: IOException) {
                NetworkResult.Error("网络连接失败，请检查网络设置")
            } catch (e: Exception) {
                Log.e(TAG, "搜索课程失败", e)
                NetworkResult.Error(e.message ?: "未知错误")
            }
        }
    }
    
    /**
     * 验证用户凭证
     */
    suspend fun validateCredentials(credentials: UserCredentials): NetworkResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                // 临时设置凭证
                RetrofitClient.setCredentials(credentials.studentId, credentials.password)
                
                // 尝试获取作业列表来验证凭证
                val response = RetrofitClient.getApiService().getUndoneList()
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.isSuccess()) {
                        // 解析成功，凭证有效
                        Log.d(TAG, "登录验证成功")
                        NetworkResult.Success(true)
                    } else {
                        val errorMsg = body?.getErrorMessage() ?: "响应数据为空"
                        Log.e(TAG, "登录验证失败：$errorMsg")
                        RetrofitClient.clearCredentials()
                        NetworkResult.Error(errorMsg)
                    }
                } else {
                    // 尝试获取错误详情
                    val errorBody = response.errorBody()?.string() ?: ""
                    Log.e(TAG, "登录失败：code=${response.code()}, error=$errorBody")
                    
                    when (response.code()) {
                        401 -> NetworkResult.Error("学号或密码错误", 401)
                        403 -> NetworkResult.Error("无权限访问", 403)
                        500 -> {
                            // 尝试从错误响应中提取更多信息
                            val errorMsg = extractErrorMessage(errorBody)
                            NetworkResult.Error("服务器错误：$errorMsg", 500)
                        }
                        else -> NetworkResult.Error("登录失败：${response.code()}", response.code())
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "网络连接失败", e)
                RetrofitClient.clearCredentials()
                NetworkResult.Error("网络连接失败，请检查网络设置")
            } catch (e: Exception) {
                Log.e(TAG, "验证凭证失败：${e.javaClass.simpleName} - ${e.message}", e)
                RetrofitClient.clearCredentials()
                val errorMessage = when (e) {
                    is java.net.UnknownHostException -> "无法连接服务器，请检查网络"
                    is java.net.SocketTimeoutException -> "连接超时，请重试"
                    is javax.net.ssl.SSLException -> "SSL 连接失败，请检查网络设置"
                    else -> e.message ?: "未知错误 (${e.javaClass.simpleName})"
                }
                NetworkResult.Error(errorMessage)
            }
        }
    }
    
    /**
     * 从JSON响应中提取错误消息
     */
    private fun extractErrorMessage(json: String): String {
        return try {
            val jsonElement = JsonParser.parseString(json)
            if (jsonElement.isJsonObject) {
                val jsonObj = jsonElement.asJsonObject
                jsonObj.get("message")?.asString
                    ?: jsonObj.get("msg")?.asString
                    ?: jsonObj.get("error")?.asString
                    ?: "未知错误"
            } else {
                "未知错误"
            }
        } catch (e: Exception) {
            "未知错误"
        }
    }
    
    /**
     * 处理 API 响应
     */
    private inline fun <T, R> handleResponse(
        response: Response<T>,
        transform: (T) -> R
    ): NetworkResult<R> {
        return if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                NetworkResult.Success(transform(body))
            } else {
                NetworkResult.Error("响应数据为空")
            }
        } else {
            val errorMsg = when (response.code()) {
                400 -> "请求参数错误"
                401 -> "未授权，请重新登录"
                403 -> "无权限访问"
                404 -> "资源不存在"
                500 -> "服务器错误"
                else -> "请求失败：${response.code()}"
            }
            NetworkResult.Error(errorMsg, response.code())
        }
    }
    
    /**
     * 检查缓存是否有效
     */
    private fun isCacheValid(): Boolean {
        return cachedHomeworkList != null &&
                (System.currentTimeMillis() - cacheTimestamp) < cacheValidDuration
    }
    
    /**
     * 清除缓存
     */
    fun clearCache() {
        cachedHomeworkList = null
        cacheTimestamp = 0
    }
    
    companion object {
        private const val TAG = "HomeworkRepository"
        
        @Volatile
        private var instance: HomeworkRepository? = null
        
        fun getInstance(): HomeworkRepository {
            return instance ?: synchronized(this) {
                instance ?: HomeworkRepository().also { instance = it }
            }
        }
    }
}

