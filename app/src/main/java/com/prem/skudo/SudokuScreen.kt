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
import androidx.compose.ui.unit.Dp
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
import kotlinx.coroutines.delay

/**
 * Redesigned Sudoku Game Screen
 * 
 * Significant UI Improvements:
 * 1. Visual Hierarchy: The Sudoku Board is now the "Hero" using weight-based layout to maximize size.
 * 2. Premium Aesthetics: Replaced flat surfaces with high-radius (24dp+) rounded cards and soft shadows.
 * 3. Compact Layout: Reduced large spacers (16dp) to a tighter hierarchy (4dp/8dp) for better space utilization.
 * 4. Unified Palette: Consistent use of MaterialTheme color scheme with subtle container alphas.
 * 5. Premium Number Pad: Circular-style keys with remaining counts, disabled states, and tactile scale feedback.
 * 6. Interactive Header: Merged Timer and settings into a clean, modern card.
 * 7. Accessibility: Large touch targets (48dp+) and improved contrast for status indicators.
 * 8. UX Flow: Back button now triggers Pause menu instead of instant exit, preventing progress loss.
 */
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
        onCellLongClick = viewModel::onCellLongClick,
        onUndo = viewModel::undo,
        onErase = viewModel::eraseCell,
        onToggleNotes = viewModel::toggleNotesMode,
        onHint = viewModel::useHint,
        onNumberClick = viewModel::selectNumber,
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
    onCellLongClick: (Int, Int) -> Unit,
    onUndo: () -> Unit,
    onErase: () -> Unit,
    onToggleNotes: () -> Unit,
    onHint: () -> Unit,
    onNumberClick: (Int) -> Unit,
    onNewGameClick: () -> Unit,
    onContinueWithGems: () -> Unit,
    onGameOver: () -> Unit
) {
    // Intercept back press to show Pause Menu instead of quitting
    BackHandler {
        onPause()
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
            // 1. Premium Header (Compact & Elegant)
            PremiumGameHeader(
                uiState = uiState,
                onBack = { onPause() },
                onPauseToggle = onPause
            )

            // 2. Main Stats Bar (Difficulty & Mistakes)
            GameStatsBar(uiState = uiState)

            // 3. Sudoku Board (The Hero)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                SudokuBoardView(
                    board = uiState.puzzle,
                    onCellClick = onCellClick,
                    onCellLongClick = onCellLongClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    boardStyle = uiState.boardStyle
                )
            }

            // 4. Compact Control Actions
            ActionButtonsRow(
                uiState = uiState,
                onUndo = onUndo,
                onErase = onErase,
                onToggleNotes = onToggleNotes,
                onHint = onHint
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 5. Premium Number Pad
            PremiumNumberPad(
                board = uiState.puzzle,
                selectedNumber = uiState.selectedNumber,
                onNumberClick = onNumberClick,
                isNotesMode = uiState.isNotesMode
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Overlays
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
fun PremiumGameHeader(
    uiState: GameState,
    onBack: () -> Unit,
    onPauseToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(44.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .shadow(2.dp, CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onSurface)
        }

        // Timer Section in a Floating Card
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 2.dp,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Timer,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatTime(uiState.timerSeconds),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
        }

        IconButton(
            onClick = onPauseToggle,
            modifier = Modifier
                .size(44.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .shadow(2.dp, CircleShape)
        ) {
            Icon(Icons.Default.Settings, null, tint = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun GameStatsBar(uiState: GameState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Difficulty Badge
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = stringResource(uiState.difficulty.resId).uppercase(),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
        }

        // Mistakes Indicator with larger icons
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "MISTAKES: ",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TextMuted
            )
            repeat(uiState.maxMistakes) { index ->
                val isLost = index < uiState.mistakes
                Icon(
                    imageVector = if (isLost) Icons.Default.Close else Icons.Default.Favorite,
                    contentDescription = null,
                    tint = if (isLost) WrongRed else EasyGreen,
                    modifier = Modifier
                        .size(22.dp)
                        .padding(horizontal = 1.dp)
                )
            }
        }
    }
}

@Composable
fun ActionButtonsRow(
    uiState: GameState,
    onUndo: () -> Unit,
    onErase: () -> Unit,
    onToggleNotes: () -> Unit,
    onHint: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
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
    }
}

@Composable
fun PremiumNumberPad(
    board: SudokuBoard,
    selectedNumber: Int?,
    onNumberClick: (Int) -> Unit,
    isNotesMode: Boolean
) {
    val counts = remember(board) {
        val c = IntArray(10) { 0 }
        board.cells.flatten().forEach { cell ->
            cell.value?.let { if (it in 1..9) c[it]++ }
        }
        c
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        (1..9).forEach { num ->
            val count = counts[num]
            val isCompleted = count >= 9
            
            PremiumNumberKey(
                num = num,
                remaining = 9 - count,
                isCompleted = isCompleted,
                isSelected = selectedNumber == num,
                modifier = Modifier.weight(1f),
                onClick = { if (!isCompleted) onNumberClick(num) }
            )
        }
    }
}

