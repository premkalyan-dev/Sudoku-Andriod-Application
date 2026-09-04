package com.prem.skudo.repository

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val context: Context) {

    // Gameplay
    private val AUTO_CHECK = booleanPreferencesKey("auto_check")
    private val HIGHLIGHT_SAME_NUMBERS = booleanPreferencesKey("highlight_same_numbers")
    private val HIGHLIGHT_MISTAKES = booleanPreferencesKey("highlight_mistakes")
    private val AUTO_REMOVE_NOTES = booleanPreferencesKey("auto_remove_notes")
    private val SHOW_TIMER = booleanPreferencesKey("show_timer")
    private val SHOW_MISTAKES_COUNTER = booleanPreferencesKey("show_mistakes_counter")
    private val LEFT_HAND_MODE = booleanPreferencesKey("left_hand_mode")

    // Appearance
    private val THEME_MODE = stringPreferencesKey("theme_mode") // SYSTEM, LIGHT, DARK
    private val ACCENT_COLOR = stringPreferencesKey("accent_color")
    private val BOARD_STYLE = stringPreferencesKey("board_style")
    private val GRID_THICKNESS = floatPreferencesKey("grid_thickness")
    private val ANIMATION_SPEED = floatPreferencesKey("animation_speed")

    // Audio
    private val SOUND_EFFECTS = booleanPreferencesKey("sound_effects")
    private val BACKGROUND_MUSIC = booleanPreferencesKey("background_music")
    private val VIBRATION = booleanPreferencesKey("vibration")
    private val BUTTON_SOUNDS = booleanPreferencesKey("button_sounds")
    private val VOLUME_LEVEL = floatPreferencesKey("volume_level")

    // Notifications
    private val DAILY_CHALLENGE_REMINDER = booleanPreferencesKey("daily_reminder")
    private val ACHIEVEMENT_REMINDER = booleanPreferencesKey("achievement_reminder")
    private val EVENT_REMINDER = booleanPreferencesKey("event_reminder")

    // Flows
    val autoCheck: Flow<Boolean> = context.settingsDataStore.data.map { it[AUTO_CHECK] ?: true }
    val highlightSameNumbers: Flow<Boolean> = context.settingsDataStore.data.map { it[HIGHLIGHT_SAME_NUMBERS] ?: true }
    val highlightMistakes: Flow<Boolean> = context.settingsDataStore.data.map { it[HIGHLIGHT_MISTAKES] ?: true }
    val autoRemoveNotes: Flow<Boolean> = context.settingsDataStore.data.map { it[AUTO_REMOVE_NOTES] ?: true }
    val showTimer: Flow<Boolean> = context.settingsDataStore.data.map { it[SHOW_TIMER] ?: true }
    val showMistakesCounter: Flow<Boolean> = context.settingsDataStore.data.map { it[SHOW_MISTAKES_COUNTER] ?: true }
    val leftHandMode: Flow<Boolean> = context.settingsDataStore.data.map { it[LEFT_HAND_MODE] ?: false }

    val themeMode: Flow<String> = context.settingsDataStore.data.map { it[THEME_MODE] ?: "SYSTEM" }
    val accentColor: Flow<String> = context.settingsDataStore.data.map { it[ACCENT_COLOR] ?: "Cyan" }
    val boardStyle: Flow<String> = context.settingsDataStore.data.map { it[BOARD_STYLE] ?: "Modern" }
    val gridThickness: Flow<Float> = context.settingsDataStore.data.map { it[GRID_THICKNESS] ?: 1.0f }
    val animationSpeed: Flow<Float> = context.settingsDataStore.data.map { it[ANIMATION_SPEED] ?: 1.0f }

    val soundEffects: Flow<Boolean> = context.settingsDataStore.data.map { it[SOUND_EFFECTS] ?: false }
    val backgroundMusic: Flow<Boolean> = context.settingsDataStore.data.map { it[BACKGROUND_MUSIC] ?: false }
    val vibration: Flow<Boolean> = context.settingsDataStore.data.map { it[VIBRATION] ?: true }
    val buttonSounds: Flow<Boolean> = context.settingsDataStore.data.map { it[BUTTON_SOUNDS] ?: false }
    val volumeLevel: Flow<Float> = context.settingsDataStore.data.map { it[VOLUME_LEVEL] ?: 1.0f }

    val dailyReminder: Flow<Boolean> = context.settingsDataStore.data.map { it[DAILY_CHALLENGE_REMINDER] ?: false }
    val achievementReminder: Flow<Boolean> = context.settingsDataStore.data.map { it[ACHIEVEMENT_REMINDER] ?: false }
    val eventReminder: Flow<Boolean> = context.settingsDataStore.data.map { it[EVENT_REMINDER] ?: false }

    // Update methods
    suspend fun updateAutoCheck(enabled: Boolean) = context.settingsDataStore.edit { it[AUTO_CHECK] = enabled }
    suspend fun updateHighlightSameNumbers(enabled: Boolean) = context.settingsDataStore.edit { it[HIGHLIGHT_SAME_NUMBERS] = enabled }
    suspend fun updateHighlightMistakes(enabled: Boolean) = context.settingsDataStore.edit { it[HIGHLIGHT_MISTAKES] = enabled }
    suspend fun updateAutoRemoveNotes(enabled: Boolean) = context.settingsDataStore.edit { it[AUTO_REMOVE_NOTES] = enabled }
    suspend fun updateShowTimer(enabled: Boolean) = context.settingsDataStore.edit { it[SHOW_TIMER] = enabled }
    suspend fun updateShowMistakesCounter(enabled: Boolean) = context.settingsDataStore.edit { it[SHOW_MISTAKES_COUNTER] = enabled }
    suspend fun updateLeftHandMode(enabled: Boolean) = context.settingsDataStore.edit { it[LEFT_HAND_MODE] = enabled }

    suspend fun updateThemeMode(mode: String) = context.settingsDataStore.edit { it[THEME_MODE] = mode }
    suspend fun updateAccentColor(color: String) = context.settingsDataStore.edit { it[ACCENT_COLOR] = color }
    suspend fun updateBoardStyle(style: String) = context.settingsDataStore.edit { it[BOARD_STYLE] = style }
    suspend fun updateGridThickness(thickness: Float) = context.settingsDataStore.edit { it[GRID_THICKNESS] = thickness }
    suspend fun updateAnimationSpeed(speed: Float) = context.settingsDataStore.edit { it[ANIMATION_SPEED] = speed }

    suspend fun updateSoundEffects(enabled: Boolean) = context.settingsDataStore.edit { it[SOUND_EFFECTS] = enabled }
    suspend fun updateBackgroundMusic(enabled: Boolean) = context.settingsDataStore.edit { it[BACKGROUND_MUSIC] = enabled }
    suspend fun updateVibration(enabled: Boolean) = context.settingsDataStore.edit { it[VIBRATION] = enabled }
    suspend fun updateButtonSounds(enabled: Boolean) = context.settingsDataStore.edit { it[BUTTON_SOUNDS] = enabled }
    suspend fun updateVolumeLevel(level: Float) = context.settingsDataStore.edit { it[VOLUME_LEVEL] = level }

    suspend fun updateDailyReminder(enabled: Boolean) = context.settingsDataStore.edit { it[DAILY_CHALLENGE_REMINDER] = enabled }
    suspend fun updateAchievementReminder(enabled: Boolean) = context.settingsDataStore.edit { it[ACHIEVEMENT_REMINDER] = enabled }
    suspend fun updateEventReminder(enabled: Boolean) = context.settingsDataStore.edit { it[EVENT_REMINDER] = enabled }
}
