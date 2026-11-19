package com.prem.skudo.model

enum class RewardKind {
    COINS,
    HINTS,
    COINS_AND_MYSTERY_GIFT
}

data class DailyRewardConfig(
    val day: Int,
    val kind: RewardKind,
    val coins: Int = 0,
    val hints: Int = 0,
    val hasMysteryGift: Boolean = false,
    val title: String,
    val subtitle: String
)

object DailyRewardSchedule {
    val REWARDS = listOf(
        DailyRewardConfig(
            day = 1,
            kind = RewardKind.COINS,
            coins = 5,
            hints = 0,
            hasMysteryGift = false,
            title = "Day 1",
            subtitle = "5 Coins"
        ),
        DailyRewardConfig(
            day = 2,
            kind = RewardKind.COINS,
            coins = 10,
            hints = 0,
            hasMysteryGift = false,
            title = "Day 2",
            subtitle = "10 Coins"
        ),
        DailyRewardConfig(
            day = 3,
            kind = RewardKind.HINTS,
            coins = 0,
            hints = 2,
            hasMysteryGift = false,
            title = "Day 3",
            subtitle = "2 Hints"
        ),
        DailyRewardConfig(
            day = 4,
            kind = RewardKind.COINS,
            coins = 15,
            hints = 0,
            hasMysteryGift = false,
            title = "Day 4",
            subtitle = "15 Coins"
        ),
        DailyRewardConfig(
            day = 5,
            kind = RewardKind.COINS,
            coins = 20,
            hints = 0,
            hasMysteryGift = false,
            title = "Day 5",
            subtitle = "20 Coins"
        ),
        DailyRewardConfig(
            day = 6,
            kind = RewardKind.HINTS,
            coins = 0,
            hints = 3,
            hasMysteryGift = false,
            title = "Day 6",
            subtitle = "3 Hints"
        ),
        DailyRewardConfig(
            day = 7,
            kind = RewardKind.COINS_AND_MYSTERY_GIFT,
            coins = 25,
            hints = 0,
            hasMysteryGift = true,
            title = "Day 7",
            subtitle = "25 Coins + Mystery Gift"
        )
    )

    fun getConfig(cycleDay: Int): DailyRewardConfig {
        val normalized = ((cycleDay - 1) % 7) + 1
        return REWARDS[normalized - 1]
    }
}
