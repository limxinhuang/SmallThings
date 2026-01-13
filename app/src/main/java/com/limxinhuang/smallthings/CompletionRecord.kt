package com.limxinhuang.smallthings

data class CompletionRecord(
    val recordId: Long = 0,
    val taskId: Long,
    val taskName: String,
    val taskColor: String,
    val completedAt: Long,
    val duration: Int
)
