package com.limxinhuang.smallthings

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TaskDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "smallthings.db"
        private const val DATABASE_VERSION = 2

        const val TABLE_TASKS = "tasks"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_COLOR_CODE = "color_code"
        const val COLUMN_DURATION_MINUTES = "duration_minutes"
        const val COLUMN_CREATED_AT = "created_at"
        const val COLUMN_COMPLETED_COUNT = "completed_count"

        const val TABLE_COMPLETION_RECORDS = "completion_records"
        const val COLUMN_RECORD_ID = "record_id"
        const val COLUMN_TASK_ID = "task_id"
        const val COLUMN_TASK_NAME = "task_name"
        const val COLUMN_TASK_COLOR = "task_color"
        const val COLUMN_COMPLETED_AT = "completed_at"
        const val COLUMN_DURATION = "duration"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTasksTable = """
            CREATE TABLE $TABLE_TASKS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_COLOR_CODE TEXT NOT NULL,
                $COLUMN_DURATION_MINUTES INTEGER NOT NULL,
                $COLUMN_CREATED_AT INTEGER NOT NULL,
                $COLUMN_COMPLETED_COUNT INTEGER DEFAULT 0
            )
        """.trimIndent()

        val createRecordsTable = """
            CREATE TABLE $TABLE_COMPLETION_RECORDS (
                $COLUMN_RECORD_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TASK_ID INTEGER NOT NULL,
                $COLUMN_TASK_NAME TEXT NOT NULL,
                $COLUMN_TASK_COLOR TEXT NOT NULL,
                $COLUMN_COMPLETED_AT INTEGER NOT NULL,
                $COLUMN_DURATION INTEGER NOT NULL
            )
        """.trimIndent()

        db.execSQL(createTasksTable)
        db.execSQL(createRecordsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            val createRecordsTable = """
                CREATE TABLE IF NOT EXISTS $TABLE_COMPLETION_RECORDS (
                    $COLUMN_RECORD_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_TASK_ID INTEGER NOT NULL,
                    $COLUMN_TASK_NAME TEXT NOT NULL,
                    $COLUMN_TASK_COLOR TEXT NOT NULL,
                    $COLUMN_COMPLETED_AT INTEGER NOT NULL,
                    $COLUMN_DURATION INTEGER NOT NULL
                )
            """.trimIndent()
            db.execSQL(createRecordsTable)
        }
    }

    // 插入任务
    fun insertTask(task: Task): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, task.name)
            put(COLUMN_COLOR_CODE, task.colorCode)
            put(COLUMN_DURATION_MINUTES, task.durationMinutes)
            put(COLUMN_CREATED_AT, task.createdAt)
            put(COLUMN_COMPLETED_COUNT, task.completedCount)
        }
        return db.insert(TABLE_TASKS, null, values)
    }

    // 获取已使用的颜色列表
    fun getUsedColors(): List<String> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_TASKS,
            arrayOf(COLUMN_COLOR_CODE),
            null,
            null,
            null,
            null,
            null
        )

        val usedColors = mutableListOf<String>()
        cursor.use {
            if (it.moveToFirst()) {
                do {
                    val color = it.getString(it.getColumnIndexOrThrow(COLUMN_COLOR_CODE))
                    usedColors.add(color)
                } while (it.moveToNext())
            }
        }

        return usedColors.distinct()
    }

    // 获取所有任务
    fun getAllTasks(): List<Task> {
        val tasks = mutableListOf<Task>()
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_TASKS,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_CREATED_AT DESC"
        )

        cursor.use {
            if (it.moveToFirst()) {
                do {
                    tasks.add(
                        Task(
                            id = it.getLong(it.getColumnIndexOrThrow(COLUMN_ID)),
                            name = it.getString(it.getColumnIndexOrThrow(COLUMN_NAME)),
                            colorCode = it.getString(it.getColumnIndexOrThrow(COLUMN_COLOR_CODE)),
                            durationMinutes = it.getInt(it.getColumnIndexOrThrow(COLUMN_DURATION_MINUTES)),
                            createdAt = it.getLong(it.getColumnIndexOrThrow(COLUMN_CREATED_AT)),
                            completedCount = it.getInt(it.getColumnIndexOrThrow(COLUMN_COMPLETED_COUNT))
                        )
                    )
                } while (it.moveToNext())
            }
        }

        return tasks
    }

    // 增加完成次数
    fun incrementCompletedCount(taskId: Long): Int {
        val db = writableDatabase
        db.execSQL("UPDATE $TABLE_TASKS SET $COLUMN_COMPLETED_COUNT = $COLUMN_COMPLETED_COUNT + 1 WHERE $COLUMN_ID = $taskId")

        // 获取更新后的完成次数
        val cursor = db.query(
            TABLE_TASKS,
            arrayOf(COLUMN_COMPLETED_COUNT),
            "$COLUMN_ID = ?",
            arrayOf(taskId.toString()),
            null,
            null,
            null
        )

        var count = 0
        cursor.use {
            if (it.moveToFirst()) {
                count = it.getInt(it.getColumnIndexOrThrow(COLUMN_COMPLETED_COUNT))
            }
        }

        return count
    }

    // 更新任务
    fun updateTask(task: Task): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, task.name)
            put(COLUMN_COLOR_CODE, task.colorCode)
            put(COLUMN_DURATION_MINUTES, task.durationMinutes)
        }

        val rowsAffected = db.update(
            TABLE_TASKS,
            values,
            "$COLUMN_ID = ?",
            arrayOf(task.id.toString())
        )

        return rowsAffected > 0
    }

    // 删除任务
    fun deleteTask(taskId: Long): Boolean {
        val db = writableDatabase
        return db.delete(TABLE_TASKS, "$COLUMN_ID = ?", arrayOf(taskId.toString())) > 0
    }

    // 获取总完成次数
    fun getTotalCompletedCount(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT SUM($COLUMN_COMPLETED_COUNT) FROM $TABLE_TASKS", null)
        var total = 0
        cursor.use {
            if (it.moveToFirst()) {
                total = it.getInt(0) ?: 0
            }
        }
        return total
    }

    // 插入完成记录
    fun insertCompletionRecord(taskId: Long, taskName: String, taskColor: String, duration: Int): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TASK_ID, taskId)
            put(COLUMN_TASK_NAME, taskName)
            put(COLUMN_TASK_COLOR, taskColor)
            put(COLUMN_COMPLETED_AT, System.currentTimeMillis())
            put(COLUMN_DURATION, duration)
        }
        return db.insert(TABLE_COMPLETION_RECORDS, null, values)
    }

    // 获取今天的完成记录
    fun getTodayCompletionRecords(): List<CompletionRecord> {
        val records = mutableListOf<CompletionRecord>()
        val db = readableDatabase

        // 获取今天0点的时间戳
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val todayStart = calendar.timeInMillis

        val cursor = db.query(
            TABLE_COMPLETION_RECORDS,
            null,
            "$COLUMN_COMPLETED_AT >= ?",
            arrayOf(todayStart.toString()),
            null,
            null,
            "$COLUMN_COMPLETED_AT DESC"
        )

        cursor.use {
            if (it.moveToFirst()) {
                do {
                    records.add(
                        CompletionRecord(
                            recordId = it.getLong(it.getColumnIndexOrThrow(COLUMN_RECORD_ID)),
                            taskId = it.getLong(it.getColumnIndexOrThrow(COLUMN_TASK_ID)),
                            taskName = it.getString(it.getColumnIndexOrThrow(COLUMN_TASK_NAME)),
                            taskColor = it.getString(it.getColumnIndexOrThrow(COLUMN_TASK_COLOR)),
                            completedAt = it.getLong(it.getColumnIndexOrThrow(COLUMN_COMPLETED_AT)),
                            duration = it.getInt(it.getColumnIndexOrThrow(COLUMN_DURATION))
                        )
                    )
                } while (it.moveToNext())
            }
        }

        return records
    }

    // 获取最近7天的完成记录
    fun getRecentCompletionRecords(days: Int = 7): List<CompletionRecord> {
        val records = mutableListOf<CompletionRecord>()
        val db = readableDatabase

        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -days)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis

        val cursor = db.query(
            TABLE_COMPLETION_RECORDS,
            null,
            "$COLUMN_COMPLETED_AT >= ?",
            arrayOf(startTime.toString()),
            null,
            null,
            "$COLUMN_COMPLETED_AT DESC"
        )

        cursor.use {
            if (it.moveToFirst()) {
                do {
                    records.add(
                        CompletionRecord(
                            recordId = it.getLong(it.getColumnIndexOrThrow(COLUMN_RECORD_ID)),
                            taskId = it.getLong(it.getColumnIndexOrThrow(COLUMN_TASK_ID)),
                            taskName = it.getString(it.getColumnIndexOrThrow(COLUMN_TASK_NAME)),
                            taskColor = it.getString(it.getColumnIndexOrThrow(COLUMN_TASK_COLOR)),
                            completedAt = it.getLong(it.getColumnIndexOrThrow(COLUMN_COMPLETED_AT)),
                            duration = it.getInt(it.getColumnIndexOrThrow(COLUMN_DURATION))
                        )
                    )
                } while (it.moveToNext())
            }
        }

        return records
    }

    // 获取本周的完成记录
    fun getWeekCompletionRecords(): List<CompletionRecord> {
        val records = mutableListOf<CompletionRecord>()
        val db = readableDatabase

        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val weekStart = calendar.timeInMillis

        val cursor = db.query(
            TABLE_COMPLETION_RECORDS,
            null,
            "$COLUMN_COMPLETED_AT >= ?",
            arrayOf(weekStart.toString()),
            null,
            null,
            "$COLUMN_COMPLETED_AT DESC"
        )

        cursor.use {
            if (it.moveToFirst()) {
                do {
                    records.add(
                        CompletionRecord(
                            recordId = it.getLong(it.getColumnIndexOrThrow(COLUMN_RECORD_ID)),
                            taskId = it.getLong(it.getColumnIndexOrThrow(COLUMN_TASK_ID)),
                            taskName = it.getString(it.getColumnIndexOrThrow(COLUMN_TASK_NAME)),
                            taskColor = it.getString(it.getColumnIndexOrThrow(COLUMN_TASK_COLOR)),
                            completedAt = it.getLong(it.getColumnIndexOrThrow(COLUMN_COMPLETED_AT)),
                            duration = it.getInt(it.getColumnIndexOrThrow(COLUMN_DURATION))
                        )
                    )
                } while (it.moveToNext())
            }
        }

        return records
    }

    // 获取本月的完成记录
    fun getMonthCompletionRecords(): List<CompletionRecord> {
        val records = mutableListOf<CompletionRecord>()
        val db = readableDatabase

        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val monthStart = calendar.timeInMillis

        val cursor = db.query(
            TABLE_COMPLETION_RECORDS,
            null,
            "$COLUMN_COMPLETED_AT >= ?",
            arrayOf(monthStart.toString()),
            null,
            null,
            "$COLUMN_COMPLETED_AT DESC"
        )

        cursor.use {
            if (it.moveToFirst()) {
                do {
                    records.add(
                        CompletionRecord(
                            recordId = it.getLong(it.getColumnIndexOrThrow(COLUMN_RECORD_ID)),
                            taskId = it.getLong(it.getColumnIndexOrThrow(COLUMN_TASK_ID)),
                            taskName = it.getString(it.getColumnIndexOrThrow(COLUMN_TASK_NAME)),
                            taskColor = it.getString(it.getColumnIndexOrThrow(COLUMN_TASK_COLOR)),
                            completedAt = it.getLong(it.getColumnIndexOrThrow(COLUMN_COMPLETED_AT)),
                            duration = it.getInt(it.getColumnIndexOrThrow(COLUMN_DURATION))
                        )
                    )
                } while (it.moveToNext())
            }
        }

        return records
    }

    // 获取本年的完成记录
    fun getYearCompletionRecords(): List<CompletionRecord> {
        val records = mutableListOf<CompletionRecord>()
        val db = readableDatabase

        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.DAY_OF_YEAR, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val yearStart = calendar.timeInMillis

        val cursor = db.query(
            TABLE_COMPLETION_RECORDS,
            null,
            "$COLUMN_COMPLETED_AT >= ?",
            arrayOf(yearStart.toString()),
            null,
            null,
            "$COLUMN_COMPLETED_AT DESC"
        )

        cursor.use {
            if (it.moveToFirst()) {
                do {
                    records.add(
                        CompletionRecord(
                            recordId = it.getLong(it.getColumnIndexOrThrow(COLUMN_RECORD_ID)),
                            taskId = it.getLong(it.getColumnIndexOrThrow(COLUMN_TASK_ID)),
                            taskName = it.getString(it.getColumnIndexOrThrow(COLUMN_TASK_NAME)),
                            taskColor = it.getString(it.getColumnIndexOrThrow(COLUMN_TASK_COLOR)),
                            completedAt = it.getLong(it.getColumnIndexOrThrow(COLUMN_COMPLETED_AT)),
                            duration = it.getInt(it.getColumnIndexOrThrow(COLUMN_DURATION))
                        )
                    )
                } while (it.moveToNext())
            }
        }

        return records
    }
}
