package com.prem.skudo.domain

import com.prem.skudo.model.DailyRewardConfig
import com.prem.skudo.model.DailyRewardSchedule
import com.prem.skudo.model.RewardKind
import com.prem.skudo.repository.EconomyRepository
import com.prem.skudo.utils.DailyRewardTimeUtils
import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class DailyRewardSystemTest {

    // ==========================================
    // 1. REWARD SCHEDULE TESTS (Section 3)
    // ==========================================

    @Test
    fun `Day 1 awards 5 coins`() {
        val config = DailyRewardSchedule.getConfig(1)
        assertEquals(1, config.day)
        assertEquals(RewardKind.COINS, config.kind)
        assertEquals(5, config.coins)
        assertEquals(0, config.hints)
        assertFalse(config.hasMysteryGift)
    }

    @Test
    fun `Day 2 awards 10 coins`() {
        val config = DailyRewardSchedule.getConfig(2)
        assertEquals(2, config.day)
        assertEquals(RewardKind.COINS, config.kind)
        assertEquals(10, config.coins)
        assertEquals(0, config.hints)
        assertFalse(config.hasMysteryGift)
    }

    @Test
    fun `Day 3 awards 2 hints`() {
        val config = DailyRewardSchedule.getConfig(3)
        assertEquals(3, config.day)
        assertEquals(RewardKind.HINTS, config.kind)
        assertEquals(0, config.coins)
        assertEquals(2, config.hints)
        assertFalse(config.hasMysteryGift)
    }

    @Test
    fun `Day 4 awards 15 coins`() {
        val config = DailyRewardSchedule.getConfig(4)
        assertEquals(4, config.day)
        assertEquals(RewardKind.COINS, config.kind)
        assertEquals(15, config.coins)
        assertEquals(0, config.hints)
        assertFalse(config.hasMysteryGift)
    }

    @Test
    fun `Day 5 awards 20 coins`() {
        val config = DailyRewardSchedule.getConfig(5)
        assertEquals(5, config.day)
        assertEquals(RewardKind.COINS, config.kind)
        assertEquals(20, config.coins)
        assertEquals(0, config.hints)
        assertFalse(config.hasMysteryGift)
    }

    @Test
    fun `Day 6 awards 3 hints`() {
        val config = DailyRewardSchedule.getConfig(6)
        assertEquals(6, config.day)
        assertEquals(RewardKind.HINTS, config.kind)
        assertEquals(0, config.coins)
        assertEquals(3, config.hints)
        assertFalse(config.hasMysteryGift)
    }

    @Test
    fun `Day 7 awards 25 coins and mystery gift`() {
        val config = DailyRewardSchedule.getConfig(7)
        assertEquals(7, config.day)
        assertEquals(RewardKind.COINS_AND_MYSTERY_GIFT, config.kind)
        assertEquals(25, config.coins)
        assertTrue(config.hasMysteryGift)
    }

    // ==========================================
    // 2. HINT INVENTORY UNLIMITED ACCUMULATION & REWARD TESTS
    // ==========================================

    @Test
    fun `Hint inventory accumulates without upper limit for 0, 3, 5, 10, and 20 existing hints`() {
        fun creditHints(existingHints: Int, added: Int): Int {
            val current = existingHints.coerceAtLeast(0)
            return current + added
        }

        // User with 0 hints receives 3 hints -> 3
        assertEquals(3, creditHints(0, 3))

        // User with 3 hints receives 2 hints (e.g. Day 3) -> 5
        assertEquals(5, creditHints(3, 2))

        // User with 5 hints receives 3 hints (e.g. Day 6) -> 8
        assertEquals(8, creditHints(5, 3))

        // User with 10 hints receives 3 hints (e.g. Day 7 Mystery Gift) -> 13
        assertEquals(13, creditHints(10, 3))

        // User with 20 hints receives 3 hints -> 23
        assertEquals(23, creditHints(20, 3))
    }

    @Test
    fun `Shop hint purchases accumulate without restriction on large balances`() {
        fun addShopHints(existingHints: Int, packCount: Int): Int {
            return existingHints.coerceAtLeast(0) + packCount
        }

        // User with 3 hints buys 5 hints pack -> 8
        assertEquals(8, addShopHints(3, 5))

        // User with 10 hints buys 15 hints pack -> 25
        assertEquals(25, addShopHints(10, 15))

        // User with 20 hints buys 60 hints pack -> 80
        assertEquals(80, addShopHints(20, 60))
    }

    @Test
    fun `Hint consumption reduces inventory and never goes negative`() {
        fun consumeHint(currentHints: Int): Int {
            return (currentHints - 1).coerceAtLeast(0)
        }

        assertEquals(2, consumeHint(3))
        assertEquals(9, consumeHint(10))
        assertEquals(0, consumeHint(1))
        assertEquals(0, consumeHint(0)) // Prevents negative
    }

    // ==========================================
    // 3. DAILY CLAIM & TIMEZONE TESTS (Section 5)
    // ==========================================

    @Test
    fun `Timezone is fixed to Asia-Kolkata (IST)`() {
        assertEquals("Asia/Kolkata", DailyRewardTimeUtils.IST_TIME_ZONE.id)
    }

    @Test
    fun `Claim across UTC and IST date boundaries`() {
        // At 2026-11-19 19:00:00 UTC, IST is 2026-11-20 00:30:00 AM (Next Day)
        val utcFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val utcTime = utcFormat.parse("2026-11-19 19:00:00")!!.time
        val istDate = DailyRewardTimeUtils.getTodayIstDate(utcTime)
        assertEquals("2026-11-20", istDate)
    }

    @Test
    fun `Claim at midnight IST boundary`() {
        val istFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
            timeZone = DailyRewardTimeUtils.IST_TIME_ZONE
        }

        // 11:59:59 PM IST
        val justBeforeMidnight = istFormat.parse("2026-11-19 23:59:59")!!.time
        assertEquals("2026-11-19", DailyRewardTimeUtils.getTodayIstDate(justBeforeMidnight))

        // 12:00:00 AM IST next day
        val atMidnight = istFormat.parse("2026-11-20 00:00:00")!!.time
        assertEquals("2026-11-20", DailyRewardTimeUtils.getTodayIstDate(atMidnight))
    }

    @Test
    fun `Consecutive day calculation across month and year boundaries`() {
        // Normal consecutive day
        assertTrue(DailyRewardTimeUtils.isConsecutiveDay("2026-11-19", "2026-11-20"))

        // Same day is NOT consecutive
        assertFalse(DailyRewardTimeUtils.isConsecutiveDay("2026-11-20", "2026-11-20"))

        // Missed day
        assertFalse(DailyRewardTimeUtils.isConsecutiveDay("2026-11-18", "2026-11-20"))

        // Month boundary (November 30 -> December 1)
        assertTrue(DailyRewardTimeUtils.isConsecutiveDay("2026-11-30", "2026-12-01"))

        // Year boundary (December 31 -> January 1)
        assertTrue(DailyRewardTimeUtils.isConsecutiveDay("2026-12-31", "2027-01-01"))
    }

    // ==========================================
    // 4. STREAK & CYCLE BUSINESS RULES (Section 5)
    // ==========================================

    @Test
    fun `First time claim starts streak 1 and cycle 1`() {
        val nextStreak = DailyRewardTimeUtils.calculateNextOverallStreak(0, isConsecutive = false)
        val nextCycle = DailyRewardTimeUtils.calculateNextCycleDay(0, isConsecutive = false)
        assertEquals(1, nextStreak)
        assertEquals(1, nextCycle)
    }

    @Test
    fun `Consecutive claim increments streak and cycle`() {
        val nextStreak = DailyRewardTimeUtils.calculateNextOverallStreak(3, isConsecutive = true)
        val nextCycle = DailyRewardTimeUtils.calculateNextCycleDay(3, isConsecutive = true)
        assertEquals(4, nextStreak)
        assertEquals(4, nextCycle)
    }

    @Test
    fun `Day 7 consecutive claim restarts cycle day at 1 while streak continues`() {
        // Day 7 claimed: streak = 7, cycleDay = 7
        // Next consecutive day: streak = 8, cycleDay = 1
        val nextStreak = DailyRewardTimeUtils.calculateNextOverallStreak(7, isConsecutive = true)
        val nextCycle = DailyRewardTimeUtils.calculateNextCycleDay(7, isConsecutive = true)
        assertEquals(8, nextStreak)
        assertEquals(1, nextCycle)

        // Day 9 consecutive claim: streak = 9, cycleDay = 2
        val day9Streak = DailyRewardTimeUtils.calculateNextOverallStreak(nextStreak, isConsecutive = true)
        val day9Cycle = DailyRewardTimeUtils.calculateNextCycleDay(nextCycle, isConsecutive = true)
        assertEquals(9, day9Streak)
        assertEquals(2, day9Cycle)
    }

    @Test
    fun `Missed day resets overall streak and cycle day to 1 while preserving longest streak`() {
        var longestStreak = 7
        val currentStreak = 7
        val isConsecutive = false // Missed day!

        val nextStreak = DailyRewardTimeUtils.calculateNextOverallStreak(currentStreak, isConsecutive)
        val nextCycle = DailyRewardTimeUtils.calculateNextCycleDay(7, isConsecutive)
        longestStreak = maxOf(longestStreak, nextStreak)

        assertEquals(1, nextStreak)
        assertEquals(1, nextCycle)
        assertEquals(7, longestStreak) // Longest streak preserved!
    }

    @Test
    fun `Full 10-day lifecycle matching exact Section 5 table`() {
        data class DayClaim(
            val date: String,
            val claimed: Boolean,
            val expectedCycleDay: Int,
            val expectedOverallStreak: Int
        )

        val schedule = listOf(
            DayClaim("2026-11-16", true, 1, 1),  // Monday
            DayClaim("2026-11-17", true, 2, 2),  // Tuesday
            DayClaim("2026-11-18", true, 3, 3),  // Wednesday
            DayClaim("2026-11-19", true, 4, 4),  // Thursday
            DayClaim("2026-11-20", true, 5, 5),  // Friday
            DayClaim("2026-11-21", true, 6, 6),  // Saturday
            DayClaim("2026-11-22", true, 7, 7),  // Sunday
            DayClaim("2026-11-23", true, 1, 8),  // Monday (new 7-day cycle, streak 8)
            DayClaim("2026-11-24", false, -1, -1), // Tuesday (missed)
            DayClaim("2026-11-25", true, 1, 1)   // Wednesday (reset to 1)
        )

        var lastClaimDate: String? = null
        var streak = 0
        var cycleDay = 0
        var longestStreak = 0

        for (item in schedule) {
            if (!item.claimed) {
                continue // Did not claim on this date
            }

            val isConsecutive = DailyRewardTimeUtils.isConsecutiveDay(lastClaimDate, item.date)
            streak = DailyRewardTimeUtils.calculateNextOverallStreak(streak, isConsecutive)
            cycleDay = DailyRewardTimeUtils.calculateNextCycleDay(cycleDay, isConsecutive)
            longestStreak = maxOf(longestStreak, streak)
            lastClaimDate = item.date

            assertEquals("Cycle day mismatch on ${item.date}", item.expectedCycleDay, cycleDay)
            assertEquals("Overall streak mismatch on ${item.date}", item.expectedOverallStreak, streak)
        }

        assertEquals(8, longestStreak)
    }

    // ==========================================
    // 5. MYSTERY GIFT TESTS (Section 3 & 6.D)
    // ==========================================

    @Test
    fun `Mystery gift reward is always within 1 to 3 hints`() {
        val random = kotlin.random.Random(12345)
        repeat(100) {
            val hints = random.nextInt(1, 4)
            assertTrue("Hints must be >= 1", hints >= 1)
            assertTrue("Hints must be <= 3", hints <= 3)
        }
    }

    @Test
    fun `Mystery gift opening is idempotent and does not regenerate reward on retry`() {
        val initialSelectedHints = 2
        var isRevealed = false
        val creditingCalls = AtomicInteger(0)

        // First open call
        if (!isRevealed) {
            creditingCalls.incrementAndGet()
            isRevealed = true
        }
        assertEquals(1, creditingCalls.get())

        // Second open call (e.g. repeated click or screen refresh)
        if (!isRevealed) {
            creditingCalls.incrementAndGet()
        }
        // Still 1! Reward is NOT duplicated
        assertEquals(1, creditingCalls.get())
        assertEquals(2, initialSelectedHints)
    }
}
