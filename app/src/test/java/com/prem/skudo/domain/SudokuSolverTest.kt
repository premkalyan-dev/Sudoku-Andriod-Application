package com.prem.skudo.domain

import com.prem.skudo.model.SudokuBoard
import com.prem.skudo.model.SudokuCell
import org.junit.Assert.*
import org.junit.Test

class SudokuSolverTest {

    private val validator = SudokuValidator()
    private val solver = SudokuSolver()

    // ─── Solve ──────────────────────────────────────────────────────────

    @Test
    fun `solve completes an empty board correctly`() {
        val board = Array(9) { IntArray(9) }
        val solved = solver.solve(board)
        assertTrue("Empty board should be solvable", solved)
        assertTrue("Solved board should be valid", validator.isBoardValid(board))
    }

    @Test
    fun `solve preserves rows with no duplicates`() {
        val board = Array(9) { IntArray(9) }
        solver.solve(board)
        for (row in 0..8) {
            val values = board[row].filter { it != 0 }
            assertEquals("Row $row should have 9 non-zero values", 9, values.size)
            assertEquals("Row $row should have no duplicates", values.size, values.toSet().size)
        }
    }

    @Test
    fun `solve preserves columns with no duplicates`() {
        val board = Array(9) { IntArray(9) }
        solver.solve(board)
        for (col in 0..8) {
            val values = IntArray(9) { r -> board[r][col] }
            val nonZero = values.filter { it != 0 }
            assertEquals("Column $col should have no duplicates", nonZero.size, nonZero.toSet().size)
        }
    }

    @Test
    fun `solve preserves boxes with no duplicates`() {
        val board = Array(9) { IntArray(9) }
        solver.solve(board)
        for (boxRow in 0..2) {
            for (boxCol in 0..2) {
                val values = mutableListOf<Int>()
                for (r in boxRow * 3 until boxRow * 3 + 3) {
                    for (c in boxCol * 3 until boxCol * 3 + 3) {
                        values.add(board[r][c])
                    }
                }
                assertEquals("Box ($boxRow,$boxCol) should have 9 values", 9, values.size)
                assertEquals("Box ($boxRow,$boxCol) should have no duplicates", values.size, values.toSet().size)
            }
        }
    }

    @Test
    fun `solve handles partially filled valid board`() {
        val board = Array(9) { IntArray(9) { 0 } }
        // Place a few givens
        board[0][0] = 5
        board[0][1] = 3
        board[1][3] = 7
        board[4][4] = 1
        board[8][8] = 9
        val solved = solver.solve(board)
        assertTrue("Partially filled board should be solvable", solved)
        assertTrue("Solved board should be valid", validator.isBoardValid(board))
    }

    @Test
    fun `solve does not mutate given cells in a partially filled board`() {
        val board = Array(9) { IntArray(9) { 0 } }
        board[0][0] = 5
        board[4][4] = 1
        solver.solve(board)
        assertEquals("Cell [0][0] should remain 5", 5, board[0][0])
        assertEquals("Cell [4][4] should remain 1", 1, board[4][4])
    }

    @Test
    fun `solve completes board with all cells filled`() {
        val board = Array(9) { IntArray(9) { 0 } }
        solver.solve(board)
        for (row in 0..8) {
            for (col in 0..8) {
                assertTrue("All cells should be non-zero", board[row][col] != 0)
                assertTrue("All values should be 1-9", board[row][col] in 1..9)
            }
        }
    }

    // ─── Count Solutions ────────────────────────────────────────────────

    @Test
    fun `countSolutions returns 1 for a complete valid board`() {
        val board = Array(9) { IntArray(9) }
        solver.solve(board)
        val count = solver.countSolutions(board, 2)
        assertEquals("Complete valid board should have exactly 1 solution", 1, count)
    }

    @Test
    fun `countSolutions returns 0 for a board with conflicts`() {
        val board = Array(9) { IntArray(9) { 0 } }
        board[0][0] = 5
        board[0][1] = 5 // Duplicate in same row — conflict
        val count = solver.countSolutions(board, 2)
        assertEquals("Conflicting board should have 0 solutions", 0, count)
    }

    @Test
    fun `countSolutions does not mutate the input board`() {
        val board = Array(9) { IntArray(9) { 0 } }
        board[0][0] = 1
        board[0][1] = 2
        val originalBoard = board.map { it.clone() }.toTypedArray()
        solver.countSolutions(board, 2)
        for (r in 0..8) {
            assertArrayEquals("Board should not be mutated", originalBoard[r], board[r])
        }
    }

    @Test
    fun `countSolutions returns 2 for board with multiple solutions when limit is 2`() {
        // An almost empty board has many solutions
        val board = Array(9) { IntArray(9) }
        val count = solver.countSolutions(board, 2)
        assertTrue("Almost empty board should have more than 1 solution", count > 1)
        assertEquals("Should stop at limit 2", 2, count)
    }

    @Test
    fun `countSolutions with limit 1 returns quickly for invalid board`() {
        val board = Array(9) { IntArray(9) { 0 } }
        board[0][0] = 1
        board[0][1] = 1 // Conflict
        val count = solver.countSolutions(board, 1)
        assertEquals("Invalid board should have 0 solutions", 0, count)
    }

    @Test
    fun `countSolutions returns 1 for minimal valid puzzle`() {
        val board = Array(9) { IntArray(9) { 0 } }
        // Place enough givens to make it uniquely solvable
        board[0][0] = 5
        board[0][1] = 3
        board[0][2] = 8
        board[1][3] = 7
        board[2][6] = 4
        board[3][1] = 6
        board[4][4] = 1
        board[5][7] = 9
        board[6][2] = 3
        board[7][5] = 8
        board[8][7] = 2
        board[8][8] = 6
        board[6][4] = 5
        board[1][8] = 2
        val count = solver.countSolutions(board, 2)
        assertEquals("Minimal valid puzzle should have exactly 1 solution", 1, count)
    }

    // ─── Solve Result Integrity ─────────────────────────────────────────

    @Test
    fun `solve result has every cell filled with 1-9`() {
        val board = Array(9) { IntArray(9) }
        solver.solve(board)
        for (row in 0..8) {
            for (col in 0..8) {
                assertTrue("Cell ($row,$col) must be 1-9, was ${board[row][col]}",
                    board[row][col] in 1..9)
            }
        }
    }

    @Test
    fun `solve handles board with many givens`() {
        val board = Array(9) { IntArray(9) { 0 } }
        // Fill 50 cells
        for (r in 0 until 5) {
            for (c in 0 until 9) {
                board[r][c] = (r * 9 + c + 1) % 9 + 1
            }
        }
        // Fix conflicts by resetting a few
        board[0][0] = 5
        board[1][0] = 3
        val solved = solver.solve(board)
        assertTrue("Board with many givens should be solvable", solved)
        assertTrue("Solved board should be valid", validator.isBoardValid(board))
    }
}
