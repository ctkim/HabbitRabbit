package com.xtine.habbitrabbit.ui.monthly

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xtine.habbitrabbit.data.model.Habbit
import com.xtine.habbitrabbit.ui.common.HabbitColor
import com.xtine.habbitrabbit.ui.theme.HabbitRabbitTheme
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun MonthlyScreen(
    onEditHabbit: (Long) -> Unit = {},
    onEditOrder: () -> Unit = {},
    viewModel: MonthlyViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MonthlyContent(
        state = state,
        onPreviousMonth = viewModel::previousMonth,
        onNextMonth = viewModel::nextMonth,
        onEditHabbit = onEditHabbit,
        onEditOrder = onEditOrder,
        onGoToToday = viewModel::goToToday
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthlyContent(
    state: MonthlyUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onEditHabbit: (Long) -> Unit,
    onEditOrder: () -> Unit,
    onGoToToday: () -> Unit
) {
    val thresholdPx = with(LocalDensity.current) { 48.dp.toPx() }
    val showToday = state.anchorMonth != YearMonth.from(state.today)
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Monthly View") },
                actions = {
                    if (showToday) {
                        IconButton(onClick = onGoToToday) {
                            Icon(
                                imageVector = Icons.Default.Today,
                                contentDescription = "Go to today"
                            )
                        }
                    }
                    if (state.habbits.isNotEmpty()) {
                        IconButton(onClick = onEditOrder) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = "Reorder habbits"
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .pointerInput(Unit) {
                    var dragTotal = 0f
                    var consumed = false
                    detectHorizontalDragGestures(
                        onDragStart = { dragTotal = 0f; consumed = false },
                        onDragEnd = { dragTotal = 0f; consumed = false },
                        onDragCancel = { dragTotal = 0f; consumed = false }
                    ) { _, dragAmount ->
                        if (consumed) return@detectHorizontalDragGestures
                        dragTotal += dragAmount
                        if (dragTotal <= -thresholdPx) {
                            onNextMonth(); consumed = true
                        } else if (dragTotal >= thresholdPx) {
                            onPreviousMonth(); consumed = true
                        }
                    }
                }
                .padding(vertical = 8.dp)
        ) {
            MonthSelector(
                month = state.anchorMonth,
                onPrevious = onPreviousMonth,
                onNext = onNextMonth
            )
            Spacer(Modifier.height(12.dp))
            if (state.habbits.isEmpty() && state.archivedHabbits.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Nothing to summarize yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Add a habbit from the Weekly tab, then check it off to see progress here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    state.habbits.forEach { habbit ->
                        HabbitMonthCard(
                            habbit = habbit,
                            state = state,
                            onEdit = { onEditHabbit(habbit.id) }
                        )
                    }
                    if (state.archivedHabbits.isNotEmpty()) {
                        ArchivedHabbitsSection(
                            habbits = state.archivedHabbits,
                            state = state,
                            onEditHabbit = onEditHabbit
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// --- Month selector ---------------------------------------------------------

@Composable
private fun MonthSelector(
    month: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous month")
        }
        Text(
            text = month.format(MonthLabelFormatter),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next month")
        }
    }
}

// --- Archived habbits section ------------------------------------------------

@Composable
private fun ArchivedHabbitsSection(
    habbits: List<Habbit>,
    state: MonthlyUiState,
    onEditHabbit: (Long) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Archived (${habbits.size})",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse archived" else "Expand archived",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                habbits.forEach { habbit ->
                    HabbitMonthCard(
                        habbit = habbit,
                        state = state,
                        onEdit = { onEditHabbit(habbit.id) }
                    )
                }
            }
        }
    }
}

// --- Habbit card -------------------------------------------------------------

@Composable
private fun HabbitMonthCard(
    habbit: Habbit,
    state: MonthlyUiState,
    onEdit: () -> Unit
) {
    val archived = habbit.isArchived
    val surfaceColor = if (archived) {
        MaterialTheme.colorScheme.surfaceContainerLowest
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentAlpha = if (archived) 0.6f else 1f
    Surface(
        color = surfaceColor,
        tonalElevation = if (archived) 0.dp else 2.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .alpha(contentAlpha)
        ) {
            CardHeader(habbit = habbit, state = state, onEdit = onEdit, archived = archived)
            Spacer(Modifier.height(12.dp))
            MonthCalendar(habbit = habbit, state = state, archived = archived)
        }
    }
}

@Composable
private fun CardHeader(
    habbit: Habbit,
    state: MonthlyUiState,
    onEdit: () -> Unit,
    archived: Boolean
) {
    val accent = Color(habbit.colorArgb).let { if (archived) it.copy(alpha = 0.5f) else it }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(color = accent, shape = CircleShape)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = habbit.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (archived) {
                    Spacer(Modifier.width(8.dp))
                    ArchivedBadge()
                }
            }
            Text(
                text = summaryLine(habbit = habbit, state = state),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onEdit) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit habbit",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ArchivedBadge() {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = "ARCHIVED",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

private fun summaryLine(habbit: Habbit, state: MonthlyUiState): String {
    val count = state.completionCount(habbit.id)
    val dayWord = if (count == 1) "day" else "days"
    val target = habbit.targetPerWeek ?: return "$count $dayWord this month"
    val weeks = weeksInMonth(state.anchorMonth)
    val goal = target * weeks
    val percent = if (goal == 0) 0 else ((count.toDouble() / goal) * 100).roundToInt()
    return "$count / $goal $dayWord · $percent%"
}

private fun weeksInMonth(month: YearMonth): Int =
    (month.lengthOfMonth() + 6) / 7

// --- Calendar grid ----------------------------------------------------------

@Composable
private fun MonthCalendar(habbit: Habbit, state: MonthlyUiState, archived: Boolean = false) {
    val grid = calendarGrid(state.anchorMonth)
    val completions = state.completionsFor(habbit.id)
    val color = Color(habbit.colorArgb).let { if (archived) it.copy(alpha = 0.5f) else it }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        DayOfWeekHeader()
        grid.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                week.forEach { date ->
                    CalendarCell(
                        date = date,
                        today = state.today,
                        completed = date != null && date in completions,
                        color = color
                    )
                }
            }
        }
    }
}

