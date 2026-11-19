package com.prem.skudo.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_claim_history",
    indices = [
        Index(value = ["userId", "claimDate"], unique = true)
    ]
)
data class DailyClaimHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val claimDate: String, // "yyyy-MM-dd" in Asia/Kolkata (IST)
    val overallStreak: Int,
    val rewardCycleDay: Int, // 1..7
    val rewardType: String, // "COINS", "HINTS", "COINS_AND_MYSTERY_GIFT"
    val rewardAmount: Int, // coins or hints
    val claimTimestamp: Long = System.currentTimeMillis()
)
