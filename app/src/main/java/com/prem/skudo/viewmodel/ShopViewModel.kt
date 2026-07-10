package com.prem.skudo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.prem.skudo.repository.ShopRepository
import com.prem.skudo.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ShopState(
    val coins: Long = 0,
    val gems: Long = 0,
    val hints: Int = 0,
    val unlockedAvatars: List<String> = emptyList(),
    val unlockedThemes: List<String> = emptyList(),
    val purchaseSuccess: Boolean = false,
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

    fun buyHints(hintsCount: Int, cost: Long) {
        viewModelScope.launch {
            val success = shopRepository.buyHintsPackage(hintsCount, cost)
            if (success) {
                _uiState.value = _uiState.value.copy(purchaseSuccess = true)
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = "Not enough coins!")
            }
        }
    }

    fun buyAvatar(avatarId: String) {
        viewModelScope.launch {
            val success = shopRepository.buyAvatar(avatarId)
            if (success) {
                _uiState.value = _uiState.value.copy(purchaseSuccess = true)
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = "Not enough coins!")
            }
        }
    }

    fun buyTheme(themeName: String) {
        viewModelScope.launch {
            val success = shopRepository.buyTheme(themeName)
            if (success) {
                _uiState.value = _uiState.value.copy(purchaseSuccess = true)
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = "Not enough coins!")
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(purchaseSuccess = false, errorMessage = null)
    }
}
