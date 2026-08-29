package com.prem.skudo.domain

class SudokuValidator {
    /**
     * Fast O(1) validity check
     */
    fun isValid(board: Array<IntArray>, row: Int, col: Int, num: Int): Boolean {
        for (i in 0..8) {
            if (board[row][i] == num) return false
            if (board[i][col] == num) return false
        }

        val boxRow = (row / 3) * 3
        val boxCol = (col / 3) * 3
        for (r in 0..2) {
            for (c in 0..2) {
                if (board[boxRow + r][boxCol + c] == num) return false
            }
        }
        return true
    }

    /**
     * O(N) bitmask validation of entire board in a single pass
     */
    fun isBoardValid(board: Array<IntArray>): Boolean {
        val rowMasks = IntArray(9)
        val colMasks = IntArray(9)
        val boxMasks = IntArray(9)

        for (r in 0..8) {
            for (c in 0..8) {
                val num = board[r][c]
                if (num != 0) {
                    val bit = 1 shl num
                    val b = (r / 3) * 3 + (c / 3)
                    if ((rowMasks[r] and bit) != 0 || (colMasks[c] and bit) != 0 || (boxMasks[b] and bit) != 0) {
                        return false
                    }
                    rowMasks[r] = rowMasks[r] or bit
                    colMasks[c] = colMasks[c] or bit
                    boxMasks[b] = boxMasks[b] or bit
                }
            }
        }
        return true
    }
}
