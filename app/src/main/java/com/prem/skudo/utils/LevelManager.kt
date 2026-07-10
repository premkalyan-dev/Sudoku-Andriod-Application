package com.prem.skudo.utils

import com.prem.skudo.model.Difficulty

object LevelManager {
    
    fun getXpForLevel(level: Int): Long {
        if (level <= 1) return 0
        val n = (level - 1).toLong()
        return n * 100 + 50 * n * (n - 1) / 2
    }

    fun getLevelFromXp(totalXp: Long): Int {
        var level = 1
        while (getXpForLevel(level + 1) <= totalXp) {
            level++
        }
        return level
    }

    fun calculateXpReward(
        difficulty: Difficulty,
        timeSeconds: Long,
        mistakes: Int,
        hintsUsed: Int,
        isWin: Boolean,
        isDailyChallenge: Boolean = false,
        winStreak: Int = 0
    ): Long {
        if (!isWin) return 10 // Participation XP
        
        var totalXp = when(difficulty) {
            Difficulty.EASY -> 100L
            Difficulty.MEDIUM -> 200L
            Difficulty.HARD -> 350L
            Difficulty.EXPERT -> 500L
        }
        
        if (isDailyChallenge) totalXp += 150
        
        // Perfect Game Bonus (No Mistakes & No Hints)
        if (mistakes == 0 && hintsUsed == 0) {
            totalXp += 200
        } else {
            // Individual bonuses if not perfect
            if (mistakes == 0) totalXp += 100
            if (hintsUsed == 0) totalXp += 100
        }
        
        // Win Streak Bonus (+25 per consecutive win)
        totalXp += (winStreak * 25)

        return totalXp
    }
    
    fun calculateCoinReward(
        difficulty: Difficulty,
        isWin: Boolean,
        isDailyChallenge: Boolean = false,
        mistakes: Int = 0,
        hintsUsed: Int = 0
    ): Long {
        if (!isWin) return 0
        var coins = when(difficulty) {
            Difficulty.EASY -> 25L
            Difficulty.MEDIUM -> 50L
            Difficulty.HARD -> 75L
            Difficulty.EXPERT -> 100L
        }
        
        if (isDailyChallenge) coins += 75
        if (mistakes == 0 && hintsUsed == 0) coins += 50
        
        return coins
    }

    fun calculateGemReward(
        isWin: Boolean,
        isDailyChallenge: Boolean = false,
        mistakes: Int = 0,
        hintsUsed: Int = 0
    ): Long {
        if (!isWin) return 0
        var gems = 0L
        if (isDailyChallenge) gems += 2
        if (mistakes == 0 && hintsUsed == 0) gems += 1
        return gems
    }

    fun getTitleForLevel(level: Int): String {
        return when {
            level < 5 -> "Beginner"
            level < 10 -> "Novice"
            level < 20 -> "Learner"
            level < 35 -> "Intermediate"
            level < 50 -> "Advanced"
            level < 75 -> "Expert"
            level < 100 -> "Master"
            level < 150 -> "Grandmaster"
            else -> "Sudoku Legend"
        }
    }
    
    fun getRankProgress(level: Int): Float {
        val currentTitle = getTitleForLevel(level)
        val nextLevelForNewTitle = when {
            level < 5 -> 5
            level < 10 -> 10
            level < 20 -> 20
            level < 35 -> 35
            level < 50 -> 50
            level < 75 -> 75
            level < 100 -> 100
            level < 150 -> 150
            else -> return 1f
        }
        
        val startLevel = when {
            level < 5 -> 1
            level < 10 -> 5
            level < 20 -> 10
            level < 35 -> 20
            level < 50 -> 35
            level < 75 -> 50
            level < 100 -> 75
            level < 150 -> 100
            else -> 150
        }
        
        return (level - startLevel).toFloat() / (nextLevelForNewTitle - startLevel)
    }
}
