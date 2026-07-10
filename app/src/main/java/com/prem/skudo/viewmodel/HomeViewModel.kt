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
import com.prem.skudo.repository.AuthRepository
import com.prem.skudo.repository.CloudSyncRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn

data class HomeState(
    val easyStats: GameStats? = null,
    val mediumStats: GameStats? = null,
    val hardStats: GameStats? = null,
    val expertStats: GameStats? = null,
    val userProfile: UserProfile? = null,
    val currentUser: FirebaseUser? = null,
    val isSyncing: Boolean = false,
    val syncError: String? = null,
    val dailyStreak: Int = 0,
    val lastChallengeDate: Int = 0,
    val hasSavedGame: Boolean = false,
    val latestSavedGame: com.prem.skudo.database.SavedGame? = null,
    val achievements: List<com.prem.skudo.database.Achievement> = emptyList(),
    val unlockedCount: Int = 0,
    val dailyRewardCoins: Long = 0,
    val dailyRewardStreak: Int = 0,
    val showDailyReward: Boolean = false
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
    private val authRepository = AuthRepository(application)
    private val syncRepository = CloudSyncRepository(application)
    
    private val _uiState = MutableStateFlow(HomeState())
    val uiState = _uiState.asStateFlow()

    init {
        loadStats()

        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.update { it.copy(currentUser = user) }
                if (user != null) {
                    performCloudSync(user.uid)
                }
            }
        }
        
        userRepository.userProfile
            .filterNotNull()
            .onEach { profile ->
                _uiState.update { it.copy(userProfile = profile) }
            }
            // Debounce sync triggers to avoid excessive writes
            .distinctUntilChangedBy { it.updatedAt }
            .onEach { profile ->
                if (profile.cloudUserId != null) {
                    performCloudSync(profile.cloudUserId)
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
                _uiState.update { it.copy(
                    hasSavedGame = savedGames.isNotEmpty(),
                    latestSavedGame = savedGames.firstOrNull()
                ) }
            }
        }
    }

    fun loadStats() {
        viewModelScope.launch {
            val latestSaved = repository.getLatestSavedGame()
            userRepository.getOrCreateProfile() // Ensure profile exists
            
            val easy = repository.getStats(Difficulty.EASY)
            val medium = repository.getStats(Difficulty.MEDIUM)
            val hard = repository.getStats(Difficulty.HARD)
            val expert = repository.getStats(Difficulty.EXPERT)
            
            _uiState.update { it.copy(
                easyStats = easy,
                mediumStats = medium,
                hardStats = hard,
                expertStats = expert,
                dailyStreak = repository.dailyStreak.first(),
                lastChallengeDate = repository.lastChallengeDate.first(),
                hasSavedGame = latestSaved != null,
                latestSavedGame = latestSaved
            ) }
        }
    }

    fun updateDisplayName(name: String): Boolean {
        val trimmedName = name.trim()
        if (trimmedName.length in 3..20) {
            viewModelScope.launch {
                userRepository.updateDisplayName(trimmedName)
            }
            return true
        }
        return false
    }

    fun updateAvatar(avatarId: String) {
        viewModelScope.launch {
            userRepository.updateAvatar(avatarId)
        }
    }

    fun checkDailyReward() {
        viewModelScope.launch {
            val result = rewardRepository.claimDailyReward()
            if (result.success) {
                _uiState.update { it.copy(
                    dailyRewardCoins = result.coins,
                    dailyRewardStreak = result.streak,
                    showDailyReward = true
                ) }
            }
        }
    }

    fun dismissDailyReward() {
        _uiState.update { it.copy(showDailyReward = false) }
    }

    fun signIn() {
        viewModelScope.launch {
            authRepository.signInWithGoogle()
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    private fun performCloudSync(uid: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, syncError = null) }
            try {
                // Update local profile with cloud info if missing
                val user = authRepository.currentUser.value
                val profile = userRepository.getOrCreateProfile()
                if (profile.cloudUserId == null && user != null) {
                    userRepository.updateProfileInfo(
                        email = user.email,
                        photoUrl = user.photoUrl?.toString(),
                        cloudUserId = user.uid,
                        displayName = user.displayName ?: profile.displayName
                    )
                }

                syncRepository.syncLocalToCloud(uid)
                _uiState.update { it.copy(isSyncing = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSyncing = false, syncError = e.message) }
            }
        }
    }
}
