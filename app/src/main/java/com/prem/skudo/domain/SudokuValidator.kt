package com.prem.skudo.domain

class SudokuValidator {
    fun isValid(board: Array<IntArray>, row: Int, col: Int, num: Int): Boolean {
        // Check row
        for (x in 0..8) {
            if (board[row][x] == num) return false
        }

        // Check column
        for (x in 0..8) {
            if (board[x][col] == num) return false
        }

        // Check 3x3 box
        val startRow = row - row % 3
        val startCol = col - col % 3
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i + startRow][j + startCol] == num) return false
            }
        }
        return true
    }

    fun isBoardValid(board: Array<IntArray>): Boolean {
        for (row in 0..8) {
            for (col in 0..8) {
                val num = board[row][col]
                if (num != 0) {
                    board[row][col] = 0
                    if (!isValid(board, row, col, num)) {
                        board[row][col] = num
                        return false
                    }
                    board[row][col] = num
                }
            }
        }
        return true
    }
}
