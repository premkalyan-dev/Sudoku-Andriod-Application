package com.prem.skudo.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "mystery_gifts",
    indices = [
        Index(value = ["userId", "claimDate"], unique = true)
    ]
)
data class MysteryGift(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val claimDate: String, // "yyyy-MM-dd" in Asia/Kolkata (IST)
    val selectedHints: Int, // 1, 2, or 3 hints (randomly selected by backend)
    val creditedHints: Int = 0, // hints actually added (accounting for max 3 cap)
    val status: String = STATUS_PENDING, // PENDING or REVEALED
    val createdAt: Long = System.currentTimeMillis(),
    val revealedAt: Long? = null
) {
    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_REVEALED = "REVEALED"
    }
}
