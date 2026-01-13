package com.limxinhuang.smallthings

data class Task(
    val id: Long = 0,
    val name: String,
    val colorCode: String,
    val durationMinutes: Int,
    val createdAt: Long = System.currentTimeMillis(),
    var completedCount: Int = 0
)
