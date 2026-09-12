package com.prem.skudo.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prem.skudo.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SudokuAcademyScreen(
    onBack: () -> Unit,
    onStartPractice: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SUDOKU ACADEMY",
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero Header
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, PrimaryCyan.copy(alpha = 0.25f)),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(PrimaryCyan.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = PrimaryCyan,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Interactive Sudoku Academy",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tap Play on each tutorial to watch the animated mechanics step-by-step.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            }

            // TUTORIAL 1: THE GOLDEN RULES
            AcademyLessonCard(
                lessonNumber = "01",
                title = "The 3 Golden Rules",
                icon = Icons.Default.CheckCircleOutline,
                accentColor = EasyGreen,
                description = "Each digit from 1 to 9 can appear only ONCE in every row, column, and 3×3 box."
            ) {
                GoldenRulesTutorialAnimation()
            }

            // TUTORIAL 2: SCANNING (CROSS-HATCHING)
            AcademyLessonCard(
                lessonNumber = "02",
                title = "Scanning (Cross-Hatching)",
                icon = Icons.Default.FilterCenterFocus,
                accentColor = PrimaryCyan,
                description = "Scan rows and columns intersecting a 3×3 box to eliminate impossible cells and pinpoint where a number fits."
            ) {
                CrossHatchingTutorialAnimation()
            }

            // TUTORIAL 3: NOTES & HIDDEN SINGLES
            AcademyLessonCard(
                lessonNumber = "03",
                title = "Making Notes & Hidden Singles",
                icon = Icons.Default.Edit,
                accentColor = AccentGold,
                description = "Use pencil notes to record candidates. If a candidate appears in only ONE cell across a box, it is a Hidden Single!"
            ) {
                NotesAndHiddenSinglesTutorialAnimation()
            }

            // Practice CTA Button
            Button(
                onClick = onStartPractice,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "START PRACTICE GAME",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TUTORIAL 1: GOLDEN RULES ANIMATION
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun GoldenRulesTutorialAnimation() {
    var isPlaying by remember { mutableStateOf(false) }
    var currentStep by remember { mutableIntStateOf(0) }
    var playToken by remember { mutableIntStateOf(0) }
    val totalSteps = 4

    // Run animation sequence when playToken is triggered
    LaunchedEffect(playToken) {
        if (playToken > 0) {
            isPlaying = true
            currentStep = 1 // Row
            delay(2800)
            currentStep = 2 // Column
            delay(2800)
            currentStep = 3 // 3x3 Box
            delay(2800)
            currentStep = 4 // Duplicate Conflict & Conclusion
            delay(3200)
            isPlaying = false
        }
    }

    // Mini 9x9 sample data
    // Row 1 (index 1): 1..9 in sequence for row rule
    // Col 4 (index 4): 1..9 in sequence for col rule
    // Box 0 (rows 0..2, cols 0..2): 1..9 in sequence for box rule
    val rowValues = remember { listOf(1, 2, 3, 4, 5, 6, 7, 8, 9) }
    val colValues = remember { listOf(1, 2, 3, 4, 5, 6, 7, 8, 9) }
    val boxValues = remember { listOf(1, 2, 3, 4, 5, 6, 7, 8, 9) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Controls Header (Play / Replay button & Step Pill)
        TutorialControlsBar(
            step = currentStep,
            totalSteps = totalSteps,
            isPlaying = isPlaying,
            hasStarted = playToken > 0,
            accentColor = EasyGreen,
            onPlayClick = {
                playToken++
            },
            onStepSelect = { selectedStep ->
                currentStep = selectedStep
                isPlaying = false
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Mini 9x9 Board Visualization
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(6.dp)) {
                for (r in 0 until 9) {
                    Row {
                        for (c in 0 until 9) {
                            val inRowTarget = currentStep == 1 && r == 1
                            val inColTarget = currentStep == 2 && c == 4
                            val inBoxTarget = currentStep == 3 && (r < 3 && c < 3)
                            val isConflict = currentStep == 4 && r == 6 && (c == 2 || c == 7)

                            // Cell value resolution
                            val displayValue = when {
                                inRowTarget -> rowValues[c]
                                inColTarget -> colValues[r]
                                inBoxTarget -> boxValues[r * 3 + c]
                                isConflict -> 7
                                currentStep == 0 -> {
                                    // Static sample clues
                                    if ((r == 0 && c == 2) || (r == 1 && c == 5) || (r == 4 && c == 4) || (r == 7 && c == 1)) {
                                        (r + c) % 9 + 1
                                    } else null
                                }
                                else -> null
                            }

                            val cellBg by animateColorAsState(
                                targetValue = when {
                                    isConflict -> WrongRed.copy(alpha = 0.25f)
                                    inRowTarget -> EasyGreen.copy(alpha = 0.22f)
                                    inColTarget -> PrimaryCyan.copy(alpha = 0.22f)
                                    inBoxTarget -> AccentGold.copy(alpha = 0.22f)
                                    else -> Color.Transparent
                                },
                                animationSpec = tween(300),
                                label = "cellBg"
                            )

                            val borderRight = if ((c + 1) % 3 == 0 && c != 8) 1.5.dp else 0.5.dp
                            val borderBottom = if ((r + 1) % 3 == 0 && r != 8) 1.5.dp else 0.5.dp
                            val borderColor = if ((c + 1) % 3 == 0 || (r + 1) % 3 == 0) {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                            } else {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                            }

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(cellBg)
                                    .border(
                                        width = if (isConflict) 1.5.dp else borderRight,
                                        color = if (isConflict) WrongRed else borderColor
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (displayValue != null) {
                                    Text(
                                        text = displayValue.toString(),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.SansSerif,
                                        color = when {
                                            isConflict -> WrongRed
                                            inRowTarget -> EasyGreen
                                            inColTarget -> PrimaryCyan
                                            inBoxTarget -> AccentGold
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Step Explanatory Caption
        val (badgeTitle, explanation) = when (currentStep) {
            1 -> "Rule 1: Rows (1–9)" to "Every horizontal row must contain numbers 1 through 9 with no duplicate digits."
            2 -> "Rule 2: Columns (1–9)" to "Every vertical column must contain numbers 1 through 9 with no duplicate digits."
            3 -> "Rule 3: 3×3 Boxes (1–9)" to "Each 3×3 subgrid must contain numbers 1 through 9. Each digit appears exactly once."
            4 -> "Duplicate Conflict" to "Notice: placing two 7s in the same row violates the rule! Duplicates are invalid."
            else -> "Ready to Learn" to "Tap 'Play Animation' to see rows, columns, 3×3 boxes, and duplicate conflicts."
        }

        TutorialCaptionCard(
            stepNumber = currentStep,
            badgeTitle = badgeTitle,
            description = explanation,
            badgeColor = when (currentStep) {
                1 -> EasyGreen
                2 -> PrimaryCyan
                3 -> AccentGold
                4 -> WrongRed
                else -> TextMuted
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TUTORIAL 2: SCANNING (CROSS-HATCHING) ANIMATION
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun CrossHatchingTutorialAnimation() {
    var isPlaying by remember { mutableStateOf(false) }
    var currentStep by remember { mutableIntStateOf(0) }
    var playToken by remember { mutableIntStateOf(0) }
    val totalSteps = 4

    LaunchedEffect(playToken) {
        if (playToken > 0) {
            isPlaying = true
            currentStep = 1 // Scan Row 0
            delay(2800)
            currentStep = 2 // Scan Row 1
            delay(2800)
            currentStep = 3 // Scan Column 7
            delay(2800)
            currentStep = 4 // Only 1 Spot Left -> Place 5!
            delay(3200)
            isPlaying = false
        }
    }

    val targetPulse = rememberInfiniteTransition(label = "pulse")
    val pulseScale by targetPulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulseScale"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TutorialControlsBar(
            step = currentStep,
            totalSteps = totalSteps,
            isPlaying = isPlaying,
            hasStarted = playToken > 0,
            accentColor = PrimaryCyan,
            onPlayClick = {
                playToken++
            },
            onStepSelect = { selectedStep ->
                currentStep = selectedStep
                isPlaying = false
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 9x9 Board with scanning beams
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(6.dp)) {
                for (r in 0 until 9) {
                    Row {
                        for (c in 0 until 9) {
                            val inTargetBox = (r < 3 && c >= 6) // Top-Right 3x3 Box
                            val isBeamRow0 = currentStep >= 1 && r == 0
                            val isBeamRow1 = currentStep >= 2 && r == 1
                            val isBeamCol7 = currentStep >= 3 && c == 7
                            val isSource5 = (r == 0 && c == 1) || (r == 1 && c == 4) || (r == 6 && c == 7)
                            val isTargetCell = r == 2 && c == 6
                            val isBlockedInBox = inTargetBox && !isTargetCell && (
                                (currentStep >= 1 && r == 0) ||
                                (currentStep >= 2 && r == 1) ||
                                (currentStep >= 3 && c == 7) ||
                                (r == 2 && c == 8) // Existing clue 9 at (2,8)
                            )

                            // Existing clues in top-right box
                            val fixedClueInBox = when {
                                r == 0 && c == 7 -> 1
                                r == 1 && c == 8 -> 8
                                r == 2 && c == 8 -> 9
                                else -> null
                            }

                            val cellBg by animateColorAsState(
                                targetValue = when {
                                    isTargetCell && currentStep >= 4 -> EasyGreen.copy(alpha = 0.35f)
                                    isTargetCell && currentStep == 3 -> PrimaryCyan.copy(alpha = 0.25f)
                                    isBlockedInBox -> WrongRed.copy(alpha = 0.18f)
                                    isSource5 -> PrimaryCyan.copy(alpha = 0.30f)
                                    isBeamRow0 || isBeamRow1 || isBeamCol7 -> PrimaryCyan.copy(alpha = 0.10f)
                                    inTargetBox -> AccentGold.copy(alpha = 0.08f)
                                    else -> Color.Transparent
                                },
                                animationSpec = tween(250),
                                label = "crossCellBg"
                            )

                            val borderRight = if ((c + 1) % 3 == 0 && c != 8) 1.5.dp else 0.5.dp
                            val borderColor = if ((c + 1) % 3 == 0 || (r + 1) % 3 == 0) {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                            } else {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                            }

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(cellBg)
                                    .border(
                                        width = if (isTargetCell && currentStep >= 4) 2.dp else borderRight,
                                        color = if (isTargetCell && currentStep >= 4) EasyGreen else borderColor
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    isSource5 -> {
                                        Text(
                                            text = "5",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = PrimaryCyan
                                        )
                                    }
                                    isTargetCell && currentStep >= 4 -> {
                                        Text(
                                            text = "5",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black,
                                            color = EasyGreen,
                                            modifier = Modifier.scale(pulseScale)
                                        )
                                    }
                                    isTargetCell && currentStep == 3 -> {
                                        // Target open cell
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(PrimaryCyan, CircleShape)
                                        )
                                    }
                                    fixedClueInBox != null -> {
                                        Text(
                                            text = fixedClueInBox.toString(),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                    isBlockedInBox -> {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp),
                                            tint = WrongRed.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val (badgeTitle, explanation) = when (currentStep) {
            1 -> "Step 1: Scan Row 1" to "Cell (0,1) already has a 5. A beam across Row 1 blocks (0,6) and (0,8) in the box."
            2 -> "Step 2: Scan Row 2" to "Cell (1,4) already has a 5. A beam across Row 2 blocks (1,6) and (1,7) in the box."
            3 -> "Step 3: Scan Column 8" to "Column 8 has a 5 below. It blocks column cells in the box. Only (2,6) remains open!"
            4 -> "Step 4: Solved!" to "Only ONE cell in the top-right 3×3 box can hold 5! It is confidently placed."
            else -> "Ready to Scan" to "Tap 'Play Animation' to watch horizontal and vertical laser scanning eliminate cells."
        }

        TutorialCaptionCard(
            stepNumber = currentStep,
            badgeTitle = badgeTitle,
            description = explanation,
            badgeColor = when (currentStep) {
                1, 2, 3 -> PrimaryCyan
                4 -> EasyGreen
                else -> TextMuted
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TUTORIAL 3: MAKING NOTES & HIDDEN SINGLES ANIMATION
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun NotesAndHiddenSinglesTutorialAnimation() {
    var isPlaying by remember { mutableStateOf(false) }
    var currentStep by remember { mutableIntStateOf(0) }
    var playToken by remember { mutableIntStateOf(0) }
    val totalSteps = 4

    LaunchedEffect(playToken) {
        if (playToken > 0) {
            isPlaying = true
            currentStep = 1 // Notes Mode Activated
            delay(2600)
            currentStep = 2 // Candidates added
            delay(2800)
            currentStep = 3 // Spot Hidden Single '4'
            delay(3000)
            currentStep = 4 // 4 Placed, notes resolved
            delay(3200)
            isPlaying = false
        }
    }

    val glowTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by glowTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glowAlpha"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TutorialControlsBar(
            step = currentStep,
            totalSteps = totalSteps,
            isPlaying = isPlaying,
            hasStarted = playToken > 0,
            accentColor = AccentGold,
            onPlayClick = {
                playToken++
            },
            onStepSelect = { selectedStep ->
                currentStep = selectedStep
                isPlaying = false
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Large 3x3 Block Focused View
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                // Fixed Clues & Candidate Sets
                val cellConfig = listOf(
                    Triple(3, emptySet<Int>(), false),                 // (0,0): Clue 3
                    Triple(null, setOf(2, 7), false),                  // (0,1): Notes 2,7
                    Triple(9, emptySet<Int>(), false),                 // (0,2): Clue 9
                    Triple(null, setOf(6, 8), false),                  // (1,0): Notes 6,8
                    Triple(null, setOf(2, 4, 8), true),                // (1,1): HIDDEN SINGLE TARGET!
                    Triple(5, emptySet<Int>(), false),                 // (1,2): Clue 5
                    Triple(null, setOf(6, 7), false),                  // (2,0): Notes 6,7
                    Triple(1, emptySet<Int>(), false),                 // (2,1): Clue 1
                    Triple(null, setOf(2, 8), false)                   // (2,2): Notes 2,8
                )

                for (r in 0 until 3) {
                    Row {
                        for (c in 0 until 3) {
                            val index = r * 3 + c
                            val (clueVal, candidateSet, isTarget) = cellConfig[index]

                            val isTargetCell = isTarget && (r == 1 && c == 1)

                            val cellBg by animateColorAsState(
                                targetValue = when {
                                    isTargetCell && currentStep >= 4 -> EasyGreen.copy(alpha = 0.30f)
                                    isTargetCell && currentStep == 3 -> AccentGold.copy(alpha = 0.25f)
                                    isTargetCell && currentStep >= 1 -> PrimaryCyan.copy(alpha = 0.08f)
                                    else -> MaterialTheme.colorScheme.surface
                                },
                                animationSpec = tween(300),
                                label = "notesCellBg"
                            )

                            Surface(
                                modifier = Modifier
                                    .size(68.dp)
                                    .padding(3.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = cellBg,
                                border = BorderStroke(
                                    width = if (isTargetCell && currentStep >= 3) 2.dp else 1.dp,
                                    color = when {
                                        isTargetCell && currentStep >= 4 -> EasyGreen
                                        isTargetCell && currentStep == 3 -> AccentGold
                                        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    }
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    when {
                                        // Placed Hidden Single '4' at step 4
                                        isTargetCell && currentStep >= 4 -> {
                                            Text(
                                                text = "4",
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Black,
                                                color = EasyGreen
                                            )
                                        }
                                        // Fixed Clue
                                        clueVal != null -> {
                                            Text(
                                                text = clueVal.toString(),
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        // Pencil Notes active
                                        currentStep >= 2 -> {
                                            // 3x3 Mini Pencil Grid inside the cell
                                            Column(
                                                modifier = Modifier.fillMaxSize().padding(4.dp),
                                                verticalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                for (nr in 0 until 3) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        for (nc in 1..3) {
                                                            val num = nr * 3 + nc
                                                            val isPresent = candidateSet.contains(num)
                                                            val isTheHiddenSingle = isTargetCell && num == 4 && currentStep == 3

                                                            Text(
                                                                text = if (isPresent) num.toString() else "",
                                                                fontSize = if (isTheHiddenSingle) 13.sp else 10.sp,
                                                                lineHeight = 10.sp,
                                                                fontWeight = if (isTheHiddenSingle) FontWeight.Black else FontWeight.SemiBold,
                                                                color = when {
                                                                    isTheHiddenSingle -> AccentGold.copy(alpha = glowAlpha)
                                                                    isPresent && isTargetCell && currentStep == 3 -> TextMuted.copy(alpha = 0.35f)
                                                                    isPresent -> TextMuted
                                                                    else -> Color.Transparent
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val (badgeTitle, explanation) = when (currentStep) {
            1 -> "Step 1: Pencil Mode" to "Tap the Pencil tool. In Notes Mode, tapping digits writes pencil candidates into the cell."
            2 -> "Step 2: Candidate Notes" to "All possible candidates are recorded in empty cells based on row and column constraints."
            3 -> "Step 3: Spot Hidden Single" to "Look at digit 4: It appears in only ONE cell in this entire 3×3 box, even with other notes!"
            4 -> "Step 4: Solved!" to "Because 4 cannot go anywhere else in this box, it MUST be 4! Candidate notes collapse into 4."
            else -> "Ready to Learn" to "Tap 'Play Animation' to see how notes are recorded and hidden singles are resolved."
        }

        TutorialCaptionCard(
            stepNumber = currentStep,
            badgeTitle = badgeTitle,
            description = explanation,
            badgeColor = when (currentStep) {
                1, 2 -> AccentGold
                3 -> MediumGold
                4 -> EasyGreen
                else -> TextMuted
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SHARED HELPER COMPONENTS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TutorialControlsBar(
    step: Int,
    totalSteps: Int,
    isPlaying: Boolean,
    hasStarted: Boolean,
    accentColor: Color,
    onPlayClick: () -> Unit,
    onStepSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Step Pills
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (s in 1..totalSteps) {
                val isCurrent = step == s
                val isCompleted = step > s

                Surface(
                    onClick = { onStepSelect(s) },
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isCurrent -> accentColor
                        isCompleted -> accentColor.copy(alpha = 0.25f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    },
                    modifier = Modifier.height(24.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Step $s",
                            fontSize = 11.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                            color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Play / Replay Button
        Button(
            onClick = onPlayClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (hasStarted) accentColor.copy(alpha = 0.15f) else accentColor,
                contentColor = if (hasStarted) accentColor else Color.White
            ),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            modifier = Modifier.height(34.dp),
            border = if (hasStarted) BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)) else null
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Refresh else if (hasStarted) Icons.Default.Replay else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isPlaying) "Playing..." else if (hasStarted) "Replay" else "Play",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun TutorialCaptionCard(
    stepNumber: Int,
    badgeTitle: String,
    description: String,
    badgeColor: Color
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(badgeColor, CircleShape)
                )
                Text(
                    text = badgeTitle,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun AcademyLessonCard(
    lessonNumber: String,
    title: String,
    icon: ImageVector,
    accentColor: Color,
    description: String,
    animationContent: @Composable () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(accentColor.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = lessonNumber,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor.copy(alpha = 0.85f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Animated Interactive Canvas
            animationContent()
        }
    }
}
