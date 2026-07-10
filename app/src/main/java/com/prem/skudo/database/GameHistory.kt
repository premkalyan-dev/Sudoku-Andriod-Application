package com.prem.skudo.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.prem.skudo.model.Difficulty

@Entity(tableName = "game_history")
data class GameHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val difficulty: Difficulty,
    val timeSeconds: Long,
    val mistakes: Int,
    val hintsUsed: Int,
    val isWin: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isDaily: Boolean = false,
    val score: Int = 0,
)
