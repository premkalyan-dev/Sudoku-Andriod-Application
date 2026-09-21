package com.prem.skudo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.prem.skudo.database.GameStats
import com.prem.skudo.model.Difficulty
import com.prem.skudo.repository.SudokuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.prem.skudo.database.UserProfile
import com.prem.skudo.repository.UserRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn

@Immutable
data class HomeState(
    val easyStats: GameStats? = null,
    val mediumStats: GameStats? = null,
    val hardStats: GameStats? = null,
    val expertStats: GameStats? = null,
    val userProfile: UserProfile? = null,
    val dailyStreak: Int = 0,
    val lastChallengeDate: Int = 0,
    val hasSavedGame: Boolean = false,
    val latestSavedGame: com.prem.skudo.database.SavedGame? = null,
    val achievements: List<com.prem.skudo.database.Achievement> = emptyList(),
    val unlockedCount: Int = 0,
    val dailyRewardCoins: Long = 0,
    val dailyRewardStreak: Int = 0,
    val showDailyReward: Boolean = false,
    val isDailyRewardClaimedToday: Boolean = false
) {
    val bestOverallStats: GameStats? by lazy {
        listOfNotNull(easyStats, mediumStats, hardStats, expertStats)
            .filter { it.bestTimeSeconds != Long.MAX_VALUE }
            .minByOrNull { it.bestTimeSeconds }
    }
    
    val totalGamesPlayed: Int
        get() = (easyStats?.gamesPlayed ?: 0) + (mediumStats?.gamesPlayed ?: 0) + 
                (hardStats?.gamesPlayed ?: 0) + (expertStats?.gamesPlayed ?: 0)
                
    val totalGamesWon: Int
        get() = (easyStats?.gamesWon ?: 0) + (mediumStats?.gamesWon ?: 0) + 
                (hardStats?.gamesWon ?: 0) + (expertStats?.gamesWon ?: 0)
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SudokuRepository(application)
    private val userRepository = UserRepository(application)
    private val rewardRepository = com.prem.skudo.repository.RewardRepository(application)
    
    private val _uiState = MutableStateFlow(HomeState())
    val uiState = _uiState.asStateFlow()

    init {
        loadStats()
        
        userRepository.userProfile
            .filterNotNull()
            .onEach { profile ->
                val claimed = rewardRepository.isClaimedToday(profile.lastDailyRewardTimestamp)
                _uiState.update { it.copy(userProfile = profile, isDailyRewardClaimedToday = claimed) }
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            repository.allAchievements.collect { achievements ->
                if (achievements.isEmpty()) {
                    repository.initializeAchievements(com.prem.skudo.utils.AchievementManager.DEFAULT_ACHIEVEMENTS)
                }
                _uiState.update { state ->
                    state.copy(
                        achievements = achievements,
                        unlockedCount = achievements.count { it.isUnlocked }
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.savedGames.collect { savedGames ->
                val latest = savedGames.firstOrNull()
                _uiState.update { state ->
                    state.copy(
                        hasSavedGame = latest != null,
                        latestSavedGame = latest
                    )
                }
            }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            val easy = repository.getStats(Difficulty.EASY)
            val medium = repository.getStats(Difficulty.MEDIUM)
            val hard = repository.getStats(Difficulty.HARD)
            val expert = repository.getStats(Difficulty.EXPERT)

            val streak = repository.dailyStreak.first()
            val lastDate = repository.lastChallengeDate.first()

            _uiState.update { state ->
                state.copy(
                    easyStats = easy,
                    mediumStats = medium,
                    hardStats = hard,
                    expertStats = expert,
                    dailyStreak = streak,
                    lastChallengeDate = lastDate
                )
            }
        }
    }

    fun openDailyRewards() {
        viewModelScope.launch {
            val profile = userRepository.getOrCreateProfile()
            val claimed = rewardRepository.isClaimedToday(profile.lastDailyRewardTimestamp)
            val now = System.currentTimeMillis()
            val oneDayMillis = 24 * 60 * 60 * 1000L
            val streak = if (now - profile.lastDailyRewardTimestamp < oneDayMillis * 2) {
                if (claimed) profile.dailyRewardStreak else (profile.dailyRewardStreak % 7) + 1
            } else {
                if (claimed) profile.dailyRewardStreak else 1
            }
            _uiState.update { it.copy(
                showDailyReward = true,
                dailyRewardCoins = profile.coins,
                dailyRewardStreak = streak,
                isDailyRewardClaimedToday = claimed
            ) }
        }
    }

    fun claimDailyReward() {
        viewModelScope.launch {
            val result = rewardRepository.claimDailyReward()
            if (result.success) {
                val profile = userRepository.getOrCreateProfile()
                _uiState.update { it.copy(
                    dailyRewardCoins = profile.coins,
                    dailyRewardStreak = result.streak,
                    isDailyRewardClaimedToday = true
                ) }
            }
        }
    }

    fun dismissDailyReward() {
        _uiState.update { it.copy(showDailyReward = false) }
    }
}