@Composable
private fun DayOfWeekHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val order = listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY
        )
        order.forEach { dow ->
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dow.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RowScope.CalendarCell(
    date: LocalDate?,
    today: LocalDate,
    completed: Boolean,
    color: Color
) {
    val shape = RoundedCornerShape(4.dp)
    val background = when {
        date == null -> Color.Transparent
        completed -> color
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val borderColor = when {
        date == today -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }
    val borderWidth = if (date == today) 2.dp else 0.dp
    Box(
        modifier = Modifier
            .weight(1f)
            .height(16.dp)
            .background(color = background, shape = shape)
            .border(width = borderWidth, color = borderColor, shape = shape)
    )
}

// --- Previews ---------------------------------------------------------------

private val previewHabbits = listOf(
    Habbit(
        id = 1,
        name = "Read 20 pages",
        colorArgb = HabbitColor.palette[2].toArgb(),
        targetPerWeek = 5,
        createdAt = Instant.EPOCH
    ),
    Habbit(
        id = 2,
        name = "Morning walk",
        colorArgb = HabbitColor.palette[5].toArgb(),
        targetPerWeek = null,
        createdAt = Instant.EPOCH
    ),
    Habbit(
        id = 3,
        name = "Water plants",
        colorArgb = HabbitColor.palette[6].toArgb(),
        targetPerWeek = 2,
        createdAt = Instant.EPOCH
    )
)

private val previewArchivedHabbit = Habbit(
    id = 4,
    name = "Meditate",
    colorArgb = HabbitColor.palette[0].toArgb(),
    targetPerWeek = 3,
    createdAt = Instant.EPOCH,
    archivedAt = Instant.EPOCH
)

private fun previewState(
    today: LocalDate = LocalDate.of(2026, 10, 15),
    habbits: List<Habbit> = previewHabbits,
    archivedHabbits: List<Habbit> = listOf(previewArchivedHabbit)
): MonthlyUiState {
    val anchor = YearMonth.from(today)
    val completions = buildMap<Long, Set<LocalDate>> {
        if (habbits.isNotEmpty()) {
            put(
                habbits[0].id,
                setOf(
                    anchor.atDay(1), anchor.atDay(2), anchor.atDay(3),
                    anchor.atDay(5), anchor.atDay(6), anchor.atDay(8),
                    anchor.atDay(9), anchor.atDay(12), anchor.atDay(13),
                    anchor.atDay(14), anchor.atDay(15)
                )
            )
        }
        if (habbits.size > 1) {
            put(habbits[1].id, setOf(anchor.atDay(2), anchor.atDay(7), anchor.atDay(10), anchor.atDay(14)))
        }
        if (habbits.size > 2) {
            put(habbits[2].id, setOf(anchor.atDay(4), anchor.atDay(11)))
        }
        archivedHabbits.forEach { archived ->
            put(archived.id, setOf(anchor.atDay(1), anchor.atDay(3), anchor.atDay(6), anchor.atDay(9)))
        }
    }
    return MonthlyUiState(
        today = today,
        anchorMonth = anchor,
        habbits = habbits,
        archivedHabbits = archivedHabbits,
        completionsByHabbit = completions
    )
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Monthly — populated",
    heightDp = 1600
)
@Composable
private fun MonthlyContentPopulatedPreview() {
    HabbitRabbitTheme(darkTheme = true, dynamicColor = false) {
        MonthlyContent(
            state = previewState(),
            onPreviousMonth = {},
            onNextMonth = {},
            onEditHabbit = {},
            onEditOrder = {},
            onGoToToday = {}
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Monthly — empty"
)
@Composable
private fun MonthlyContentEmptyPreview() {
    HabbitRabbitTheme(darkTheme = true, dynamicColor = false) {
        MonthlyContent(
            state = previewState(habbits = emptyList(), archivedHabbits = emptyList()),
            onPreviousMonth = {},
            onNextMonth = {},
            onEditHabbit = {},
            onEditOrder = {},
            onGoToToday = {}
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Habbit card — with target"
)
@Composable
private fun HabbitMonthCardTargetPreview() {
    HabbitRabbitTheme(darkTheme = true, dynamicColor = false) {
        Box(Modifier.padding(16.dp)) {
            HabbitMonthCard(habbit = previewHabbits[0], state = previewState(), onEdit = {})
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Habbit card — track only"
)
@Composable
private fun HabbitMonthCardTrackOnlyPreview() {
    HabbitRabbitTheme(darkTheme = true, dynamicColor = false) {
        Box(Modifier.padding(16.dp)) {
            HabbitMonthCard(habbit = previewHabbits[1], state = previewState(), onEdit = {})
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Habbit card — archived"
)
@Composable
private fun HabbitMonthCardArchivedPreview() {
    HabbitRabbitTheme(darkTheme = true, dynamicColor = false) {
        Box(Modifier.padding(16.dp)) {
            HabbitMonthCard(habbit = previewArchivedHabbit, state = previewState(), onEdit = {})
        }
    }
}
