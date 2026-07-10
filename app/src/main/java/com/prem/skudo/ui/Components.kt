package com.prem.skudo.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prem.skudo.database.UserProfile
import com.prem.skudo.database.GameStats
import com.prem.skudo.model.AvatarProvider
import com.prem.skudo.model.Difficulty
import com.prem.skudo.ui.theme.*

@Composable
fun UserAvatar(
    avatarId: String,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 48.dp,
    tint: Color = MaterialTheme.colorScheme.primary,
    imageUrl: String? = null
) {
    val context = LocalContext.current
    val avatar = AvatarProvider.getById(avatarId)
    val resourceId = context.resources.getIdentifier(avatar.resourceName, "drawable", context.packageName)

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                if (avatarId.startsWith("skudo_")) Brush.radialGradient(
                    colors = listOf(tint.copy(alpha = 0.2f), tint.copy(alpha = 0.05f))
                ) else Brush.verticalGradient(
                    colors = listOf(tint.copy(alpha = 0.15f), tint.copy(alpha = 0.05f))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null) {
            Icon(Icons.Default.AccountCircle, null, Modifier.size(size * 0.8f), tint = tint)
        } else if (resourceId != 0) {
            Image(
                painter = painterResource(id = resourceId),
                contentDescription = "Avatar",
                modifier = Modifier.size(size * 0.75f)
            )
        } else {
            // Branded fallback for new Skudo avatars
            when (avatarId) {
                "skudo_pencil" -> Icon(Icons.Default.Edit, "Pencil", Modifier.size(size * 0.65f), tint)
                "skudo_grid" -> Icon(Icons.Default.GridView, "Grid", Modifier.size(size * 0.6f), tint)
                "skudo_win" -> {
                    // 4-square icon like in the logo 'o'
                    Column(Modifier.size(size * 0.45f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Box(Modifier.weight(1f).fillMaxHeight().background(tint, RoundedCornerShape(1.dp)))
                            Box(Modifier.weight(1f).fillMaxHeight().background(tint, RoundedCornerShape(1.dp)))
                        }
                        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Box(Modifier.weight(1f).fillMaxHeight().background(tint, RoundedCornerShape(1.dp)))
                            Box(Modifier.weight(1f).fillMaxHeight().background(tint, RoundedCornerShape(1.dp)))
                        }
                    }
                }
                "skudo_brain" -> Icon(Icons.Default.Psychology, "Logic", Modifier.size(size * 0.65f), tint)
                "skudo_rocket" -> Icon(Icons.Default.RocketLaunch, "Speed", Modifier.size(size * 0.6f), tint)
                "skudo_star" -> Icon(Icons.Default.Star, "Mastery", Modifier.size(size * 0.65f), tint)
                "skudo_trophy" -> Icon(Icons.Default.EmojiEvents, "Victory", Modifier.size(size * 0.65f), tint)
                "skudo_bulb" -> Icon(Icons.Default.Lightbulb, "Hint", Modifier.size(size * 0.6f), tint)
                "skudo_number_5" -> Text("5", color = tint, fontWeight = FontWeight.Black, fontSize = (size.value * 0.55f).sp)
                "skudo_number_9" -> Text("9", color = tint, fontWeight = FontWeight.Black, fontSize = (size.value * 0.55f).sp)
                else -> Icon(Icons.Default.Person, "Avatar", Modifier.size(size * 0.6f), tint)
            }
        }
    }
}

@Composable
fun ProfileSummaryCard(
    profile: UserProfile,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(avatarId = profile.avatarId, size = 48.dp, imageUrl = profile.photoUrl)
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(profile.displayName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("Level ${profile.level} • ${profile.currentRank}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                
                val xpForCurrent = com.prem.skudo.utils.LevelManager.getXpForLevel(profile.level)
                val xpForNext = com.prem.skudo.utils.LevelManager.getXpForLevel(profile.level + 1)
                val range = (xpForNext - xpForCurrent).toFloat()
                val progress = if (range > 0) (profile.xp - xpForCurrent).toFloat() / range else 0f

                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .height(4.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            }
            
            Icon(Icons.Default.ChevronRight, null, tint = TextMuted)
        }
    }
}

