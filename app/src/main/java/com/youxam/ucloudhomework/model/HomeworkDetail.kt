package com.youxam.ucloudhomework.model

import com.google.gson.annotations.SerializedName

/**
 * 作业详情数据模型
 * API 返回格式：直接返回作业详情对象，无外层 data 包装
 */
data class HomeworkDetail(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("assignmentTitle")
    val assignmentTitle: String,
    
    @SerializedName("assignmentContent")
    val assignmentContent: String?,
    
    @SerializedName("assignmentComment")
    val assignmentComment: String?,
    
    @SerializedName("className")
    val className: String?,
    
    @SerializedName("chapterName")
    val chapterName: String?,
    
    @SerializedName("assignmentType")
    val assignmentType: Int = 0,
    
    @SerializedName("noSubmitNum")
    val noSubmitNum: Int = 0,
    
    @SerializedName("totalNum")
    val totalNum: Int = 0,
    
    @SerializedName("stayReadNum")
    val stayReadNum: Int = 0,
    
    @SerializedName("alreadyReadNum")
    val alreadyReadNum: Int = 0,
    
    @SerializedName("isGroupExcellent")
    val isGroupExcellent: Int = 0,
    
    @SerializedName("assignmentBeginTime")
    val assignmentBeginTime: String?,
    
    @SerializedName("assignmentEndTime")
    val assignmentEndTime: String?,
    
    @SerializedName("isOvertimeCommit")
    val isOvertimeCommit: Int = 0,
    
    @SerializedName("assignmentStatus")
    val assignmentStatus: Int = 0,
    
    @SerializedName("teamId")
    val teamId: Int = -1,
    
    @SerializedName("isOpenEvaluation")
    val isOpenEvaluation: Int = 0,
    
    @SerializedName("status")
    val status: Int = 0,
    
    @SerializedName("groupScore")
    val groupScore: Int = -1,
    
    @SerializedName("assignmentScore")
    val assignmentScore: Int = -1,
    
    @SerializedName("fullMark")
    val fullMark: Int = 0,
    
    @SerializedName("assignmentResource")
    val assignmentResource: List<Resource>?,
    
    @SerializedName("assignmentMutualEvaluation")
    val assignmentMutualEvaluation: Any?,
    
    @SerializedName("courseInfo")
    val courseInfo: CourseInfo? = null,
    
    @SerializedName("resource")
    val resource: List<ResourceDetail>? = null
) {
    /**
     * 获取课程名称
     */
    fun getCourseName(): String = courseInfo?.name ?: "未知课程"
    
    /**
     * 获取作业标题
     */
    fun getTitle(): String = assignmentTitle
    
    /**
     * 获取作业内容（HTML 格式）
     */
    fun getContent(): String = assignmentContent ?: ""
    
    /**
     * 获取教师名称
     */
    fun getTeacherName(): String = courseInfo?.teachers ?: ""
    
    /**
     * 获取截止时间
     */
    fun getDeadline(): String = assignmentEndTime ?: "无截止时间"
    
    /**
     * 获取格式化的截止时间
     */
    fun getFormattedDeadline(): String = assignmentEndTime ?: "无截止时间"
}

/**
 * 资源文件数据模型
 */
data class Resource(
    @SerializedName("resourceId")
    val resourceId: String,
    
    @SerializedName("resourceName")
    val resourceName: String,
    
    @SerializedName("resourceType")
    val resourceType: String
)

/**
 * 资源详情数据模型（用于 resource 字段）
 */
data class ResourceDetail(
    @SerializedName("storageId")
    val storageId: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("ext")
    val ext: String,
    
    @SerializedName("url")
    val url: String? = null,
    
    @SerializedName("id")
    val id: String
)
