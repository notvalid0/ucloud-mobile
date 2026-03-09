package com.youxam.ucloudhomework.model

import com.google.gson.annotations.SerializedName

/**
 * 搜索课程结果数据模型
 */
data class SearchResult(
    @SerializedName("courseId")
    val courseId: String?,
    
    @SerializedName("courseName")
    val courseName: String?,
    
    @SerializedName("teacherName")
    val teacherName: String?,
    
    @SerializedName("semester")
    val semester: String?,
    
    @SerializedName("credit")
    val credit: Double?,
    
    @SerializedName("description")
    val description: String?
)

