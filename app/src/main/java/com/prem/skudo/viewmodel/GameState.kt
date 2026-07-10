package com.prem.skudo.viewmodel

import com.prem.skudo.model.Difficulty
import com.prem.skudo.model.SudokuBoard

data class GameState(
    val puzzle: SudokuBoard = SudokuBoard(),
    val solution: SudokuBoard = SudokuBoard(),
    val difficulty: Difficulty = Difficulty.EASY,
    val selectedCell: Pair<Int, Int>? = null,
    val isGameOver: Boolean = false,
    val isLoading: Boolean = false,
    
    // Premium features
    val mistakes: Int = 0,
    val maxMistakes: Int = 3,
    val hintsRemaining: Int = 3,
    val isNotesMode: Boolean = false,
    val timerSeconds: Long = 0,
    val isPaused: Boolean = false,
    val isVictory: Boolean = false,
    val isDailyChallenge: Boolean = false,
    val xpEarned: Long = 0,
    val coinsEarned: Long = 0,
    val gemsEarned: Long = 0,
    val newLevel: Int = 1,
    val leveledUp: Boolean = false,
    val isPerfect: Boolean = false,
    val unlockedAchievements: List<com.prem.skudo.database.Achievement> = emptyList(),
    val showContinueDialog: Boolean = false,
    val showLeaveDialog: Boolean = false,
    val showDiscardConfirmation: Boolean = false,
    val showStartNewConfirmation: Boolean = false,
    val showRestartConfirmation: Boolean = false,
    val userGems: Long = 0,
    
    // Resume Metadata
    val lastPlayedTimestamp: Long = 0,
    
    // Completion animations
    val lastCompletedRows: Set<Int> = emptySet(),
    val lastCompletedCols: Set<Int> = emptySet(),
    val lastCompletedBoxes: Set<Int> = emptySet(),
    
    // Appearance
    val boardStyle: String = "Modern",
    
    // Settings
    val autoCheck: Boolean = true,
    val highlightIdentical: Boolean = true,
    val highlightRelated: Boolean = true,
    val autoRemoveNotes: Boolean = true
)
