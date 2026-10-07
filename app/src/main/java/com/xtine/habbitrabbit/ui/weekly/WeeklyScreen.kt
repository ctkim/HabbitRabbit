package com.xtine.habbitrabbit.ui.weekly

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xtine.habbitrabbit.data.model.Habbit
import com.xtine.habbitrabbit.ui.common.HabbitColor
import com.xtine.habbitrabbit.ui.theme.HabbitRabbitTheme
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun WeeklyScreen(
    onAddHabbit: () -> Unit = {},
    onEditHabbit: (Long) -> Unit = {},
    onEditOrder: () -> Unit = {},
    viewModel: WeeklyViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    WeeklyContent(
        state = state,
        onAddHabbit = onAddHabbit,
        onEditHabbit = onEditHabbit,
        onEditOrder = onEditOrder,
        onPreviousWeek = viewModel::previousWeek,
        onNextWeek = viewModel::nextWeek,
        onDaySelected = viewModel::onDaySelected,
        onToggle = viewModel::toggle,
        onGoToToday = viewModel::goToToday
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeeklyContent(
    state: WeeklyUiState,
    onAddHabbit: () -> Unit,
    onEditHabbit: (Long) -> Unit,
    onEditOrder: () -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onDaySelected: (LocalDate) -> Unit,
    onToggle: (Long, LocalDate) -> Unit,
    onGoToToday: () -> Unit
) {
    val showToday = state.selectedDay != state.today
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("HabbitRabbit") },
                actions = {
                    if (showToday) {
                        IconButton(onClick = onGoToToday) {
                            Icon(
                                imageVector = Icons.Default.Today,
                                contentDescription = "Go to today"
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddHabbit) {
                Icon(Icons.Default.Add, contentDescription = "Add habbit")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp)
        ) {
            DayHeaderRow(
                state = state,
                onDaySelected = onDaySelected,
                onPreviousWeek = onPreviousWeek,
                onNextWeek = onNextWeek
            )
            Spacer(Modifier.height(16.dp))
            DaySection(
                state = state,
                onToggle = onToggle,
                onEditHabbit = onEditHabbit,
                onEditOrder = onEditOrder
            )
            Spacer(Modifier.height(80.dp)) // room for FAB
        }
    }
}

// --- Day header -------------------------------------------------------------

// Large virtual range centered so the user can swipe many weeks in either
// direction without hitting the pager's bounds. The center page represents
// the week that was shown when the composable first entered composition.
private const val WeekPagerPageCount = 10_001
private const val WeekPagerInitialPage = WeekPagerPageCount / 2

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DayHeaderRow(
    state: WeeklyUiState,
    onDaySelected: (LocalDate) -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit
) {
    val baseWeekStart = remember { state.weekStart }
    val currentOffset = ChronoUnit.WEEKS.between(baseWeekStart, state.weekStart).toInt()
    val pagerState = rememberPagerState(
        initialPage = WeekPagerInitialPage,
        pageCount = { WeekPagerPageCount }
    )
    // Tracks the page we've already reconciled with the view-model so we can
    // tell swipe-driven movement apart from external week changes.
    val lastSyncedPage = remember { mutableIntStateOf(WeekPagerInitialPage) }

    // External week changes (today button, day tap, etc.) animate the pager
    // to the matching page.
    LaunchedEffect(currentOffset) {
        val target = WeekPagerInitialPage + currentOffset
        if (pagerState.currentPage != target) {
            lastSyncedPage.intValue = target
            pagerState.animateScrollToPage(target)
        }
    }

    // When a swipe settles on a new page, step the view-model one week per
    // page delta so its selectedDay follows the gesture.
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { settled ->
                val delta = settled - lastSyncedPage.intValue
                if (delta == 0) return@collect
                lastSyncedPage.intValue = settled
                when {
                    delta > 0 -> repeat(delta) { onNextWeek() }
                    delta < 0 -> repeat(-delta) { onPreviousWeek() }
                }
            }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxWidth()
    ) { page ->
        val pageWeekStart = baseWeekStart.plusWeeks((page - WeekPagerInitialPage).toLong())
        val pageDays = remember(pageWeekStart) { pageWeekStart.daysOfWeek() }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            pageDays.forEach { day ->
                DayHeaderCell(
                    day = day,
                    isToday = day == state.today,
                    isSelected = day == state.selectedDay,
                    onClick = { onDaySelected(day) }
                )
            }
        }
    }
}

