package com.prem.skudo.model

data class SudokuCell(
    val row: Int,
    val col: Int,
    val value: Int? = null,
    val isClue: Boolean = false,
    val isValid: Boolean = true,
    val isHighlighted: Boolean = false,
    val isRelated: Boolean = false,
    val isMatchingNumber: Boolean = false,
    val isAnimatingCompletion: Boolean = false,
    val notes: Set<Int> = emptySet()
)
