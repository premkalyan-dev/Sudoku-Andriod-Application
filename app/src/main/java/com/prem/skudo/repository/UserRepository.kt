package com.prem.skudo.repository

import android.content.Context
import com.prem.skudo.database.AppDatabase
import com.prem.skudo.database.UserProfile
import com.prem.skudo.model.Difficulty
import com.prem.skudo.utils.LevelManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserRepository(context: Context) {
    private val userDao = AppDatabase.getDatabase(context).userDao()
    
    val userProfile: Flow<UserProfile?> = userDao.observeUserProfile()

    suspend fun getOrCreateProfile(): UserProfile {
        var profile = userDao.getUserProfile()
        if (profile == null) {
            profile = UserProfile()
            userDao.insertProfile(profile)
        }
        return profile
    }

    suspend fun updateDisplayName(name: String) {
        val current = getOrCreateProfile()
        val updated = current.copy(displayName = name, updatedAt = System.currentTimeMillis())
        userDao.updateProfile(updated)
    }

    suspend fun updateAvatar(avatarId: String) {
        val current = getOrCreateProfile()
        val updated = current.copy(avatarId = avatarId, updatedAt = System.currentTimeMillis())
        userDao.updateProfile(updated)
    }

    suspend fun updateProfileInfo(email: String?, photoUrl: String?, cloudUserId: String?, displayName: String) {
        val current = getOrCreateProfile()
        val updated = current.copy(
            email = email,
            photoUrl = photoUrl,
            cloudUserId = cloudUserId,
            displayName = displayName,
            updatedAt = System.currentTimeMillis()
        )
        userDao.updateProfile(updated)
    }

    suspend fun addGameResults(difficulty: Difficulty, timeSeconds: Long, mistakes: Int, hintsUsed: Int, isWin: Boolean) {
        val current = getOrCreateProfile()
        val xpReward = LevelManager.calculateXpReward(difficulty, timeSeconds, mistakes, hintsUsed, isWin)
        val coinReward = LevelManager.calculateCoinReward(difficulty, isWin)
        
        val newXp = current.xp + xpReward
        val newLevel = LevelManager.getLevelFromXp(newXp)
        val newCoins = current.coins + coinReward
        val newPlayTime = current.totalPlayTime + timeSeconds
        
        val updated = current.copy(
            xp = newXp,
            level = newLevel,
            coins = newCoins,
            totalPlayTime = newPlayTime,
            currentRank = LevelManager.getTitleForLevel(newLevel),
            lastActiveAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        userDao.updateProfile(updated)
    }
}
