package com.youxam.ucloudhomework.model

/**
 * 用户凭证数据模型
 */
data class UserCredentials(
    val studentId: String,
    val password: String,
    val rememberMe: Boolean = false
) {
    /**
     * 检查凭证是否有效
     */
    fun isValid(): Boolean {
        return studentId.isNotBlank() && password.isNotBlank()
    }
}

/**
 * 用户会话状态
 */
data class UserSession(
    val isLoggedIn: Boolean,
    val studentId: String? = null,
    val lastLoginTime: Long? = null
)

