package com.youxam.ucloudhomework.network

import android.util.Log
import okhttp3.Authenticator
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.Proxy
import java.net.ProxySelector
import java.net.URI
import java.util.concurrent.TimeUnit

/**
 * Retrofit 客户端单例对象
 * 负责创建和管理 API 服务实例
 * 支持系统代理设置
 */
object RetrofitClient {
    
    private const val BASE_URL = "https://ucloud.youxam.workers.dev/"
    private const val CONNECT_TIMEOUT = 60L
    private const val READ_TIMEOUT = 60L
    private const val WRITE_TIMEOUT = 60L
    private const val TAG = "RetrofitClient"
    
    @Volatile
    private var credentials: String? = null
    
    @Volatile
    private var retrofit: Retrofit? = null
    
    @Volatile
    private var apiService: UCloudApiService? = null
    
    /**
     * 认证拦截器 - 自动添加 HTTP Basic Auth 头
     */
    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()
        
        credentials?.let { cred ->
            // cred 已经包含 "Basic " 前缀，直接使用
            requestBuilder.addHeader("Authorization", cred)
        }
        
        requestBuilder.method(original.method, original.body)
        chain.proceed(requestBuilder.build())
    }
    
    /**
     * 日志拦截器 - 用于调试
     */
    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d(TAG, message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    /**
     * OkHttpClient 配置
     * 使用系统代理选择器，自动支持手机代理软件
     */
    private fun createOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .proxySelector(ProxySelector.getDefault())
            .proxyAuthenticator(ProxyAuthenticator())
        
        return builder.build()
    }
    
    /**
     * 代理认证器
     * 处理需要认证的代理服务器
     */
    private class ProxyAuthenticator : Authenticator {
        override fun authenticate(route: Route?, response: okhttp3.Response): okhttp3.Request? {
            // 检查是否是代理认证挑战
            val challenges = response.challenges()
            if (challenges.isEmpty()) return null
            
            Log.d(TAG, "代理需要认证: ${challenges.first().scheme}")
            
            // 如果代理软件需要认证，可以从系统属性或设置中读取
            // 大多数手机代理软件不需要认证
            return null
        }
    }
    
    /**
     * 设置用户凭证
     * @param studentId 学号
     * @param password 统一认证密码
     */
    fun setCredentials(studentId: String, password: String) {
        credentials = Credentials.basic(studentId, password)
        // 凭证更新后，重新创建 Retrofit 实例
        retrofit = null
        apiService = null
    }
    
    /**
     * 清除用户凭证
     */
    fun clearCredentials() {
        credentials = null
        retrofit = null
        apiService = null
    }
    
    /**
     * 获取 Retrofit 实例
     */
    private fun getRetrofit(): Retrofit {
        return retrofit ?: synchronized(this) {
            retrofit ?: buildRetrofit().also { retrofit = it }
        }
    }
    
    /**
     * 构建 Retrofit 实例
     */
    private fun buildRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    /**
     * 获取 API 服务实例
     */
    fun getApiService(): UCloudApiService {
        return apiService ?: synchronized(this) {
            apiService ?: getRetrofit().create(UCloudApiService::class.java).also { apiService = it }
        }
    }
    
    /**
     * 检查是否已设置凭证
     */
    fun hasCredentials(): Boolean = credentials != null
}
