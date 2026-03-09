package com.youxam.ucloudhomework.model

import com.google.gson.annotations.SerializedName

/**
 * API 通用响应包装类
 * 处理API返回的嵌套数据结构
 */
data class ApiResponse<T>(
    @SerializedName("code")
    val code: Int? = null,
    
    @SerializedName("data")
    val data: T? = null,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("msg")
    val msg: String? = null,
    
    @SerializedName("success")
    val success: Boolean? = null
) {
    /**
     * 判断请求是否成功
     */
    fun isSuccess(): Boolean {
        return code == 200 || code == 0 || success == true
    }
    
    /**
     * 获取错误消息
     */
    fun getErrorMessage(): String {
        return message ?: msg ?: "未知错误"
    }
}
