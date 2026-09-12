package com.prem.skudo.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prem.skudo.ui.theme.AccentGold
import com.prem.skudo.ui.theme.EasyGreen
import com.prem.skudo.ui.theme.TextMuted
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyRewardScreen(
    streak: Int,
    coins: Long,
    isAlreadyClaimed: Boolean = false,
    onClaim: () -> Unit = {},
    onBack: () -> Unit
) {
    val dayStreak = if (streak <= 0) 1 else streak
    val currentDayIndex = ((dayStreak - 1) % 7) + 1
    val rewardAmounts = listOf(25L, 40L, 60L, 75L, 100L, 150L, 250L)

    // Staggered card animation
    val cardVisibilities = remember { List(7) { mutableStateOf(false) } }
    LaunchedEffect(Unit) {
        for (i in cardVisibilities.indices) {
            delay(80L)
            cardVisibilities[i].value = true
        }
    }

    // Pulsing animation for the current day card
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Header fade-in
    var headerVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(50)
        headerVisible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Daily Rewards",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // ── Streak Banner ──
            AnimatedVisibility(
                visible = headerVisible,
                enter = fadeIn(tween(400)) + slideInVertically(
                    initialOffsetY = { -40 },
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                )
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Transparent,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        AccentGold.copy(alpha = 0.10f)
                                    )
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 24.dp, vertical = 20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Flame icon
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(
                                        AccentGold.copy(alpha = 0.15f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = AccentGold,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = "$dayStreak Day Streak!",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = if (isAlreadyClaimed)
                                        "Come back tomorrow to keep your streak alive"
                                    else
                                        "Claim today's reward to keep your streak going",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }

            // ── Section Label ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "THIS WEEK'S REWARDS",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
            }

            // ── 7-Day Reward Grid: Row of 4 + Row of 3 ──
            // First row: Days 1–4
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (i in 0..3) {
                    val day = i + 1
                    val isClaimed = if (isAlreadyClaimed) day <= currentDayIndex else day < currentDayIndex
                    val isCurrent = day == currentDayIndex && !isAlreadyClaimed
                    val dayCoins = rewardAmounts[i]

                    AnimatedVisibility(
                        visible = cardVisibilities[i].value,
                        enter = fadeIn(tween(300)) + scaleIn(
                            initialScale = 0.8f,
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        DayRewardCard(
                            day = day,
                            coins = dayCoins,
                            isClaimed = isClaimed,
                            isCurrent = isCurrent,
                            isBonus = false,
                            pulseScale = if (isCurrent) pulseScale else 1f
                        )
                    }
                }
            }

            // Second row: Days 5–7
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (i in 4..6) {
                    val day = i + 1
                    val isClaimed = if (isAlreadyClaimed) day <= currentDayIndex else day < currentDayIndex
                    val isCurrent = day == currentDayIndex && !isAlreadyClaimed
                    val dayCoins = rewardAmounts[i]

                    AnimatedVisibility(
                        visible = cardVisibilities[i].value,
                        enter = fadeIn(tween(300)) + scaleIn(
                            initialScale = 0.8f,
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        DayRewardCard(
                            day = day,
                            coins = dayCoins,
                            isClaimed = isClaimed,
                            isCurrent = isCurrent,
                            isBonus = day == 7,
                            pulseScale = if (isCurrent) pulseScale else 1f
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── Today's Reward Highlight ──
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = if (isAlreadyClaimed) listOf(
                                    EasyGreen.copy(alpha = 0.08f),
                                    EasyGreen.copy(alpha = 0.04f)
                                ) else listOf(
                                    AccentGold.copy(alpha = 0.12f),
                                    AccentGold.copy(alpha = 0.05f)
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .border(
                            1.dp,
                            if (isAlreadyClaimed) EasyGreen.copy(alpha = 0.2f)
                            else AccentGold.copy(alpha = 0.25f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Coin icon
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    if (isAlreadyClaimed) EasyGreen.copy(alpha = 0.12f)
                                    else AccentGold.copy(alpha = 0.15f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (isAlreadyClaimed) Icons.Default.CheckCircle
                                else Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = if (isAlreadyClaimed) EasyGreen else AccentGold,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (isAlreadyClaimed) "Today's Reward Claimed!"
                            else "Today's Reward",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = AccentGold,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$coins Coins",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = AccentGold
                            )
                        }

                        if (isAlreadyClaimed) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Day ${if (currentDayIndex >= 7) 1 else currentDayIndex + 1} reward is waiting for you tomorrow!",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // ── Claim / Come Back Button ──
            Button(
                onClick = {
                    if (!isAlreadyClaimed) {
                        onClaim()
                    }
                },
                enabled = !isAlreadyClaimed,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = TextMuted
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 1.dp
                )
            ) {
                Icon(
                    if (isAlreadyClaimed) Icons.Default.CalendarMonth
                    else Icons.Default.CardGiftcard,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isAlreadyClaimed) "Come Back Tomorrow" else "CLAIM REWARD",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DayRewardCard(
    day: Int,
    coins: Long,
    isClaimed: Boolean,
    isCurrent: Boolean,
    isBonus: Boolean,
    pulseScale: Float
) {
    val bgColor = when {
        isClaimed -> EasyGreen.copy(alpha = 0.10f)
        isCurrent -> MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        isBonus -> AccentGold.copy(alpha = 0.08f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    }

    val borderColor = when {
        isClaimed -> EasyGreen.copy(alpha = 0.35f)
        isCurrent -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        isBonus -> AccentGold.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
    }

    val iconTint = when {
        isClaimed -> EasyGreen
        isCurrent -> AccentGold
        isBonus -> AccentGold
        else -> TextMuted.copy(alpha = 0.5f)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Day label
        Text(
            text = "Day $day",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Medium,
            color = when {
                isCurrent -> MaterialTheme.colorScheme.primary
                isClaimed -> EasyGreen
                else -> TextMuted
            },
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.78f)
                .scale(pulseScale)
                .background(bgColor, RoundedCornerShape(14.dp))
                .border(
                    if (isCurrent) 2.dp else 1.dp,
                    borderColor,
                    RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isClaimed) {
                // Claimed state
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Claimed",
                        tint = EasyGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "$coins",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EasyGreen.copy(alpha = 0.7f)
                    )
                }
            } else {
                // Unclaimed / current / future
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        if (isBonus) Icons.Default.CardGiftcard else Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(if (isBonus) 22.dp else 20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "$coins",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = when {
                            isCurrent -> MaterialTheme.colorScheme.primary
                            isBonus -> AccentGold
                            else -> TextMuted
                        }
                    )
                }
            }

            // Bonus badge for Day 7
            if (isBonus && !isClaimed) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-4).dp, y = 4.dp)
                        .background(AccentGold, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        "BONUS",
                        fontSize = 7.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
