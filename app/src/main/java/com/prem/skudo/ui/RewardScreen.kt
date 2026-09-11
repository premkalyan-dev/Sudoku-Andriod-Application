package com.prem.skudo.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
            .fillMaxWidth(0.88f)
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Victory Icon & Title
            Box(contentAlignment = Alignment.Center) {
                val infiniteTransition = rememberInfiniteTransition(label = "iconScale")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.08f,
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
                        .size(48.dp)
                        .scale(scale),
                    tint = AccentGold
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                stringResource(R.string.victory),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 2.sp
            )
            
            Text(
                stringResource(R.string.puzzle_completed),
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )

            if (state.isPerfect) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = AccentGold.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, null, Modifier.size(14.dp), AccentGold)
                        Spacer(Modifier.width(4.dp))
                        Text("PERFECT GAME", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
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
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Level Up / Progress Section
            if (state.leveledUp) {
                LevelUpSection(state.newLevel)
            } else {
                LevelProgressSection(state)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Rewards Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RewardCard(
                    value = "+${state.xpEarned}",
                    label = stringResource(R.string.xp),
                    icon = Icons.Default.AddCircle,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                RewardCard(
                    value = "+${state.coinsEarned}",
                    label = stringResource(R.string.coins),
                    icon = Icons.Default.MonetizationOn,
                    color = AccentGold,
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (state.unlockedAchievements.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                AchievementUnlocks(state.unlockedAchievements)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onHome,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Text(
                        stringResource(R.string.home),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Button(
                    onClick = onPlayAgain,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        stringResource(R.string.play_again),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun InfoBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
fun RewardCard(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    var animatedValue by remember { mutableIntStateOf(0) }
    val target = value.filter { it.isDigit() }.toIntOrNull() ?: 0
    
    LaunchedEffect(Unit) {
        delay(500)
        val duration = 800
        val steps = 15
        for (i in 1..steps) {
            delay((duration / steps).toLong())
            animatedValue = (target * (i.toFloat() / steps)).toInt()
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .background(color.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                if (target > 0) "+$animatedValue" else value, 
                fontWeight = FontWeight.ExtraBold, 
                fontSize = 15.sp, 
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(label, fontSize = 10.sp, color = TextMuted)
        }
    }
}

@Composable
fun LevelProgressSection(state: GameState) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.level, state.newLevel), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(
                text = stringResource(R.string.rank, com.prem.skudo.utils.LevelManager.getTitleForLevel(state.newLevel)),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        LinearProgressIndicator(
            progress = { 0.7f },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun LevelUpSection(newLevel: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "levelup")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
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
                RoundedCornerShape(12.dp)
            )
            .padding(8.dp)
    ) {
        Text(
            stringResource(R.string.level_up),
            color = AccentGold,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            modifier = Modifier.scale(alpha + 0.1f)
        )
        Text(
            stringResource(R.string.welcome_level, newLevel),
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            com.prem.skudo.utils.LevelManager.getTitleForLevel(newLevel),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AchievementUnlocks(achievements: List<com.prem.skudo.database.Achievement>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Text(stringResource(R.string.achievements_unlocked), style = MaterialTheme.typography.labelSmall, color = TextMuted)
        achievements.forEach { achievement ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                Icon(Icons.Default.Stars, null, tint = AccentGold, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(achievement.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}
