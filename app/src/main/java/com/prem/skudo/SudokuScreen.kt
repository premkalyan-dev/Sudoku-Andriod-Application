package com.prem.skudo

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prem.skudo.model.Difficulty
import com.prem.skudo.model.SudokuBoard
import com.prem.skudo.ui.*
import com.prem.skudo.ui.theme.*
import com.prem.skudo.viewmodel.GameState
import com.prem.skudo.viewmodel.SudokuViewModel

@Composable
fun SudokuGameScreen(
    difficulty: Difficulty,
    onBack: () -> Unit,
    viewModel: SudokuViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.onLifecyclePause()
            } else if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onLifecycleResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    SudokuScreenContent(
        uiState = uiState,
        onBack = onBack,
        onPause = viewModel::pauseGame,
        onResume = viewModel::resumeGame,
        onSaveAndExit = {
            viewModel.saveAndExit()
            onBack()
        },
        onDiscard = viewModel::discardGame,
        onConfirmDiscard = {
            viewModel.confirmDiscard()
            onBack()
        },
        onCancelDiscard = viewModel::cancelDiscard,
        onRestartGame = viewModel::restartGame,
        onConfirmRestart = viewModel::confirmRestart,
        onCancelRestart = viewModel::cancelRestart,
        onStartNewGame = { viewModel.forceStartNewGame(uiState.difficulty, uiState.isDailyChallenge) },
        onCancelStartNew = {
            viewModel.cancelStartNew()
            onBack()
        },
        onContinueGame = { viewModel.continueGame(uiState.difficulty, uiState.isDailyChallenge) },
        onCellClick = viewModel::selectCell,
        onUndo = viewModel::undo,
        onErase = viewModel::eraseCell,
        onToggleNotes = viewModel::toggleNotesMode,
        onHint = viewModel::useHint,
        onNumberClick = viewModel::enterNumber,
        onNewGameClick = { viewModel.startNewGame(uiState.difficulty) },
        onContinueWithGems = viewModel::continueWithGems,
        onGameOver = viewModel::gameOver
    )
}

