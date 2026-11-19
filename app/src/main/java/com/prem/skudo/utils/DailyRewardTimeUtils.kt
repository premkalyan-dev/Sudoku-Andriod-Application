package com.prem.skudo.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DailyRewardTimeUtils {
    val IST_TIME_ZONE: TimeZone = TimeZone.getTimeZone("Asia/Kolkata")

    fun getTodayIstDate(nowMillis: Long = System.currentTimeMillis()): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = IST_TIME_ZONE
        }
        return sdf.format(Date(nowMillis))
    }

    fun isConsecutiveDay(previousDateStr: String?, currentDateStr: String): Boolean {
        if (previousDateStr == null || previousDateStr.isBlank()) return false
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = IST_TIME_ZONE
        }
        return try {
            val prevDate = sdf.parse(previousDateStr) ?: return false
            val cal = Calendar.getInstance(IST_TIME_ZONE).apply {
                time = prevDate
                add(Calendar.DAY_OF_YEAR, 1)
            }
            val expectedNext = sdf.format(cal.time)
            expectedNext == currentDateStr
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Calculates the next reward cycle day (1..7).
     * After Day 7, consecutive claim resets cycle day back to 1.
     * A missed day resets cycle day back to 1.
     */
    fun calculateNextCycleDay(previousCycleDay: Int, isConsecutive: Boolean): Int {
        if (!isConsecutive) return 1
        return if (previousCycleDay >= 7) 1 else previousCycleDay + 1
    }

    /**
     * Calculates the next overall streak.
     * Consecutive claim increments overall streak.
     * A missed day resets overall streak to 1.
     */
    fun calculateNextOverallStreak(previousStreak: Int, isConsecutive: Boolean): Int {
        return if (isConsecutive) previousStreak + 1 else 1
    }

    /**
     * Milliseconds remaining until the next 12:00 AM IST.
     */
    fun getMillisUntilNextMidnightIst(nowMillis: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance(IST_TIME_ZONE).apply {
            timeInMillis = nowMillis
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return (cal.timeInMillis - nowMillis).coerceAtLeast(0L)
    }
}
