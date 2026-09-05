package com.prem.skudo

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prem.skudo.database.UserProfile
import com.prem.skudo.model.AvatarProvider
import com.prem.skudo.ui.*
import com.prem.skudo.ui.theme.*
import com.prem.skudo.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,

    onViewAchievements: () -> Unit,
    homeViewModel: HomeViewModel = viewModel(),
) {
    val uiState by homeViewModel.uiState.collectAsState()
    val profile = uiState.userProfile ?: UserProfile()
    
    var showEditName by remember { mutableStateOf(value = false) }
    var showAvatarPicker by remember { mutableStateOf(value = false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
                // Profile Header Card
                ProfileHeader(
                    profile = profile,
                    onEditAvatar = { showAvatarPicker = true },
                    onEditName = {
                        showEditName = true
                    },
                )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Stats Grid
            Text(
                "Game Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            StatsGrid(uiState)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Resources Section
            ResourceCard(
                Modifier.fillMaxWidth(),
                "Coins",
                profile.coins.toString(),
                Icons.Default.MonetizationOn,
                Color(0xFFFFD700)
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // Achievements Button
            HomeActionButton(
                label = "VIEW ACHIEVEMENTS",
                icon = Icons.Default.EmojiEvents,
                color = AccentGold,
                onClick = onViewAchievements
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            // Account Info
            AccountInfoCard(profile)


        }
    }
    
    if (showEditName) {
        var name by remember { mutableStateOf(profile.displayName) }
        var error by remember { mutableStateOf<String?>(null) }
        
        AlertDialog(
            onDismissRequest = { showEditName = false },
            title = { Text("Edit Name") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { 
                            name = it
                            error = null
                        },
                        label = { Text("Display Name") },
                        isError = error != null,
                        supportingText = { error?.let { Text(it) } },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    if (homeViewModel.updateDisplayName(name)) {
                        showEditName = false 
                    } else {
                        error = "Name must be 3-20 characters"
                    }
                }) { Text("SAVE") }
            },
            dismissButton = {
                TextButton(onClick = { showEditName = false }) { Text("CANCEL") }
            }
        )
    }
    
    if (showAvatarPicker) {
        AvatarPicker(
            onDismiss = { showAvatarPicker = false },
            onSelect = { avatarId ->
                homeViewModel.updateAvatar(avatarId)
            }
        )
    }
}

@Composable
fun ProfileHeader(
    profile: UserProfile,
    onEditAvatar: () -> Unit,
    onEditName: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                // Avatar Circle
                UserAvatar(
                    avatarId = profile.avatarId,
                    imageUrl = profile.photoUrl,
                    size = 100.dp,
                    modifier = Modifier.clickable { onEditAvatar() }
                )
                
                IconButton(
                    onClick = onEditAvatar,
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    profile.displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onEditName) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp), tint = TextMuted)
                }
            }
            
            Text(
                profile.currentRank.uppercase(),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                style = MaterialTheme.typography.labelLarge
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Level & XP
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Level ${profile.level}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("${profile.xp} XP", color = TextMuted)
            }
            
            val xpForCurrent = com.prem.skudo.utils.LevelManager.getXpForLevel(profile.level)
            val xpForNext = com.prem.skudo.utils.LevelManager.getXpForLevel(profile.level + 1)
            val range = (xpForNext - xpForCurrent).toFloat()
            val progress = if (range > 0) (profile.xp - xpForCurrent).toFloat() / range else 0f

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .padding(vertical = 4.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
fun StatsGrid(uiState: com.prem.skudo.viewmodel.HomeState) {
    val totalPlayed = uiState.totalGamesPlayed
    val totalWon = uiState.totalGamesWon
    val winRate = if (totalPlayed > 0) (totalWon * 100 / totalPlayed) else 0
    val bestOverall = uiState.bestOverallStats
    val profile = uiState.userProfile ?: com.prem.skudo.database.UserProfile()
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MiniStatCard(Modifier.weight(1f), "Win Rate", "$winRate%", Icons.Default.TrendingUp)
            MiniStatCard(Modifier.weight(1f), "Games Won", totalWon.toString(), Icons.Default.EmojiEvents)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MiniStatCard(Modifier.weight(1f), "Daily Streak", uiState.dailyStreak.toString(), Icons.Default.Whatshot)
            MiniStatCard(Modifier.weight(1f), "Best Time", if (bestOverall != null) formatTime(bestOverall.bestTimeSeconds) else "--:--", Icons.Default.Timer)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MiniStatCard(Modifier.weight(1f), "Play Time", formatDuration(profile.totalPlayTime), Icons.Default.AccessTime)
            MiniStatCard(Modifier.weight(1f), "Games Played", totalPlayed.toString(), Icons.Default.SportsEsports)
        }
    }
}

@Composable
fun MiniStatCard(modifier: Modifier, label: String, value: String, icon: ImageVector) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(value, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
        }
    }
}

@Composable
fun ResourceCard(modifier: Modifier, label: String, value: String, icon: ImageVector, color: Color) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        }
    }
}

@Composable
fun AccountInfoCard(profile: UserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Account Information", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow("User ID", profile.userId.take(8) + "...")
            InfoRow("Join Date", java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(profile.joinedDate)))
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextMuted, style = MaterialTheme.typography.bodyMedium)
        Text(value, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun AvatarPicker(onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Avatar") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(64.dp),
                modifier = Modifier.height(300.dp),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AvatarProvider.avatars) { avatar ->
                    UserAvatar(
                        avatarId = avatar.id,
                        size = 64.dp,
                        modifier = Modifier
                            .clickable { 
                                onSelect(avatar.id)
                                onDismiss() 
                            }
                            .padding(4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL") }
        }
    )
}
