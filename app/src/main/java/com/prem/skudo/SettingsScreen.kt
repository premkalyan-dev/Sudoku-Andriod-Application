package com.prem.skudo

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
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
    var showThemeDialog by remember { mutableStateOf(false) }

    val isSystemDark = isSystemInDarkTheme()
    val isDarkActive = when (settingsState.themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemDark
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = settingsState.themeMode,
            onThemeSelected = { mode ->
                settingsViewModel.updateThemeMode(mode)
            },
            onDismiss = { showThemeDialog = false }
        )
    }

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
            }

            // Appearance Section
            SettingsSection("Appearance") {
                SettingsToggle(
                    title = "Dark Theme",
                    subtitle = when (settingsState.themeMode) {
                        "DARK" -> "Dark theme enabled"
                        "LIGHT" -> "Light theme enabled"
                        else -> "Following system (${if (isSystemDark) "Dark" else "Light"})"
                    },
                    checked = isDarkActive,
                    icon = if (isDarkActive) Icons.Default.DarkMode else Icons.Default.LightMode
                ) { isChecked ->
                    settingsViewModel.updateThemeMode(if (isChecked) "DARK" else "LIGHT")
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )

                SettingsClickable(
                    title = "Theme Mode",
                    value = when (settingsState.themeMode) {
                        "DARK" -> "Dark Theme"
                        "LIGHT" -> "Light Theme"
                        else -> "System Default"
                    },
                    icon = when (settingsState.themeMode) {
                        "DARK" -> Icons.Default.DarkMode
                        "LIGHT" -> Icons.Default.LightMode
                        else -> Icons.Default.BrightnessAuto
                    }
                ) {
                    showThemeDialog = true
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )

                SettingsClickable("Accent Color", settingsState.accentColor, Icons.Default.ColorLens) {
                    val nextColor = when(settingsState.accentColor) {
                        "Cyan" -> "Gold"
                        else -> "Cyan"
                    }
                    settingsViewModel.updateAccentColor(nextColor)
                }
            }

            // Audio Section
            SettingsSection("Audio") {
                SettingsToggle("Vibration", "Haptic feedback on actions", settingsState.vibration, Icons.Default.Vibration) {
                    settingsViewModel.updateVibration(it)
                }
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
                    Text("Version 1.0.0 (Production Build)", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
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
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
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
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            if (value != null) {
                Text(value, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: String,
    onThemeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        Triple("SYSTEM", "System Default", Icons.Default.BrightnessAuto to "Follow your device's system appearance"),
        Triple("LIGHT", "Light Theme", Icons.Default.LightMode to "Clean, bright display for daytime"),
        Triple("DARK", "Dark Theme", Icons.Default.DarkMode to "Dark, sleek display for lower eye strain")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                "Select Theme",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { (mode, title, details) ->
                    val (icon, subtitle) = details
                    val isSelected = currentTheme == mode
                    Surface(
                        onClick = {
                            onThemeSelected(mode)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    onThemeSelected(mode)
                                    onDismiss()
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary,
                                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
