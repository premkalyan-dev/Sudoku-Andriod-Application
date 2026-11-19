package com.prem.skudo.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyRewardDao {
    @Query("SELECT * FROM daily_claim_history WHERE userId = :userId AND claimDate = :claimDate LIMIT 1")
    suspend fun getClaimForDate(userId: String, claimDate: String): DailyClaimHistory?

    @Query("SELECT * FROM daily_claim_history WHERE userId = :userId ORDER BY claimTimestamp DESC LIMIT 1")
    suspend fun getLastClaim(userId: String): DailyClaimHistory?

    @Query("SELECT * FROM daily_claim_history WHERE userId = :userId ORDER BY claimTimestamp DESC")
    fun getAllClaims(userId: String): Flow<List<DailyClaimHistory>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertClaim(claim: DailyClaimHistory): Long

    @Query("SELECT * FROM mystery_gifts WHERE userId = :userId AND status = 'PENDING' ORDER BY createdAt DESC LIMIT 1")
    suspend fun getPendingMysteryGift(userId: String): MysteryGift?

    @Query("SELECT * FROM mystery_gifts WHERE userId = :userId AND claimDate = :claimDate LIMIT 1")
    suspend fun getMysteryGiftForDate(userId: String, claimDate: String): MysteryGift?

    @Query("SELECT * FROM mystery_gifts WHERE id = :giftId AND userId = :userId LIMIT 1")
    suspend fun getMysteryGiftById(giftId: String, userId: String): MysteryGift?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMysteryGift(gift: MysteryGift)

    @Update
    suspend fun updateMysteryGift(gift: MysteryGift)
}
