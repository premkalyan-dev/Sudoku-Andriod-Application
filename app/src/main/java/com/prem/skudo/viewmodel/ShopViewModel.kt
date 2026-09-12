package com.prem.skudo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.prem.skudo.repository.ShopRepository
import com.prem.skudo.repository.UserRepository
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Immutable
data class PurchaseRewardDetails(
    val coinsGained: Long,
    val hintsReceived: Int,
    val rewardsReceived: String
)

@Immutable
data class ShopState(
    val coins: Long = 0,
    val gems: Long = 0,
    val hints: Int = 0,
    val unlockedAvatars: List<String> = emptyList(),
    val unlockedThemes: List<String> = emptyList(),
    val purchaseSuccess: Boolean = false,
    val purchaseRewardDetails: PurchaseRewardDetails? = null,
    val errorMessage: String? = null
)

class ShopViewModel(application: Application) : AndroidViewModel(application) {
    private val shopRepository = ShopRepository(application)
    private val userRepository = UserRepository(application)
    
    private val _uiState = MutableStateFlow(ShopState())
    val uiState: StateFlow<ShopState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.userProfile.collect { profile ->
                profile?.let {
                    _uiState.value = _uiState.value.copy(
                        coins = it.coins,
                        gems = it.gems,
                        hints = it.hints,
                        unlockedAvatars = it.unlockedAvatars.split(","),
                        unlockedThemes = it.unlockedThemes.split(",")
                    )
                }
            }
        }
    }

    fun buyCoinPack(coins: Long, bonusHints: Int, bonusGems: Long, rewardsDescription: String) {
        viewModelScope.launch {
            val success = shopRepository.buyCoinPack(coins, bonusHints, bonusGems)
            if (success) {
                _uiState.value = _uiState.value.copy(
                    purchaseSuccess = true,
                    purchaseRewardDetails = PurchaseRewardDetails(
                        coinsGained = coins,
                        hintsReceived = bonusHints,
                        rewardsReceived = rewardsDescription
                    )
                )
            }
        }
    }

    fun buyHints(hintsCount: Int, cost: Long, packName: String = "Hint Pack") {
        viewModelScope.launch {
            val success = shopRepository.buyHintsPackage(hintsCount, cost)
            if (success) {
                _uiState.value = _uiState.value.copy(
                    purchaseSuccess = true,
                    purchaseRewardDetails = PurchaseRewardDetails(
                        coinsGained = 0,
                        hintsReceived = hintsCount,
                        rewardsReceived = "$packName Activated"
                    )
                )
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = "Not enough coins!")
            }
        }
    }

    fun buyAvatar(avatarId: String) {
        viewModelScope.launch {
            val success = shopRepository.buyAvatar(avatarId)
            if (success) {
                _uiState.value = _uiState.value.copy(
                    purchaseSuccess = true,
                    purchaseRewardDetails = PurchaseRewardDetails(
                        coinsGained = 0,
                        hintsReceived = 0,
                        rewardsReceived = "Avatar Unlocked"
                    )
                )
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = "Not enough coins!")
            }
        }
    }

    fun buyTheme(themeName: String) {
        viewModelScope.launch {
            val success = shopRepository.buyTheme(themeName)
            if (success) {
                _uiState.value = _uiState.value.copy(
                    purchaseSuccess = true,
                    purchaseRewardDetails = PurchaseRewardDetails(
                        coinsGained = 0,
                        hintsReceived = 0,
                        rewardsReceived = "$themeName Theme Unlocked"
                    )
                )
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = "Not enough coins!")
            }
        }
    }

    fun dismissPurchaseDialog() {
        _uiState.value = _uiState.value.copy(
            purchaseSuccess = false,
            purchaseRewardDetails = null
        )
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            purchaseSuccess = false,
            purchaseRewardDetails = null,
            errorMessage = null
        )
    }
}
