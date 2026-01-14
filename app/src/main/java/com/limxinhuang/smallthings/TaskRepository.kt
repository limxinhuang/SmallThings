package com.limxinhuang.smallthings

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Calendar

class TaskRepository(context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val taskDao = db.taskDao()

    // 挂起函数需要在后台线程执行
    suspend fun insertTask(task: Task): Long = withContext(Dispatchers.IO) {
        taskDao.insertTask(task)
    }

    suspend fun updateTask(task: Task) = withContext(Dispatchers.IO) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(taskId: Long): Int = withContext(Dispatchers.IO) {
        taskDao.deleteTask(taskId)
    }

    suspend fun getAllTasks(): List<Task> = withContext(Dispatchers.IO) {
        taskDao.getAllTasks()
    }

    suspend fun getUsedColors(): List<String> = withContext(Dispatchers.IO) {
        taskDao.getUsedColors()
    }

    suspend fun incrementCompletedCount(taskId: Long): Int = withContext(Dispatchers.IO) {
        taskDao.incrementCompletedCount(taskId)
        taskDao.getCompletedCount(taskId)
    }

    suspend fun getTotalCompletedCount(): Int = withContext(Dispatchers.IO) {
        taskDao.getTotalCompletedCount()
    }

    suspend fun insertCompletionRecord(record: CompletionRecord): Long = withContext(Dispatchers.IO) {
        taskDao.insertCompletionRecord(record)
    }

    suspend fun getTodayCompletionRecords(): List<CompletionRecord> = withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val todayStart = calendar.timeInMillis
        taskDao.getTodayCompletionRecords(todayStart)
    }

    suspend fun getWeekCompletionRecords(): List<CompletionRecord> = withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val weekStart = calendar.timeInMillis
        taskDao.getWeekCompletionRecords(weekStart)
    }

    suspend fun getMonthCompletionRecords(): List<CompletionRecord> = withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val monthStart = calendar.timeInMillis
        taskDao.getMonthCompletionRecords(monthStart)
    }

    suspend fun getYearCompletionRecords(): List<CompletionRecord> = withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_YEAR, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val yearStart = calendar.timeInMillis
        taskDao.getYearCompletionRecords(yearStart)
    }

    // 导出数据为 JSON 字符串
    suspend fun exportDataToJson(): String = withContext(Dispatchers.IO) {
        val tasks = taskDao.getAllTasks()
        val records = taskDao.getAllCompletionRecords()

        val backupData = BackupData(
            tasks = tasks,
            completionRecords = records
        )

        Json.encodeToString(backupData)
    }

    // 从 JSON 字符串导入数据
    suspend fun importDataFromJson(jsonString: String) = withContext(Dispatchers.IO) {
        val backupData = Json.decodeFromString<BackupData>(jsonString)

        // 清除现有数据
        taskDao.clearAllCompletionRecords()
        taskDao.clearAllTasks()

        // 导入任务
        backupData.tasks.forEach { task ->
            taskDao.insertTask(task)
        }

        // 导入完成记录
        backupData.completionRecords.forEach { record ->
            taskDao.insertCompletionRecord(record)
        }
    }

    // 获取所有每日总结(按日期倒序)
    suspend fun getAllDailySummaries(): List<DailySummary> = withContext(Dispatchers.IO) {
        val allRecords = taskDao.getAllCompletionRecords()

        // 按日期分组
        val recordsByDate = allRecords.groupBy { record ->
            // 将时间戳转换为当天0点
            val calendar = Calendar.getInstance().apply {
                timeInMillis = record.completedAt
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            calendar.timeInMillis
        }

        // 为每一天创建总结
        recordsByDate.map { (date, records) ->
            val taskCount = records.size
            val totalDuration = records.sumOf { it.duration }

            // 计算最专注任务(完成次数最多的)
            val taskCounts = records.groupingBy { it.taskName }.eachCount()
            val mostFocusedTaskName = taskCounts.maxByOrNull { it.value }?.key
            val mostFocusedTaskCount = taskCounts.maxByOrNull { it.value }?.value ?: 0

            // 计算最专注任务的总时长
            val mostFocusedTaskRecords = records.filter { it.taskName == mostFocusedTaskName }
            val mostFocusedTaskDuration = mostFocusedTaskRecords.sumOf { it.duration }

            DailySummary(
                date = date,
                taskCount = taskCount,
                totalDuration = totalDuration,
                mostFocusedTask = mostFocusedTaskName,
                mostFocusedTaskCount = mostFocusedTaskCount,
                mostFocusedTaskDuration = mostFocusedTaskDuration,
                records = records.sortedByDescending { it.completedAt }
            )
        }.sortedByDescending { it.date }
    }
}
