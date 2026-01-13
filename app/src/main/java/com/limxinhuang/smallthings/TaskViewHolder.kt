package com.limxinhuang.smallthings

import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.limxinhuang.smallthings.databinding.ItemTaskBinding

class TaskViewHolder(private val binding: ItemTaskBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(task: Task, onClick: (Task) -> Unit) {
        binding.tvTaskName.text = task.name
        binding.tvTaskDuration.text = "${task.durationMinutes} 分钟"

        val color = Color.parseColor(task.colorCode)
        binding.viewTaskColor.setBackgroundColor(color)

        // 给整个卡片布局设置点击事件
        binding.layoutCardContent.setOnClickListener {
            onClick(task)
        }
    }
}
