package com.xtine.habbitrabbit.data.model

import kotlinx.serialization.Serializable

@Serializable
data class HabbitStore(
    val habbits: List<Habbit> = emptyList(),
    val entries: List<HabbitEntry> = emptyList(),
    val nextHabbitId: Long = 1
)
