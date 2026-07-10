package com.prem.skudo.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_challenges")
data class DailyChallenge(
    @PrimaryKey val date: Int, // yyyyMMdd
    val difficulty: String,
    val isCompleted: Boolean = false,
    val completionTimeSeconds: Long = 0,
    val completionTimestamp: Long = 0,
    val boardData: String? = null,
)
