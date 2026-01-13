package com.limxinhuang.smallthings

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "completion_records")
@Serializable
data class CompletionRecord(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "record_id")
    val recordId: Long = 0,

    @ColumnInfo(name = "task_id")
    val taskId: Long,

    @ColumnInfo(name = "task_name")
    val taskName: String,

    @ColumnInfo(name = "task_color")
    val taskColor: String,

    @ColumnInfo(name = "completed_at")
    val completedAt: Long,

    @ColumnInfo(name = "duration")
    val duration: Int
)
