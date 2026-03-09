package com.youxam.ucloudhomework.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.youxam.ucloudhomework.R
import com.youxam.ucloudhomework.databinding.ItemHomeworkBinding
import com.youxam.ucloudhomework.model.HomeworkItem

/**
 * 作业列表适配器
 */
class HomeworkAdapter(
    private val onItemClick: (HomeworkItem) -> Unit
) : ListAdapter<HomeworkItem, HomeworkAdapter.HomeworkViewHolder>(HomeworkDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeworkViewHolder {
        val binding = ItemHomeworkBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HomeworkViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: HomeworkViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HomeworkViewHolder(
        private val binding: ItemHomeworkBinding,
        private val onItemClick: (HomeworkItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HomeworkItem) {
            binding.apply {
                // 课程名称
                tvCourseName.text = item.getCourseName()
                
                // 作业标题
                tvTitle.text = item.getTitle()
                
                // 截止时间
                tvDeadline.text = "截止：${item.getFormattedDeadline()}"
                
                // 剩余时间
                val remainingTime = item.getRemainingTime()
                tvRemainingTime.text = remainingTime
                
                // 根据是否过期设置颜色
                val context = root.context
                if (item.isOverdue()) {
                    statusIndicator.setBackgroundColor(
                        context.getColor(R.color.status_overdue)
                    )
                    tvRemainingTime.setTextColor(
                        context.getColor(R.color.status_overdue)
                    )
                    tvRemainingTime.text = "已过期"
                } else {
                    statusIndicator.setBackgroundColor(
                        context.getColor(R.color.status_pending)
                    )
                    tvRemainingTime.setTextColor(
                        context.getColor(R.color.status_pending)
                    )
                }
                
                // 教师名称
                tvTeacherName.text = if (!item.courseInfo?.teachers.isNullOrBlank()) {
                    "教师：${item.courseInfo?.teachers}"
                } else {
                    ""
                }
                
                // 点击事件
                root.setOnClickListener {
                    onItemClick(item)
                }
            }
        }
    }

    /**
     * DiffUtil 回调
     */
    class HomeworkDiffCallback : DiffUtil.ItemCallback<HomeworkItem>() {
        override fun areItemsTheSame(oldItem: HomeworkItem, newItem: HomeworkItem): Boolean {
            return oldItem.activityId == newItem.activityId
        }

        override fun areContentsTheSame(oldItem: HomeworkItem, newItem: HomeworkItem): Boolean {
            return oldItem == newItem
        }
    }
}
