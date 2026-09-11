package com.prem.skudo

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import com.prem.skudo.model.AvatarProvider
import com.prem.skudo.ui.ProfileSummaryCard
import com.prem.skudo.ui.UserAvatar
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
    var showEditProfileDialog by remember { mutableStateOf(false) }

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

            // Statistics Section
            SettingsSection("Statistics") {
                SettingsClickable(
                    title = "Game Statistics",
                    value = if (homeUiState.totalGamesPlayed > 0) "${homeUiState.totalGamesWon * 100 / homeUiState.totalGamesPlayed}% Win Rate" else "View all",
                    icon = Icons.Default.BarChart
                ) {
                    onViewProfile()
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                )

                // Pencil/Edit button placed under Statistics
                SettingsClickable(
                    title = "Edit Profile",
                    value = homeUiState.userProfile?.displayName ?: "Player",
                    icon = Icons.Default.Edit
                ) {
                    showEditProfileDialog = true
                }
            }

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
            }

            // Audio Section
            SettingsSection("Audio") {
                SettingsToggle("Sound Effects", "Game interaction sounds", settingsState.soundEffects, Icons.AutoMirrored.Filled.VolumeUp) {
                    settingsViewModel.updateSoundEffects(it)
                }
                SettingsToggle("Vibration", "Haptic feedback on actions", settingsState.vibration, Icons.Default.Vibration) {
                    settingsViewModel.updateVibration(it)
                }
                SettingsToggle("Button Sounds", "Play sounds when tapping buttons", settingsState.buttonSounds, Icons.Default.TouchApp) {
                    settingsViewModel.updateButtonSounds(it)
                }
            }

            // Language Section
            SettingsSection("Language") {
                SettingsClickable("Language", "English", Icons.Default.Language) {}
            }

            // About Section
            val context = LocalContext.current
            SettingsSection("About") {
                SettingsClickable("Privacy Policy", null, Icons.Default.PrivacyTip) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://fabulous-crostata-6b5805.netlify.app/"))
                    context.startActivity(intent)
                }
                
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

    if (showEditProfileDialog) {
        val currentProfile = homeUiState.userProfile ?: com.prem.skudo.database.UserProfile()
        EditProfileDialog(
            profile = currentProfile,
            onDismiss = { showEditProfileDialog = false },
            onSave = { newName, newAvatarId ->
                if (newName.isNotBlank()) {
                    homeViewModel.updateDisplayName(newName)
                }
                homeViewModel.updateAvatar(newAvatarId)
            }
        )
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

@Composable
fun EditProfileDialog(
    profile: com.prem.skudo.database.UserProfile,
    onDismiss: () -> Unit,
    onSave: (name: String, avatarId: String) -> Unit
) {
    var displayName by remember { mutableStateOf(profile.displayName) }
    var selectedAvatarId by remember { mutableStateOf(profile.avatarId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit Profile", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar preview
                Box(contentAlignment = Alignment.Center) {
                    UserAvatar(
                        avatarId = selectedAvatarId,
                        size = 64.dp
                    )
                }

                OutlinedTextField(
                    value = displayName,
                    onValueChange = { if (it.length <= 20) displayName = it },
                    label = { Text("Display Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    text = "Choose Avatar",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted,
                    modifier = Modifier.align(Alignment.Start)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AvatarProvider.avatars.forEach { avatar ->
                        val isSelected = avatar.id == selectedAvatarId
                        Surface(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable { selectedAvatarId = avatar.id }
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                ),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                UserAvatar(avatarId = avatar.id, size = 44.dp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(displayName, selectedAvatarId)
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp)
    )
}
