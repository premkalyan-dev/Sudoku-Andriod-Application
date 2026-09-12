package com.prem.skudo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.prem.skudo.ui.theme.AccentGold
import com.prem.skudo.ui.theme.EasyGreen
import com.prem.skudo.ui.theme.TextMuted

@Composable
fun DailyRewardDialog(
    streak: Int,
    coins: Long,
    isAlreadyClaimed: Boolean = false,
    onClaim: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val dayStreak = if (streak <= 0) 1 else streak
    val currentDayIndex = ((dayStreak - 1) % 7) + 1

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Top-right Close Button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Icon
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                if (isAlreadyClaimed) EasyGreen.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isAlreadyClaimed) Icons.Default.CalendarMonth else Icons.Default.CardGiftcard,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = if (isAlreadyClaimed) EasyGreen else MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Text(
                        "DAILY REWARD",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        if (isAlreadyClaimed) "Day $currentDayIndex claimed! Come back tomorrow for Day ${if (currentDayIndex >= 7) 1 else currentDayIndex + 1}."
                        else "Day $currentDayIndex of your login streak!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // 7-day Reward Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val rewardAmounts = listOf(25L, 40L, 60L, 75L, 100L, 150L, 250L)
                        
                        repeat(7) { index ->
                            val day = index + 1
                            val isClaimed = if (isAlreadyClaimed) day <= currentDayIndex else day < currentDayIndex
                            val isCurrent = day == currentDayIndex
                            val dayCoins = rewardAmounts[index]
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f).padding(horizontal = 2.dp)
                            ) {
                                Text(
                                    "D$day",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrent) MaterialTheme.colorScheme.primary else TextMuted,
                                    fontSize = 10.sp
                                )
                                
                                Spacer(modifier = Modifier.height(3.dp))
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .background(
                                            when {
                                                isClaimed -> EasyGreen.copy(alpha = 0.12f)
                                                isCurrent -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                day == 7 -> AccentGold.copy(alpha = 0.10f)
                                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            },
                                            RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            1.dp,
                                            when {
                                                isClaimed -> EasyGreen.copy(alpha = 0.35f)
                                                isCurrent -> MaterialTheme.colorScheme.primary
                                                day == 7 -> AccentGold.copy(alpha = 0.35f)
                                                else -> Color.Transparent
                                            },
                                            RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isClaimed) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Claimed",
                                            tint = EasyGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            if (day == 7) {
                                                Icon(
                                                    Icons.Default.CardGiftcard,
                                                    contentDescription = null,
                                                    tint = AccentGold,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            } else {
                                                Icon(
                                                    Icons.Default.MonetizationOn,
                                                    contentDescription = null,
                                                    tint = if (isCurrent) AccentGold else TextMuted.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                            Text(
                                                "$dayCoins",
                                                color = if (isCurrent) MaterialTheme.colorScheme.primary else TextMuted,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Reward Banner - "Today's Claim" in a single line
                    Surface(
                        color = AccentGold.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = AccentGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Today's Claim: $coins Coins",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = AccentGold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Awesome button / Daily Claim button
                    Button(
                        onClick = {
                            if (!isAlreadyClaimed) {
                                onClaim()
                            }
                        },
                        enabled = !isAlreadyClaimed,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = TextMuted
                        )
                    ) {
                        Text(
                            text = if (isAlreadyClaimed) "Come Back Tomorrow" else "AWESOME",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
