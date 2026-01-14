package com.limxinhuang.smallthings

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.limxinhuang.smallthings.databinding.ItemDailySummaryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailySummaryAdapter(
    private var summaries: List<DailySummary>
) : RecyclerView.Adapter<DailySummaryAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemDailySummaryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDailySummaryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val summary = summaries[position]

        holder.binding.apply {
            // 设置日期
            tvDate.text = summary.getRelativeDate()

            // 设置统计数据
            tvTaskCount.text = summary.taskCount.toString()
            tvTotalDuration.text = "${summary.totalDuration} 分钟"

            // 设置最专注任务
            if (summary.mostFocusedTask != null && summary.mostFocusedTaskCount > 0) {
                layoutMostFocused.visibility = View.VISIBLE
                tvMostFocusedTask.text = summary.mostFocusedTask
                tvMostFocusedStats.text = "完成 ${summary.mostFocusedTaskCount} 次 · ${summary.mostFocusedTaskDuration} 分钟"
            } else {
                layoutMostFocused.visibility = View.GONE
            }

            // 设置展开/收起
            val isExpanded = rvDetails.visibility == View.VISIBLE
            tvExpand.text = if (isExpanded) "收起详情 ▲" else "展开详情 ▼"

            tvExpand.setOnClickListener {
                if (rvDetails.visibility == View.VISIBLE) {
                    rvDetails.visibility = View.GONE
                    tvExpand.text = "展开详情 ▼"
                } else {
                    rvDetails.visibility = View.VISIBLE
                    tvExpand.text = "收起详情 ▲"

                    // 设置详情列表
                    if (rvDetails.adapter == null) {
                        rvDetails.layoutManager = LinearLayoutManager(rvDetails.context)
                    }
                    val detailAdapter = SummaryDetailAdapter(summary.records)
                    rvDetails.adapter = detailAdapter
                }
            }
        }
    }

    override fun getItemCount(): Int = summaries.size

    fun updateSummaries(newSummaries: List<DailySummary>) {
        summaries = newSummaries
        notifyDataSetChanged()
    }
}

// 详情列表的 Adapter
class SummaryDetailAdapter(
    private val records: List<CompletionRecord>
) : RecyclerView.Adapter<SummaryDetailAdapter.ViewHolder>() {

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_summary_detail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = records[position]

        holder.view.apply {
            // 设置任务颜色
            val colorView = findViewById<View>(R.id.view_color)
            try {
                colorView.setBackgroundColor(Color.parseColor(record.taskColor))
            } catch (e: Exception) {
                colorView.setBackgroundColor(Color.parseColor("#FF5252"))
            }

            // 设置任务名称
            findViewById<TextView>(R.id.tv_task_name).text = record.taskName

            // 设置完成时间
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            findViewById<TextView>(R.id.tv_task_time).text = timeFormat.format(Date(record.completedAt))

            // 设置时长
            findViewById<TextView>(R.id.tv_duration).text = "${record.duration} 分钟"
        }
    }

    override fun getItemCount(): Int = records.size
}
