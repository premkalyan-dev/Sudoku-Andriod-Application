package com.prem.skudo.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val userId: String = UUID.randomUUID().toString(),
    val displayName: String = "Player",
    val avatarId: String = "skudo_pencil",
    val country: String? = null,
    val preferredLanguage: String = "en",
    val timezone: String = java.util.TimeZone.getDefault().id,
    
    val joinedDate: Long = System.currentTimeMillis(),
    val lastActiveAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    
    // Progression
    val level: Int = 1,
    val xp: Long = 0,
    val totalPlayTime: Long = 0,
    val currentRank: String = "Beginner",
    val winStreak: Int = 0,
    
    // Economy
    val coins: Long = 0,
    val gems: Long = 0,
    val premiumStatus: Boolean = false,
    
    // Stats Expansion
    val totalXpEarned: Long = 0,
    val totalCoinsEarned: Long = 0,
    val totalGemsEarned: Long = 0,
    val highestLevel: Int = 1,
    val highestRank: String = "Beginner",
    val longestDailyStreak: Int = 0,
    val bestDailyChallengeTime: Long = Long.MAX_VALUE,
    val averageCompletionTime: Long = 0,
    val perfectGames: Int = 0,
    val noHintWins: Int = 0,
    val noMistakeWins: Int = 0,
    
    // Unlocks
    val unlockedAvatars: String = "skudo_pencil",
    val unlockedThemes: String = "Classic,Dark",
    val hints: Int = 3,
    
    // Daily Rewards
    val lastDailyRewardTimestamp: Long = 0,
    val dailyRewardStreak: Int = 0,
    
    // Future Cloud Support
    val cloudUserId: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val provider: String? = null,
    val syncEnabled: Boolean = false,
    val lastSyncAt: Long? = null,
)
