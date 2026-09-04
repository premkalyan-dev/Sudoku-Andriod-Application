package com.prem.skudo.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prem.skudo.model.SudokuCell
import com.prem.skudo.ui.theme.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SudokuCellView(
    cell: SudokuCell,
    row: Int,
    col: Int,
    onClick: (Int, Int) -> Unit,
    onLongClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    boardStyle: String = "Modern",
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // 1. Organic, Smooth Color Transitions
    val targetBackgroundColor = when {
        cell.isAnimatingCompletion -> EasyGreen.copy(alpha = 0.45f)
        cell.isHighlighted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
        !cell.isValid -> MaterialTheme.colorScheme.error.copy(alpha = 0.18f)
        cell.isMatchingNumber -> MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
        cell.isRelated -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        else -> if (boardStyle == "Glass") Color.White.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
    }
    
    val backgroundColor by animateColorAsState(
        targetValue = targetBackgroundColor,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cellBackground"
    )

    val textColor = when {
        !cell.isValid -> MaterialTheme.colorScheme.error
        cell.isClue -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.primary
    }

    // 2. Buttery Smooth Tactile Scale Feedback
    val targetScale = when {
        isPressed -> 0.92f
        cell.isAnimatingCompletion -> 1.10f
        cell.isHighlighted -> 1.02f
        else -> 1.0f
    }

    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = if (isPressed) Spring.DampingRatioNoBouncy else Spring.DampingRatioMediumBouncy,
            stiffness = if (isPressed) Spring.StiffnessHigh else Spring.StiffnessMediumLow
        ),
        label = "cellScale"
    )

    // 3. Number Entrance Pop & Shake
    var lastValue by remember { mutableStateOf(cell.value) }
    val valuePop = remember { Animatable(1f) }
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(cell.value) {
        if (cell.value != lastValue && cell.value != null) {
            if (cell.isValid) {
                // Satisfying bouncy pop for valid placed digit
                valuePop.snapTo(0.4f)
                valuePop.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            } else {
                // Subtle tactile shake for error
                repeat(3) {
                    shakeOffset.animateTo(5f, tween(35, easing = LinearEasing))
                    shakeOffset.animateTo(-5f, tween(35, easing = LinearEasing))
                }
                shakeOffset.animateTo(0f, spring(Spring.DampingRatioMediumBouncy))
            }
        }
        lastValue = cell.value
    }

    val cellShape = when (boardStyle) {
        "Glass" -> RoundedCornerShape(8.dp)
        else -> RoundedCornerShape(4.dp)
    }

    // Compute border once — avoids 3 nested Box composables per cell
    val borderWidth = when {
        cell.isHighlighted -> 2.dp
        cell.isMatchingNumber -> 1.dp
        else -> 0.5.dp
    }
    val borderColor = when {
        cell.isHighlighted -> MaterialTheme.colorScheme.primary
        cell.isMatchingNumber -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(1.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = shakeOffset.value
            }
            .clip(cellShape)
            .background(backgroundColor)
            .border(borderWidth, borderColor, cellShape)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = true,
                    color = MaterialTheme.colorScheme.primary
                ),
                onClick = { onClick(row, col) },
                onLongClick = { onLongClick(row, col) }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (cell.value != null) {
            Text(
                text = cell.value.toString(),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 26.sp,
                    fontWeight = if (cell.isClue) FontWeight.ExtraBold else FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                ),
                color = textColor,
                modifier = Modifier.graphicsLayer {
                    scaleX = valuePop.value
                    scaleY = valuePop.value
                }
            )
        } else if (cell.notes.isNotEmpty()) {
            SudokuCellNotes(cell.notes)
        }
    }
}

@Composable
fun SudokuCellNotes(notes: Set<Int>) {
    Column(
        modifier = Modifier.fillMaxSize().padding(2.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        for (i in 0 until 3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (j in 1 until 4) {
                    val note = i * 3 + j
                    Text(
                        text = if (notes.contains(note)) note.toString() else "",
                        fontSize = 9.sp,
                        lineHeight = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

