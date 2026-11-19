package com.prem.skudo.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.prem.skudo.model.DailyRewardConfig
import com.prem.skudo.model.DailyRewardSchedule
import com.prem.skudo.model.RewardKind
import com.prem.skudo.repository.DailyRewardInfo
import com.prem.skudo.repository.OpenMysteryGiftResult
import com.prem.skudo.repository.ScheduleItemStatus
import com.prem.skudo.ui.theme.AccentGold
import com.prem.skudo.ui.theme.EasyGreen
import com.prem.skudo.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyRewardScreen(
    dailyRewardInfo: DailyRewardInfo? = null,
    streak: Int = dailyRewardInfo?.currentStreak ?: 1,
    coins: Long = dailyRewardInfo?.walletBalance ?: 0L,
    isAlreadyClaimed: Boolean = dailyRewardInfo?.isClaimedToday ?: false,
    isClaiming: Boolean = false,
    isOpeningGift: Boolean = false,
    rewardMessage: String? = null,
    mysteryGiftResult: OpenMysteryGiftResult? = null,
    onClaim: () -> Unit = {},
    onOpenGift: (String?) -> Unit = {},
    onDismissMessage: () -> Unit = {},
    onBack: () -> Unit
) {
    val info = dailyRewardInfo ?: DailyRewardInfo(
        currentStreak = streak,
        walletBalance = coins,
        hintInventory = 3,
        isClaimedToday = isAlreadyClaimed
    )

    var showGiftDialog by remember { mutableStateOf(false) }

    // If there is an unopened pending mystery gift or a fresh mystery gift result, show the dialog
    val hasPendingGift = info.pendingMysteryGift != null && !info.pendingMysteryGift.isRevealed
    LaunchedEffect(hasPendingGift, mysteryGiftResult) {
        if (hasPendingGift || mysteryGiftResult != null) {
            showGiftDialog = true
        }
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
                actions = {
                    // Currency Balances in Top Bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        // Coins Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AccentGold.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold.copy(alpha = 0.3f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = AccentGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${info.walletBalance}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = AccentGold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Hints Pill (Max 3)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = EasyGreen.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EasyGreen.copy(alpha = 0.3f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = EasyGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${info.hintInventory}/3",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = EasyGreen
                                )
                            }
                        }
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp))

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
                            text = "${info.currentStreak} Day Streak!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (info.isClaimedToday)
                                "Next reward resets at 12:00 AM IST"
                            else
                                "Claim today's reward to keep it going",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            }

            // ── Pending Mystery Gift Banner (if available to open) ──
            if (hasPendingGift) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    onClick = { showGiftDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    color = AccentGold.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, AccentGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CardGiftcard,
                            contentDescription = null,
                            tint = AccentGold,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Day 7 Mystery Gift Ready!",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 14.sp
                            )
                            Text(
                                "Tap here to open your mystery box",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = AccentGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── 7 Day Cards ──
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val schedule = if (info.schedule.isNotEmpty()) {
                    info.schedule
                } else {
                    DailyRewardSchedule.REWARDS.map { config ->
                        val status = when {
                            info.isClaimedToday && config.day <= info.currentCycleDay -> ScheduleItemStatus.CLAIMED
                            !info.isClaimedToday && config.day < info.currentCycleDay -> ScheduleItemStatus.CLAIMED
                            !info.isClaimedToday && config.day == info.currentCycleDay -> ScheduleItemStatus.AVAILABLE
                            else -> ScheduleItemStatus.LOCKED
                        }
                        com.prem.skudo.repository.DailyRewardScheduleItem(
                            day = config.day,
                            kind = config.kind,
                            coins = config.coins,
                            hints = config.hints,
                            hasMysteryGift = config.hasMysteryGift,
                            title = config.title,
                            subtitle = config.subtitle,
                            status = status
                        )
                    }
                }

                schedule.forEach { item ->
                    DayRewardRow(item = item)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Claim / Come Back Button ──
            Button(
                onClick = {
                    if (!info.isClaimedToday && !isClaiming) {
                        onClaim()
                    }
                },
                enabled = !info.isClaimedToday && !isClaiming,
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
                if (isClaiming) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CLAIMING...", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                } else {
                    Icon(
                        if (info.isClaimedToday) Icons.Default.CalendarMonth
                        else Icons.Default.CardGiftcard,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (info.isClaimedToday) "Come Back Tomorrow (12:00 AM IST)" else "CLAIM TODAY'S REWARD",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // ── Mystery Gift Dialog ──
    if (showGiftDialog) {
        MysteryGiftDialog(
            isOpening = isOpeningGift,
            pendingGift = info.pendingMysteryGift,
            result = mysteryGiftResult,
            onOpenGift = {
                onOpenGift(info.pendingMysteryGift?.giftId)
            },
            onDismiss = {
                showGiftDialog = false
                onDismissMessage()
            }
        )
    }
}

@Composable
private fun DayRewardRow(
    item: com.prem.skudo.repository.DailyRewardScheduleItem
) {
    val isClaimed = item.status == ScheduleItemStatus.CLAIMED
    val isAvailable = item.status == ScheduleItemStatus.AVAILABLE
    val isLocked = item.status == ScheduleItemStatus.LOCKED
    val isBonus = item.hasMysteryGift

    val bgColor = when {
        isClaimed -> EasyGreen.copy(alpha = 0.08f)
        isAvailable -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        isBonus -> AccentGold.copy(alpha = 0.08f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    }

    val borderColor = when {
        isClaimed -> EasyGreen.copy(alpha = 0.35f)
        isAvailable -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        isBonus -> AccentGold.copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(bgColor, RoundedCornerShape(12.dp))
            .border(
                if (isAvailable) 1.5.dp else 1.dp,
                borderColor,
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            // Day number circle
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(
                        when {
                            isClaimed -> EasyGreen.copy(alpha = 0.15f)
                            isAvailable -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            isBonus -> AccentGold.copy(alpha = 0.15f)
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
                        modifier = Modifier.size(18.dp)
                    )
                } else if (isLocked) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = TextMuted.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text(
                        "${item.day}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isBonus) AccentGold else MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Day label and status
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isBonus) "Day ${item.day} — Mystery Box!" else "Day ${item.day}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isAvailable || isBonus) FontWeight.Bold else FontWeight.Medium,
                    color = when {
                        isClaimed -> EasyGreen
                        isAvailable -> MaterialTheme.colorScheme.onBackground
                        else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    }
                )
                Text(
                    text = when {
                        isClaimed -> "Claimed"
                        isAvailable -> "Available now"
                        else -> "Upcoming"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        isClaimed -> EasyGreen.copy(alpha = 0.7f)
                        isAvailable -> MaterialTheme.colorScheme.primary
                        else -> TextMuted.copy(alpha = 0.7f)
                    },
                    fontSize = 11.sp
                )
            }

            // Reward badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                when {
                    item.hasMysteryGift -> {
                        Icon(
                            Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = AccentGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            "25 + ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = AccentGold
                        )
                        Icon(
                            Icons.Default.CardGiftcard,
                            contentDescription = null,
                            tint = AccentGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    item.kind == RewardKind.HINTS -> {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = if (isClaimed) EasyGreen.copy(alpha = 0.6f) else EasyGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${item.hints} Hints",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = if (isClaimed) EasyGreen.copy(alpha = 0.6f) else EasyGreen
                        )
                    }
                    else -> {
                        Icon(
                            Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = if (isClaimed) EasyGreen.copy(alpha = 0.6f) else AccentGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${item.coins}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = if (isClaimed) EasyGreen.copy(alpha = 0.6f) else AccentGold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MysteryGiftDialog(
    isOpening: Boolean,
    pendingGift: com.prem.skudo.repository.PendingGiftInfo?,
    result: OpenMysteryGiftResult?,
    onOpenGift: () -> Unit,
    onDismiss: () -> Unit
) {
    val isRevealed = result != null

    // Breathing / pulsing animation for unrevealed gift box
    val infiniteTransition = rememberInfiniteTransition(label = "gift_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Dialog(
        onDismissRequest = {
            if (!isOpening) onDismiss()
        },
        properties = DialogProperties(dismissOnBackPress = !isOpening, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isRevealed) {
                    // ── Gift Box Unopened State ──
                    Text(
                        text = "Day 7 Mystery Gift!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Tap the gift box below to open and reveal your mystery hint reward!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .scale(pulseScale)
                            .background(AccentGold.copy(alpha = 0.15f), CircleShape)
                            .border(2.dp, AccentGold, CircleShape)
                            .clickable(enabled = !isOpening) { onOpenGift() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CardGiftcard,
                            contentDescription = "Open Gift",
                            tint = AccentGold,
                            modifier = Modifier.size(56.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onOpenGift,
                        enabled = !isOpening,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = Color.Black)
                    ) {
                        if (isOpening) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("OPENING...", fontWeight = FontWeight.Bold)
                        } else {
                            Text("OPEN GIFT BOX", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = onDismiss,
                        enabled = !isOpening
                    ) {
                        Text("Open Later", color = TextMuted)
                    }
                } else {
                    // ── Revealed State ──
                    Text(
                        text = "Congratulations!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(EasyGreen.copy(alpha = 0.15f), CircleShape)
                            .border(2.dp, EasyGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = "Hints Won",
                            tint = EasyGreen,
                            modifier = Modifier.size(52.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "+${result.selectedHints} Hints!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = EasyGreen
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (result.creditedHints < result.selectedHints) {
                            "Inventory capacity is max 3 hints. Added ${result.creditedHints} hint(s) to your inventory."
                        } else {
                            "All ${result.selectedHints} hint(s) credited to your inventory."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "Current Hint Inventory: ${result.totalHintInventory}/3",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("AWESOME!", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
