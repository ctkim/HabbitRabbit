package com.xtine.habbitrabbit.ui.reorder

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xtine.habbitrabbit.data.model.Habbit

@Composable
fun ReorderHabbitsScreen(
    onBack: () -> Unit,
    viewModel: ReorderHabbitsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ReorderHabbitsContent(
        habbits = state.habbits,
        onBack = {
            viewModel.commit()
            onBack()
        },
        onMove = viewModel::moveItem,
        onDragEnd = viewModel::commit
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReorderHabbitsContent(
    habbits: List<Habbit>,
    onBack: () -> Unit,
    onMove: (Int, Int) -> Unit,
    onDragEnd: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reorder habbits") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Done"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (habbits.isEmpty()) {
                Text(
                    text = "No active habbits to reorder.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "Drag the handles to reorder.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ReorderableList(
                    habbits = habbits,
                    onMove = onMove,
                    onDragEnd = onDragEnd
                )
            }
        }
    }
}

@Composable
private fun ReorderableList(
    habbits: List<Habbit>,
    onMove: (Int, Int) -> Unit,
    onDragEnd: () -> Unit
) {
    val itemHeights = remember { mutableStateMapOf<Long, Int>() }
    var draggingId by remember { mutableStateOf<Long?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val haptics = LocalHapticFeedback.current
    val spacingPx = with(LocalDensity.current) { 8.dp.toPx() }
    // Keep the drag closure's view of the list fresh across reorders — the
    // pointerInput coroutine is pinned via `key(habbit.id)` so it survives
    // list reorders, but the enclosing composable captures stale references.
    val latestHabbits by rememberUpdatedState(habbits)
    val latestOnMove by rememberUpdatedState(onMove)
    val latestOnDragEnd by rememberUpdatedState(onDragEnd)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        habbits.forEach { habbit ->
            // Pinning the slot to habbit.id keeps the pointerInput coroutine
            // alive when the list reorders mid-drag; otherwise the gesture is
            // cancelled on each swap and the card gets stuck between slots.
            key(habbit.id) {
                val isDragging = draggingId == habbit.id
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onSizeChanged { itemHeights[habbit.id] = it.height }
                        .zIndex(if (isDragging) 1f else 0f)
                        .graphicsLayer { translationY = if (isDragging) dragOffset else 0f }
                ) {
                    ReorderRow(
                        habbit = habbit,
                        isDragging = isDragging,
                        dragModifier = Modifier.pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    draggingId = habbit.id
                                    dragOffset = 0f
                                },
                                onDragEnd = {
                                    draggingId = null
                                    dragOffset = 0f
                                    latestOnDragEnd()
                                },
                                onDragCancel = {
                                    draggingId = null
                                    dragOffset = 0f
                                }
                            ) { change, drag ->
                                change.consume()
                                dragOffset += drag.y
                                val currentId = draggingId ?: return@detectDragGestures
                                val list = latestHabbits
                                val currentIndex = list.indexOfFirst { it.id == currentId }
                                if (currentIndex < 0) return@detectDragGestures
                                val currentHeight = itemHeights[currentId] ?: return@detectDragGestures
                                val threshold = currentHeight * 0.6f
                                if (dragOffset > threshold && currentIndex < list.lastIndex) {
                                    val neighborHeight = list.getOrNull(currentIndex + 1)
                                        ?.let { itemHeights[it.id] } ?: currentHeight
                                    latestOnMove(currentIndex, currentIndex + 1)
                                    dragOffset -= neighborHeight.toFloat() + spacingPx
                                } else if (dragOffset < -threshold && currentIndex > 0) {
                                    val neighborHeight = list.getOrNull(currentIndex - 1)
                                        ?.let { itemHeights[it.id] } ?: currentHeight
                                    latestOnMove(currentIndex, currentIndex - 1)
                                    dragOffset += neighborHeight.toFloat() + spacingPx
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReorderRow(
    habbit: Habbit,
    isDragging: Boolean,
    dragModifier: Modifier
) {
    Surface(
        tonalElevation = if (isDragging) 6.dp else 2.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isDragging) Modifier.shadow(8.dp, MaterialTheme.shapes.medium) else Modifier)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(color = Color(habbit.colorArgb), shape = CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(habbit.name, style = MaterialTheme.typography.titleMedium)
                val subtitle = habbit.targetPerWeek?.let { "Target: $it/week" } ?: "Track-only"
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Drag to reorder",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(32.dp)
                    .padding(4.dp)
                    .then(dragModifier)
            )
        }
    }
}
