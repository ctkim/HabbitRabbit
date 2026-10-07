package com.xtine.habbitrabbit.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.xtine.habbitrabbit.MainActivity
import com.xtine.habbitrabbit.R
import com.xtine.habbitrabbit.data.model.Habbit
import kotlinx.coroutines.flow.collect
import java.time.LocalDate

class HabbitRabbitWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entry = context.widgetEntryPoint()
        val repository = entry.habbitRepository()
        val today = entry.clock().today()

        // Observe the store so the widget re-renders on every change. Reading
        // once via first() can race with the DataStore-backed StateFlow, which
        // caused toggles to appear sticky.
        repository.store.collect { store ->
            val completed = store.entries
                .asSequence()
                .filter { it.date == today }
                .map { it.habbitId }
                .toSet()
            val visibleHabbits = store.habbits.filterNot { it.isArchived }
            provideContent {
                GlanceTheme {
                    WidgetBody(
                        habbits = visibleHabbits,
                        completedToday = completed,
                        today = today
                    )
                }
            }
        }
    }
}

@Composable
private fun WidgetBody(
    habbits: List<Habbit>,
    completedToday: Set<Long>,
    today: LocalDate
) {
    val bg = GlanceTheme.colors.widgetBackground
    val onBg = GlanceTheme.colors.onSurface
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bg)
            .padding(12.dp)
    ) {
        WidgetHeader(today = today, color = onBg)
        Spacer(GlanceModifier.height(8.dp))
        if (habbits.isEmpty()) {
            EmptyBody()
        } else {
            LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                items(habbits, itemId = { it.id }) { habbit ->
                    HabbitWidgetRow(
                        habbit = habbit,
                        completed = habbit.id in completedToday
                    )
                }
            }
        }
    }
}

@Composable
private fun WidgetHeader(today: LocalDate, color: ColorProvider) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = GlanceModifier
            .fillMaxWidth()
            .clickable(actionStartActivity(MainActivity::class.java))
    ) {
        Text(
            text = "Today · ${today.month.name.lowercase().replaceFirstChar { it.titlecase() }} ${today.dayOfMonth}",
            style = TextStyle(fontWeight = FontWeight.Medium, color = color)
        )
    }
}

@Composable
private fun EmptyBody() {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(actionStartActivity(MainActivity::class.java)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No habbits yet. Tap to add.",
            style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant)
        )
    }
}

@Composable
private fun HabbitWidgetRow(habbit: Habbit, completed: Boolean) {
    val habbitColor = Color(habbit.colorArgb)
    val toggleAction = actionRunCallback<ToggleHabbitAction>(
        actionParametersOf(ToggleHabbitAction.HabbitIdKey to habbit.id)
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(toggleAction)
    ) {
        Box(
            modifier = GlanceModifier
                .size(10.dp)
                .cornerRadius(5.dp)
                .background(ColorProvider(habbitColor)),
            content = {}
        )
        Spacer(GlanceModifier.width(8.dp))
        Text(
            text = habbit.name,
            style = TextStyle(color = GlanceTheme.colors.onSurface),
            modifier = GlanceModifier.defaultWeight()
        )
        Spacer(GlanceModifier.width(8.dp))
        CheckChip(color = habbitColor, completed = completed)
    }
}

@Composable
private fun CheckChip(color: Color, completed: Boolean) {
    val background: ColorProvider =
        if (completed) ColorProvider(color) else GlanceTheme.colors.surfaceVariant
    Box(
        modifier = GlanceModifier
            .size(28.dp)
            .cornerRadius(14.dp)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        if (completed) {
            Image(
                provider = ImageProvider(R.drawable.ic_widget_check),
                contentDescription = "Completed",
                colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
                modifier = GlanceModifier.size(16.dp)
            )
        }
    }
}

@Suppress("UNUSED") // kept for future loading state
@Composable
private fun WidgetLoading() {
    Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

private fun Color.argb(): Int = toArgb()
