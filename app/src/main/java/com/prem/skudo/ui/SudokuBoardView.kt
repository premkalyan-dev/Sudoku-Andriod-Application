package com.prem.skudo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
    val blockBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f)
    val outerBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    val boardShape = RoundedCornerShape(18.dp)
    val blockShape = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(
                elevation = 10.dp,
                shape = boardShape,
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            )
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), boardShape)
            .border(1.5.dp, outerBorderColor, boardShape)
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            for (rowGroup in 0 until 3) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    for (colGroup in 0 until 3) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(blockShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .border(
                                    width = 1.dp,
                                    color = blockBorderColor,
                                    shape = blockShape
                                )
                                .padding(1.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                for (row in 0 until 3) {
                                    Row(modifier = Modifier.weight(1f)) {
                                        for (col in 0 until 3) {
                                            val actualRow = (rowGroup * 3) + row
                                            val actualCol = (colGroup * 3) + col
                                            key("${actualRow}_${actualCol}") {
                                                SudokuCellView(
                                                    cell = board[actualRow, actualCol],
                                                    onClick = { onCellClick(actualRow, actualCol) },
                                                    onLongClick = { onCellLongClick(actualRow, actualCol) },
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
                }
            }
        }
    }
}
