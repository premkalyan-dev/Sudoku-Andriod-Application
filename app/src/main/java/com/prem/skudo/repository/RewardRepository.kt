package com.prem.skudo.repository

import android.content.Context
import com.prem.skudo.database.AppDatabase
import com.prem.skudo.database.DailyClaimHistory
import com.prem.skudo.database.MysteryGift
import com.prem.skudo.model.DailyRewardConfig
import com.prem.skudo.model.DailyRewardSchedule
import com.prem.skudo.model.RewardKind
import com.prem.skudo.model.Difficulty
import com.prem.skudo.utils.DailyRewardTimeUtils
import com.prem.skudo.utils.LevelManager
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class RewardRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val userDao = db.userDao()
    private val statsDao = db.statsDao()
    private val dailyRewardDao = db.dailyRewardDao()
    private val economyRepository = EconomyRepository(context)
    private val xpRepository = XPRepository(context)
    private val claimMutex = Mutex()

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

    /**
     * Fetches current daily reward state, 7-day schedule, today's reward,
     * and pending mystery gift (Maps to GET /api/rewards/daily).
     */
    suspend fun getDailyRewardInfo(): DailyRewardInfo {
        val profile = userDao.getUserProfile() ?: com.prem.skudo.database.UserProfile()
        val todayDate = DailyRewardTimeUtils.getTodayIstDate()
        val todayClaim = dailyRewardDao.getClaimForDate(profile.userId, todayDate)
        val isClaimed = todayClaim != null || profile.lastDailyClaimDate == todayDate

        val currentStreak: Int
        val cycleDay: Int
        if (isClaimed) {
            currentStreak = profile.dailyRewardStreak
            cycleDay = profile.dailyRewardCycleDay
        } else {
            val isConsecutive = DailyRewardTimeUtils.isConsecutiveDay(profile.lastDailyClaimDate, todayDate)
            currentStreak = DailyRewardTimeUtils.calculateNextOverallStreak(profile.dailyRewardStreak, isConsecutive)
            cycleDay = DailyRewardTimeUtils.calculateNextCycleDay(profile.dailyRewardCycleDay, isConsecutive)
        }

        val todayConfig = DailyRewardSchedule.getConfig(cycleDay)
        val schedule = (1..7).map { day ->
            val config = DailyRewardSchedule.getConfig(day)
            val status = when {
                isClaimed -> if (day <= cycleDay) ScheduleItemStatus.CLAIMED else ScheduleItemStatus.LOCKED
                day < cycleDay -> ScheduleItemStatus.CLAIMED
                day == cycleDay -> ScheduleItemStatus.AVAILABLE
                else -> ScheduleItemStatus.LOCKED
            }
            DailyRewardScheduleItem(
                day = day,
                kind = config.kind,
                coins = config.coins,
                hints = config.hints,
                hasMysteryGift = config.hasMysteryGift,
                title = config.title,
                subtitle = config.subtitle,
                status = status
            )
        }

        val pendingGift = dailyRewardDao.getPendingMysteryGift(profile.userId)
        val pendingGiftInfo = pendingGift?.let {
            PendingGiftInfo(
                giftId = it.id,
                claimDate = it.claimDate,
                isRevealed = it.status == MysteryGift.STATUS_REVEALED,
                selectedHints = it.selectedHints,
                creditedHints = it.creditedHints
            )
        }

        return DailyRewardInfo(
            currentStreak = currentStreak,
            longestStreak = profile.longestDailyStreak,
            currentCycleDay = cycleDay,
            isClaimedToday = isClaimed,
            todayReward = todayConfig,
            schedule = schedule,
            walletBalance = profile.coins,
            hintInventory = profile.hints,
            pendingMysteryGift = pendingGiftInfo,
            nextResetMillis = DailyRewardTimeUtils.getMillisUntilNextMidnightIst()
        )
    }

    /**
     * Claims today's daily reward in a thread-safe atomic transaction
     * (Maps to POST /api/rewards/daily/claim).
     */
    suspend fun claimDailyReward(): ClaimDailyRewardResult = claimMutex.withLock {
        val profile = userDao.getUserProfile()
            ?: return@withLock ClaimDailyRewardResult(false, "User profile not found")

        val todayDate = DailyRewardTimeUtils.getTodayIstDate()
        val existingClaim = dailyRewardDao.getClaimForDate(profile.userId, todayDate)
        if (existingClaim != null || profile.lastDailyClaimDate == todayDate) {
            return@withLock ClaimDailyRewardResult(
                success = false,
                message = "Reward already claimed today! Come back tomorrow after 12:00 AM IST.",
                cycleDay = profile.dailyRewardCycleDay,
                overallStreak = profile.dailyRewardStreak,
                updatedWalletBalance = profile.coins,
                updatedHintInventory = profile.hints,
                dailyRewardInfo = getDailyRewardInfo()
            )
        }

        val isConsecutive = DailyRewardTimeUtils.isConsecutiveDay(profile.lastDailyClaimDate, todayDate)
        val newStreak = DailyRewardTimeUtils.calculateNextOverallStreak(profile.dailyRewardStreak, isConsecutive)
        val newCycleDay = DailyRewardTimeUtils.calculateNextCycleDay(profile.dailyRewardCycleDay, isConsecutive)
        val newLongestStreak = maxOf(profile.longestDailyStreak, newStreak)

        val config = DailyRewardSchedule.getConfig(newCycleDay)
        val coinsAwarded = config.coins
        val hintsAwarded = config.hints
        val hintsCredited = hintsAwarded

        var createdGiftId: String? = null
        if (config.hasMysteryGift) {
            // Day 7 Mystery Gift Box randomly awards 1, 2, or 3 hints
            val selectedHints = Random.nextInt(1, 4)
            val gift = MysteryGift(
                userId = profile.userId,
                claimDate = todayDate,
                selectedHints = selectedHints,
                creditedHints = 0,
                status = MysteryGift.STATUS_PENDING,
                createdAt = System.currentTimeMillis()
            )
            dailyRewardDao.insertMysteryGift(gift)
            createdGiftId = gift.id
        }

        val claimRecord = DailyClaimHistory(
            userId = profile.userId,
            claimDate = todayDate,
            overallStreak = newStreak,
            rewardCycleDay = newCycleDay,
            rewardType = config.kind.name,
            rewardAmount = if (coinsAwarded > 0) coinsAwarded else hintsAwarded,
            claimTimestamp = System.currentTimeMillis()
        )
        dailyRewardDao.insertClaim(claimRecord)

        val updatedCoins = profile.coins + coinsAwarded
        val updatedHints = (profile.hints + hintsCredited).coerceAtLeast(0)

        val updatedProfile = profile.copy(
            coins = updatedCoins,
            totalCoinsEarned = profile.totalCoinsEarned + coinsAwarded,
            hints = updatedHints,
            dailyRewardStreak = newStreak,
            longestDailyStreak = newLongestStreak,
            dailyRewardCycleDay = newCycleDay,
            lastDailyClaimDate = todayDate,
            lastDailyRewardTimestamp = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        userDao.updateProfile(updatedProfile)

        val msg = when {
            config.hasMysteryGift -> "Claimed 25 Coins and unlocked a Mystery Gift Box!"
            coinsAwarded > 0 -> "Claimed $coinsAwarded Coins!"
            else -> "Claimed $hintsAwarded Hints!"
        }

        return@withLock ClaimDailyRewardResult(
            success = true,
            message = msg,
            cycleDay = newCycleDay,
            overallStreak = newStreak,
            coinsAwarded = coinsAwarded,
            hintsAwarded = hintsAwarded,
            hintsCredited = hintsCredited,
            hasMysteryGift = config.hasMysteryGift,
            pendingGiftId = createdGiftId,
            updatedWalletBalance = updatedCoins,
            updatedHintInventory = updatedHints,
            dailyRewardInfo = getDailyRewardInfo()
        )
    }

    /**
     * Opens the pending Day 7 mystery gift and credits hints up to the max capacity of 3
     * (Maps to POST /api/rewards/daily/gift/open).
     */
    suspend fun openMysteryGift(giftId: String? = null): OpenMysteryGiftResult = claimMutex.withLock {
        val profile = userDao.getUserProfile()
            ?: return@withLock OpenMysteryGiftResult(false, "User profile not found")

        val gift = if (giftId != null) {
            dailyRewardDao.getMysteryGiftById(giftId, profile.userId)
        } else {
            dailyRewardDao.getPendingMysteryGift(profile.userId)
        } ?: return@withLock OpenMysteryGiftResult(false, "No pending mystery gift found")

        if (gift.status == MysteryGift.STATUS_REVEALED) {
            return@withLock OpenMysteryGiftResult(
                success = true,
                message = "Gift already revealed",
                giftId = gift.id,
                selectedHints = gift.selectedHints,
                creditedHints = gift.creditedHints,
                totalHintInventory = profile.hints,
                alreadyRevealed = true
            )
        }

        // Day 7 mystery gift awards 1, 2, or 3 hints without restricting total inventory
        val selectedHints = gift.selectedHints.coerceIn(1, 3)
        val credited = selectedHints
        val newHints = (profile.hints + credited).coerceAtLeast(0)

        userDao.updateProfile(
            profile.copy(
                hints = newHints,
                updatedAt = System.currentTimeMillis()
            )
        )

        dailyRewardDao.updateMysteryGift(
            gift.copy(
                status = MysteryGift.STATUS_REVEALED,
                creditedHints = credited,
                revealedAt = System.currentTimeMillis()
            )
        )

        val msg = "Revealed $credited Hints! Added to your inventory."

        return@withLock OpenMysteryGiftResult(
            success = true,
            message = msg,
            giftId = gift.id,
            selectedHints = selectedHints,
            creditedHints = credited,
            totalHintInventory = newHints,
            alreadyRevealed = false
        )
    }

    fun isClaimedToday(lastTimestamp: Long, lastClaimDate: String? = null): Boolean {
        val today = DailyRewardTimeUtils.getTodayIstDate()
        if (lastClaimDate != null && lastClaimDate == today) return true
        if (lastTimestamp <= 0L) return false
        val timestampDate = DailyRewardTimeUtils.getTodayIstDate(lastTimestamp)
        return timestampDate == today
    }

    fun getRewardForDay(day: Int): Long {
        val config = DailyRewardSchedule.getConfig(day)
        return config.coins.toLong()
    }
}

