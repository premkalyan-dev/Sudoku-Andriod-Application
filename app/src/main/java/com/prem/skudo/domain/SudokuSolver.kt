package com.prem.skudo.domain

class SudokuSolver(private val validator: SudokuValidator = SudokuValidator()) {
    
    fun solve(board: Array<IntArray>): Boolean {
        for (row in 0..8) {
            for (col in 0..8) {
                if (board[row][col] == 0) {
                    for (num in 1..9) {
                        if (validator.isValid(board, row, col, num)) {
                            board[row][col] = num
                            if (solve(board)) return true
                            board[row][col] = 0
                        }
                    }
                    return false
                }
            }
        }
        return true
    }

    fun countSolutions(board: Array<IntArray>, limit: Int = 2): Int {
        var count = 0
        fun solveInternal() {
            if (count >= limit) return
            
            var row = -1
            var col = -1
            var empty = true
            for (i in 0..8) {
                for (j in 0..8) {
                    if (board[i][j] == 0) {
                        row = i
                        col = j
                        empty = false
                        break
                    }
                }
                if (!empty) break
            }

            if (empty) {
                count++
                return
            }

            for (num in 1..9) {
                if (validator.isValid(board, row, col, num)) {
                    board[row][col] = num
                    solveInternal()
                    board[row][col] = 0
                }
            }
        }
        solveInternal()
        return count
    }
}
