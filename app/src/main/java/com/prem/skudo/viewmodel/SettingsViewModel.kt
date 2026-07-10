package com.prem.skudo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.prem.skudo.repository.SettingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsState(
    val autoCheck: Boolean = true,
    val highlightSameNumbers: Boolean = true,
    val highlightMistakes: Boolean = true,
    val autoRemoveNotes: Boolean = true,
    val showTimer: Boolean = true,
    val showMistakesCounter: Boolean = true,
    val leftHandMode: Boolean = false,
    val themeMode: String = "SYSTEM",
    val accentColor: String = "Cyan",
    val boardStyle: String = "Modern",
    val soundEffects: Boolean = true,
    val backgroundMusic: Boolean = false,
    val vibration: Boolean = true,
    val buttonSounds: Boolean = true,
    val dailyReminder: Boolean = false,
    val achievementReminder: Boolean = false,
    val eventReminder: Boolean = false
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SettingsRepository(application)

    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState = _settingsState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.autoCheck,
                repository.highlightSameNumbers,
                repository.highlightMistakes,
                repository.autoRemoveNotes,
                repository.showTimer,
                repository.showMistakesCounter,
                repository.leftHandMode,
                repository.themeMode,
                repository.accentColor,
                repository.boardStyle,
                repository.soundEffects,
                repository.backgroundMusic,
                repository.vibration,
                repository.buttonSounds,
                repository.dailyReminder,
                repository.achievementReminder,
                repository.eventReminder
            ) { args ->
                SettingsState(
                    autoCheck = args[0] as Boolean,
                    highlightSameNumbers = args[1] as Boolean,
                    highlightMistakes = args[2] as Boolean,
                    autoRemoveNotes = args[3] as Boolean,
                    showTimer = args[4] as Boolean,
                    showMistakesCounter = args[5] as Boolean,
                    leftHandMode = args[6] as Boolean,
                    themeMode = args[7] as String,
                    accentColor = args[8] as String,
                    boardStyle = args[9] as String,
                    soundEffects = args[10] as Boolean,
                    backgroundMusic = args[11] as Boolean,
                    vibration = args[12] as Boolean,
                    buttonSounds = args[13] as Boolean,
                    dailyReminder = args[14] as Boolean,
                    achievementReminder = args[15] as Boolean,
                    eventReminder = args[16] as Boolean
                )
            }.collect {
                _settingsState.value = it
            }
        }
    }

    fun updateAutoCheck(enabled: Boolean) = viewModelScope.launch { repository.updateAutoCheck(enabled) }
    fun updateHighlightSameNumbers(enabled: Boolean) = viewModelScope.launch { repository.updateHighlightSameNumbers(enabled) }
    fun updateHighlightMistakes(enabled: Boolean) = viewModelScope.launch { repository.updateHighlightMistakes(enabled) }
    fun updateAutoRemoveNotes(enabled: Boolean) = viewModelScope.launch { repository.updateAutoRemoveNotes(enabled) }
    fun updateShowTimer(enabled: Boolean) = viewModelScope.launch { repository.updateShowTimer(enabled) }
    fun updateShowMistakesCounter(enabled: Boolean) = viewModelScope.launch { repository.updateShowMistakesCounter(enabled) }
    fun updateLeftHandMode(enabled: Boolean) = viewModelScope.launch { repository.updateLeftHandMode(enabled) }
    fun updateThemeMode(mode: String) = viewModelScope.launch { repository.updateThemeMode(mode) }
    fun updateAccentColor(color: String) = viewModelScope.launch { repository.updateAccentColor(color) }
    fun updateBoardStyle(style: String) = viewModelScope.launch { repository.updateBoardStyle(style) }
    fun updateSoundEffects(enabled: Boolean) = viewModelScope.launch { repository.updateSoundEffects(enabled) }
    fun updateBackgroundMusic(enabled: Boolean) = viewModelScope.launch { repository.updateBackgroundMusic(enabled) }
    fun updateVibration(enabled: Boolean) = viewModelScope.launch { repository.updateVibration(enabled) }
    fun updateButtonSounds(enabled: Boolean) = viewModelScope.launch { repository.updateButtonSounds(enabled) }
    fun updateDailyReminder(enabled: Boolean) = viewModelScope.launch { repository.updateDailyReminder(enabled) }
    fun updateAchievementReminder(enabled: Boolean) = viewModelScope.launch { repository.updateAchievementReminder(enabled) }
    fun updateEventReminder(enabled: Boolean) = viewModelScope.launch { repository.updateEventReminder(enabled) }
}
