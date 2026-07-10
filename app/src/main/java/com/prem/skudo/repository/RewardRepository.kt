package com.prem.skudo.repository

import android.content.Context
import com.prem.skudo.database.AppDatabase
import com.prem.skudo.model.Difficulty
import com.prem.skudo.utils.LevelManager
import java.util.Date
import java.util.Locale

class RewardRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val userDao = db.userDao()
    private val statsDao = db.statsDao()
    private val economyRepository = EconomyRepository(context)
    private val xpRepository = XPRepository(context)

    suspend fun processGameResult(
        difficulty: Difficulty,
        timeSeconds: Long,
        mistakes: Int,
        hintsUsed: Int,
        isWin: Boolean,
        isDailyChallenge: Boolean = false
    ): GameRewards {
        val profile = userDao.getUserProfile() ?: return GameRewards()
        
        val xpAmount = LevelManager.calculateXpReward(difficulty, timeSeconds, mistakes, hintsUsed, isWin, isDailyChallenge, profile.winStreak)
        val coinAmount = LevelManager.calculateCoinReward(difficulty, isWin, isDailyChallenge, mistakes, hintsUsed)
        val gemAmount = LevelManager.calculateGemReward(isWin, isDailyChallenge, mistakes, hintsUsed)
        
        val (newLevel, leveledUp) = if (isWin) xpRepository.addXP(xpAmount) else profile.level to false
        if (isWin) {
            economyRepository.addCoins(coinAmount)
            economyRepository.addGems(gemAmount)
            updateWinStats(timeSeconds, mistakes, hintsUsed, isDailyChallenge)
        } else {
            resetWinStreak()
        }
        
        return GameRewards(
            xp = xpAmount,
            coins = coinAmount,
            gems = gemAmount,
            newLevel = newLevel,
            leveledUp = leveledUp
        )
    }

    private suspend fun updateWinStats(
        timeSeconds: Long,
        mistakes: Int,
        hintsUsed: Int,
        isDailyChallenge: Boolean
    ) {
        val profile = userDao.getUserProfile() ?: return
        val newStreak = profile.winStreak + 1
        
        var updatedProfile = profile.copy(
            winStreak = newStreak,
            totalPlayTime = profile.totalPlayTime + timeSeconds,
            updatedAt = System.currentTimeMillis()
        )
        
        // Expansion stats
        if (mistakes == 0 && hintsUsed == 0) {
            updatedProfile = updatedProfile.copy(perfectGames = profile.perfectGames + 1)
        }
        if (hintsUsed == 0) {
            updatedProfile = updatedProfile.copy(noHintWins = profile.noHintWins + 1)
        }
        if (mistakes == 0) {
            updatedProfile = updatedProfile.copy(noMistakeWins = profile.noMistakeWins + 1)
        }
        
        if (isDailyChallenge) {
            if (timeSeconds < profile.bestDailyChallengeTime) {
                updatedProfile = updatedProfile.copy(bestDailyChallengeTime = timeSeconds)
            }
        }
        
        // Total wins for average time
        val easy = statsDao.getStats("EASY")?.gamesWon ?: 0
        val medium = statsDao.getStats("MEDIUM")?.gamesWon ?: 0
        val hard = statsDao.getStats("HARD")?.gamesWon ?: 0
        val expert = statsDao.getStats("EXPERT")?.gamesWon ?: 0
        val allWinsBeforeThis = (easy + medium + hard + expert).toLong()
        val allWins = allWinsBeforeThis + 1
        
        val newAverage = if (allWins > 0) {
            (profile.averageCompletionTime * allWinsBeforeThis + timeSeconds) / allWins
        } else timeSeconds
        
        updatedProfile = updatedProfile.copy(averageCompletionTime = newAverage)
        
        userDao.updateProfile(updatedProfile)
    }

    private suspend fun resetWinStreak() {
        val profile = userDao.getUserProfile() ?: return
        userDao.updateProfile(profile.copy(winStreak = 0))
    }
    
    suspend fun claimDailyReward(): DailyRewardResult {
        val profile = userDao.getUserProfile() ?: return DailyRewardResult(false)
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L
        
        if (now - profile.lastDailyRewardTimestamp < oneDayMillis && isSameDay(now, profile.lastDailyRewardTimestamp)) {
            return DailyRewardResult(false)
        }
        
        val newStreak = if (now - profile.lastDailyRewardTimestamp < oneDayMillis * 2) {
            (profile.dailyRewardStreak % 7) + 1
        } else {
            1
        }
        
        val rewardCoins = when(newStreak) {
            1 -> 25L
            2 -> 40L
            3 -> 60L
            4 -> 75L
            5 -> 100L
            6 -> 150L
            7 -> 250L
            else -> 25L
        }
        
        val currentProfile = userDao.getUserProfile() ?: return DailyRewardResult(false)
        val updatedProfile = currentProfile.copy(
            coins = currentProfile.coins + rewardCoins,
            totalCoinsEarned = currentProfile.totalCoinsEarned + rewardCoins,
            lastDailyRewardTimestamp = now,
            dailyRewardStreak = newStreak,
            updatedAt = now
        )
        userDao.updateProfile(updatedProfile)
        
        return DailyRewardResult(true, rewardCoins, newStreak)
    }
    
    private fun isSameDay(t1: Long, t2: Long): Boolean {
        if (t1 == 0L || t2 == 0L) return false
        val fmt = java.text.SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return fmt.format(Date(t1)) == fmt.format(Date(t2))
    }
}

data class GameRewards(
    val xp: Long = 0,
    val coins: Long = 0,
    val gems: Long = 0,
    val newLevel: Int = 1,
    val leveledUp: Boolean = false
)

data class DailyRewardResult(
    val success: Boolean,
    val coins: Long = 0,
    val streak: Int = 0
)
