package com.limxinhuang.smallthings

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DailySummary(
    val date: Long,  // 当天0点的时间戳
    val taskCount: Int,  // 完成任务数
    val totalDuration: Int,  // 总时长(分钟)
    val mostFocusedTask: String?,  // 最专注的任务名称
    val mostFocusedTaskCount: Int,  // 最专注任务完成次数
    val mostFocusedTaskDuration: Int,  // 最专注任务总时长
    val records: List<CompletionRecord>  // 当天的所有完成记录
) {
    // 格式化日期显示: "2025年1月13日 周一"
    fun getFormattedDate(): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = date
        }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val weekDays = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")
        val weekDay = weekDays[calendar.get(Calendar.DAY_OF_WEEK) - 1]

        return "${year}年${month}月${day}日 $weekDay"
    }

    // 判断是否是今天
    fun isToday(): Boolean {
        val todayCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return date == todayCalendar.timeInMillis
    }

    // 判断是否是昨天
    fun isYesterday(): Boolean {
        val yesterdayCalendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return date == yesterdayCalendar.timeInMillis
    }

    // 获取相对日期显示
    fun getRelativeDate(): String {
        return when {
            isToday() -> "今天"
            isYesterday() -> "昨天"
            else -> getFormattedDate()
        }
    }
}
