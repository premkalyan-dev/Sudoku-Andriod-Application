package com.prem.skudo.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val iconId: String,
    val isUnlocked: Boolean = false,
    val progress: Int = 0,
    val target: Int = 1,
    val unlockDate: Long = 0,
    val xpReward: Long = 0,
    val coinReward: Long = 0,
    val gemReward: Long = 0,
)