@Composable
private fun RowScope.DayHeaderCell(
    day: LocalDate,
    isToday: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tileShape = RoundedCornerShape(10.dp)
    val tileColor = when {
        isSelected -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val numberColor = when {
        isSelected -> MaterialTheme.colorScheme.onSecondaryContainer
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }
    val labelColor = when {
        isSelected -> MaterialTheme.colorScheme.onSecondaryContainer
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = Modifier
            .weight(1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = tileColor, shape = tileShape)
                .clickable(onClick = onClick)
                .padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = day.format(DayLabelFormatter).take(1),
                style = MaterialTheme.typography.labelSmall,
                color = labelColor
            )
            Text(
                text = day.format(DayNumberFormatter),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = numberColor
            )
        }
    }
}

// --- Day section ------------------------------------------------------------

@Composable
private fun DaySection(
    state: WeeklyUiState,
    onToggle: (Long, LocalDate) -> Unit,
    onEditHabbit: (Long) -> Unit,
    onEditOrder: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val title = when (state.selectedDay) {
            state.today -> "Today"
            else -> state.selectedDay.format(FullDayFormatter)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            if (state.habbits.any { !it.isArchived }) {
                IconButton(onClick = onEditOrder) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Reorder habbits"
                    )
                }
            }
        }
        if (state.habbits.isEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "No habbits yet.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Tap + to add your first habbit and start tracking it today.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            state.habbits.forEach { habbit ->
                DayHabbitRow(
                    habbit = habbit,
                    state = state,
                    onToggle = onToggle,
                    onEdit = { onEditHabbit(habbit.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun DayHabbitRow(
    habbit: Habbit,
    state: WeeklyUiState,
    onToggle: (Long, LocalDate) -> Unit,
    onEdit: () -> Unit
) {
    val selected = state.selectedDay
    val completed = state.isCompleted(habbit.id, selected)
    val haptics = LocalHapticFeedback.current
    val context = LocalContext.current
    val today = state.today
    val hapticToggle: (Long, LocalDate) -> Unit = { id, date ->
        if (date.isAfter(today)) {
            Toast.makeText(context, "Are you a time traveler?", Toast.LENGTH_SHORT).show()
        } else {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onToggle(id, date)
        }
    }
    val archived = habbit.isArchived
    val surfaceColor = if (archived) {
        MaterialTheme.colorScheme.surfaceContainerLowest
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentAlpha = if (archived) 0.6f else 1f
    val accent = Color(habbit.colorArgb).let { if (archived) it.copy(alpha = 0.5f) else it }
    Surface(
        color = surfaceColor,
        tonalElevation = if (archived) 0.dp else 2.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { hapticToggle(habbit.id, selected) },
                onLongClick = onEdit
            )
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .alpha(contentAlpha)
        ) {
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
                    habbit.targetPerWeek?.let { target ->
                        Text(
                            text = "Target: $target/week",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit habbit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                CheckButton(
                    color = accent,
                    completed = completed,
                    onClick = { hapticToggle(habbit.id, selected) }
                )
            }
            Spacer(Modifier.height(10.dp))
            HabbitWeekHeatmap(habbit = habbit, state = state, onToggle = hapticToggle)
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

@Composable
private fun HabbitWeekHeatmap(
    habbit: Habbit,
    state: WeeklyUiState,
    onToggle: (Long, LocalDate) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        state.weekDays.forEach { day ->
            HeatmapCell(
                color = Color(habbit.colorArgb),
                completed = state.isCompleted(habbit.id, day),
                isSelected = day == state.selectedDay,
                isFuture = day.isAfter(state.today),
                onClick = { onToggle(habbit.id, day) }
            )
        }
    }
}

@Composable
private fun RowScope.HeatmapCell(
    color: Color,
    completed: Boolean,
    isSelected: Boolean,
    isFuture: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(6.dp)
    val emptyFill = MaterialTheme.colorScheme.surfaceContainerHigh
    val background = when {
        completed -> color
        isFuture -> Color.Transparent
        else -> emptyFill
    }
    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.onSurface
        completed -> color
        isFuture -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        else -> Color.Transparent
    }
    val borderWidth = when {
        isSelected -> 2.dp
        isFuture && !completed -> 1.5.dp
        else -> 0.dp
    }
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .alpha(if (isFuture && !completed) 0.6f else 1f)
            .background(color = background, shape = shape)
            .border(width = borderWidth, color = borderColor, shape = shape)
            .clickable(onClick = onClick)
    )
}

@Composable
private fun CheckButton(color: Color, completed: Boolean, onClick: () -> Unit) {
    val background = if (completed) color else Color.Transparent
    val borderColor = if (completed) color else MaterialTheme.colorScheme.outline
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(color = background, shape = CircleShape)
            .border(width = 2.dp, color = borderColor, shape = CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (completed) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Completed",
                tint = Color.White
            )
        }
    }
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
    ),
    Habbit(
        id = 4,
        name = "Meditate",
        colorArgb = HabbitColor.palette[0].toArgb(),
        targetPerWeek = 3,
        createdAt = Instant.EPOCH,
        archivedAt = Instant.EPOCH
    )
)

private fun previewState(
    today: LocalDate = LocalDate.of(2026, 10, 7),
    selectedOffset: Int = 0,
    habbits: List<Habbit> = previewHabbits
): WeeklyUiState {
    val weekStart = today.weekStart()
    val selected = today.plusDays(selectedOffset.toLong())
    val completions = buildSet {
        if (habbits.isNotEmpty()) {
            add(habbits[0].id to today.minusDays(1))
            add(habbits[0].id to today)
            if (habbits.size > 1) add(habbits[1].id to today.minusDays(2))
            if (habbits.size > 2) add(habbits[2].id to today.minusDays(3))
            val archived = habbits.firstOrNull { it.isArchived }
            if (archived != null) {
                add(archived.id to today.minusDays(4))
                add(archived.id to today.minusDays(5))
            }
        }
    }
    return WeeklyUiState(
        today = today,
        weekStart = weekStart,
        selectedDay = selected,
        habbits = habbits,
        completions = completions
    )
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Weekly — populated",
    heightDp = 1000
)
@Composable
private fun WeeklyContentPopulatedPreview() {
    HabbitRabbitTheme(darkTheme = true, dynamicColor = false) {
        WeeklyContent(
            state = previewState(),
            onAddHabbit = {},
            onEditHabbit = {},
            onEditOrder = {},
            onPreviousWeek = {},
            onNextWeek = {},
            onDaySelected = {},
            onToggle = { _, _ -> },
            onGoToToday = {}
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Weekly — empty",
    heightDp = 820
)
@Composable
private fun WeeklyContentEmptyPreview() {
    HabbitRabbitTheme(darkTheme = true, dynamicColor = false) {
        WeeklyContent(
            state = previewState(habbits = emptyList()),
            onAddHabbit = {},
            onEditHabbit = {},
            onEditOrder = {},
            onPreviousWeek = {},
            onNextWeek = {},
            onDaySelected = {},
            onToggle = { _, _ -> },
            onGoToToday = {}
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Day section — selected past day"
)
@Composable
private fun DaySectionPastDayPreview() {
    HabbitRabbitTheme(darkTheme = true, dynamicColor = false) {
        Box(Modifier.padding(16.dp)) {
            DaySection(
                state = previewState(selectedOffset = -2),
                onToggle = { _, _ -> },
                onEditHabbit = {},
                onEditOrder = {}
            )
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Habbit row (day)"
)
@Composable
private fun DayHabbitRowPreview() {
    HabbitRabbitTheme(darkTheme = true, dynamicColor = false) {
        Box(Modifier.padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DayHabbitRow(
                    habbit = previewHabbits[0],
                    state = previewState(),
                    onToggle = { _, _ -> },
                    onEdit = {}
                )
                DayHabbitRow(
                    habbit = previewHabbits[1],
                    state = previewState(),
                    onToggle = { _, _ -> },
                    onEdit = {}
                )
                DayHabbitRow(
                    habbit = previewHabbits.first { it.isArchived },
                    state = previewState(),
                    onToggle = { _, _ -> },
                    onEdit = {}
                )
            }
        }
    }
}
