package com.prem.skudo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
                title = { Text("Coin Shop", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    CurrencyBadge(uiState.hints.toLong(), Icons.Default.Lightbulb, EasyGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    CurrencyBadge(uiState.coins, Icons.Default.MonetizationOn, AccentGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    CurrencyBadge(uiState.gems, Icons.Default.Diamond, Color(0xFF00CED1))
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
                .padding(16.dp)
        ) {
            Text(
                "Buy Hints",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val hintPackages = listOf(
                Triple(5, 200, Icons.Default.Lightbulb),
                Triple(15, 500, Icons.Default.TipsAndUpdates),
                Triple(35, 1000, Icons.Default.EmojiObjects),
                Triple(60, 1500, Icons.Default.Psychology)
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Game Themes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val themes = listOf("Ocean", "Forest", "Neon", "Gold")
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
        }
    }
}

@Composable
fun CurrencyBadge(amount: Long, icon: ImageVector, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                amount.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground
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
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(EasyGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = EasyGreen, modifier = Modifier.size(32.dp))
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text("$hintsCount Hints", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MonetizationOn, null, tint = AccentGold, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(price.toString(), fontWeight = FontWeight.Bold, color = AccentGold)
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
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !isUnlocked) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isUnlocked) Icons.Default.Check else Icons.Default.Lock,
                    null,
                    tint = if (isUnlocked) EasyGreen else MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            
            Spacer(modifier = Modifier.height(4.dp))
            
            if (isUnlocked) {
                Text("UNLOCKED", color = EasyGreen, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MonetizationOn, null, tint = AccentGold, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(price.toString(), fontWeight = FontWeight.Bold, color = AccentGold)
                }
            }
        }
    }
}
