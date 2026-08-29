package com.prem.skudo.domain

class SudokuSolver(private val validator: SudokuValidator = SudokuValidator()) {
    
    fun solve(board: Array<IntArray>): Boolean {
        val rowMasks = IntArray(9)
        val colMasks = IntArray(9)
        val boxMasks = IntArray(9)
        val emptyCells = IntArray(81)
        var emptyCount = 0

        for (r in 0..8) {
            for (c in 0..8) {
                val num = board[r][c]
                if (num != 0) {
                    val bit = 1 shl num
                    rowMasks[r] = rowMasks[r] or bit
                    colMasks[c] = colMasks[c] or bit
                    boxMasks[(r / 3) * 3 + (c / 3)] = boxMasks[(r / 3) * 3 + (c / 3)] or bit
                } else {
                    emptyCells[emptyCount++] = (r shl 4) or c
                }
            }
        }

        fun solveRecursive(idx: Int): Boolean {
            if (idx == emptyCount) return true
            val pos = emptyCells[idx]
            val r = pos shr 4
            val c = pos and 0x0F
            val b = (r / 3) * 3 + (c / 3)
            val used = rowMasks[r] or colMasks[c] or boxMasks[b]

            for (num in 1..9) {
                val bit = 1 shl num
                if ((used and bit) == 0) {
                    board[r][c] = num
                    rowMasks[r] = rowMasks[r] or bit
                    colMasks[c] = colMasks[c] or bit
                    boxMasks[b] = boxMasks[b] or bit

                    if (solveRecursive(idx + 1)) return true

                    board[r][c] = 0
                    val bitInv = bit.inv()
                    rowMasks[r] = rowMasks[r] and bitInv
                    colMasks[c] = colMasks[c] and bitInv
                    boxMasks[b] = boxMasks[b] and bitInv
                }
            }
            return false
        }

        return solveRecursive(0)
    }

    fun countSolutions(board: Array<IntArray>, limit: Int = 2): Int {
        val rowMasks = IntArray(9)
        val colMasks = IntArray(9)
        val boxMasks = IntArray(9)
        val emptyCells = IntArray(81)
        var emptyCount = 0

        for (r in 0..8) {
            for (c in 0..8) {
                val num = board[r][c]
                if (num != 0) {
                    val bit = 1 shl num
                    rowMasks[r] = rowMasks[r] or bit
                    colMasks[c] = colMasks[c] or bit
                    boxMasks[(r / 3) * 3 + (c / 3)] = boxMasks[(r / 3) * 3 + (c / 3)] or bit
                } else {
                    emptyCells[emptyCount++] = (r shl 4) or c
                }
            }
        }

        var count = 0

        fun countRecursive(idx: Int) {
            if (count >= limit) return
            if (idx == emptyCount) {
                count++
                return
            }
            val pos = emptyCells[idx]
            val r = pos shr 4
            val c = pos and 0x0F
            val b = (r / 3) * 3 + (c / 3)
            val used = rowMasks[r] or colMasks[c] or boxMasks[b]

            for (num in 1..9) {
                val bit = 1 shl num
                if ((used and bit) == 0) {
                    rowMasks[r] = rowMasks[r] or bit
                    colMasks[c] = colMasks[c] or bit
                    boxMasks[b] = boxMasks[b] or bit

                    countRecursive(idx + 1)

                    val bitInv = bit.inv()
                    rowMasks[r] = rowMasks[r] and bitInv
                    colMasks[c] = colMasks[c] and bitInv
                    boxMasks[b] = boxMasks[b] and bitInv
                }
            }
        }

        countRecursive(0)
        return count
    }
}
