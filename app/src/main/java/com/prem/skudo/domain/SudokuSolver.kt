package com.prem.skudo.domain

class SudokuSolver(private val validator: SudokuValidator = SudokuValidator()) {
    
    fun solve(board: Array<IntArray>): Boolean {
        return solveFrom(board, 0)
    }

    private fun solveFrom(board: Array<IntArray>, index: Int): Boolean {
        if (index >= 81) return true
        val row = index / 9
        val col = index % 9
        if (board[row][col] != 0) {
            return solveFrom(board, index + 1)
        }
        for (num in 1..9) {
            if (validator.isValid(board, row, col, num)) {
                board[row][col] = num
                if (solveFrom(board, index + 1)) return true
                board[row][col] = 0
            }
        }
        return false
    }

    fun countSolutions(board: Array<IntArray>, limit: Int = 2): Int {
        var count = 0
        fun solveInternal(index: Int) {
            if (count >= limit) return
            if (index >= 81) {
                count++
                return
            }
            val row = index / 9
            val col = index % 9
            if (board[row][col] != 0) {
                solveInternal(index + 1)
                return
            }
            for (num in 1..9) {
                if (validator.isValid(board, row, col, num)) {
                    board[row][col] = num
                    solveInternal(index + 1)
                    board[row][col] = 0
                }
            }
        }
        solveInternal(0)
        return count
    }
}
