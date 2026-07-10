package com.prem.skudo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.prem.skudo.ui.ProfileSummaryCard
import com.prem.skudo.ui.theme.*
import com.prem.skudo.viewmodel.HomeViewModel
import com.prem.skudo.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onViewProfile: () -> Unit,
    homeViewModel: HomeViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val homeUiState by homeViewModel.uiState.collectAsState()
    val settingsState by settingsViewModel.settingsState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Section
            Text("Profile", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            ProfileSummaryCard(
                profile = homeUiState.userProfile ?: com.prem.skudo.database.UserProfile(),
                onClick = onViewProfile
            )

            // Appearance Section
            SettingsSection("Appearance") {
                SettingsClickable("Theme", settingsState.themeMode, Icons.Default.Palette) {
                    // Logic to cycle themes or show dialog
                    val nextTheme = when(settingsState.themeMode) {
                        "SYSTEM" -> "LIGHT"
                        "LIGHT" -> "DARK"
                        else -> "SYSTEM"
                    }
                    settingsViewModel.updateThemeMode(nextTheme)
                }
                SettingsClickable("Accent Color", settingsState.accentColor, Icons.Default.ColorLens) {
                    val nextColor = when(settingsState.accentColor) {
                        "Cyan" -> "Gold"
                        "Gold" -> "Green"
                        "Green" -> "Purple"
                        "Purple" -> "Red"
                        else -> "Cyan"
                    }
                    settingsViewModel.updateAccentColor(nextColor)
                }
                SettingsClickable("Board Style", settingsState.boardStyle, Icons.Default.GridOn) {
                    val nextStyle = when(settingsState.boardStyle) {
                        "Modern" -> "Classic"
                        "Classic" -> "Glass"
                        else -> "Modern"
                    }
                    settingsViewModel.updateBoardStyle(nextStyle)
                }
            }

            // Audio Section
            SettingsSection("Audio") {
                SettingsToggle("Sound Effects", "Game interaction sounds", settingsState.soundEffects, Icons.Default.VolumeUp) {
                    settingsViewModel.updateSoundEffects(it)
                }
                SettingsToggle("Vibration", "Haptic feedback on actions", settingsState.vibration, Icons.Default.Vibration) {
                    settingsViewModel.updateVibration(it)
                }
                SettingsToggle("Button Sounds", "Play sounds when tapping buttons", settingsState.buttonSounds, Icons.Default.TouchApp) {
                    settingsViewModel.updateButtonSounds(it)
                }
            }

            // Notifications Section
            SettingsSection("Notifications") {
                SettingsToggle("Daily Reminder", "Remind me to play daily challenge", settingsState.dailyReminder, Icons.Default.Notifications) {
                    settingsViewModel.updateDailyReminder(it)
                }
                SettingsToggle("Achievement Reminder", "Notify when achievements are unlocked", settingsState.achievementReminder, Icons.Default.EmojiEvents) {
                    settingsViewModel.updateAchievementReminder(it)
                }
            }

            // Language Section
            SettingsSection("Language") {
                SettingsClickable("Language", "English", Icons.Default.Language) {}
            }

            // About Section
            SettingsSection("About") {
                SettingsClickable("Privacy Policy", null, Icons.Default.PrivacyTip) {}
                SettingsClickable("Terms of Service", null, Icons.Default.Description) {}
                SettingsClickable("Open Source Licenses", null, Icons.Default.Info) {}
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Version 1.0.0 (Production Build)", color = TextMuted, style = MaterialTheme.typography.labelMedium)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    icon: ImageVector? = null,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, null, tint = TextMuted, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = TextMuted.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun SettingsClickable(
    title: String,
    value: String?,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = TextMuted, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            if (value != null) {
                Text(value, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
        Icon(Icons.Default.ChevronRight, null, tint = TextMuted)
    }
}
