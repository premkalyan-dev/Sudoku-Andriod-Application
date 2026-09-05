package com.prem.skudo.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.res.stringResource
import com.prem.skudo.R
import com.prem.skudo.ui.theme.*
import com.prem.skudo.viewmodel.GameState
import kotlinx.coroutines.delay

@Composable
fun PremiumRewardScreen(
    state: GameState,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    Dialog(
        onDismissRequest = onHome,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f)),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + scaleIn(initialScale = 0.8f, animationSpec = spring(Spring.DampingRatioMediumBouncy)),
                exit = fadeOut() + scaleOut()
            ) {
                Box(contentAlignment = Alignment.Center) {
                    RewardContent(state, onPlayAgain, onHome)
                    ConfettiOverlay(visible)
                }
            }
        }
    }
}

@Composable
fun ConfettiOverlay(visible: Boolean) {
    if (!visible) return
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    
    Box(modifier = Modifier.fillMaxSize()) {
        repeat(20) { index ->
            val xPos = remember { (10..90).random() / 100f }
            val delay = remember { (0..2000).random() }
            
            val yPos by infiniteTransition.animateFloat(
                initialValue = -0.1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 2500, delayMillis = delay, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "confettiY"
            )

            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing)
                ),
                label = "confettiRotation"
            )

            val color = remember { listOf(PrimaryCyan, AccentGold, Color.Red, Color.Yellow, Color.Green).random() }

            Box(
                modifier = Modifier
                    .fillMaxSize(0.03f)
                    .align(Alignment.TopStart)
                    .graphicsLayer {
                        translationX = xPos * 1000f // Rough estimation
                        translationY = yPos * 2000f
                        rotationZ = rotation
                    }
                    .background(color, RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
fun RewardContent(
    state: GameState,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(vertical = 24.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Victory Icon & Title
            Box(contentAlignment = Alignment.Center) {
                val infiniteTransition = rememberInfiniteTransition(label = "iconScale")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )
                
                Icon(
                    Icons.Default.EmojiEvents,
                    null,
                    modifier = Modifier
                        .size(80.dp)
                        .scale(scale),
                    tint = AccentGold
                )
            }
            
            Text(
                stringResource(R.string.victory),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 4.sp
            )
            
            Text(
                stringResource(R.string.puzzle_completed),
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )

            if (state.isPerfect) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = AccentGold.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, null, Modifier.size(16.dp), AccentGold)
                        Spacer(Modifier.width(4.dp))
                        Text("PERFECT GAME", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Game Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InfoBadge(stringResource(state.difficulty.resId), MaterialTheme.colorScheme.primary)
                InfoBadge(formatTime(state.timerSeconds), MaterialTheme.colorScheme.secondary)
                
                // Cells Per Minute (CPM)
                val cluesCount = state.puzzle.cells.flatten().count { it.isClue }
                val filledCount = 81 - cluesCount
                val cpm = if (state.timerSeconds > 0) (filledCount.toFloat() / (state.timerSeconds.toFloat() / 60f)).toInt() else 0
                InfoBadge("$cpm CPM", AccentGold)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Level Up / Progress Section
            if (state.leveledUp) {
                LevelUpSection(state.newLevel)
            } else {
                LevelProgressSection(state)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Rewards Section
            Text(
                stringResource(R.string.rewards_earned),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RewardCard("+${state.xpEarned}", stringResource(R.string.xp), Icons.Default.AddCircle, MaterialTheme.colorScheme.primary)
                RewardCard("+${state.coinsEarned}", stringResource(R.string.coins), Icons.Default.MonetizationOn, AccentGold)
            }
            
            if (state.unlockedAchievements.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                AchievementUnlocks(state.unlockedAchievements)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Action Buttons
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onPlayAgain,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.play_again), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onHome,
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(stringResource(R.string.home), color = MaterialTheme.colorScheme.onSurface)
                    }
                    
                    Button(
                        onClick = onHome, // Or continue to next puzzle
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text(stringResource(R.string.continue_btn), color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun InfoBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(text, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun RewardCard(value: String, label: String, icon: ImageVector, color: Color) {
    var animatedValue by remember { mutableIntStateOf(0) }
    val target = value.filter { it.isDigit() }.toIntOrNull() ?: 0
    
    LaunchedEffect(Unit) {
        delay(500)
        val duration = 1000
        val steps = 20
        for (i in 1..steps) {
            delay((duration / steps).toLong())
            animatedValue = (target * (i.toFloat() / steps)).toInt()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(color.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(12.dp)
            .widthIn(min = 80.dp)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        Text(
            if (target > 0) "+$animatedValue" else value, 
            fontWeight = FontWeight.ExtraBold, 
            fontSize = 18.sp, 
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(label, fontSize = 10.sp, color = TextMuted)
    }
}

@Composable
fun LevelProgressSection(state: GameState) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.level, state.newLevel), fontWeight = FontWeight.Bold)
            Text(
                text = stringResource(R.string.rank, com.prem.skudo.utils.LevelManager.getTitleForLevel(state.newLevel)),
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // This is a bit tricky because we don't have current progress easily in state
        // In a real app we'd get this from the VM
        LinearProgressIndicator(
            progress = { 0.7f }, // Placeholder
            modifier = Modifier.fillMaxWidth().height(12.dp).clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun LevelUpSection(newLevel: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "levelup")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "alpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(AccentGold.copy(0.1f), Color.Transparent)),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            stringResource(R.string.level_up),
            color = AccentGold,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            modifier = Modifier.scale(alpha + 0.5f)
        )
        Text(
            stringResource(R.string.welcome_level, newLevel),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            com.prem.skudo.utils.LevelManager.getTitleForLevel(newLevel),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun AchievementUnlocks(achievements: List<com.prem.skudo.database.Achievement>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Text(stringResource(R.string.achievements_unlocked), style = MaterialTheme.typography.labelMedium, color = TextMuted)
        achievements.forEach { achievement ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                Icon(Icons.Default.Stars, null, tint = AccentGold, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(achievement.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}
