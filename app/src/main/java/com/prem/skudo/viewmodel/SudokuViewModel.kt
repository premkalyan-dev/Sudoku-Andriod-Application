package com.prem.skudo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.prem.skudo.domain.SudokuGenerator
import com.prem.skudo.model.Difficulty
import com.prem.skudo.model.SudokuBoard
import com.prem.skudo.repository.SudokuRepository
import com.prem.skudo.repository.UserRepository
import com.prem.skudo.database.AppDatabase
import com.prem.skudo.utils.TimerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.collections.ArrayDeque

class SudokuViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SudokuRepository(application)
    private val userRepository = UserRepository(application)
    private val settingsRepository = com.prem.skudo.repository.SettingsRepository(application)
    private val rewardRepository = com.prem.skudo.repository.RewardRepository(application)
    private val generator = SudokuGenerator()
    private val timerManager = TimerManager()
    private val gson = Gson()
    private val soundManager = com.prem.skudo.utils.SoundManager(application)
    private val hapticManager = com.prem.skudo.utils.HapticManager(application)
    
    private val _uiState = MutableStateFlow(GameState())
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    // Timer is decoupled from GameState so the board/controls don't recompose every second.
    // Only UI components that display the timer need to observe this.
    private val _timerSeconds = MutableStateFlow(0L)
    val timerSeconds: StateFlow<Long> = _timerSeconds.asStateFlow()

    private val undoStack = ArrayDeque<SudokuBoard>()
    private val redoStack = ArrayDeque<SudokuBoard>()
    private var autoSaveJob: kotlinx.coroutines.Job? = null

    init {
        // Load settings from SettingsRepository
        settingsRepository.autoCheck.onEach { autoCheck ->
            _uiState.update { it.copy(autoCheck = autoCheck) }
        }.launchIn(viewModelScope)
        
        settingsRepository.highlightSameNumbers.onEach { highlight ->
            _uiState.update { it.copy(highlightIdentical = highlight) }
        }.launchIn(viewModelScope)

        settingsRepository.autoRemoveNotes.onEach { autoRemove ->
            _uiState.update { it.copy(autoRemoveNotes = autoRemove) }
        }.launchIn(viewModelScope)

        repository.mistakeLimit.onEach { limit ->
            _uiState.update { it.copy(maxMistakes = limit) }
        }.launchIn(viewModelScope)

        settingsRepository.boardStyle.onEach { style ->
            _uiState.update { it.copy(boardStyle = style) }
        }.launchIn(viewModelScope)

        userRepository.userProfile.onEach { profile ->
            _uiState.update { it.copy(userGems = profile?.gems ?: 0) }
        }.launchIn(viewModelScope)
    }

    fun startNewGame(difficulty: Difficulty, isDaily: Boolean = false) {
        forceStartNewGame(difficulty, isDaily)
    }

    fun restartGame() {
        _uiState.update { it.copy(showRestartConfirmation = true) }
    }

    fun confirmRestart() {
        _uiState.update { it.copy(showRestartConfirmation = false, showLeaveDialog = false) }
        forceStartNewGame(_uiState.value.difficulty, _uiState.value.isDailyChallenge)
    }

    fun cancelRestart() {
        _uiState.update { it.copy(showRestartConfirmation = false) }
    }

    fun forceStartNewGame(difficulty: Difficulty, isDaily: Boolean = false) {
        _uiState.update { it.copy(isLoading = true, difficulty = difficulty, isDailyChallenge = isDaily, showStartNewConfirmation = false) }
        _timerSeconds.value = 0
        viewModelScope.launch(Dispatchers.Default) {
            val (puzzle, solution) = generator.generate(difficulty)
            undoStack.clear()
            redoStack.clear()
            
            val profile = userRepository.getOrCreateProfile()
            
            _uiState.update { 
                it.copy(
                    puzzle = puzzle,
                    solution = solution,
                    isLoading = false,
                    isGameOver = false,
                    isVictory = false,
                    isPaused = false,
                    selectedCell = null,
                    mistakes = 0,
                    hintsRemaining = profile.hints,
                    showLeaveDialog = false
                )
            }
            startTimer()
            autoSave(immediate = true)
        }
    }

    fun continueGame(difficulty: Difficulty, isDaily: Boolean = false) {
        _uiState.update { it.copy(isLoading = true, showStartNewConfirmation = false) }
        _timerSeconds.value = 0
        viewModelScope.launch(Dispatchers.IO) {
            val saved = repository.getSavedGame(difficulty, isDaily) ?: return@launch
            val puzzle = gson.fromJson(saved.puzzleJson, SudokuBoard::class.java)
            val solution = gson.fromJson(saved.solutionJson, SudokuBoard::class.java)
            
            undoStack.clear()
            redoStack.clear()

            _uiState.update { 
                it.copy(
                    puzzle = puzzle,
                    solution = solution,
                    difficulty = if (saved.isDailyChallenge) Difficulty.EXPERT else Difficulty.valueOf(saved.difficulty),
                    mistakes = saved.mistakes,
                    maxMistakes = saved.maxMistakes,
                    hintsRemaining = saved.hintsRemaining,
                    isDailyChallenge = saved.isDailyChallenge,
                    isLoading = false,
                    isGameOver = false,
                    isVictory = false,
                    isPaused = false,
                    selectedCell = if (saved.selectedCellRow != null && saved.selectedCellCol != null) 
                                    saved.selectedCellRow to saved.selectedCellCol else null,
                    lastPlayedTimestamp = saved.lastPlayedTimestamp
                )
            }
            _timerSeconds.value = saved.timerSeconds
            startTimer()
            _uiState.update { updateHighlights(it) }
        }
    }

    private fun startTimer() {
        timerManager.start(_timerSeconds.value) { seconds ->
            // Update only the separate timer flow — NOT _uiState.
            // This prevents the entire game screen from recomposing every second.
            _timerSeconds.value = seconds
            if ((seconds % 30) == 0L) autoSave()
        }
    }

    fun pauseGame() {
        timerManager.stop()
        _uiState.update { it.copy(isPaused = true, showLeaveDialog = true) }
        autoSave(immediate = true)
    }

    fun resumeGame() {
        _uiState.update { it.copy(isPaused = false, showLeaveDialog = false) }
        startTimer()
    }

    fun saveAndExit() {
        timerManager.stop()
        autoSave(immediate = true)
        // Navigation is handled by the screen
    }

    fun discardGame() {
        _uiState.update { it.copy(showDiscardConfirmation = true) }
    }

    fun confirmDiscard() {
        timerManager.stop()
        viewModelScope.launch {
            repository.deleteSavedGame(_uiState.value.difficulty, _uiState.value.isDailyChallenge)
            _uiState.update { it.copy(isGameOver = true, showDiscardConfirmation = false, showLeaveDialog = false) }
        }
    }

    fun cancelDiscard() {
        _uiState.update { it.copy(showDiscardConfirmation = false) }
    }

    fun cancelStartNew() {
        _uiState.update { it.copy(showStartNewConfirmation = false) }
    }

    fun selectCell(row: Int, col: Int) {
        if (_uiState.value.isPaused || _uiState.value.isGameOver) return
        if (_uiState.value.selectedCell == row to col) return
        hapticManager.vibrate(com.prem.skudo.utils.HapticType.LIGHT)

        _uiState.update { 
            val cellValue = it.puzzle[row, col].value
            val newState = it.copy(
                selectedCell = row to col,
                selectedNumber = cellValue
            )
            updateHighlights(newState)
        }
        // No autoSave on cell selection — saves happen on actual moves (enterNumber, erase, hint, undo/redo)
    }

    fun selectNumber(num: Int) {
        if (_uiState.value.isPaused || _uiState.value.isGameOver) return
        val currentState = _uiState.value
        if (currentState.selectedCell != null) {
            // Cell-First: If a cell is selected, fill it
            enterNumber(num)
        } else {
            _uiState.update { 
                val newNum = if (it.selectedNumber == num) null else num
                val newState = it.copy(selectedNumber = newNum) 
                updateHighlights(newState)
            }
            hapticManager.vibrate(com.prem.skudo.utils.HapticType.LIGHT)
        }
    }

    fun onCellLongClick(row: Int, col: Int) {
        if (_uiState.value.isPaused || _uiState.value.isGameOver) return
        selectCell(row, col)
    }

    fun toggleNotesMode() {
        _uiState.update { it.copy(isNotesMode = !it.isNotesMode) }
    }

    fun enterNumber(num: Int) {
        if (_uiState.value.isPaused || _uiState.value.isGameOver) return

        val currentState = _uiState.value
        val (row, col) = currentState.selectedCell ?: run {
            // No cell selected — just update the selected number for digit-first mode
            _uiState.update { it.copy(selectedNumber = num) }
            return
        }
        val currentCell = currentState.puzzle[row, col]

        if ((currentCell.isClue) || (currentCell.value == num)) {
            _uiState.update { it.copy(selectedNumber = num) }
            return
        }

        hapticManager.vibrate(com.prem.skudo.utils.HapticType.LIGHT)

        if (currentState.isNotesMode) {
            // Also update selectedNumber in the same conceptual action
            _uiState.update { it.copy(selectedNumber = num) }
            updateNotes(row, col, num)
        } else {
            saveToUndoStack()
            
            // Single atomic state update — value placement + highlights + note removal all in one pass
            _uiState.update { state ->
                val correctValue = state.solution[row, col].value
                val actuallyCorrect = num == correctValue
                val isCorrect = !state.autoCheck || actuallyCorrect
                
                var newMistakes = state.mistakes
                
                if (!actuallyCorrect && state.autoCheck) {
                    newMistakes++
                    hapticManager.vibrate(com.prem.skudo.utils.HapticType.STRONG)
                    if (state.maxMistakes in 1..newMistakes) {
                        timerManager.stop()
                        return@update state.copy(
                            selectedNumber = num,
                            mistakes = newMistakes,
                            showContinueDialog = true
                        )
                    }
                }

                // Selected cell info for highlights
                val selR = row
                val selC = col
                val selectedValue = num // The value being placed
                val shouldAutoRemoveNotes = state.autoRemoveNotes && actuallyCorrect

                // Single unified pass over all 81 cells:
                // - Place digit at (row, col)
                // - Update highlight/related/matching states
                // - Remove notes from related cells if needed
                val newCells = state.puzzle.cells.mapIndexed { ri, rowList ->
                    rowList.mapIndexed { ci, cell ->
                        val isTarget = ri == row && ci == col
                        
                        // 1. Place the digit
                        val updatedCell = if (isTarget) {
                            cell.copy(value = num, isValid = isCorrect, notes = emptySet())
                        } else cell

                        // 2. Compute highlights
                        val newHighlighted = ri == selR && ci == selC
                        val newRelated = state.highlightRelated && (ri == selR || ci == selC || (ri / 3 == selR / 3 && ci / 3 == selC / 3))
                        val newMatching = state.highlightIdentical && updatedCell.value == selectedValue

                        // 3. Auto-remove notes from related cells
                        val updatedNotes = if (!isTarget && shouldAutoRemoveNotes) {
                            val inSameBox = (ri / 3 == row / 3 && ci / 3 == col / 3)
                            if (ri == row || ci == col || inSameBox) {
                                updatedCell.notes - num
                            } else updatedCell.notes
                        } else updatedCell.notes

                        // Only create a new copy if something actually changed
                        if (updatedCell.isHighlighted == newHighlighted &&
                            updatedCell.isRelated == newRelated &&
                            updatedCell.isMatchingNumber == newMatching &&
                            updatedCell.notes == updatedNotes) {
                            updatedCell
                        } else {
                            updatedCell.copy(
                                isHighlighted = newHighlighted,
                                isRelated = newRelated,
                                isMatchingNumber = newMatching,
                                notes = updatedNotes
                            )
                        }
                    }
                }
                
                val finalState = state.copy(
                    puzzle = SudokuBoard(newCells),
                    selectedNumber = num,
                    mistakes = newMistakes,
                )
                checkCompletions(finalState, row, col)
            }
            checkVictory()
        }
        autoSave()
    }

    private fun checkCompletions(state: GameState, row: Int, col: Int): GameState {
        val puzzle = state.puzzle
        
        val isRowComplete = (0..8).all { c -> puzzle[row, c].value != null && puzzle[row, c].isValid }
        val isColComplete = (0..8).all { r -> puzzle[r, col].value != null && puzzle[r, col].isValid }
        
        val boxRow = (row / 3) * 3
        val boxCol = (col / 3) * 3
        val boxIndex = (row / 3) * 3 + (col / 3)
        val isBoxComplete = (boxRow until boxRow + 3).all { r ->
            (boxCol until boxCol + 3).all { c ->
                puzzle[r, c].value != null && puzzle[r, c].isValid
            }
        }

        val newRows = if (isRowComplete) state.lastCompletedRows + row else state.lastCompletedRows
        val newCols = if (isColComplete) state.lastCompletedCols + col else state.lastCompletedCols
        val newBoxes = if (isBoxComplete) state.lastCompletedBoxes + boxIndex else state.lastCompletedBoxes

        if (isRowComplete || isColComplete || isBoxComplete) {
            hapticManager.vibrate(com.prem.skudo.utils.HapticType.MEDIUM)
            
            val animatingCells = puzzle.cells.mapIndexed { ri, rowList ->
                rowList.mapIndexed { ci, cell ->
                    val inCompletedBox = isBoxComplete && (ri / 3 == row / 3 && ci / 3 == col / 3)
                    if ((isRowComplete && ri == row) || (isColComplete && ci == col) || inCompletedBox) {
                        cell.copy(isAnimatingCompletion = true)
                    } else cell
                }
            }
            
            val animatingState = state.copy(
                puzzle = SudokuBoard(animatingCells),
                lastCompletedRows = newRows,
                lastCompletedCols = newCols,
                lastCompletedBoxes = newBoxes
            )
            
            viewModelScope.launch {
                delay(1000)
                _uiState.update { currentState ->
                    val clearedCells = currentState.puzzle.cells.map { r ->
                        r.map { it.copy(isAnimatingCompletion = false) }
                    }
                    currentState.copy(puzzle = SudokuBoard(clearedCells))
                }
            }
            return animatingState
        }

        return state.copy(
            lastCompletedRows = newRows,
            lastCompletedCols = newCols,
            lastCompletedBoxes = newBoxes
        )
    }

    private fun updateNotes(row: Int, col: Int, num: Int) {
        _uiState.update { state ->
            val cell = state.puzzle[row, col]
            val newNotes = if (cell.notes.contains(num)) {
                cell.notes - num
            } else {
                cell.notes + num
            }
            val newCells = state.puzzle.cells.mapIndexed { ri, rowList ->
                rowList.mapIndexed { ci, sudokuCell ->
                    if ((ri == row) && (ci == col)) sudokuCell.copy(notes = newNotes) else sudokuCell
                }
            }
            state.copy(puzzle = SudokuBoard(newCells))
        }
    }

    fun eraseCell() {
        val state = _uiState.value
        if (state.isPaused || state.isGameOver) return
        val (row, col) = state.selectedCell ?: return
        if (!state.puzzle[row, col].isClue) {
            hapticManager.vibrate(com.prem.skudo.utils.HapticType.LIGHT)
            saveToUndoStack()
            _uiState.update { currentState ->
                val newState = updateCellInternal(currentState, row, col, null)
                checkCompletions(newState, row, col)
            }
            autoSave()
        }
    }

    fun useHint() {
        val state = _uiState.value
        if (state.isPaused || state.isGameOver || state.hintsRemaining <= 0) return
        
        hapticManager.vibrate(com.prem.skudo.utils.HapticType.MEDIUM)
        
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (r in 0..8) {
            for (c in 0..8) {
                val cell = state.puzzle[r, c]
                if (!cell.isClue && (cell.value == null || cell.value != state.solution[r, c].value)) {
                    emptyCells.add(r to c)
                }
            }
        }

        if (emptyCells.isNotEmpty()) {
            val (r, c) = emptyCells.random()
            saveToUndoStack()
            
            viewModelScope.launch {
                val profile = userRepository.getOrCreateProfile()
                if (profile.hints > 0) {
                    val updatedProfile = profile.copy(hints = profile.hints - 1)
                    AppDatabase.getDatabase(getApplication<Application>()).userDao().updateProfile(updatedProfile)
                }
            }

            _uiState.update { currentState ->
                val updatedHints = currentState.copy(hintsRemaining = currentState.hintsRemaining - 1)
                val newState = updateCellInternal(updatedHints, r, c, currentState.solution[r, c].value)
                checkCompletions(newState, r, c)
            }
            checkVictory()
            autoSave()
        }
    }

    private fun updateCellInternal(state: GameState, row: Int, col: Int, value: Int?): GameState {
        val newCells = state.puzzle.cells.mapIndexed { ri, rowList ->
            rowList.mapIndexed { ci, cell ->
                if (ri == row && ci == col) {
                    val isCorrect = !state.autoCheck || value == null || value == state.solution[ri, ci].value
                    cell.copy(value = value, isValid = isCorrect, notes = emptySet())
                } else cell
            }
        }
        val newState = state.copy(puzzle = SudokuBoard(newCells), selectedNumber = value)
        return validateAndHighlight(newState)
    }

    private fun validateAndHighlight(state: GameState): GameState {
        val puzzle = state.puzzle
        val solution = state.solution
        
        val newCells = puzzle.cells.mapIndexed { ri, rowList ->
            rowList.mapIndexed { ci, cell ->
                val value = cell.value
                val isValid = if (!state.autoCheck || value == null) true else value == solution[ri, ci].value
                if (cell.isValid == isValid) cell else cell.copy(isValid = isValid)
            }
        }
        val updatedState = state.copy(puzzle = SudokuBoard(newCells))
        return updateHighlights(updatedState)
    }

    private fun updateHighlights(state: GameState): GameState {
        val selectedCell = state.selectedCell
        val selR = selectedCell?.first
        val selC = selectedCell?.second
        val selectedValue = if (selectedCell != null) state.puzzle[selR!!, selC!!].value else state.selectedNumber

        val newCells = state.puzzle.cells.mapIndexed { ri, rowList ->
            rowList.mapIndexed { ci, cell ->
                val newHighlighted = selectedCell != null && ri == selR && ci == selC
                val newRelated = selectedCell != null && state.highlightRelated && (ri == selR || ci == selC || (ri / 3 == selR!! / 3 && ci / 3 == selC!! / 3))
                val newMatching = state.highlightIdentical && selectedValue != null && cell.value == selectedValue

                if (cell.isHighlighted == newHighlighted && 
                    cell.isRelated == newRelated && 
                    cell.isMatchingNumber == newMatching) {
                    cell
                } else {
                    cell.copy(
                        isHighlighted = newHighlighted,
                        isRelated = newRelated,
                        isMatchingNumber = newMatching
                    )
                }
            }
        }
        return state.copy(puzzle = SudokuBoard(newCells))
    }

    private fun checkVictory() {
        val state = _uiState.value
        if (state.isGameOver) return

        val isSolved = state.puzzle.cells.all { row ->
            row.all { cell ->
                cell.value != null && cell.value == state.solution[cell.row, cell.col].value
            }
        }
        if (isSolved) {
            timerManager.stop()
            val finalTime = _timerSeconds.value
            hapticManager.vibrate(com.prem.skudo.utils.HapticType.SUCCESS)
            
            _uiState.update { it.copy(isGameOver = true, isVictory = true, isLoading = true, timerSeconds = finalTime) }

            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val hintsUsed = 3 - state.hintsRemaining
                    
                    val rewards = rewardRepository.processGameResult(
                        state.difficulty,
                        finalTime,
                        state.mistakes,
                        hintsUsed,
                        isWin = true,
                        isDailyChallenge = state.isDailyChallenge
                    )

                    repository.updateStats(state.difficulty, isWin = true, finalTime)
                    repository.addGameToHistory(state.difficulty, finalTime, state.mistakes, hintsUsed, true, state.isDailyChallenge)
                    
                    val allAchievements = repository.allAchievements.first()
                    var totalWins = 0
                    for (diff in Difficulty.entries) {
                        totalWins += repository.getStats(diff).gamesWon
                    }
                    
                    val newlyUnlocked = com.prem.skudo.utils.AchievementManager.checkAchievements(
                        allAchievements, state.difficulty, finalTime, state.mistakes, hintsUsed, totalWins
                    )
                    
                    newlyUnlocked.forEach { achievement ->
                        repository.initializeAchievements(listOf(achievement))
                    }

                    if (state.isDailyChallenge) {
                        val today = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(java.util.Date()).toInt()
                        repository.updateDailyStreak(today)
                        repository.saveDailyChallenge(com.prem.skudo.database.DailyChallenge(
                            date = today,
                            difficulty = state.difficulty.name,
                            isCompleted = true,
                            completionTimeSeconds = finalTime,
                            completionTimestamp = System.currentTimeMillis()
                        ))
                    }
                    
                    repository.deleteSavedGame(state.difficulty, state.isDailyChallenge)
                    
                    _uiState.update { 
                        it.copy(
                            isVictory = true, 
                            isLoading = false,
                            xpEarned = rewards.xp, 
                            coinsEarned = rewards.coins,
                            gemsEarned = rewards.gems,
                            newLevel = rewards.newLevel,
                            leveledUp = rewards.leveledUp,
                            isPerfect = state.mistakes == 0 && hintsUsed == 0,
                            unlockedAchievements = newlyUnlocked.filter { a -> a.isUnlocked }
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    _uiState.update { it.copy(isLoading = false, isVictory = true) }
                }
            }
        }
    }

    private fun autoSave(immediate: Boolean = false) {
        val state = _uiState.value
        if (state.isGameOver || state.isLoading) return
        
        autoSaveJob?.cancel()
        
        val currentTimerSeconds = _timerSeconds.value
        val saveBlock: suspend () -> Unit = {
            repository.saveCurrentGame(
                state.puzzle, state.solution, state.difficulty,
                currentTimerSeconds, state.mistakes, state.maxMistakes, 
                state.hintsRemaining, state.isDailyChallenge,
                state.selectedCell
            )
        }

        if (immediate) {
            viewModelScope.launch(Dispatchers.IO) { saveBlock() }
        } else {
            autoSaveJob = viewModelScope.launch(Dispatchers.IO) {
                delay(2000)
                saveBlock()
            }
        }
    }

    private fun saveToUndoStack() {
        undoStack.addLast(_uiState.value.puzzle)
        redoStack.clear()
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            hapticManager.vibrate(com.prem.skudo.utils.HapticType.LIGHT)
            redoStack.addLast(_uiState.value.puzzle)
            val previousBoard = undoStack.removeLast()
            _uiState.update { state ->
                validateAndHighlight(state.copy(puzzle = previousBoard))
            }
            checkVictory()
            autoSave()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            hapticManager.vibrate(com.prem.skudo.utils.HapticType.LIGHT)
            undoStack.addLast(_uiState.value.puzzle)
            val nextBoard = redoStack.removeLast()
            _uiState.update { state ->
                validateAndHighlight(state.copy(puzzle = nextBoard))
            }
            checkVictory()
            autoSave()
        }
    }

    fun resetPuzzle() {
        saveToUndoStack()
        _uiState.update { state ->
            val newCells = state.puzzle.cells.map { row ->
                row.map { if (it.isClue) it else it.copy(value = null, isValid = true, notes = emptySet()) }
            }
            state.copy(
                puzzle = SudokuBoard(newCells),
                isGameOver = false,
                isVictory = false,
                isPaused = false,
                mistakes = 0
            )
        }
        _timerSeconds.value = 0
        startTimer()
        autoSave()
    }

    fun solvePuzzle() {
        timerManager.stop()
        _uiState.update { it.copy(puzzle = it.solution, isGameOver = true, isVictory = true) }
        viewModelScope.launch { repository.deleteSavedGame(_uiState.value.difficulty, _uiState.value.isDailyChallenge) }
    }

    fun checkBoard() {
        _uiState.update { state ->
            val newCells = state.puzzle.cells.mapIndexed { ri, rowList ->
                rowList.mapIndexed { ci, cell ->
                    val isCorrect = cell.value == null || cell.value == state.solution[ri, ci].value
                    cell.copy(isValid = isCorrect)
                }
            }
            state.copy(puzzle = SudokuBoard(newCells))
        }
    }

    fun setAutoCheck(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateAutoCheck(enabled)
        }
    }

    fun setMistakeLimit(limit: Int) {
        viewModelScope.launch {
            repository.updateMistakeLimit(limit)
        }
    }

    fun continueWithGems() {
        val currentState = _uiState.value
        if (currentState.userGems >= 1) {
            viewModelScope.launch {
                val economyRepository = com.prem.skudo.repository.EconomyRepository(getApplication())
                val success = economyRepository.spendGems(1)
                if (success) {
                    _uiState.update { 
                        it.copy(
                            mistakes = it.mistakes - 1,
                            isGameOver = false,
                            showContinueDialog = false
                        )
                    }
                    startTimer()
                    autoSave()
                }
            }
        }
    }

    fun gameOver() {
        val currentState = _uiState.value
        val finalTime = _timerSeconds.value
        _uiState.update { it.copy(isGameOver = true, showContinueDialog = false) }
        viewModelScope.launch {
            repository.updateStats(currentState.difficulty, isWin = false, finalTime)
            userRepository.addGameResults(
                currentState.difficulty,
                finalTime,
                currentState.mistakes,
                3 - currentState.hintsRemaining,
                isWin = false
            )
            repository.addGameToHistory(
                currentState.difficulty,
                finalTime,
                currentState.mistakes,
                3 - currentState.hintsRemaining,
                isWin = false,
                isDaily = currentState.isDailyChallenge
            )
            repository.deleteSavedGame(currentState.difficulty, currentState.isDailyChallenge)
        }
    }

    fun onLifecyclePause() {
        autoSave(immediate = true)
        timerManager.stop()
    }

    fun onLifecycleResume() {
        if (!_uiState.value.isPaused && !_uiState.value.isGameOver) {
            startTimer()
        }
    }

    override fun onCleared() {
        timerManager.stop()
    }
}
