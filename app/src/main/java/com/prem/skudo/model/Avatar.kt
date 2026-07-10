package com.prem.skudo.model

data class Avatar(
    val id: String,
    val category: String,
    val resourceName: String // Assuming icons/drawables
)

object AvatarProvider {
    val avatars = listOf(
        Avatar("skudo_pencil", "Skudo", "ic_avatar_pencil"),
        Avatar("skudo_grid", "Skudo", "ic_avatar_grid"),
        Avatar("skudo_win", "Skudo", "ic_avatar_win"),
        Avatar("skudo_brain", "Classic", "ic_avatar_brain"),
        Avatar("skudo_rocket", "Classic", "ic_avatar_rocket"),
        Avatar("skudo_star", "Classic", "ic_avatar_star"),
        Avatar("skudo_trophy", "Classic", "ic_avatar_trophy"),
        Avatar("skudo_bulb", "Classic", "ic_avatar_bulb"),
        Avatar("skudo_number_5", "Numbers", "ic_avatar_5"),
        Avatar("skudo_number_9", "Numbers", "ic_avatar_9")
    )
    
    fun getById(id: String) = avatars.find { it.id == id } ?: avatars.first()
}
