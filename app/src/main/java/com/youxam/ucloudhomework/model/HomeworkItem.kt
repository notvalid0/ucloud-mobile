package com.youxam.ucloudhomework.model

import com.google.gson.annotations.SerializedName

/**
 * 未完成作业列表项数据模型
 * 对应API返回的 UndoneListItem 结构
 */
data class HomeworkItem(
    @SerializedName("siteId")
    val siteId: Long = 0,
    
    @SerializedName("siteName")
    val siteName: String = "",
    
    @SerializedName("activityName")
    val activityName: String = "",
    
    @SerializedName("activityId")
    val activityId: String = "",
    
    @SerializedName("type")
    val type: Int = 0,
    
    @SerializedName("endTime")
    val endTime: String = "",
    
    @SerializedName("assignmentType")
    val assignmentType: Int = 0,
    
    @SerializedName("evaluationStatus")
    val evaluationStatus: Int = 0,
    
    @SerializedName("isOpenEvaluation")
    val isOpenEvaluation: Int = 0,
    
    @SerializedName("courseInfo")
    val courseInfo: CourseInfo? = null
) {
    /**
     * 获取显示标题（作业名称）
     */
    fun getTitle(): String = activityName.ifEmpty { "未知作业" }
    
    /**
     * 获取课程名称
     */
    fun getCourseName(): String = courseInfo?.name ?: siteName.ifEmpty { "未知课程" }
    
    /**
     * 获取截止时间字符串
     */
    fun getDeadline(): String = endTime
    
    /**
     * 判断作业是否已过期
     */
    fun isOverdue(): Boolean {
        if (endTime.isEmpty()) return false
        return try {
            val deadlineTime = endTime.toLongOrNull() ?: return false
            System.currentTimeMillis() > deadlineTime
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 获取格式化的截止时间
     */
    fun getFormattedDeadline(): String {
        if (endTime.isEmpty()) return "无截止时间"
        return try {
            val timestamp = endTime.toLongOrNull() ?: return endTime
            val date = java.util.Date(timestamp)
            val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.CHINA)
            format.format(date)
        } catch (e: Exception) {
            endTime
        }
    }
    
    /**
     * 获取剩余时间描述
     */
    fun getRemainingTime(): String {
        if (endTime.isEmpty()) return "无截止时间"
        
        return try {
            val deadlineTime = endTime.toLongOrNull() ?: return endTime
            val now = System.currentTimeMillis()
            val diff = deadlineTime - now
            
            if (diff < 0) return "已过期"
            
            val days = diff / (24 * 60 * 60 * 1000)
            val hours = (diff % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000)
            val minutes = (diff % (60 * 60 * 1000)) / (60 * 1000)
            
            when {
                days > 0 -> "${days}天${hours}小时"
                hours > 0 -> "${hours}小时${minutes}分钟"
                minutes > 0 -> "${minutes}分钟"
                else -> "即将截止"
            }
        } catch (e: Exception) {
            endTime
        }
    }
    
    /**
     * 获取作业类型描述
     */
    fun getTypeText(): String {
        return when (type) {
            1 -> "作业"
            2 -> "测验"
            3 -> "考试"
            else -> "任务"
        }
    }
    
    /**
     * 获取紧急程度颜色（用于UI显示）
     * 0: 已过期/紧急（红色）
     * 1: 临近截止（橙色）
     * 2: 正常（绿色）
     */
    fun getUrgencyLevel(): Int {
        if (endTime.isEmpty()) return 2
        return try {
            val deadlineTime = endTime.toLongOrNull() ?: return 2
            val now = System.currentTimeMillis()
            val diff = deadlineTime - now
            
            when {
                diff < 0 -> 0  // 已过期
                diff < 24 * 60 * 60 * 1000 -> 1  // 24小时内
                else -> 2  // 正常
            }
        } catch (e: Exception) {
            2
        }
    }
}

/**
 * 课程信息
 */
data class CourseInfo(
    @SerializedName("id")
    val id: String? = null,
    
    @SerializedName("name")
    val name: String? = null,
    
    @SerializedName("teachers")
    val teachers: String? = null
)

/**
 * API 响应结构
 * 实际返回格式：{"siteNum":19,"undoneNum":0,"undoneList":[]}
 */
data class UndoneListResponse(
    @SerializedName("siteNum")
    val siteNum: Int = 0,
    
    @SerializedName("undoneNum")
    val undoneNum: Int = 0,
    
    @SerializedName("undoneList")
    val undoneList: List<HomeworkItem> = emptyList()
) {
    fun isSuccess(): Boolean = true  // 200 状态码即成功
    
    fun getErrorMessage(): String = "请求失败"
    
    fun getHomeworkList(): List<HomeworkItem> = undoneList
}
