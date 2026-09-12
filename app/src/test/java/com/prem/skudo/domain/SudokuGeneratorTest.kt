package com.prem.skudo.domain

import com.prem.skudo.model.Difficulty
import com.prem.skudo.model.SudokuBoard
import com.prem.skudo.model.SudokuCell
import org.junit.Assert.*
import org.junit.Test

class SudokuGeneratorTest {

    private val solver = SudokuSolver()
    private val validator = SudokuValidator()
    private val generator = SudokuGenerator(solver, validator)

    // ─── Basic Generation ───────────────────────────────────────────────

    @Test
    fun `generate returns non-null puzzle and solution for Easy difficulty`() {
        val (puzzle, solution) = generator.generate(Difficulty.EASY)
        assertNotNull("Puzzle should not be null", puzzle)
        assertNotNull("Solution should not be null", solution)
    }

    @Test
    fun `generate returns non-null puzzle and solution for Medium difficulty`() {
        val (puzzle, solution) = generator.generate(Difficulty.MEDIUM)
        assertNotNull(puzzle)
        assertNotNull(solution)
    }

    @Test
    fun `generate returns non-null puzzle and solution for Hard difficulty`() {
        val (puzzle, solution) = generator.generate(Difficulty.HARD)
        assertNotNull(puzzle)
        assertNotNull(solution)
    }

    @Test
    fun `generate returns non-null puzzle and solution for Expert difficulty`() {
        val (puzzle, solution) = generator.generate(Difficulty.EXPERT)
        assertNotNull(puzzle)
        assertNotNull(solution)
    }

    @Test
    fun `generate produces a valid puzzle for all difficulty levels`() {
        val difficulties = listOf(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD, Difficulty.EXPERT)
        for (difficulty in difficulties) {
            val (puzzle, solution) = generator.generate(difficulty)
            assertTrue("Solution board should be valid for $difficulty",
                validator.isBoardValid(sudokuBoardToArray(solution)))
            assertTrue("Puzzle should be solvable for $difficulty",
                solver.countSolutions(sudokuBoardToArray(puzzle), 2) == 1)
        }
    }

    // ─── Solution Correctness ───────────────────────────────────────────

    @Test
    fun `solution contains no duplicate numbers in any row`() {
        repeat(50) {
            val (_, solution) = generator.generate(Difficulty.MEDIUM)
            for (row in 0..8) {
                val values = (0..8).map { solution[row, it].value }
                val nonNullValues = values.filterNotNull()
                assertEquals("Row $row should have no duplicates", nonNullValues.size, nonNullValues.toSet().size)
                assertTrue("Row $row should have exactly 9 values", values.all { it != null })
            }
        }
    }

    @Test
    fun `solution contains no duplicate numbers in any column`() {
        repeat(50) {
            val (_, solution) = generator.generate(Difficulty.MEDIUM)
            for (col in 0..8) {
                val values = (0..8).map { solution[col, it].value }
                val nonNullValues = values.filterNotNull()
                assertEquals("Column $col should have no duplicates", nonNullValues.size, nonNullValues.toSet().size)
                assertTrue("Column $col should have exactly 9 values", values.all { it != null })
            }
        }
    }

    @Test
    fun `solution contains no duplicate numbers in any 3x3 box`() {
        repeat(50) {
            val (_, solution) = generator.generate(Difficulty.MEDIUM)
            for (boxRow in 0..2) {
                for (boxCol in 0..2) {
                    val values = mutableListOf<Int>()
                    for (r in boxRow * 3 until boxRow * 3 + 3) {
                        for (c in boxCol * 3 until boxCol * 3 + 3) {
                            solution[r, c].value?.let { values.add(it) }
                        }
                    }
                    assertEquals("Box ($boxRow,$boxCol) should have 9 values", 9, values.size)
                    assertEquals("Box ($boxRow,$boxCol) should have no duplicates", values.size, values.toSet().size)
                }
            }
        }
    }

    // ─── Puzzle Solvability ─────────────────────────────────────────────

    @Test
    fun `generated puzzle is always solvable`() {
        val iterations = 100
        for (i in 0 until iterations) {
            val (puzzle, _) = generator.generate(Difficulty.MEDIUM)
            val solutionCount = solver.countSolutions(sudokuBoardToArray(puzzle), 2)
            assertEquals("Puzzle iteration $i must have exactly 1 solution", 1, solutionCount)
        }
    }

    @Test
    fun `generated puzzle has exactly one solution for each difficulty`() {
        for (difficulty in Difficulty.entries) {
            repeat(20) {
                val (puzzle, _) = generator.generate(difficulty)
                val solutionCount = solver.countSolutions(sudokuBoardToArray(puzzle), 2)
                assertEquals("Puzzle for $difficulty must have exactly 1 solution", 1, solutionCount)
            }
        }
    }

    // ─── Clue Count ─────────────────────────────────────────────────────

    @Test
    fun `Easy difficulty has 40-45 clues`() {
        repeat(20) {
            val (puzzle, _) = generator.generate(Difficulty.EASY)
            val clueCount = countClues(puzzle)
            assertTrue("Easy should have 40-45 clues, got $clueCount", clueCount in 40..45)
        }
    }

