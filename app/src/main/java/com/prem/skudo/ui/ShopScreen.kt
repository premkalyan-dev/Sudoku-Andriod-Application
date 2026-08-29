package com.prem.skudo.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prem.skudo.ui.theme.*
import com.prem.skudo.viewmodel.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    onBack: () -> Unit,
    viewModel: ShopViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.purchaseSuccess, uiState.errorMessage) {
        if (uiState.purchaseSuccess) {
            snackbarHostState.showSnackbar("Purchase successful!")
            viewModel.clearMessages()
        } else if (uiState.errorMessage != null) {
            snackbarHostState.showSnackbar(uiState.errorMessage!!)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Coin Shop", 
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    CurrencyBadge(uiState.hints.toLong(), Icons.Default.Lightbulb, EasyGreen)
                    Spacer(modifier = Modifier.width(6.dp))
                    CurrencyBadge(uiState.coins, Icons.Default.MonetizationOn, AccentGold)
                    Spacer(modifier = Modifier.width(6.dp))
                    CurrencyBadge(uiState.gems, Icons.Default.Diamond, GemCyan)
                    Spacer(modifier = Modifier.width(16.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Section 1: Buy Hints
            SectionHeader(
                title = "Buy Hints",
                subtitle = "Power-ups to assist your puzzle solving"
            )
            
            Spacer(modifier = Modifier.height(14.dp))
            
            val hintPackages = listOf(
                Triple(5, 200, Icons.Default.Lightbulb),
                Triple(15, 500, Icons.Default.TipsAndUpdates),
                Triple(35, 1000, Icons.Default.EmojiObjects),
                Triple(60, 1500, Icons.Default.Psychology)
            )
            
            // 2x2 Grid via Columns of Rows (safe with outer verticalScroll)
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    HintShopItemCard(
                        hintsCount = hintPackages[0].first,
                        price = hintPackages[0].second,
                        icon = hintPackages[0].third,
                        onClick = { viewModel.buyHints(hintPackages[0].first, hintPackages[0].second.toLong()) },
                        modifier = Modifier.weight(1f)
                    )
                    HintShopItemCard(
                        hintsCount = hintPackages[1].first,
                        price = hintPackages[1].second,
                        icon = hintPackages[1].third,
                        onClick = { viewModel.buyHints(hintPackages[1].first, hintPackages[1].second.toLong()) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    HintShopItemCard(
                        hintsCount = hintPackages[2].first,
                        price = hintPackages[2].second,
                        icon = hintPackages[2].third,
                        onClick = { viewModel.buyHints(hintPackages[2].first, hintPackages[2].second.toLong()) },
                        modifier = Modifier.weight(1f)
                    )
                    HintShopItemCard(
                        hintsCount = hintPackages[3].first,
                        price = hintPackages[3].second,
                        icon = hintPackages[3].third,
                        onClick = { viewModel.buyHints(hintPackages[3].first, hintPackages[3].second.toLong()) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // Section 2: Game Themes
            SectionHeader(
                title = "Game Themes",
                subtitle = "Personalize your board styling & colors"
            )
            
            Spacer(modifier = Modifier.height(14.dp))
            
            val themes = listOf("Ocean", "Forest", "Neon", "Gold")
            
            // 2x2 Grid via Columns of Rows (safe with outer verticalScroll)
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    ShopItemCard(
                        title = themes[0],
                        price = 1000,
                        isUnlocked = uiState.unlockedThemes.contains(themes[0]),
                        onClick = { viewModel.buyTheme(themes[0]) },
                        modifier = Modifier.weight(1f)
                    )
                    ShopItemCard(
                        title = themes[1],
                        price = 1000,
                        isUnlocked = uiState.unlockedThemes.contains(themes[1]),
                        onClick = { viewModel.buyTheme(themes[1]) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    ShopItemCard(
                        title = themes[2],
                        price = 1000,
                        isUnlocked = uiState.unlockedThemes.contains(themes[2]),
                        onClick = { viewModel.buyTheme(themes[2]) },
                        modifier = Modifier.weight(1f)
                    )
                    ShopItemCard(
                        title = themes[3],
                        price = 1000,
                        isUnlocked = uiState.unlockedThemes.contains(themes[3]),
                        onClick = { viewModel.buyTheme(themes[3]) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
    }
}

@Composable
fun CurrencyBadge(amount: Long, icon: ImageVector, color: Color) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(13.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = amount.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun HintShopItemCard(
    hintsCount: Int,
    price: Int,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp, pressedElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Glowing circular icon badge
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .shadow(4.dp, CircleShape, spotColor = EasyGreen.copy(alpha = 0.4f))
                    .background(EasyGreen.copy(alpha = 0.14f), CircleShape)
                    .border(1.5.dp, EasyGreen.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = EasyGreen,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "$hintsCount Hints",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Price CTA Pill
            Surface(
                color = AccentGold.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = AccentGold,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = price.toString(),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = AccentGold
                    )
                }
            }
        }
    }
}

@Composable
fun ShopItemCard(
    title: String,
    price: Int,
    isUnlocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardColor = if (isUnlocked) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(enabled = !isUnlocked) { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isUnlocked) 1.dp else 3.dp,
            pressedElevation = 6.dp
        ),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(
            1.dp,
            if (isUnlocked) EasyGreen.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val badgeColor = if (isUnlocked) EasyGreen else MaterialTheme.colorScheme.primary
            
            // Icon Badge
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .shadow(4.dp, CircleShape, spotColor = badgeColor.copy(alpha = 0.35f))
                    .background(badgeColor.copy(alpha = 0.14f), CircleShape)
                    .border(1.5.dp, badgeColor.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isUnlocked) Icons.Default.Check else Icons.Default.Lock,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom CTA: Price Chip vs Unlocked Badge
            if (isUnlocked) {
                Surface(
                    color = EasyGreen.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, EasyGreen.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "UNLOCKED",
                            color = EasyGreen,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            } else {
                Surface(
                    color = AccentGold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = AccentGold,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = price.toString(),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = AccentGold
                        )
                    }
                }
            }
        }
    }
}
