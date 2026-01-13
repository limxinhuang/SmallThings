package com.limxinhuang.smallthings

import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.limxinhuang.smallthings.databinding.ItemCompletionRecordBinding
import java.text.SimpleDateFormat
import java.util.*

class CompletionRecordViewHolder(private val binding: ItemCompletionRecordBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(record: CompletionRecord) {
        binding.tvTaskName.text = record.taskName
        binding.tvDuration.text = "${record.duration} 分钟"

        // 设置任务名称颜色
        try {
            val color = Color.parseColor(record.taskColor)
            binding.tvTaskName.setTextColor(color)
        } catch (e: Exception) {
            binding.tvTaskName.setTextColor(Color.parseColor("#FF5252"))
        }

        // 设置时间条颜色
        binding.viewColorBar.setBackgroundColor(Color.parseColor(record.taskColor))

        // 格式化完成时间 - 显示完整日期和时间
        val dateFormat = SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault())
        val timeStr = dateFormat.format(Date(record.completedAt))
        binding.tvTime.text = timeStr
    }
}
