package com.prem.skudo.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prem.skudo.ui.theme.*

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
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(PrimaryCyan.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = PrimaryCyan,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Master the Grid",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "From core rules to master solver techniques.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            }

            // Lesson 1: Core Rules
            AcademyLessonCard(
                lessonNumber = "01",
                title = "The 3 Golden Rules",
                icon = Icons.Default.CheckCircleOutline,
                accentColor = EasyGreen,
                content = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        RuleBullet(
                            title = "Rows",
                            desc = "Every horizontal row must contain digits 1 through 9 with no repeats."
                        )
                        RuleBullet(
                            title = "Columns",
                            desc = "Every vertical column must contain digits 1 through 9 with no repeats."
                        )
                        RuleBullet(
                            title = "3×3 Boxes",
                            desc = "Each of the 9 subgrids must contain all digits from 1 to 9."
                        )
                    }
                }
            )

            // Lesson 2: Cross-Hatching
            AcademyLessonCard(
                lessonNumber = "02",
                title = "Scanning (Cross-Hatching)",
                icon = Icons.Default.FilterCenterFocus,
                accentColor = PrimaryCyan,
                content = {
                    Text(
                        text = "Scan rows and columns that intersect a 3×3 box. If a number already exists in those rows or columns, it cannot be placed anywhere in that line. Often, this leaves only one available cell in the box for that number!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        lineHeight = 20.sp
                    )
                }
            )

            // Lesson 3: Notes Mode & Naked Singles
            AcademyLessonCard(
                lessonNumber = "03",
                title = "Pencil Notes & Naked Singles",
                icon = Icons.Default.Edit,
                accentColor = AccentGold,
                content = {
                    Text(
                        text = "Tap the Pencil icon to toggle Notes Mode. Write down possible numbers for challenging cells. When a cell only has one candidate left after eliminating row, column, and box conflicts, that number is a 'Naked Single' and is guaranteed to be correct!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        lineHeight = 20.sp
                    )
                }
            )

            // Lesson 4: Hidden Singles & Line Elimination
            AcademyLessonCard(
                lessonNumber = "04",
                title = "Hidden Singles",
                icon = Icons.Default.Lightbulb,
                accentColor = HardPurple,
                content = {
                    Text(
                        text = "Even if a cell has multiple candidates written in its notes, check if a specific digit appears only once across the entire row, column, or 3×3 box. If it does, that cell must be that digit!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        lineHeight = 20.sp
                    )
                }
            )

            // Practice CTA Button
            Button(
                onClick = onStartPractice,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(top = 8.dp),
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

@Composable
private fun AcademyLessonCard(
    lessonNumber: String,
    title: String,
    icon: ImageVector,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
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
                    color = accentColor.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun RuleBullet(title: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(6.dp)
                .background(EasyGreen, CircleShape)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                lineHeight = 16.sp
            )
        }
    }
}
