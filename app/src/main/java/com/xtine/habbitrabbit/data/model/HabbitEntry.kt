package com.xtine.habbitrabbit.data.model

import com.xtine.habbitrabbit.data.serial.InstantSerializer
import com.xtine.habbitrabbit.data.serial.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate

@Serializable
data class HabbitEntry(
    val habbitId: Long,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    @Serializable(with = InstantSerializer::class)
    val completedAt: Instant
)
