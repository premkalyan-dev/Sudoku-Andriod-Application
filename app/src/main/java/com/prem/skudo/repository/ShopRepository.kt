package com.prem.skudo.repository

import android.content.Context
import com.prem.skudo.database.AppDatabase

class ShopRepository(context: Context) {
    private val economyRepository = EconomyRepository(context)

    suspend fun buyHintsPackage(hintsCount: Int, cost: Long): Boolean {
        return economyRepository.addHints(hintsCount, cost)
    }

    suspend fun buyExtraLife(): Boolean {
        return economyRepository.spendCoins(200L)
    }

    suspend fun buyAvatar(avatarId: String): Boolean {
        return economyRepository.unlockAvatar(avatarId, 500L)
    }

    suspend fun buyTheme(themeName: String): Boolean {
        return economyRepository.unlockTheme(themeName, 1000L)
    }
}
