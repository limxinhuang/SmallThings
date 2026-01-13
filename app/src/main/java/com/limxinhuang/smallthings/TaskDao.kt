package com.limxinhuang.smallthings

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: Long): Int

    @Query("SELECT * FROM tasks ORDER BY created_at DESC")
    suspend fun getAllTasks(): List<Task>

    @Query("SELECT DISTINCT color_code FROM tasks")
    suspend fun getUsedColors(): List<String>

    @Query("UPDATE tasks SET completed_count = completed_count + 1 WHERE id = :taskId")
    suspend fun incrementCompletedCount(taskId: Long)

    @Query("SELECT completed_count FROM tasks WHERE id = :taskId")
    suspend fun getCompletedCount(taskId: Long): Int

    @Query("SELECT SUM(completed_count) FROM tasks")
    suspend fun getTotalCompletedCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletionRecord(record: CompletionRecord): Long

    @Query("SELECT * FROM completion_records WHERE completed_at >= :todayStart ORDER BY completed_at DESC")
    suspend fun getTodayCompletionRecords(todayStart: Long): List<CompletionRecord>

    @Query("SELECT * FROM completion_records WHERE completed_at >= :startTime ORDER BY completed_at DESC")
    suspend fun getRecentCompletionRecords(startTime: Long): List<CompletionRecord>

    @Query("SELECT * FROM completion_records WHERE completed_at >= :weekStart ORDER BY completed_at DESC")
    suspend fun getWeekCompletionRecords(weekStart: Long): List<CompletionRecord>

    @Query("SELECT * FROM completion_records WHERE completed_at >= :monthStart ORDER BY completed_at DESC")
    suspend fun getMonthCompletionRecords(monthStart: Long): List<CompletionRecord>

    @Query("SELECT * FROM completion_records WHERE completed_at >= :yearStart ORDER BY completed_at DESC")
    suspend fun getYearCompletionRecords(yearStart: Long): List<CompletionRecord>

    // 导出/导入相关查询
    @Query("SELECT * FROM completion_records ORDER BY completed_at DESC")
    suspend fun getAllCompletionRecords(): List<CompletionRecord>

    @Query("DELETE FROM completion_records")
    suspend fun clearAllCompletionRecords()

    @Query("DELETE FROM tasks")
    suspend fun clearAllTasks()
}