@Composable
fun PremiumNumberKey(
    num: Int,
    remaining: Int,
    isCompleted: Boolean,
    isSelected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else if (isSelected) 1.06f else 1f,
        animationSpec = spring(
            dampingRatio = if (isPressed) Spring.DampingRatioNoBouncy else Spring.DampingRatioMediumBouncy,
            stiffness = if (isPressed) Spring.StiffnessHigh else Spring.StiffnessMedium
        ),
        label = "numberKeyScale"
    )

    val containerColor by animateColorAsState(
        targetValue = if (isCompleted) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)
        else if (isSelected) MaterialTheme.colorScheme.primary
        else if (isPressed) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surface,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
        label = "numberKeyBg"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isCompleted) TextMuted.copy(alpha = 0.4f)
        else if (isSelected) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.primary,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
        label = "numberKeyText"
    )

    Surface(
        onClick = {
            if (!isCompleted) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
        },
        interactionSource = interactionSource,
        modifier = modifier
            .aspectRatio(0.7f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        shadowElevation = if (isPressed || isCompleted) 0.dp else 2.dp,
        border = BorderStroke(
            width = 1.dp,
            color = if (isCompleted) Color.Transparent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = num.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = contentColor
            )
            if (!isCompleted) {
                Text(
                    text = "($remaining)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else TextMuted,
                    fontSize = 9.sp
                )
            } else {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(12.dp), tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else EasyGreen.copy(alpha = 0.5f))
            }
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
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(
            dampingRatio = if (isPressed) Spring.DampingRatioNoBouncy else Spring.DampingRatioMediumBouncy,
            stiffness = if (isPressed) Spring.StiffnessHigh else Spring.StiffnessMedium
        ),
        label = "actionBtnScale"
    )
    val haptic = LocalHapticFeedback.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(64.dp)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Surface(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
                interactionSource = interactionSource,
                shape = CircleShape,
                color = if (active) MaterialTheme.colorScheme.primary else Color.Transparent,
                modifier = Modifier
                    .size(40.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            badge?.let {
                Surface(
                    color = MaterialTheme.colorScheme.error,
                    shape = CircleShape,
                    modifier = Modifier.offset(x = 4.dp, y = (-4).dp)
                ) {
                    Text(
                        text = it,
                        fontSize = 9.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }
        }
        Text(
            text = label.uppercase(),
            fontSize = 9.sp,
            color = if (active) MaterialTheme.colorScheme.primary else TextMuted,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp
        )
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
        shape = RoundedCornerShape(32.dp),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PAUSED",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${stringResource(difficulty.resId).uppercase()} • ${formatTime(timerSeconds)}",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onResume,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, null)
                    Spacer(Modifier.width(12.dp))
                    Text("RESUME GAME", fontWeight = FontWeight.Black, fontSize = 16.sp)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onRestartGame,
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("RESTART", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    
                    OutlinedButton(
                        onClick = onSaveAndExit,
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("EXIT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
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
        shape = RoundedCornerShape(28.dp),
        title = { Text("Restart game?", fontWeight = FontWeight.Black) },
        text = { Text("Your current progress will be lost. Are you sure?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("RESTART", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("CANCEL", fontWeight = FontWeight.Bold)
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
        shape = RoundedCornerShape(28.dp),
        title = { Text("Discard game?", fontWeight = FontWeight.Black) },
        text = { Text("Your current progress will be permanently deleted.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("DISCARD", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("CANCEL", fontWeight = FontWeight.Bold)
            }
        }
    )
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
        shape = RoundedCornerShape(32.dp),
        icon = { 
            Surface(color = GemCyan.copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.size(80.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Diamond, null, Modifier.size(40.dp), GemCyan)
                }
            }
        },
        title = { Text(stringResource(R.string.out_of_mistakes), fontWeight = FontWeight.Black, textAlign = TextAlign.Center) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.continue_with_gems), textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text = stringResource(R.string.you_have_gems, gems.toInt()),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onContinue,
                enabled = gems >= 1,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GemCyan)
            ) {
                Text(stringResource(R.string.continue_1_gem), color = Color.White, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.no_thanks), color = TextMuted, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun GameOverDialog(maxMistakes: Int, onRestart: () -> Unit, onHome: () -> Unit) {
    AlertDialog(
        onDismissRequest = { },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(32.dp),
        icon = { Icon(Icons.Default.ErrorOutline, null, Modifier.size(64.dp), WrongRed) },
        title = { Text(stringResource(R.string.game_over), fontWeight = FontWeight.Black, letterSpacing = 2.sp) },
        text = { Text(stringResource(R.string.game_over_mistakes, maxMistakes), textAlign = TextAlign.Center) },
        confirmButton = {
            Button(
                onClick = onRestart, 
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
            ) {
                Text(stringResource(R.string.restart).uppercase(), color = Color.White, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onHome, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.exit).uppercase(), color = TextMuted, fontWeight = FontWeight.Bold)
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
            onCellLongClick = { _, _ -> },
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
