package com.xtine.habbitrabbit.data.model

import com.xtine.habbitrabbit.data.serial.InstantSerializer
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Habbit(
    val id: Long,
    val name: String,
    val colorArgb: Int,
    val targetPerWeek: Int? = null,
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant,
    @Serializable(with = InstantSerializer::class)
    val archivedAt: Instant? = null
) {
    val isArchived: Boolean get() = archivedAt != null
}
