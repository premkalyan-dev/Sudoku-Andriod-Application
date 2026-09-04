package com.prem.skudo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.prem.skudo.model.SudokuBoard

@Composable
fun SudokuBoardView(
    board: SudokuBoard,
    onCellClick: (Int, Int) -> Unit,
    onCellLongClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    boardStyle: String = "Modern",
) {
    val boardShape = RoundedCornerShape(14.dp)
    val outerBorderColor = MaterialTheme.colorScheme.outline
    val blockDividerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f)
    val cellDividerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(
                elevation = 8.dp,
                shape = boardShape,
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
                ambientColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            )
            .clip(boardShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(2.dp, outerBorderColor, boardShape)
            .pointerInput(onCellClick) {
                awaitEachGesture {
                    val down = awaitFirstDown(pass = PointerEventPass.Initial)
                    val pointerId = down.id
                    val w = size.width.toFloat()
                    val h = size.height.toFloat()
                    if (w > 0f && h > 0f) {
                        val startCol = (down.position.x / w * 9f).toInt().coerceIn(0, 8)
                        val startRow = (down.position.y / h * 9f).toInt().coerceIn(0, 8)
                        var lastCell = startRow to startCol
                        onCellClick(startRow, startCol)

                        while (true) {
                            val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                            val change = event.changes.firstOrNull { it.id == pointerId } ?: event.changes.firstOrNull() ?: break
                            if (!change.pressed) break

                            val curCol = (change.position.x / w * 9f).toInt().coerceIn(0, 8)
                            val curRow = (change.position.y / h * 9f).toInt().coerceIn(0, 8)
                            val curCell = curRow to curCol
                            if (curCell != lastCell) {
                                lastCell = curCell
                                onCellClick(curRow, curCol)
                            }
                        }
                    }
                }
            }
            .drawWithContent {
                // 1. Draw cell contents and smooth continuous highlights
                drawContent()

                // 2. Draw precision classic grid lines
                val cellW = size.width / 9f
                val cellH = size.height / 9f
                val thinWidth = 0.75.dp.toPx()
                val boldWidth = 2.dp.toPx()

                // Vertical dividers
                for (i in 1..8) {
                    val x = cellW * i
                    val isBlockDivider = (i % 3 == 0)
                    drawLine(
                        color = if (isBlockDivider) blockDividerColor else cellDividerColor,
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = if (isBlockDivider) boldWidth else thinWidth
                    )
                }

                // Horizontal dividers
                for (i in 1..8) {
                    val y = cellH * i
                    val isBlockDivider = (i % 3 == 0)
                    drawLine(
                        color = if (isBlockDivider) blockDividerColor else cellDividerColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = if (isBlockDivider) boldWidth else thinWidth
                    )
                }
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            for (row in 0 until 9) {
                Row(modifier = Modifier.weight(1f)) {
                    for (col in 0 until 9) {
                        key("${row}_${col}") {
                            SudokuCellView(
                                cell = board[row, col],
                                row = row,
                                col = col,
                                onClick = onCellClick,
                                onLongClick = onCellLongClick,
                                modifier = Modifier.weight(1f),
                                boardStyle = boardStyle
                            )
                        }
                    }
                }
            }
        }
    }
}
