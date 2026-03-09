package com.youxam.ucloudhomework.network

/**
 * 网络请求结果封装类
 * 用于统一处理成功、失败和加载状态
 */
sealed class NetworkResult<out T> {
    
    /**
     * 成功状态
     * @param data 返回的数据
     */
    data class Success<out T>(val data: T) : NetworkResult<T>()
    
    /**
     * 错误状态
     * @param message 错误信息
     * @param code 错误代码（可选）
     */
    data class Error(val message: String, val code: Int? = null) : NetworkResult<Nothing>()
    
    /**
     * 加载中状态
     */
    data object Loading : NetworkResult<Nothing>()
    
    /**
     * 判断是否成功
     */
    val isSuccess: Boolean
        get() = this is Success
    
    /**
     * 判断是否失败
     */
    val isError: Boolean
        get() = this is Error
    
    /**
     * 判断是否加载中
     */
    val isLoading: Boolean
        get() = this is Loading
    
    /**
     * 获取数据，如果失败则返回 null
     */
    fun getOrNull(): T? {
        return when (this) {
            is Success -> data
            else -> null
        }
    }
    
    /**
     * 获取错误信息，如果成功则返回 null
     */
    fun errorOrNull(): String? {
        return when (this) {
            is Error -> message
            else -> null
        }
    }
    
    /**
     * 映射转换数据
     */
    inline fun <R> map(transform: (T) -> R): NetworkResult<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> Error(message, code)
            is Loading -> Loading
        }
    }
    
    /**
     * 成功时执行操作
     */
    inline fun onSuccess(action: (T) -> Unit): NetworkResult<T> {
        if (this is Success) {
            action(data)
        }
        return this
    }
    
    /**
     * 失败时执行操作
     */
    inline fun onError(action: (String, Int?) -> Unit): NetworkResult<T> {
        if (this is Error) {
            action(message, code)
        }
        return this
    }
    
    /**
     * 加载中时执行操作
     */
    inline fun onLoading(action: () -> Unit): NetworkResult<T> {
        if (this is Loading) {
            action()
        }
        return this
    }
}
