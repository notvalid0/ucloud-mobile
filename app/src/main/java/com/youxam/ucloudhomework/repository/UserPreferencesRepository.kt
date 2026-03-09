package com.youxam.ucloudhomework.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.youxam.ucloudhomework.model.UserCredentials
import com.youxam.ucloudhomework.model.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 用户凭证存储仓库
 * 使用 EncryptedSharedPreferences 安全存储用户账号密码
 */
class UserPreferencesRepository private constructor(context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        SECURE_PREFS_FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    private val _userSession = MutableStateFlow<UserSession>(UserSession(isLoggedIn = false))
    val userSession: StateFlow<UserSession> = _userSession.asStateFlow()
    
    init {
        loadSession()
    }
    
    /**
     * 保存用户凭证
     * @param credentials 用户凭证
     */
    fun saveCredentials(credentials: UserCredentials) {
        encryptedPrefs.edit()
            .putString(KEY_STUDENT_ID, credentials.studentId)
            .putString(KEY_PASSWORD, credentials.password)
            .putBoolean(KEY_REMEMBER_ME, credentials.rememberMe)
            .putLong(KEY_LAST_LOGIN_TIME, System.currentTimeMillis())
            .apply()
        
        updateSession(credentials.studentId)
    }
    
    /**
     * 获取保存的用户凭证
     * @return 用户凭证，如果不存在则返回 null
     */
    fun getCredentials(): UserCredentials? {
        val studentId = encryptedPrefs.getString(KEY_STUDENT_ID, null) ?: return null
        val password = encryptedPrefs.getString(KEY_PASSWORD, null) ?: return null
        val rememberMe = encryptedPrefs.getBoolean(KEY_REMEMBER_ME, false)
        
        return UserCredentials(studentId, password, rememberMe)
    }
    
    /**
     * 清除保存的用户凭证
     */
    fun clearCredentials() {
        encryptedPrefs.edit()
            .remove(KEY_STUDENT_ID)
            .remove(KEY_PASSWORD)
            .remove(KEY_REMEMBER_ME)
            .clear()
            .apply()
        
        _userSession.value = UserSession(isLoggedIn = false)
    }
    
    /**
     * 检查是否已保存凭证
     */
    fun hasCredentials(): Boolean {
        val studentId = encryptedPrefs.getString(KEY_STUDENT_ID, null)
        val password = encryptedPrefs.getString(KEY_PASSWORD, null)
        return !studentId.isNullOrBlank() && !password.isNullOrBlank()
    }
    
    /**
     * 更新会话状态
     */
    private fun updateSession(studentId: String) {
        val lastLoginTime = encryptedPrefs.getLong(KEY_LAST_LOGIN_TIME, System.currentTimeMillis())
        _userSession.value = UserSession(
            isLoggedIn = true,
            studentId = studentId,
            lastLoginTime = lastLoginTime
        )
    }
    
    /**
     * 加载已保存的会话
     */
    private fun loadSession() {
        val credentials = getCredentials()
        _userSession.value = UserSession(
            isLoggedIn = credentials != null,
            studentId = credentials?.studentId,
            lastLoginTime = encryptedPrefs.getLong(KEY_LAST_LOGIN_TIME, 0)
        )
    }
    
    /**
     * 更新最后刷新时间
     */
    fun updateLastRefreshTime() {
        encryptedPrefs.edit()
            .putLong(KEY_LAST_REFRESH_TIME, System.currentTimeMillis())
            .apply()
    }
    
    /**
     * 获取最后刷新时间
     */
    fun getLastRefreshTime(): Long {
        return encryptedPrefs.getLong(KEY_LAST_REFRESH_TIME, 0)
    }
    
    companion object {
        private const val SECURE_PREFS_FILE_NAME = "secure_prefs"
        private const val KEY_STUDENT_ID = "student_id"
        private const val KEY_PASSWORD = "password"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_LAST_LOGIN_TIME = "last_login_time"
        private const val KEY_LAST_REFRESH_TIME = "last_refresh_time"
        
        @Volatile
        private var instance: UserPreferencesRepository? = null
        
        fun getInstance(context: Context): UserPreferencesRepository {
            return instance ?: synchronized(this) {
                instance ?: UserPreferencesRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
