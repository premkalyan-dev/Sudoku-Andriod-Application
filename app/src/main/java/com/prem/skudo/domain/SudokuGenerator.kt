package com.prem.skudo.domain

import com.prem.skudo.model.Difficulty
import com.prem.skudo.model.SudokuBoard
import com.prem.skudo.model.SudokuCell
import kotlin.random.Random

class SudokuGenerator(
    private val solver: SudokuSolver = SudokuSolver(),
    private val validator: SudokuValidator = SudokuValidator()
) {
    fun generate(difficulty: Difficulty): Pair<SudokuBoard, SudokuBoard> {
        val fullBoard = Array(9) { IntArray(9) }
        fillBoard(fullBoard)
        
        val solution = SudokuBoard(
            fullBoard.mapIndexed { r, row ->
                row.mapIndexed { c, value ->
                    SudokuCell(r, c, value, isClue = true)
                }
            }
        )

        val puzzleBoard = Array(9) { r -> fullBoard[r].copyOf() }
        removeCells(puzzleBoard, difficulty)

        val puzzle = SudokuBoard(
            puzzleBoard.mapIndexed { r, row ->
                row.mapIndexed { c, value ->
                    SudokuCell(r, c, if (value == 0) null else value, isClue = value != 0)
                }
            }
        )

        return puzzle to solution
    }

    private fun fillBoard(board: Array<IntArray>): Boolean {
        for (row in 0..8) {
            for (col in 0..8) {
                if (board[row][col] == 0) {
                    val numbers = (1..9).shuffled()
                    for (num in numbers) {
                        if (validator.isValid(board, row, col, num)) {
                            board[row][col] = num
                            if (fillBoard(board)) return true
                            board[row][col] = 0
                        }
                    }
                    return false
                }
            }
        }
        return true
    }

    private fun removeCells(board: Array<IntArray>, difficulty: Difficulty) {
        val targetClues = difficulty.clueRange.random()
        var currentClues = 81
        val positions = (0..80).shuffled().toMutableList()

        while (currentClues > targetClues && positions.isNotEmpty()) {
            val pos = positions.removeAt(0)
            val r = pos / 9
            val c = pos % 9
            val temp = board[r][c]
            board[r][c] = 0
            
            if (solver.countSolutions(Array(9) { i -> board[i].copyOf() }) != 1) {
                board[r][c] = temp
            } else {
                currentClues--
            }
        }
    }
}
