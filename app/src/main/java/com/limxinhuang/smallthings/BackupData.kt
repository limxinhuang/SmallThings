package com.limxinhuang.smallthings

import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val tasks: List<Task>,
    val completionRecords: List<CompletionRecord>,
    val backupDate: Long = System.currentTimeMillis()
)
