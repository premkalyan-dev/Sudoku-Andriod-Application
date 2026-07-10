package com.prem.skudo.repository

import android.content.Context
import com.prem.skudo.database.AppDatabase
import com.prem.skudo.database.UserProfile
import com.prem.skudo.utils.LevelManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class XPRepository(context: Context) {
    private val userDao = AppDatabase.getDatabase(context).userDao()
    
    suspend fun addXP(amount: Long): Pair<Int, Boolean> {
        val profile = userDao.getUserProfile() ?: return 1 to false
        val oldLevel = profile.level
        val newXP = profile.xp + amount
        val newLevel = LevelManager.getLevelFromXp(newXP)
        val leveledUp = newLevel > oldLevel
        
        val updatedProfile = profile.copy(
            xp = newXP,
            level = newLevel,
            totalXpEarned = profile.totalXpEarned + amount,
            highestLevel = if (newLevel > profile.highestLevel) newLevel else profile.highestLevel,
            currentRank = LevelManager.getTitleForLevel(newLevel),
            highestRank = LevelManager.getTitleForLevel(if (newLevel > profile.highestLevel) newLevel else profile.highestLevel),
            updatedAt = System.currentTimeMillis()
        )
        
        userDao.updateProfile(updatedProfile)
        return newLevel to leveledUp
    }

    suspend fun getXpToNextLevel(currentXp: Long, currentLevel: Int): Long {
        val nextLevelXp = LevelManager.getXpForLevel(currentLevel + 1)
        return nextLevelXp - currentXp
    }

    suspend fun getLevelProgress(currentXp: Long, currentLevel: Int): Float {
        val currentLevelXp = LevelManager.getXpForLevel(currentLevel)
        val nextLevelXp = LevelManager.getXpForLevel(currentLevel + 1)
        val range = nextLevelXp - currentLevelXp
        if (range <= 0) return 1f
        return (currentXp - currentLevelXp).toFloat() / range
    }
}
