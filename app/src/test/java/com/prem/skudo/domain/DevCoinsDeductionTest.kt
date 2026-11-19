package com.prem.skudo.domain

import com.prem.skudo.database.UserProfile
import org.junit.Assert.*
import org.junit.Test

class DevCoinsDeductionTest {

    private fun applyDevCoinsDeduction(profile: UserProfile): UserProfile {
        if (profile.devCoinsDeducted) {
            return profile // Skipped: already adjusted
        }
        val newCoins = if (profile.coins >= 20000L) profile.coins - 20000L else 0L
        return profile.copy(
            coins = newCoins,
            devCoinsDeducted = true,
            updatedAt = System.currentTimeMillis()
        )
    }

    @Test
    fun `Account with exact 20000 dev coins is deducted to 0`() {
        val initial = UserProfile(coins = 20000L, devCoinsDeducted = false)
        val adjusted = applyDevCoinsDeduction(initial)
        assertEquals(0L, adjusted.coins)
        assertTrue(adjusted.devCoinsDeducted)
    }

    @Test
    fun `Active player with 20850 coins preserves all 850 legitimate earned coins`() {
        val initial = UserProfile(coins = 20850L, totalCoinsEarned = 850L, devCoinsDeducted = false)
        val adjusted = applyDevCoinsDeduction(initial)
        assertEquals(850L, adjusted.coins)
        assertEquals(850L, adjusted.totalCoinsEarned)
        assertTrue(adjusted.devCoinsDeducted)
    }

    @Test
    fun `Partial spender with 18600 coins is clamped to 0 without negative balance`() {
        val initial = UserProfile(coins = 18600L, totalCoinsEarned = 100L, devCoinsDeducted = false)
        val adjusted = applyDevCoinsDeduction(initial)
        assertEquals(0L, adjusted.coins)
        assertTrue("Balance must not be negative", adjusted.coins >= 0L)
        assertTrue(adjusted.devCoinsDeducted)
    }

    @Test
    fun `User with 0 coins remains at 0 coins`() {
        val initial = UserProfile(coins = 0L, devCoinsDeducted = false)
        val adjusted = applyDevCoinsDeduction(initial)
        assertEquals(0L, adjusted.coins)
        assertTrue(adjusted.devCoinsDeducted)
    }

    @Test
    fun `Deduction is idempotent and does not deduct multiple times`() {
        val initial = UserProfile(coins = 20850L, totalCoinsEarned = 850L, devCoinsDeducted = false)
        val firstRun = applyDevCoinsDeduction(initial)
        assertEquals(850L, firstRun.coins)
        assertTrue(firstRun.devCoinsDeducted)

        val secondRun = applyDevCoinsDeduction(firstRun)
        assertEquals(850L, secondRun.coins) // Untouched!
    }

    @Test
    fun `New profile defaults to 0 coins`() {
        val freshProfile = UserProfile()
        assertEquals(0L, freshProfile.coins)
    }
}
