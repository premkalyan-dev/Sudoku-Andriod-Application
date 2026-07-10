package com.prem.skudo.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_game")
data class SavedGame(
    @PrimaryKey val difficulty: String, // "EASY", "MEDIUM", "HARD", "EXPERT", or "DAILY"
    val puzzleJson: String,
    val solutionJson: String,
    val timerSeconds: Long,
    val mistakes: Int,
    val maxMistakes: Int,
    val hintsRemaining: Int,
    val isDailyChallenge: Boolean = false,
    val lastPlayedTimestamp: Long = System.currentTimeMillis(),
    val selectedCellRow: Int? = null,
    val selectedCellCol: Int? = null,
)