    @Test
    fun `Medium difficulty has 30-35 clues`() {
        repeat(20) {
            val (puzzle, _) = generator.generate(Difficulty.MEDIUM)
            val clueCount = countClues(puzzle)
            assertTrue("Medium should have 30-35 clues, got $clueCount", clueCount in 30..35)
        }
    }

    @Test
    fun `Hard difficulty has 22-28 clues`() {
        repeat(20) {
            val (puzzle, _) = generator.generate(Difficulty.HARD)
            val clueCount = countClues(puzzle)
            assertTrue("Hard should have 22-28 clues, got $clueCount", clueCount in 22..28)
        }
    }

    @Test
    fun `Expert difficulty has 17-21 clues`() {
        repeat(20) {
            val (puzzle, _) = generator.generate(Difficulty.EXPERT)
            val clueCount = countClues(puzzle)
            assertTrue("Expert should have 17-21 clues, got $clueCount", clueCount in 17..21)
        }
    }

    @Test
    fun `all generated puzzles have at least some clues`() {
        repeat(20) {
            val (puzzle, _) = generator.generate(Difficulty.EXPERT)
            assertTrue("Puzzle should have at least 17 clues", countClues(puzzle) >= 17)
        }
    }

    // ─── Clue Validity ──────────────────────────────────────────────────

    @Test
    fun `all clues in the puzzle are consistent with the solution`() {
        repeat(50) {
            val (puzzle, solution) = generator.generate(Difficulty.MEDIUM)
            for (row in 0..8) {
                for (col in 0..8) {
                    val cell = puzzle[row, col]
                    if (cell.isClue) {
                        assertEquals("Clue at ($row,$col) must match solution",
                            cell.value, solution[row, col].value)
                    }
                }
            }
        }
    }

    @Test
    fun `non-clue cells in puzzle are empty (null)`() {
        repeat(50) {
            val (puzzle, _) = generator.generate(Difficulty.MEDIUM)
            for (row in 0..8) {
                for (col in 0..8) {
                    val cell = puzzle[row, col]
                    if (!cell.isClue) {
                        assertNull("Non-clue cell at ($row,$col) should be null", cell.value)
                    }
                }
            }
        }
    }

    // ─── Determinism of Structure ───────────────────────────────────────

    @Test
    fun `generated board always has 9 rows and 9 columns`() {
        repeat(50) {
            val (puzzle, _) = generator.generate(Difficulty.MEDIUM)
            assertEquals("Board must have 9 rows", 9, puzzle.cells.size)
            puzzle.cells.forEach { row ->
                assertEquals("Each row must have 9 cells", 9, row.size)
            }
        }
    }

    @Test
    fun `generated puzzle has correct row col box indices in each cell`() {
        repeat(50) {
            val (puzzle, _) = generator.generate(Difficulty.MEDIUM)
            for (row in 0..8) {
                for (col in 0..8) {
                    val cell = puzzle[row, col]
                    assertEquals("Cell row index mismatch at ($row,$col)", row, cell.row)
                    assertEquals("Cell col index mismatch at ($row,$col)", col, cell.col)
                }
            }
        }
    }

    // ─── Repeated Generation Stability ──────────────────────────────────

    @Test
    fun `100 consecutive generations all produce valid solvable puzzles`() {
        repeat(100) { iteration ->
            val (puzzle, solution) = generator.generate(Difficulty.MEDIUM)
            // Solution must be a valid complete board
            assertTrue("Solution must be valid (iteration $iteration)",
                validator.isBoardValid(sudokuBoardToArray(solution)))
            // Puzzle must have exactly one solution
            assertEquals("Puzzle must have exactly 1 solution (iteration $iteration)",
                1, solver.countSolutions(sudokuBoardToArray(puzzle), 2))
            // Clues must match solution values
            for (row in 0..8) {
                for (col in 0..8) {
                    if (puzzle[row, col].isClue) {
                        assertEquals("Clue mismatch at ($row,$col) (iteration $iteration)",
                            puzzle[row, col].value, solution[row, col].value)
                    }
                }
            }
        }
    }

    @Test
    fun `different difficulties produce different clue counts`() {
        val easyClues = (1..20).map { generator.generate(Difficulty.EASY).first }.map { countClues(it) }.average()
        val expertClues = (1..20).map { generator.generate(Difficulty.EXPERT).first }.map { countClues(it) }.average()
        assertTrue("Easy should have more clues than Expert (avg Easy=$easyClues, avg Expert=$expertClues)",
            easyClues > expertClues)
    }

    // ─── Helper Functions ───────────────────────────────────────────────

    private fun sudokuBoardToArray(board: SudokuBoard): Array<IntArray> {
        return Array(9) { r ->
            IntArray(9) { c ->
                board[r, c].value ?: 0
            }
        }
    }

    private fun countClues(board: SudokuBoard): Int {
        var count = 0
        for (row in 0..8) {
            for (col in 0..8) {
                if (board[row, col].isClue) count++
            }
        }
        return count
    }
}