@Composable
fun HomeProfileHeader(
    profile: UserProfile,
    isSyncing: Boolean = false,
    onClick: () -> Unit
) {
    val email = profile.email
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            UserAvatar(avatarId = profile.avatarId, size = 50.dp, imageUrl = profile.photoUrl)
            if (isSyncing) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(10.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            } else if (profile.lastSyncAt != null) {
                Icon(
                    Icons.Default.CloudDone,
                    null,
                    modifier = Modifier
                        .size(16.dp)
                        .background(EasyGreen, CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                        .padding(2.dp),
                    tint = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(profile.displayName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            if (email != null) {
                Text(email, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            } else {
                Text("Level ${profile.level} • ${profile.currentRank}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
            
            val xpForCurrent = com.prem.skudo.utils.LevelManager.getXpForLevel(profile.level)
            val xpForNext = com.prem.skudo.utils.LevelManager.getXpForLevel(profile.level + 1)
            val range = (xpForNext - xpForCurrent).toFloat()
            val progress = if (range > 0) (profile.xp - xpForCurrent).toFloat() / range else 0f
            
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .height(6.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        }
        
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(start = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MonetizationOn, null, Modifier.size(16.dp), AccentGold)
                Text(" ${profile.coins}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Diamond, null, Modifier.size(16.dp), MaterialTheme.colorScheme.primary)
                Text(" ${profile.gems}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun StatsCard(stats: GameStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (stats.gamesPlayed > 0) "${stats.difficulty} BEST STATS" else "GAME STATISTICS",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )
                if (stats.bestTimeSeconds != Long.MAX_VALUE) {
                    Icon(Icons.Default.EmojiEvents, null, Modifier.size(16.dp), AccentGold)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (stats.gamesPlayed > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("Played", stats.gamesPlayed.toString())
                    StatItem("Wins", stats.gamesWon.toString())
                    StatItem("Best", if (stats.bestTimeSeconds == Long.MAX_VALUE) "--:--" else formatTime(stats.bestTimeSeconds))
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Win a game to see your best time!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, color = TextMuted, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun DifficultyGrid(onStartGame: (Difficulty) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DifficultyCard(Difficulty.EASY, EasyGreen, 1, Modifier.weight(1f)) { onStartGame(Difficulty.EASY) }
            DifficultyCard(Difficulty.MEDIUM, MediumGold, 2, Modifier.weight(1f)) { onStartGame(Difficulty.MEDIUM) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DifficultyCard(Difficulty.HARD, HardPurple, 3, Modifier.weight(1f)) { onStartGame(Difficulty.HARD) }
            DifficultyCard(Difficulty.EXPERT, ExpertRed, 4, Modifier.weight(1f)) { onStartGame(Difficulty.EXPERT) }
        }
    }
}

@Composable
fun DifficultyCard(
    difficulty: Difficulty,
    color: Color,
    stars: Int,
    modifier: Modifier = Modifier,
    overrideLabel: String? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .heightIn(min = 80.dp, max = 100.dp)
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(listOf(color.copy(0.8f), color)), RoundedCornerShape(20.dp))
            .border(1.dp, Color.White.copy(0.3f), RoundedCornerShape(20.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = (overrideLabel ?: difficulty.name).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Row {
                repeat(4) { index ->
                    Icon(
                        if (index < stars) Icons.Default.Star else Icons.Default.StarOutline,
                        null,
                        Modifier.size(16.dp),
                        Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun HomeActionButton(label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp, max = 56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(24.dp), color)
            Spacer(Modifier.width(12.dp))
            Text(label, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}

fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%dh %dm".format(h, m) else "%dm %ds".format(m, s)
}

fun formatTime(seconds: Long): String {
    if (seconds == Long.MAX_VALUE) return "--:--"
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
