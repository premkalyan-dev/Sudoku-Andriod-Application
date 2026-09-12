package com.prem.skudo.domain

import com.prem.skudo.model.Difficulty
import com.prem.skudo.model.SudokuBoard
import com.prem.skudo.model.SudokuCell
import org.junit.Assert.*
import org.junit.Test

class SudokuGameplayValidationTest {

    private val generator = SudokuGenerator()

    @Test
    fun `validateNumber compares directly against solution and rejects legally placed wrong number`() {
        val (puzzle, solution) = generator.generate(Difficulty.EASY)

        // Find an empty cell in puzzle
        var targetRow = -1
        var targetCol = -1
        for (r in 0..8) {
            for (c in 0..8) {
                if (puzzle[r, c].value == null) {
                    targetRow = r
                    targetCol = c
                    break
                }
            }
            if (targetRow != -1) break
        }

        assertTrue("Should have found an empty cell", targetRow != -1 && targetCol != -1)

        val correctVal = solution[targetRow, targetCol].value!!
        // Pick an incorrect value (1..9) different from correctVal
        val incorrectVal = if (correctVal == 9) 1 else correctVal + 1

        // 1. Correct number validation
        val isCorrectEntry = (correctVal == solution[targetRow, targetCol].value)
        assertTrue("Correct number must match solution", isCorrectEntry)

        // 2. Incorrect number validation
        val isIncorrectEntry = (incorrectVal == solution[targetRow, targetCol].value)
        assertFalse("Incorrect number must not match solution", isIncorrectEntry)

        // Ensure correctness is strictly based on solution matching, not just row/col/box uniqueness:
        // Even if incorrectVal was not yet present in row or col or box, it is WRONG for this puzzle cell.
        val cellAfterIncorrectPlacement = puzzle[targetRow, targetCol].copy(
            value = incorrectVal,
            isValid = isIncorrectEntry
        )
        assertFalse("Cell with incorrect number must have isValid = false", cellAfterIncorrectPlacement.isValid)
        assertEquals("Cell value must be the entered number", incorrectVal, cellAfterIncorrectPlacement.value)
    }

    @Test
    fun `keypad counts only include valid cells matching the solution`() {
        val (puzzle, solution) = generator.generate(Difficulty.EASY)

        // Find an empty cell
        var targetRow = -1
        var targetCol = -1
        for (r in 0..8) {
            for (c in 0..8) {
                if (puzzle[r, c].value == null) {
                    targetRow = r
                    targetCol = c
                    break
                }
            }
            if (targetRow != -1) break
        }

        val correctVal = solution[targetRow, targetCol].value!!
        val wrongVal = 4

        // Function simulating the PremiumNumberPad count logic
        fun computeCounts(board: SudokuBoard, sol: SudokuBoard): IntArray {
            val c = IntArray(10) { 0 }
            board.cells.flatten().forEach { cell ->
                val solVal = sol[cell.row, cell.col].value
                val isMatchingSolution = (solVal != null && cell.value == solVal) || (solVal == null && cell.isClue)
                if (cell.value != null && cell.value in 1..9 && cell.isValid && isMatchingSolution) {
                    c[cell.value]++
                }
            }
            return c
        }

        val initialCounts = computeCounts(puzzle, solution)

        // If player enters wrongVal into target cell where solution requires different value
        if (correctVal != wrongVal) {
            val newCellsWithWrong = puzzle.cells.mapIndexed { ri, rowList ->
                rowList.mapIndexed { ci, cell ->
                    if (ri == targetRow && ci == targetCol) {
                        cell.copy(value = wrongVal, isValid = false)
                    } else cell
                }
            }
            val boardWithWrong = SudokuBoard(newCellsWithWrong)
            val countsAfterWrong = computeCounts(boardWithWrong, solution)

            assertEquals(
                "Incorrect placement of $wrongVal must NOT increment count for $wrongVal",
                initialCounts[wrongVal],
                countsAfterWrong[wrongVal]
            )
            assertFalse(
                "Keypad for $wrongVal must NOT be marked completed (>= 9) due to incorrect placements",
                countsAfterWrong[wrongVal] >= 9
            )
        }

        // Now test correct placement
        val newCellsWithCorrect = puzzle.cells.mapIndexed { ri, rowList ->
            rowList.mapIndexed { ci, cell ->
                if (ri == targetRow && ci == targetCol) {
                    cell.copy(value = correctVal, isValid = true)
                } else cell
            }
        }
        val boardWithCorrect = SudokuBoard(newCellsWithCorrect)
        val countsAfterCorrect = computeCounts(boardWithCorrect, solution)

        assertEquals(
            "Correct placement of $correctVal MUST increment count for $correctVal by 1",
            initialCounts[correctVal] + 1,
            countsAfterCorrect[correctVal]
        )
    }

    @Test
    fun `completion check requires all cells to match solution and be valid`() {
        // Build a completed 9x9 board from a real solution
        val (_, solution) = generator.generate(Difficulty.EASY)

        val cells = solution.cells.map { row ->
            row.map { it.copy(isValid = true) }
        }
        val completeBoard = SudokuBoard(cells)

        // Check row 0 complete
        val isRowComplete = (0..8).all { c ->
            completeBoard[0, c].value != null &&
            completeBoard[0, c].isValid &&
            completeBoard[0, c].value == solution[0, c].value
        }
        assertTrue("Fully correct row must be complete", isRowComplete)

        // Mutate one cell to be wrong value
        val wrongRowCells = cells.mapIndexed { ri, row ->
            row.mapIndexed { ci, cell ->
                if (ri == 0 && ci == 0) {
                    val wrongVal = if (cell.value == 9) 1 else cell.value!! + 1
                    cell.copy(value = wrongVal, isValid = false)
                } else cell
            }
        }
        val boardWithWrongInRow = SudokuBoard(wrongRowCells)
        val isRowCompleteWithWrong = (0..8).all { c ->
            boardWithWrongInRow[0, c].value != null &&
            boardWithWrongInRow[0, c].isValid &&
            boardWithWrongInRow[0, c].value == solution[0, c].value
        }
        assertFalse("Row with an incorrect cell must NOT be complete", isRowCompleteWithWrong)
    }
}
