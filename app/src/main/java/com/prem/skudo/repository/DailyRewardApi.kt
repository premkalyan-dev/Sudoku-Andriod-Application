package com.prem.skudo.repository

/**
 * Interface representing the Daily Reward API contract as specified in Section 7:
 * - GET  /api/rewards/daily       -> [getDailyReward]
 * - POST /api/rewards/daily/claim -> [claimDailyReward]
 * - POST /api/rewards/daily/gift/open -> [openMysteryGift]
 */
interface DailyRewardApi {
    /**
     * GET /api/rewards/daily
     * Fetches current daily reward state, streak, calendar schedule, balances, and pending gift.
     */
    suspend fun getDailyReward(): DailyRewardInfo

    /**
     * POST /api/rewards/daily/claim
     * Claims today's daily reward based on IST calendar date and streak calculations.
     */
    suspend fun claimDailyReward(): ClaimDailyRewardResult

    /**
     * POST /api/rewards/daily/gift/open
     * Opens the pending Day 7 mystery gift and credits hints up to the max inventory of 3.
     */
    suspend fun openMysteryGift(giftId: String? = null): OpenMysteryGiftResult
}

/**
 * Local implementation of [DailyRewardApi] delegating to [RewardRepository].
 */
class LocalDailyRewardApi(private val rewardRepository: RewardRepository) : DailyRewardApi {
    override suspend fun getDailyReward(): DailyRewardInfo = rewardRepository.getDailyRewardInfo()
    override suspend fun claimDailyReward(): ClaimDailyRewardResult = rewardRepository.claimDailyReward()
    override suspend fun openMysteryGift(giftId: String?): OpenMysteryGiftResult = rewardRepository.openMysteryGift(giftId)
}
