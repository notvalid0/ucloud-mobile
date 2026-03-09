package com.youxam.ucloudhomework.network

import com.youxam.ucloudhomework.model.HomeworkDetail
import com.youxam.ucloudhomework.model.SearchResult
import com.youxam.ucloudhomework.model.UndoneListResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming

/**
 * UCloud API 服务接口
 */
interface UCloudApiService {
    
    /**
     * 获取未完成作业列表
     */
    @GET("undoneList")
    suspend fun getUndoneList(): Response<UndoneListResponse>
    
    /**
     * 获取作业详情
     * @param activityId 作业活动ID
     */
    @GET("homework")
    suspend fun getHomeworkDetail(
        @Query("id") activityId: String
    ): Response<HomeworkDetail>
    
    /**
     * 通过作业反向搜索课程信息
     * @param activityId 作业活动ID
     * @param keyword 搜索关键词（建议为作业标题）
     */
    @GET("search")
    suspend fun searchCourse(
        @Query("id") activityId: String,
        @Query("keyword") keyword: String
    ): Response<List<SearchResult>>
}
