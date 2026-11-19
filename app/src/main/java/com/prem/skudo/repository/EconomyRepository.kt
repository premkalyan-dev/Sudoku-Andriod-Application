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

    companion object {
        const val MAX_HINT_CAPACITY = 3
    }

    /**
     * Credits hints to the user's inventory strictly respecting the MAX_HINT_CAPACITY (3).
     * Returns the actual number of hints credited (which may be 0 if inventory is already full).
     * Never allows inventory to exceed 3 or become negative.
     */
    suspend fun creditHints(count: Int): Int {
        if (count <= 0) return 0
        val profile = userDao.getUserProfile() ?: return 0
        val currentHints = profile.hints.coerceAtLeast(0)
        val availableCapacity = (MAX_HINT_CAPACITY - currentHints).coerceAtLeast(0)
        val hintsToCredit = count.coerceAtMost(availableCapacity)

        if (hintsToCredit > 0) {
            val updatedProfile = profile.copy(
                hints = currentHints + hintsToCredit,
                updatedAt = System.currentTimeMillis()
            )
            userDao.updateProfile(updatedProfile)
        }
        return hintsToCredit
    }

    suspend fun addHints(count: Int, cost: Long): Boolean {
        val profile = userDao.getUserProfile() ?: return false
        val currentHints = profile.hints.coerceAtLeast(0)
        if (currentHints >= MAX_HINT_CAPACITY) return false
        if (profile.coins < cost) return false

        if (spendCoins(cost)) {
            val available = (MAX_HINT_CAPACITY - currentHints).coerceAtLeast(0)
            val toAdd = count.coerceAtMost(available)
            val updatedProfile = userDao.getUserProfile()?.copy(
                hints = currentHints + toAdd,
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
