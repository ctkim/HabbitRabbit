package com.xtine.habbitrabbit.ui.monthly

import com.xtine.habbitrabbit.ui.weekly.weekStart
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

internal val MonthLabelFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("LLLL yyyy")

/**
 * Returns the calendar grid for [month] starting on Monday. The number of rows is the
 * number of Monday-anchored weeks the month spans (4, 5, or 6). Cells outside the month
 * are null so the UI can render them as blanks.
 */
internal fun calendarGrid(month: YearMonth): List<List<LocalDate?>> {
    val gridStart = month.atDay(1).weekStart()
    val lastDayStart = month.atEndOfMonth().weekStart()
    val weekCount = (java.time.temporal.ChronoUnit.WEEKS.between(gridStart, lastDayStart) + 1).toInt()
    return (0 until weekCount).map { weekIdx ->
        (0 until 7).map { dayIdx ->
            val date = gridStart.plusDays((weekIdx * 7 + dayIdx).toLong())
            if (YearMonth.from(date) == month) date else null
        }
    }
}
