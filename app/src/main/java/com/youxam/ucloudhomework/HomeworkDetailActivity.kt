package com.youxam.ucloudhomework

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.youxam.ucloudhomework.databinding.ActivityHomeworkDetailBinding
import com.youxam.ucloudhomework.network.NetworkResult
import com.youxam.ucloudhomework.viewmodel.HomeworkDetailViewModel
import kotlinx.coroutines.launch

/**
 * 作业详情页面
 */
class HomeworkDetailActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityHomeworkDetailBinding
    private val viewModel: HomeworkDetailViewModel by viewModels {
        HomeworkDetailViewModel.Factory(application)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeworkDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initViews()
        setupObservers()
        loadDetail()
    }
    
    private fun initViews() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // 设置初始标题
        intent.getStringExtra(EXTRA_TITLE)?.let {
            binding.tvTitle.text = it
        }
        
        intent.getStringExtra(EXTRA_COURSE_NAME)?.let {
            binding.tvCourseName.text = it
        }
        
        // 重试按钮
        binding.btnRetry.setOnClickListener {
            loadDetail()
        }
    }
    
    private fun loadDetail() {
        val activityId = intent.getStringExtra(EXTRA_ACTIVITY_ID)
        android.util.Log.d("HomeworkDetail", "加载详情：activityId=$activityId")
        if (!activityId.isNullOrBlank()) {
            viewModel.loadHomeworkDetail(activityId)
        } else {
            android.util.Log.e("HomeworkDetail", "activityId 为空")
        }
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.detailState.collect { result ->
                    when (result) {
                        is NetworkResult.Loading -> {
                            showLoading(true)
                            showError(false)
                        }
                        is NetworkResult.Success -> {
                            showLoading(false)
                            showError(false)
                            bindDetail(result.data)
                        }
                        is NetworkResult.Error -> {
                            showLoading(false)
                            showError(true, result.message)
                        }
                    }
                }
            }
        }
    }
    
    private fun bindDetail(detail: com.youxam.ucloudhomework.model.HomeworkDetail) {
        android.util.Log.d("HomeworkDetail", "绑定详情数据")
        android.util.Log.d("HomeworkDetail", "courseInfo: ${detail.courseInfo}")
        android.util.Log.d("HomeworkDetail", "courseName: ${detail.getCourseName()}")
        android.util.Log.d("HomeworkDetail", "assignmentTitle: ${detail.assignmentTitle}")
        android.util.Log.d("HomeworkDetail", "assignmentEndTime: ${detail.assignmentEndTime}")
        
        binding.apply {
            // 课程名称
            tvCourseName.text = detail.getCourseName()
            
            // 标题
            tvTitle.text = detail.getTitle()
            
            // 教师名称
            val teacherText = if (!detail.getTeacherName().isNullOrBlank()) {
                "授课教师：${detail.getTeacherName()}"
            } else {
                "未找到教师信息"
            }
            tvTeacherName.text = teacherText
            
            // 截止时间
            tvDeadline.text = detail.getFormattedDeadline()
            
            // 剩余时间和颜色
            val remainingTime = detail.assignmentEndTime?.let { deadline ->
                // 尝试解析日期时间字符串
                try {
                    // 处理 "2026-03-16 23:59" 格式
                    val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.CHINA)
                    val timestamp = format.parse(deadline)?.time ?: System.currentTimeMillis()
                    val now = System.currentTimeMillis()
                    val diff = timestamp - now
                    
                    when {
                        diff < 0 -> {
                            // 已过期 - 红色
                            tvRemainingTime.setTextColor(getColor(R.color.status_overdue))
                            "已过期"
                        }
                        diff < 24 * 60 * 60 * 1000 -> {
                            // 1 天内 - 橙色
                            tvRemainingTime.setTextColor(getColor(R.color.status_urgent))
                            val hours = diff / (60 * 60 * 1000)
                            val minutes = (diff % (60 * 60 * 1000)) / (60 * 1000)
                            when {
                                hours > 0 -> "剩余 ${hours}小时 ${minutes}分钟"
                                minutes > 0 -> "剩余 ${minutes}分钟"
                                else -> "即将截止"
                            }
                        }
                        else -> {
                            // 正常 - 绿色
                            tvRemainingTime.setTextColor(getColor(R.color.status_pending))
                            val days = diff / (24 * 60 * 60 * 1000)
                            val hours = (diff % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000)
                            when {
                                days > 0 -> "剩余 ${days}天 ${hours}小时"
                                hours > 0 -> "剩余 ${hours}小时"
                                else -> "即将截止"
                            }
                        }
                    }
                } catch (e: Exception) {
                    // 解析失败，直接显示原文本
                    tvRemainingTime.setTextColor(getColor(R.color.status_pending))
                    "剩余时间：$deadline"
                }
            } ?: "无截止时间"
            
            tvRemainingTime.text = remainingTime
            
            // 作业内容（HTML 格式）
            val contentHtml = detail.getContent()
            if (!contentHtml.isBlank()) {
                contentCard.visibility = View.VISIBLE
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    tvContent.text = android.text.Html.fromHtml(contentHtml, android.text.Html.FROM_HTML_MODE_COMPACT)
                } else {
                    @Suppress("DEPRECATION")
                    tvContent.text = android.text.Html.fromHtml(contentHtml)
                }
            } else {
                contentCard.visibility = View.GONE
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.loadingView.isVisible = show
    }
    
    private fun showError(show: Boolean, message: String? = null) {
        binding.errorView.isVisible = show
        
        if (show && message != null) {
            binding.tvError.text = message
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    companion object {
        const val EXTRA_ACTIVITY_ID = "activity_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_COURSE_NAME = "course_name"
    }
}
