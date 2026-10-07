package com.xtine.habbitrabbit.ui.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xtine.habbitrabbit.ui.common.HabbitColor
import com.xtine.habbitrabbit.ui.theme.HabbitRabbitTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHabbitScreen(
    onBack: () -> Unit,
    viewModel: AddEditHabbitViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showRemoveDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.saved, state.deleted) {
        if (state.saved || state.deleted) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Edit habbit" else "New habbit") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.isEditing) {
                        IconButton(
                            onClick = { showRemoveDialog = true },
                            enabled = !state.loading
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete or archive")
                        }
                    }
                }
            )
        }
    ) { padding ->
        AddEditForm(
            state = state,
            onNameChange = viewModel::onNameChange,
            onColorSelected = viewModel::onColorSelected,
            onTargetEnabledChange = viewModel::onTargetEnabledChange,
            onTargetValueChange = viewModel::onTargetValueChange,
            onSave = viewModel::save,
            padding = padding
        )
    }

    if (showRemoveDialog) {
        RemoveHabbitDialog(
            habbitName = state.name,
            onDismiss = { showRemoveDialog = false },
            onArchive = {
                showRemoveDialog = false
                viewModel.archive()
            },
            onDelete = {
                showRemoveDialog = false
                viewModel.delete()
            }
        )
    }
}

@Composable
private fun RemoveHabbitDialog(
    habbitName: String,
    onDismiss: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    val title = if (habbitName.isBlank()) "Remove habbit?" else "Remove \"$habbitName\"?"
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Archive hides the habbit from future weeks, " +
                        "but keeps its past history.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Delete removes the habbit and all of its tracked data.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            OutlinedButton(onClick = onArchive) { Text("Archive") }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                TextButton(
                    onClick = onDelete,
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Delete") }
            }
        }
    )
}

@Composable
private fun AddEditForm(
    state: AddEditUiState,
    onNameChange: (String) -> Unit,
    onColorSelected: (Int) -> Unit,
    onTargetEnabledChange: (Boolean) -> Unit,
    onTargetValueChange: (Int) -> Unit,
    onSave: () -> Unit,
    padding: PaddingValues
) {
    val focusManager: FocusManager = LocalFocusManager.current
    val keyboard: SoftwareKeyboardController? = LocalSoftwareKeyboardController.current
    val dismissInteractionSource = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .clickable(
                interactionSource = dismissInteractionSource,
                indication = null
            ) {
                focusManager.clearFocus()
                keyboard?.hide()
            }
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        OutlinedTextField(
            value = state.name,
            onValueChange = onNameChange,
            label = { Text("Enter Habbit Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        ColorPicker(
            selectedArgb = state.colorArgb,
            onColorSelected = onColorSelected
        )

        TargetSection(
            enabled = state.targetEnabled,
            value = state.targetPerWeek,
            onEnabledChange = onTargetEnabledChange,
            onValueChange = onTargetValueChange
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onSave,
            enabled = state.canSave,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isEditing) "Save changes" else "Add habbit")
        }
    }
}

@Composable
private fun ColorPicker(
    selectedArgb: Int,
    onColorSelected: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Color", style = MaterialTheme.typography.labelLarge)
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(132.dp)
        ) {
            items(HabbitColor.palette) { color ->
                val argb = color.toArgb()
                ColorSwatch(
                    color = color,
                    selected = argb == selectedArgb,
                    onClick = { onColorSelected(argb) }
                )
            }
        }
    }
}

@Composable
private fun ColorSwatch(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.onSurface else Color.Transparent
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(color = color, shape = CircleShape)
            .border(width = 3.dp, color = borderColor, shape = CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@Composable
private fun TargetSection(
    enabled: Boolean,
    value: Int,
    onEnabledChange: (Boolean) -> Unit,
    onValueChange: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Weekly target", style = MaterialTheme.typography.labelLarge)
                Text(
                    text = if (enabled) "Aim for $value day${if (value == 1) "" else "s"} a week"
                    else "Track-only, no goal",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Switch(checked = enabled, onCheckedChange = onEnabledChange)
        }
        if (enabled) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Slider(
                    value = value.toFloat(),
                    onValueChange = { onValueChange(it.toInt()) },
                    valueRange = 1f..7f,
                    steps = 5,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.size(12.dp))
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// --- Previews ---------------------------------------------------------------

private val previewNewState = AddEditUiState(
    id = 0,
    name = "",
    colorArgb = HabbitColor.defaultArgb,
    targetEnabled = false,
    targetPerWeek = 3
)

private val previewEditingState = AddEditUiState(
    id = 42,
    name = "Read 20 pages",
    colorArgb = HabbitColor.palette[2].toArgb(),
    targetEnabled = true,
    targetPerWeek = 5
)

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "AddEdit — new habbit"
)
@Composable
private fun AddEditFormNewPreview() {
    HabbitRabbitTheme(darkTheme = true, dynamicColor = false) {
        AddEditForm(
            state = previewNewState,
            onNameChange = {},
            onColorSelected = {},
            onTargetEnabledChange = {},
            onTargetValueChange = {},
            onSave = {},
            padding = PaddingValues(0.dp)
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "AddEdit — editing"
)
@Composable
private fun AddEditFormEditingPreview() {
    HabbitRabbitTheme(darkTheme = true, dynamicColor = false) {
        AddEditForm(
            state = previewEditingState,
            onNameChange = {},
            onColorSelected = {},
            onTargetEnabledChange = {},
            onTargetValueChange = {},
            onSave = {},
            padding = PaddingValues(0.dp)
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Color picker"
)
@Composable
private fun ColorPickerPreview() {
    HabbitRabbitTheme(darkTheme = true, dynamicColor = false) {
        Box(Modifier.padding(16.dp)) {
            ColorPicker(
                selectedArgb = HabbitColor.palette[4].toArgb(),
                onColorSelected = {}
            )
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Target — enabled"
)
@Composable
private fun TargetSectionEnabledPreview() {
    HabbitRabbitTheme(darkTheme = true, dynamicColor = false) {
        Box(Modifier.padding(16.dp)) {
            TargetSection(
                enabled = true,
                value = 4,
                onEnabledChange = {},
                onValueChange = {}
            )
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Target — disabled"
)
@Composable
private fun TargetSectionDisabledPreview() {
    HabbitRabbitTheme(darkTheme = true, dynamicColor = false) {
        Box(Modifier.padding(16.dp)) {
            TargetSection(
                enabled = false,
                value = 3,
                onEnabledChange = {},
                onValueChange = {}
            )
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Remove dialog"
)
@Composable
private fun RemoveHabbitDialogPreview() {
    HabbitRabbitTheme(darkTheme = true, dynamicColor = false) {
        RemoveHabbitDialog(
            habbitName = "Read 20 pages",
            onDismiss = {},
            onArchive = {},
            onDelete = {}
        )
    }
}
