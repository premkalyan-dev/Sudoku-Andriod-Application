package com.prem.skudo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.prem.skudo.model.SudokuBoard
import com.prem.skudo.ui.theme.BoxBorder

@Composable
fun SudokuBoardView(
    board: SudokuBoard,
    onCellClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    boardStyle: String = "Modern",
) {
    val heavyBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    val boardShape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(16.dp, boardShape, spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            .background(MaterialTheme.colorScheme.surface, boardShape)
            .border(2.dp, heavyBorderColor, boardShape)
            .padding(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            for (rowGroup in 0 until 3) {
                Row(modifier = Modifier.weight(1f)) {
                    for (colGroup in 0 until 3) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .border(
                                    width = 1.dp,
                                    color = heavyBorderColor
                                )
                        ) {
                            Column {
                                for (row in 0 until 3) {
                                    Row(modifier = Modifier.weight(1f)) {
                                        for (col in 0 until 3) {
                                            val actualRow = (rowGroup * 3) + row
                                            val actualCol = (colGroup * 3) + col
                                            SudokuCellView(
                                                cell = board[actualRow, actualCol],
                                                onClick = { onCellClick(actualRow, actualCol) },
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
