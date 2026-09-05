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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prem.skudo.ui.theme.*
import com.prem.skudo.viewmodel.ShopViewModel

data class HintPack(
    val name: String,
    val hintsCount: Int,
    val price: Int,
    val description: String,
    val headerIcon: ImageVector,
    val isBestValue: Boolean = false
)

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
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    CurrencyBadge(uiState.hints.toLong(), Icons.Default.Lightbulb, EasyGreen)
                    Spacer(modifier = Modifier.width(4.dp))
                    CurrencyBadge(uiState.coins, Icons.Default.MonetizationOn, AccentGold)
                    Spacer(modifier = Modifier.width(4.dp))
                    CurrencyBadge(uiState.gems, Icons.Default.Diamond, GemCyan)
                    Spacer(modifier = Modifier.width(8.dp))
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
            // Hint Shop Header with Shop Icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Hint Shop",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Spend your coins to purchase hint packs.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
            Spacer(modifier = Modifier.height(16.dp))

            // 4 Full-Width Hint Pack Tiers
            val hintPacks = listOf(
                HintPack(
                    name = "Starter Pack",
                    hintsCount = 5,
                    price = 200,
                    description = "Quick boost when you get stuck on tricky cells.",
                    headerIcon = Icons.Default.Star
                ),
                HintPack(
                    name = "Popular Pack",
                    hintsCount = 15,
                    price = 500,
                    description = "Great balance of hints for everyday casual puzzles.",
                    headerIcon = Icons.Default.AutoAwesome
                ),
                HintPack(
                    name = "Super Pack",
                    hintsCount = 35,
                    price = 1000,
                    description = "More hints to master hard & expert difficulty boards.",
                    headerIcon = Icons.Default.WorkspacePremium
                ),
                HintPack(
                    name = "Mega Value Pack",
                    hintsCount = 60,
                    price = 1500,
                    description = "Maximum hints per coin. Save the most with this pack!",
                    headerIcon = Icons.Default.RocketLaunch,
                    isBestValue = true
                )
            )

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                hintPacks.forEach { pack ->
                    HintPackCard(
                        pack = pack,
                        onBuy = { viewModel.buyHints(pack.hintsCount, pack.price.toLong()) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Game Themes Section
            SectionHeader(
                title = "Game Themes",
                subtitle = "Personalize your board styling & colors"
            )

            Spacer(modifier = Modifier.height(14.dp))

            val themes = listOf("Ocean", "Forest", "Neon", "Gold")

            // 2x2 Grid via Columns of Rows
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
fun HintPackCard(
    pack: HintPack,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = if (pack.isBestValue) 10.dp else 0.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (pack.isBestValue) 4.dp else 2.dp,
                pressedElevation = 4.dp
            ),
            border = if (pack.isBestValue) {
                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            } else {
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Top Row: (Header + Hints count on Left) & (Price + Coins label on Right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Column: Pack name + Hint count
                    Column(modifier = Modifier.weight(1f)) {
                        // 1. Header row: Icon + Bold Pack Name
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = pack.headerIcon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = pack.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // 2. Second row: Lightbulb + Hint count
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = EasyGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${pack.hintsCount} Hints",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = EasyGreen
                            )
                        }
                    }

                    // 3. Right Column: Coin Icon + Large bold price + Coins Label
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = AccentGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = pack.price.toString(),
                                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 22.sp),
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Coins",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 4. Description Line
                Text(
                    text = pack.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 5. Full-width "Buy" Button
                Button(
                    onClick = onBuy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 4.dp
                    )
                ) {
                    Text(
                        text = "Buy",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // Overlapping "BEST VALUE" Pill Badge
        if (pack.isBestValue) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 4.dp
            ) {
                Text(
                    text = "BEST VALUE",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                )
            }
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
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.5.dp,
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(11.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = amount.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
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
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !isUnlocked) { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isUnlocked) 1.dp else 3.dp,
            pressedElevation = 6.dp
        ),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(
            1.dp,
            if (isUnlocked) EasyGreen.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.outlineVariant
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
