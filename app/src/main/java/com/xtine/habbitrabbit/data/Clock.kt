package com.xtine.habbitrabbit.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

fun interface Clock {
    fun now(): Instant

    fun today(zone: ZoneId = ZoneId.systemDefault()): LocalDate =
        now().atZone(zone).toLocalDate()

    companion object {
        val System: Clock = Clock { Instant.now() }
    }
}
