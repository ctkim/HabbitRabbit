package com.xtine.habbitrabbit.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xtine.habbitrabbit.data.model.ThemePreference
import com.xtine.habbitrabbit.ui.theme.HabbitRabbitTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) }
    ) { padding ->
        SettingsContent(
            selected = theme,
            onSelect = viewModel::onThemeSelected,
            padding = padding
        )
    }
}

@Composable
private fun SettingsContent(
    selected: ThemePreference,
    onSelect: (ThemePreference) -> Unit,
    padding: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Appearance",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Surface(
            tonalElevation = 2.dp,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.selectableGroup()) {
                ThemeOption(
                    title = "System default",
                    description = "Follow the device setting",
                    selected = selected == ThemePreference.SYSTEM,
                    onClick = { onSelect(ThemePreference.SYSTEM) }
                )
                ThemeOption(
                    title = "Light",
                    description = "Always use the light theme",
                    selected = selected == ThemePreference.LIGHT,
                    onClick = { onSelect(ThemePreference.LIGHT) }
                )
                ThemeOption(
                    title = "Dark",
                    description = "Always use the dark theme",
                    selected = selected == ThemePreference.DARK,
                    onClick = { onSelect(ThemePreference.DARK) }
                )
            }
        }
    }
}

@Composable
private fun ThemeOption(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        androidx.compose.foundation.layout.Spacer(Modifier.padding(horizontal = 6.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// --- Previews ---------------------------------------------------------------

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Settings — System"
)
@Composable
private fun SettingsSystemPreview() {
    HabbitRabbitTheme(darkTheme = true, dynamicColor = false) {
        SettingsContent(
            selected = ThemePreference.SYSTEM,
            onSelect = {},
            padding = PaddingValues(0.dp)
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Settings — Dark"
)
@Composable
private fun SettingsDarkPreview() {
    HabbitRabbitTheme(darkTheme = true, dynamicColor = false) {
        SettingsContent(
            selected = ThemePreference.DARK,
            onSelect = {},
            padding = PaddingValues(0.dp)
        )
    }
}
