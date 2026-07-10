package com.prem.skudo.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prem.skudo.model.SudokuCell
import com.prem.skudo.ui.theme.*

@Composable
fun SudokuCellView(
    cell: SudokuCell,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    boardStyle: String = "Modern",
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // 1. Organic Color Transitions
    val targetBackgroundColor = when {
        cell.isAnimatingCompletion -> EasyGreen.copy(alpha = 0.4f)
        cell.isHighlighted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        !cell.isValid -> MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
        cell.isMatchingNumber -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
        cell.isRelated -> MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
        else -> if (boardStyle == "Glass") Color.White.copy(alpha = 0.1f) else Color.Transparent
    }
    
    val backgroundColor by animateColorAsState(
        targetValue = targetBackgroundColor,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cellBackground"
    )

    val textColor = when {
        !cell.isValid -> MaterialTheme.colorScheme.error
        cell.isClue -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.primary
    }

    // 2. Tactile Scale Feedback
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else if (cell.isAnimatingCompletion) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cellScale"
    )

    // 3. Number "Entrance" Pop
    var lastValue by remember { mutableStateOf(cell.value) }
    val valuePop = remember { androidx.compose.animation.core.Animatable(1f) }

    LaunchedEffect(cell.value) {
        if ((cell.value != lastValue) && (cell.value != null)) {
            valuePop.snapTo(0.7f)
            valuePop.animateTo(1f, spring(Spring.DampingRatioHighBouncy, Spring.StiffnessMedium))
        }
        lastValue = cell.value
    }

    val cellShape = if (boardStyle == "Glass") RoundedCornerShape(6.dp) else RoundedCornerShape(0.dp)

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(cellShape)
            .background(backgroundColor)
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Custom ripple or no ripple for premium feel
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Selection Border
        if (cell.isHighlighted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = cellShape
                    )
            )
        }
        
        if (cell.value != null) {
            Text(
                text = cell.value.toString(),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 28.sp,
                    fontWeight = if (cell.isClue) FontWeight.ExtraBold else FontWeight.Normal,
                    fontFamily = FontFamily.SansSerif // Or a custom premium font if available
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
                        fontSize = 10.sp,
                        lineHeight = 10.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}
