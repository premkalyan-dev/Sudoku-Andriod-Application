package com.prem.skudo.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.google.gson.Gson
import com.prem.skudo.database.AppDatabase
import com.prem.skudo.database.GameStats
import com.prem.skudo.database.SavedGame
import com.prem.skudo.database.GameHistory
import com.prem.skudo.database.Achievement
import com.prem.skudo.database.DailyChallenge
import com.prem.skudo.model.Difficulty
import com.prem.skudo.model.SudokuBoard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SudokuRepository(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val statsDao = db.statsDao()
    private val gameDao = db.gameDao()
    private val historyDao = db.historyDao()
    private val achievementDao = db.achievementDao()
    private val dailyChallengeDao = db.dailyChallengeDao()
    private val gson = Gson()

    val allAchievements = achievementDao.getAllAchievements()
    val gameHistory = historyDao.getAllHistory()
    val completedChallenges = dailyChallengeDao.getCompletedChallenges()
    val savedGames: Flow<List<SavedGame>> = gameDao.observeAllSavedGames()

    // Settings Keys
    private val MISTAKE_LIMIT = intPreferencesKey("mistake_limit")
    private val AUTO_CHECK = booleanPreferencesKey("auto_check")
    private val DAILY_STREAK = intPreferencesKey("daily_streak")
    private val LAST_CHALLENGE_DATE = intPreferencesKey("last_challenge_date")

    val mistakeLimit: Flow<Int> = context.settingsDataStore.data.map { it[MISTAKE_LIMIT] ?: 3 }
    val autoCheck: Flow<Boolean> = context.settingsDataStore.data.map { it[AUTO_CHECK] ?: true }
    val dailyStreak: Flow<Int> = context.settingsDataStore.data.map { it[DAILY_STREAK] ?: 0 }
    val lastChallengeDate: Flow<Int> = context.settingsDataStore.data.map { it[LAST_CHALLENGE_DATE] ?: 0 }

    suspend fun updateMistakeLimit(limit: Int) {
        context.settingsDataStore.edit { it[MISTAKE_LIMIT] = limit }
    }

    suspend fun updateAutoCheck(enabled: Boolean) {
        context.settingsDataStore.edit { it[AUTO_CHECK] = enabled }
    }

    suspend fun updateDailyStreak(date: Int) {
        context.settingsDataStore.edit { preferences ->
            val currentStreak = preferences[DAILY_STREAK] ?: 0
            val lastDate = preferences[LAST_CHALLENGE_DATE] ?: 0
            
            if (date == lastDate + 1) {
                preferences[DAILY_STREAK] = currentStreak + 1
            } else if (date > lastDate + 1) {
                preferences[DAILY_STREAK] = 1
            }
            preferences[LAST_CHALLENGE_DATE] = date
        }
    }

    suspend fun getStats(difficulty: Difficulty): GameStats {
        return statsDao.getStats(difficulty.name) ?: GameStats(difficulty.name)
    }

    suspend fun updateStats(difficulty: Difficulty, isWin: Boolean, time: Long) {
        val currentStats = getStats(difficulty)
        val isNewBest = isWin && time < currentStats.bestTimeSeconds
        val newStats = currentStats.copy(
            gamesPlayed = currentStats.gamesPlayed + 1,
            gamesWon = if (isWin) currentStats.gamesWon + 1 else currentStats.gamesWon,
            bestTimeSeconds = if (isNewBest) time else currentStats.bestTimeSeconds,
            bestTimeTimestamp = if (isNewBest) System.currentTimeMillis() else currentStats.bestTimeTimestamp,
            totalTimeSeconds = currentStats.totalTimeSeconds + time
        )
        statsDao.insertStats(newStats)
    }

    suspend fun addGameToHistory(
        difficulty: Difficulty,
        timeSeconds: Long,
        mistakes: Int,
        hintsUsed: Int,
        isWin: Boolean,
        isDaily: Boolean = false
    ) {
        val history = GameHistory(
            difficulty = difficulty,
            timeSeconds = timeSeconds,
            mistakes = mistakes,
            hintsUsed = hintsUsed,
            isWin = isWin,
            isDaily = isDaily,
            score = calculateScore(difficulty, timeSeconds, mistakes, isWin)
        )
        historyDao.insertHistory(history)
    }

    private fun calculateScore(difficulty: Difficulty, time: Long, mistakes: Int, isWin: Boolean): Int {
        if (!isWin) return 0
        val base = when(difficulty) {
            Difficulty.EASY -> 1000
            Difficulty.MEDIUM -> 2000
            Difficulty.HARD -> 4000
            Difficulty.EXPERT -> 8000
        }
        val timeBonus = Math.max(0L, 2000 - time * 2).toInt()
        val mistakePenalty = mistakes * 100
        return Math.max(100, base + timeBonus - mistakePenalty)
    }

    suspend fun initializeAchievements(achievements: List<Achievement>) {
        achievementDao.insertAchievements(achievements)
    }

    suspend fun updateAchievementProgress(id: String, progress: Int) {
        // This would be called to update progress on specific achievements
        // Actual implementation would need to fetch first, then update
    }

    suspend fun getDailyChallenge(date: Int): DailyChallenge? {
        return dailyChallengeDao.getChallenge(date)
    }

    suspend fun saveDailyChallenge(challenge: DailyChallenge) {
        dailyChallengeDao.insertChallenge(challenge)
    }

    suspend fun saveCurrentGame(
        puzzle: SudokuBoard,
        solution: SudokuBoard,
        difficulty: Difficulty,
        timerSeconds: Long,
        mistakes: Int,
        maxMistakes: Int,
        hintsRemaining: Int,
        isDailyChallenge: Boolean,
        selectedCell: Pair<Int, Int>? = null
    ) {
        val difficultyKey = if (isDailyChallenge) "DAILY" else difficulty.name
        val savedGame = SavedGame(
            difficulty = difficultyKey,
            puzzleJson = gson.toJson(puzzle),
            solutionJson = gson.toJson(solution),
            timerSeconds = timerSeconds,
            mistakes = mistakes,
            maxMistakes = maxMistakes,
            hintsRemaining = hintsRemaining,
            isDailyChallenge = isDailyChallenge,
            lastPlayedTimestamp = System.currentTimeMillis(),
            selectedCellRow = selectedCell?.first,
            selectedCellCol = selectedCell?.second
        )
        gameDao.saveGame(savedGame)
    }

    suspend fun getSavedGame(difficulty: Difficulty, isDaily: Boolean = false): SavedGame? {
        val key = if (isDaily) "DAILY" else difficulty.name
        return gameDao.getSavedGame(key)
    }

    suspend fun getLatestSavedGame(): SavedGame? = gameDao.getLatestSavedGame()

    suspend fun deleteSavedGame(difficulty: Difficulty, isDaily: Boolean = false) {
        val key = if (isDaily) "DAILY" else difficulty.name
        gameDao.deleteSavedGame(key)
    }
}