data class DailyRewardInfo(
    val currentStreak: Int = 1,
    val longestStreak: Int = 0,
    val currentCycleDay: Int = 1,
    val isClaimedToday: Boolean = false,
    val todayReward: DailyRewardConfig = DailyRewardSchedule.getConfig(1),
    val schedule: List<DailyRewardScheduleItem> = emptyList(),
    val walletBalance: Long = 0L,
    val hintInventory: Int = 0,
    val pendingMysteryGift: PendingGiftInfo? = null,
    val nextResetMillis: Long = 0L
)

data class DailyRewardScheduleItem(
    val day: Int,
    val kind: RewardKind,
    val coins: Int,
    val hints: Int,
    val hasMysteryGift: Boolean,
    val title: String,
    val subtitle: String,
    val status: ScheduleItemStatus
)

enum class ScheduleItemStatus {
    CLAIMED,
    AVAILABLE,
    LOCKED
}

data class PendingGiftInfo(
    val giftId: String,
    val claimDate: String,
    val isRevealed: Boolean,
    val selectedHints: Int? = null,
    val creditedHints: Int? = null
)

data class ClaimDailyRewardResult(
    val success: Boolean,
    val message: String,
    val cycleDay: Int = 0,
    val overallStreak: Int = 0,
    val coinsAwarded: Int = 0,
    val hintsAwarded: Int = 0,
    val hintsCredited: Int = 0,
    val hasMysteryGift: Boolean = false,
    val pendingGiftId: String? = null,
    val updatedWalletBalance: Long = 0,
    val updatedHintInventory: Int = 0,
    val dailyRewardInfo: DailyRewardInfo? = null
)

data class OpenMysteryGiftResult(
    val success: Boolean,
    val message: String,
    val giftId: String = "",
    val selectedHints: Int = 0,
    val creditedHints: Int = 0,
    val totalHintInventory: Int = 0,
    val alreadyRevealed: Boolean = false
)

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
