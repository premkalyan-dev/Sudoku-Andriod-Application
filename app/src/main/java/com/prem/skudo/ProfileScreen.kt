package com.prem.skudo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prem.skudo.database.GameStats
import com.prem.skudo.database.UserProfile
import com.prem.skudo.ui.formatDuration
import com.prem.skudo.ui.formatTime
import com.prem.skudo.ui.theme.*
import com.prem.skudo.viewmodel.HomeState
import com.prem.skudo.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onViewAchievements: () -> Unit = {},
    homeViewModel: HomeViewModel = viewModel(),
) {
    val uiState by homeViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Game Statistics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatsGrid(uiState)
        }
    }
}

@Composable
fun StatsGrid(uiState: HomeState) {
    val totalPlayed = uiState.totalGamesPlayed
    val totalWon = uiState.totalGamesWon
    val winRate = if (totalPlayed > 0) (totalWon * 100 / totalPlayed) else 0
    val bestOverall = uiState.bestOverallStats
    val profile = uiState.userProfile ?: UserProfile()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Overall Overview",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiniStatCard(Modifier.weight(1f), "Win Rate", "$winRate%", Icons.AutoMirrored.Filled.TrendingUp)
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

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Difficulty Breakdown",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DifficultyStatsCard("Easy", uiState.easyStats, EasyGreen)
            DifficultyStatsCard("Medium", uiState.mediumStats, MediumGold)
            DifficultyStatsCard("Hard", uiState.hardStats, HardPurple)
            DifficultyStatsCard("Expert", uiState.expertStats, ExpertRed)
        }
    }
}

@Composable
fun MiniStatCard(modifier: Modifier, label: String, value: String, icon: ImageVector) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
        }
    }
}

@Composable
fun DifficultyStatsCard(title: String, stats: GameStats?, accentColor: Color) {
    val played = stats?.gamesPlayed ?: 0
    val won = stats?.gamesWon ?: 0
    val winRate = if (played > 0) (won * 100 / played) else 0
    val bestTime = if (stats != null && stats.bestTimeSeconds != Long.MAX_VALUE) formatTime(stats.bestTimeSeconds) else "--:--"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    color = accentColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "$winRate% Win Rate",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Won / Played", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text("$won / $played", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Best Time", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(bestTime, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}
