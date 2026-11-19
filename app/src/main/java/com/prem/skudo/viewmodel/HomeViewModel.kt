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
import com.prem.skudo.repository.DailyRewardInfo
import com.prem.skudo.repository.OpenMysteryGiftResult
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
    val isDailyRewardClaimedToday: Boolean = false,
    val dailyRewardInfo: DailyRewardInfo? = null,
    val isClaimingReward: Boolean = false,
    val isOpeningGift: Boolean = false,
    val rewardMessage: String? = null,
    val mysteryGiftResult: OpenMysteryGiftResult? = null
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
                val info = rewardRepository.getDailyRewardInfo()
                _uiState.update {
                    it.copy(
                        userProfile = profile,
                        dailyRewardInfo = info,
                        isDailyRewardClaimedToday = info.isClaimedToday,
                        dailyRewardCoins = profile.coins,
                        dailyRewardStreak = info.currentStreak
                    )
                }
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
            val info = rewardRepository.getDailyRewardInfo()
            _uiState.update {
                it.copy(
                    showDailyReward = true,
                    dailyRewardInfo = info,
                    dailyRewardCoins = info.walletBalance,
                    dailyRewardStreak = info.currentStreak,
                    isDailyRewardClaimedToday = info.isClaimedToday
                )
            }
        }
    }

    fun claimDailyReward() {
        viewModelScope.launch {
            if (_uiState.value.isClaimingReward) return@launch
            _uiState.update { it.copy(isClaimingReward = true) }
            val result = rewardRepository.claimDailyReward()
            val info = rewardRepository.getDailyRewardInfo()
            _uiState.update {
                it.copy(
                    isClaimingReward = false,
                    dailyRewardInfo = info,
                    dailyRewardCoins = info.walletBalance,
                    dailyRewardStreak = info.currentStreak,
                    isDailyRewardClaimedToday = info.isClaimedToday,
                    rewardMessage = result.message
                )
            }
        }
    }

    fun openMysteryGift(giftId: String? = null) {
        viewModelScope.launch {
            if (_uiState.value.isOpeningGift) return@launch
            _uiState.update { it.copy(isOpeningGift = true) }
            val result = rewardRepository.openMysteryGift(giftId)
            val info = rewardRepository.getDailyRewardInfo()
            _uiState.update {
                it.copy(
                    isOpeningGift = false,
                    dailyRewardInfo = info,
                    mysteryGiftResult = result,
                    rewardMessage = result.message
                )
            }
        }
    }

    fun clearRewardMessage() {
        _uiState.update { it.copy(rewardMessage = null, mysteryGiftResult = null) }
    }

    fun dismissDailyReward() {
        _uiState.update { it.copy(showDailyReward = false, rewardMessage = null, mysteryGiftResult = null) }
    }
}