@Composable
fun SudokuScreenContent(
    uiState: GameState,
    onBack: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onSaveAndExit: () -> Unit,
    onDiscard: () -> Unit,
    onConfirmDiscard: () -> Unit,
    onCancelDiscard: () -> Unit,
    onRestartGame: () -> Unit,
    onConfirmRestart: () -> Unit,
    onCancelRestart: () -> Unit,
    onStartNewGame: () -> Unit,
    onCancelStartNew: () -> Unit,
    onContinueGame: () -> Unit,
    onCellClick: (Int, Int) -> Unit,
    onUndo: () -> Unit,
    onErase: () -> Unit,
    onToggleNotes: () -> Unit,
    onHint: () -> Unit,
    onNumberClick: (Int) -> Unit,
    onNewGameClick: () -> Unit,
    onContinueWithGems: () -> Unit,
    onGameOver: () -> Unit
) {
    BackHandler {
        onSaveAndExit()
    }

    if (uiState.showLeaveDialog) {
        PauseMenuDialog(
            onResume = onResume,
            onSaveAndExit = onSaveAndExit,
            onRestartGame = onRestartGame,
            difficulty = uiState.difficulty,
            timerSeconds = uiState.timerSeconds
        )
    }

    if (uiState.showDiscardConfirmation) {
        DiscardConfirmationDialog(
            onConfirm = onConfirmDiscard,
            onCancel = onCancelDiscard
        )
    }

    if (uiState.showRestartConfirmation) {
        RestartConfirmationDialog(
            onConfirm = onConfirmRestart,
            onCancel = onCancelRestart
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Premium Compact Header
            PremiumGameHeader(
                uiState = uiState,
                onBack = onSaveAndExit,
                onPauseToggle = onPause
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Sudoku Board (Hero of the screen)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                SudokuBoardView(
                    board = uiState.puzzle,
                    onCellClick = onCellClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    boardStyle = uiState.boardStyle
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Compact Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompactActionButton(Icons.AutoMirrored.Filled.Undo, stringResource(R.string.undo), onUndo)
                CompactActionButton(Icons.Default.Delete, stringResource(R.string.erase), onErase)
                CompactActionButton(
                    icon = if (uiState.isNotesMode) Icons.Default.Edit else Icons.Outlined.Edit,
                    label = stringResource(R.string.notes),
                    onClick = onToggleNotes,
                    active = uiState.isNotesMode
                )
                CompactActionButton(
                    icon = Icons.Default.Lightbulb,
                    label = stringResource(R.string.hint),
                    onClick = onHint,
                    badge = if (uiState.hintsRemaining > 0) uiState.hintsRemaining.toString() else null
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Premium Number Pad with Remaining Counts
            PremiumNumberPad(
                board = uiState.puzzle,
                onNumberClick = onNumberClick,
                isNotesMode = uiState.isNotesMode
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        if (uiState.isVictory) {
            PremiumRewardScreen(state = uiState, onPlayAgain = onNewGameClick, onHome = onBack)
        }

        if (uiState.showContinueDialog) {
            ContinueGameDialog(
                gems = uiState.userGems,
                onContinue = onContinueWithGems,
                onCancel = onGameOver
            )
        }

        if (uiState.isGameOver && !uiState.isVictory && !uiState.showLeaveDialog) {
            GameOverDialog(maxMistakes = uiState.maxMistakes, onRestart = onNewGameClick, onHome = onBack)
        }
    }
}

@Composable
fun PauseMenuDialog(
    onResume: () -> Unit,
    onSaveAndExit: () -> Unit,
    onRestartGame: () -> Unit,
    difficulty: Difficulty,
    timerSeconds: Long
) {
    AlertDialog(
        onDismissRequest = onResume,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Pause,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "GAME PAUSED",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${difficulty.name} • ${formatTime(timerSeconds)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onResume,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, null)
                    Spacer(Modifier.width(8.dp))
                    Text("RESUME", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onRestartGame,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Icon(Icons.Default.Refresh, null)
                    Spacer(Modifier.width(8.dp))
                    Text("RESTART GAME", fontWeight = FontWeight.Bold)
                }
                
                OutlinedButton(
                    onClick = onSaveAndExit,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, null)
                    Spacer(Modifier.width(8.dp))
                    Text("EXIT TO HOME", fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

@Composable
fun RestartConfirmationDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Restart this game?") },
        text = { Text("All progress in this game will be lost.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("RESTART", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("CANCEL")
            }
        }
    )
}

@Composable
fun DiscardConfirmationDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Discard this game?") },
        text = { Text("Your progress will be permanently deleted.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("DISCARD", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("CANCEL")
            }
        }
    )
}

@Composable
fun PremiumGameHeader(
    uiState: GameState,
    onBack: () -> Unit,
    onPauseToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Back Button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(44.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onSurface)
        }

        // Stats Container
        Surface(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Difficulty & Title
                Column {
                    Text(
                        text = stringResource(uiState.difficulty.resId).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = formatTime(uiState.timerSeconds),
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Divider
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(MaterialTheme.colorScheme.outlineVariant))

                // Mistakes
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(uiState.maxMistakes) { index ->
                        val isLost = index < uiState.mistakes
                        Icon(
                            imageVector = if (isLost) Icons.Default.Close else Icons.Default.Favorite,
                            contentDescription = null,
                            tint = if (isLost) WrongRed else EasyGreen,
                            modifier = Modifier.size(14.dp).padding(horizontal = 1.dp)
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${uiState.mistakes}/${uiState.maxMistakes}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.mistakes >= uiState.maxMistakes - 1) WrongRed else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Pause Button
        IconButton(
            onClick = onPauseToggle,
            modifier = Modifier
                .size(44.dp)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
        ) {
            Icon(
                imageVector = if (uiState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun CompactActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    badge: String? = null,
    active: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.92f else 1f)
    val haptic = LocalHapticFeedback.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Surface(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
                interactionSource = interactionSource,
                shape = RoundedCornerShape(16.dp),
                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .size(48.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale),
                shadowElevation = if (isPressed) 0.dp else 2.dp,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            badge?.let {
                Surface(
                    color = MaterialTheme.colorScheme.error,
                    shape = CircleShape,
                    modifier = Modifier.offset(x = 6.dp, y = (-6).dp)
                ) {
                    Text(
                        text = it,
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (active) MaterialTheme.colorScheme.primary else TextMuted,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun PremiumNumberPad(
    board: SudokuBoard,
    onNumberClick: (Int) -> Unit,
    isNotesMode: Boolean
) {
    // Calculate remaining numbers
    val counts = remember(board) {
        val c = IntArray(10) { 0 }
        board.cells.flatten().forEach { cell ->
            cell.value?.let { if (it in 1..9) c[it]++ }
        }
        c
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        (1..9).forEach { num ->
            val count = counts[num]
            val isCompleted = count >= 9
            
            PremiumNumberKey(
                num = num,
                count = 9 - count,
                isCompleted = isCompleted,
                modifier = Modifier.weight(1f),
                onClick = { if (!isCompleted) onNumberClick(num) }
            )
        }
    }
}

@Composable
fun PremiumNumberKey(
    num: Int,
    count: Int,
    isCompleted: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.88f else 1f)
    val haptic = LocalHapticFeedback.current

    val bgColor by animateColorAsState(
        if (isCompleted) EasyGreen.copy(alpha = 0.1f) 
        else if (isPressed) MaterialTheme.colorScheme.primaryContainer 
        else MaterialTheme.colorScheme.surface
    )
    
    val contentColor by animateColorAsState(
        if (isCompleted) EasyGreen.copy(alpha = 0.5f) 
        else MaterialTheme.colorScheme.primary
    )

    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        interactionSource = interactionSource,
        modifier = modifier
            .height(60.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale),
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        shadowElevation = if (isPressed || isCompleted) 0.dp else 3.dp,
        border = BorderStroke(
            1.dp, 
            if (isCompleted) EasyGreen.copy(alpha = 0.3f) 
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isCompleted) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp), tint = EasyGreen)
                } else {
                    Text(
                        text = num.toString(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = contentColor
                    )
                    Text(
                        text = count.toString(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun ContinueGameDialog(
    gems: Long,
    onContinue: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = MaterialTheme.colorScheme.surface,
        icon = { Icon(Icons.Default.Diamond, null, Modifier.size(64.dp), GemCyan) },
        title = { Text(stringResource(R.string.out_of_mistakes), fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.continue_with_gems))
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.you_have_gems, gems.toInt()),
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onContinue,
                enabled = gems >= 1,
                colors = ButtonDefaults.buttonColors(containerColor = GemCyan)
            ) {
                Text(stringResource(R.string.continue_1_gem), color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.no_thanks), color = TextMuted)
            }
        }
    )
}

@Composable
fun GameOverDialog(maxMistakes: Int, onRestart: () -> Unit, onHome: () -> Unit) {
    AlertDialog(
        onDismissRequest = { },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = WrongRed,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        icon = { Icon(Icons.Default.ErrorOutline, null, Modifier.size(64.dp), WrongRed) },
        title = { Text(stringResource(R.string.game_over), fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp) },
        text = { Text(stringResource(R.string.game_over_mistakes, maxMistakes), textAlign = TextAlign.Center) },
        confirmButton = {
            Button(onClick = onRestart, colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)) {
                Text(stringResource(R.string.restart), color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onHome, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)) {
                Text(stringResource(R.string.exit))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun SudokuScreenPreview() {
    SudokuTheme {
        SudokuScreenContent(
            uiState = GameState(
                puzzle = SudokuBoard(),
                difficulty = Difficulty.MEDIUM,
                timerSeconds = 125,
                mistakes = 1,
                maxMistakes = 3
            ),
            onBack = {},
            onPause = {},
            onResume = {},
            onSaveAndExit = {},
            onDiscard = {},
            onConfirmDiscard = {},
            onCancelDiscard = {},
            onStartNewGame = {},
            onCancelStartNew = {},
            onContinueGame = {},
            onCellClick = { _, _ -> },
            onUndo = {},
            onErase = {},
            onToggleNotes = {},
            onHint = {},
            onNumberClick = {},
            onNewGameClick = {},
            onContinueWithGems = {},
            onGameOver = {},
            onRestartGame = {},
            onConfirmRestart = {},
            onCancelRestart = {}
        )
    }
}
