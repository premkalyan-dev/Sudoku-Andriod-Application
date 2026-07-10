package com.prem.skudo.model

import com.prem.skudo.R

enum class Difficulty(val label: String, val clueRange: IntRange, val resId: Int) {
    EASY("Easy", 40..45, R.string.difficulty_easy),
    MEDIUM("Medium", 30..35, R.string.difficulty_medium),
    HARD("Hard", 22..28, R.string.difficulty_hard),
    EXPERT("Expert", 17..21, R.string.difficulty_expert)
}
