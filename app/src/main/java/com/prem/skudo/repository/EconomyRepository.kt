package com.prem.skudo.repository

import android.content.Context
import com.prem.skudo.database.AppDatabase

class EconomyRepository(context: Context) {
    private val userDao = AppDatabase.getDatabase(context).userDao()

    suspend fun addCoins(amount: Long) {
        val profile = userDao.getUserProfile() ?: return
        val updatedProfile = profile.copy(
            coins = profile.coins + amount,
            totalCoinsEarned = profile.totalCoinsEarned + amount,
            updatedAt = System.currentTimeMillis()
        )
        userDao.updateProfile(updatedProfile)
    }

    suspend fun addGems(amount: Long) {
        val profile = userDao.getUserProfile() ?: return
        val updatedProfile = profile.copy(
            gems = profile.gems + amount,
            totalGemsEarned = profile.totalGemsEarned + amount,
            updatedAt = System.currentTimeMillis()
        )
        userDao.updateProfile(updatedProfile)
    }

    suspend fun spendCoins(amount: Long): Boolean {
        val profile = userDao.getUserProfile() ?: return false
        if (profile.coins < amount) return false
        
        val updatedProfile = profile.copy(
            coins = profile.coins - amount,
            updatedAt = System.currentTimeMillis()
        )
        userDao.updateProfile(updatedProfile)
        return true
    }

    suspend fun spendGems(amount: Long): Boolean {
        val profile = userDao.getUserProfile() ?: return false
        if (profile.gems < amount) return false
        
        val updatedProfile = profile.copy(
            gems = profile.gems - amount,
            updatedAt = System.currentTimeMillis()
        )
        userDao.updateProfile(updatedProfile)
        return true
    }

    suspend fun addHints(count: Int, cost: Long): Boolean {
        val profile = userDao.getUserProfile() ?: return false
        if (profile.coins < cost) return false

        if (spendCoins(cost)) {
            val updatedProfile = userDao.getUserProfile()?.copy(
                hints = (userDao.getUserProfile()?.hints ?: 0) + count,
                updatedAt = System.currentTimeMillis()
            ) ?: return false
            userDao.updateProfile(updatedProfile)
            return true
        }
        return false
    }
    
    suspend fun unlockAvatar(avatarId: String, cost: Long): Boolean {
        val profile = userDao.getUserProfile() ?: return false
        val unlockedList = profile.unlockedAvatars.split(",").toMutableList()
        if (unlockedList.contains(avatarId)) return true
        
        if (spendCoins(cost)) {
            unlockedList.add(avatarId)
            val updatedProfile = userDao.getUserProfile()?.copy(
                unlockedAvatars = unlockedList.joinToString(","),
                updatedAt = System.currentTimeMillis()
            ) ?: return false
            userDao.updateProfile(updatedProfile)
            return true
        }
        return false
    }

    suspend fun unlockTheme(themeName: String, cost: Long): Boolean {
        val profile = userDao.getUserProfile() ?: return false
        val unlockedList = profile.unlockedThemes.split(",").toMutableList()
        if (unlockedList.contains(themeName)) return true
        
        if (spendCoins(cost)) {
            unlockedList.add(themeName)
            val updatedProfile = userDao.getUserProfile()?.copy(
                unlockedThemes = unlockedList.joinToString(","),
                updatedAt = System.currentTimeMillis()
            ) ?: return false
            userDao.updateProfile(updatedProfile)
            return true
        }
        return false
    }
}
