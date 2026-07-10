package com.prem.skudo.utils

import com.prem.skudo.database.Achievement
import com.prem.skudo.model.Difficulty

object AchievementManager {
    val DEFAULT_ACHIEVEMENTS = listOf(
        Achievement("first_win", "First Victory", "Win your first Sudoku game", "ic_trophy", target = 1, xpReward = 100, coinReward = 10),
        Achievement("win_5", "Sudoku Starter", "Win 5 Sudoku games", "ic_star", target = 5, xpReward = 200, coinReward = 25),
        Achievement("win_10", "Dedicated Player", "Win 10 Sudoku games", "ic_star", target = 10, xpReward = 400, coinReward = 50),
        Achievement("win_50", "Sudoku Expert", "Win 50 Sudoku games", "ic_medal", target = 50, xpReward = 1000, coinReward = 200),
        Achievement("win_100", "Sudoku Master", "Win 100 Sudoku games", "ic_crown", target = 100, xpReward = 2500, coinReward = 500),
        
        Achievement("easy_master", "Easy Peasy", "Win an Easy game", "ic_check", target = 1, xpReward = 50, coinReward = 5),
        Achievement("medium_master", "Balanced Mind", "Win a Medium game", "ic_check", target = 1, xpReward = 100, coinReward = 10),
        Achievement("hard_master", "Hard Worker", "Win a Hard game", "ic_check", target = 1, xpReward = 200, coinReward = 20),
        Achievement("expert_master", "Absolute Expert", "Win an Expert game", "ic_check", target = 1, xpReward = 400, coinReward = 40),
        
        Achievement("no_hints", "No Help Needed", "Win a game without using hints", "ic_hint_off", target = 1, xpReward = 150, coinReward = 15),
        Achievement("no_mistakes", "Perfectionist", "Win a game without any mistakes", "ic_perfect", target = 1, xpReward = 150, coinReward = 15),
        Achievement("speed_solver", "Speed Demon", "Win a game in under 3 minutes", "ic_speed", target = 1, xpReward = 200, coinReward = 20)
    )

    fun checkAchievements(
        currentAchievements: List<Achievement>,
        difficulty: Difficulty,
        timeSeconds: Long,
        mistakes: Int,
        hintsUsed: Int,
        totalWins: Int
    ): List<Achievement> {
        val updated = mutableListOf<Achievement>()
        
        currentAchievements.forEach { achievement ->
            if (achievement.isUnlocked) return@forEach
            
            var newProgress = achievement.progress
            var shouldUnlock = false
            
            when (achievement.id) {
                "first_win" -> {
                    newProgress = Math.min(1, totalWins)
                    shouldUnlock = newProgress >= 1
                }
                "win_5" -> {
                    newProgress = Math.min(5, totalWins)
                    shouldUnlock = newProgress >= 5
                }
                "win_10" -> {
                    newProgress = Math.min(10, totalWins)
                    shouldUnlock = newProgress >= 10
                }
                "win_50" -> {
                    newProgress = Math.min(50, totalWins)
                    shouldUnlock = newProgress >= 50
                }
                "win_100" -> {
                    newProgress = Math.min(100, totalWins)
                    shouldUnlock = newProgress >= 100
                }
                "easy_master" -> if (difficulty == Difficulty.EASY) { newProgress = 1; shouldUnlock = true }
                "medium_master" -> if (difficulty == Difficulty.MEDIUM) { newProgress = 1; shouldUnlock = true }
                "hard_master" -> if (difficulty == Difficulty.HARD) { newProgress = 1; shouldUnlock = true }
                "expert_master" -> if (difficulty == Difficulty.EXPERT) { newProgress = 1; shouldUnlock = true }
                "no_hints" -> if (hintsUsed == 0) { newProgress = 1; shouldUnlock = true }
                "no_mistakes" -> if (mistakes == 0) { newProgress = 1; shouldUnlock = true }
                "speed_solver" -> if (timeSeconds < 180) { newProgress = 1; shouldUnlock = true }
            }
            
            if (shouldUnlock || newProgress > achievement.progress) {
                updated.add(achievement.copy(
                    progress = newProgress,
                    isUnlocked = shouldUnlock,
                    unlockDate = if (shouldUnlock) System.currentTimeMillis() else 0
                ))
            }
        }
        
        return updated
    }
}
