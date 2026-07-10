package com.prem.skudo.model

data class SudokuBoard(
    val cells: List<List<SudokuCell>> = List(9) { r ->
        List(9) { c -> SudokuCell(r, c) }
    }
) {
    operator fun get(row: Int, col: Int): SudokuCell = cells[row][col]
}
