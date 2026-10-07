package com.xtine.habbitrabbit.ui.weekly

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

internal fun LocalDate.weekStart(): LocalDate =
    with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

internal fun LocalDate.daysOfWeek(): List<LocalDate> {
    val start = weekStart()
    return (0..6).map { start.plusDays(it.toLong()) }
}

internal val DayLabelFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE")
internal val DayNumberFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d")
internal val WeekRangeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d")
internal val FullDayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d")

internal fun weekRangeLabel(weekStart: LocalDate): String {
    val end = weekStart.plusDays(6)
    val left = weekStart.format(WeekRangeFormatter)
    val right = if (weekStart.month == end.month) end.dayOfMonth.toString()
    else end.format(WeekRangeFormatter)
    return "$left – $right"
}
