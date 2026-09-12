package com.prem.skudo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prem.skudo.ui.theme.AccentGold
import com.prem.skudo.ui.theme.EasyGreen
import com.prem.skudo.ui.theme.TextMuted

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
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Streak Banner ──
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
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(AccentGold.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = AccentGold,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "$dayStreak Day Streak!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (isAlreadyClaimed)
                                "Come back tomorrow to keep it going"
                            else
                                "Claim today's reward to continue",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 7 Day Cards ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(7) { index ->
                    val day = index + 1
                    val isClaimed = if (isAlreadyClaimed) day <= currentDayIndex else day < currentDayIndex
                    val isCurrent = day == currentDayIndex && !isAlreadyClaimed
                    val dayCoins = rewardAmounts[index]
                    val isBonus = day == 7

                    DayRewardRow(
                        day = day,
                        coins = dayCoins,
                        isClaimed = isClaimed,
                        isCurrent = isCurrent,
                        isBonus = isBonus,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
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
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun DayRewardRow(
    day: Int,
    coins: Long,
    isClaimed: Boolean,
    isCurrent: Boolean,
    isBonus: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isClaimed -> EasyGreen.copy(alpha = 0.08f)
        isCurrent -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        isBonus -> AccentGold.copy(alpha = 0.06f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    }

    val borderColor = when {
        isClaimed -> EasyGreen.copy(alpha = 0.3f)
        isCurrent -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        isBonus -> AccentGold.copy(alpha = 0.25f)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(12.dp))
            .border(
                if (isCurrent) 1.5.dp else 1.dp,
                borderColor,
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            // Day number circle
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        when {
                            isClaimed -> EasyGreen.copy(alpha = 0.15f)
                            isCurrent -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            isBonus -> AccentGold.copy(alpha = 0.12f)
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isClaimed) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Claimed",
                        tint = EasyGreen,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        "$day",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = when {
                            isCurrent -> MaterialTheme.colorScheme.primary
                            isBonus -> AccentGold
                            else -> TextMuted
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Day label and status
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isBonus) "Day $day — Bonus!" else "Day $day",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isCurrent || isBonus) FontWeight.Bold else FontWeight.Medium,
                    color = when {
                        isClaimed -> EasyGreen
                        isCurrent -> MaterialTheme.colorScheme.onBackground
                        else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    }
                )
                Text(
                    text = when {
                        isClaimed -> "Claimed"
                        isCurrent -> "Available now"
                        else -> "Upcoming"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        isClaimed -> EasyGreen.copy(alpha = 0.7f)
                        isCurrent -> MaterialTheme.colorScheme.primary
                        else -> TextMuted.copy(alpha = 0.7f)
                    },
                    fontSize = 11.sp
                )
            }

            // Coins
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isBonus) Icons.Default.CardGiftcard else Icons.Default.MonetizationOn,
                    contentDescription = null,
                    tint = when {
                        isClaimed -> EasyGreen.copy(alpha = 0.6f)
                        isCurrent -> AccentGold
                        isBonus -> AccentGold
                        else -> TextMuted.copy(alpha = 0.5f)
                    },
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$coins",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = when {
                        isClaimed -> EasyGreen.copy(alpha = 0.6f)
                        isCurrent -> AccentGold
                        isBonus -> AccentGold
                        else -> TextMuted
                    }
                )
            }
        }
    }
}
