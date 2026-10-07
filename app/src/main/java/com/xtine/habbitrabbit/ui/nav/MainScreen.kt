package com.xtine.habbitrabbit.ui.nav

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.xtine.habbitrabbit.ui.edit.AddEditHabbitScreen
import com.xtine.habbitrabbit.ui.edit.AddEditHabbitViewModel
import com.xtine.habbitrabbit.ui.monthly.MonthlyScreen
import com.xtine.habbitrabbit.ui.reorder.ReorderHabbitsScreen
import com.xtine.habbitrabbit.ui.settings.SettingsScreen
import com.xtine.habbitrabbit.ui.theme.HabbitRabbitTheme
import com.xtine.habbitrabbit.ui.weekly.WeeklyScreen

object Routes {
    const val WEEKLY = "weekly"
    const val MONTHLY = "monthly"
    const val SETTINGS = "settings"
    const val ADD_HABIT = "habbit/new"
    const val EDIT_HABIT = "habbit/{${AddEditHabbitViewModel.ARG_HABIT_ID}}"
    const val REORDER_HABITS = "habbits/reorder"

    fun editHabbit(id: Long): String = "habbit/$id"
}

private data class BottomTab(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val bottomTabs = listOf(
    BottomTab(Routes.WEEKLY, "Weekly", Icons.Default.CalendarViewWeek),
    BottomTab(Routes.MONTHLY, "Monthly", Icons.Default.CalendarMonth),
    BottomTab(Routes.SETTINGS, "Settings", Icons.Default.Settings)
)

private val tabRoutes = bottomTabs.map { it.route }.toSet()

@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val currentRoute = destination?.route
    val showBottomBar = currentRoute in tabRoutes
    val selectedTab = bottomTabs.firstOrNull { tab ->
        destination?.hierarchy?.any { it.route == tab.route } == true
    }?.route ?: Routes.WEEKLY

    MainScaffold(
        selectedRoute = selectedTab,
        showBottomBar = showBottomBar,
        onTabSelected = { route ->
            if (route != currentRoute) {
                navController.navigate(route) {
                    popUpTo(Routes.WEEKLY) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.WEEKLY,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.WEEKLY) {
                WeeklyScreen(
                    onAddHabbit = { navController.navigate(Routes.ADD_HABIT) },
                    onEditHabbit = { id -> navController.navigate(Routes.editHabbit(id)) },
                    onEditOrder = { navController.navigate(Routes.REORDER_HABITS) }
                )
            }
            composable(Routes.MONTHLY) {
                MonthlyScreen(
                    onEditHabbit = { id -> navController.navigate(Routes.editHabbit(id)) },
                    onEditOrder = { navController.navigate(Routes.REORDER_HABITS) }
                )
            }
            composable(Routes.REORDER_HABITS) {
                ReorderHabbitsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.SETTINGS) {
                SettingsScreen()
            }
            composable(Routes.ADD_HABIT) {
                AddEditHabbitScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = Routes.EDIT_HABIT,
                arguments = listOf(
                    navArgument(AddEditHabbitViewModel.ARG_HABIT_ID) { type = NavType.LongType }
                )
            ) {
                AddEditHabbitScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun MainScaffold(
    selectedRoute: String,
    showBottomBar: Boolean,
    onTabSelected: (String) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = tab.route == selectedRoute,
                            onClick = { onTabSelected(tab.route) },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        content(padding)
    }
}

// --- Previews ---------------------------------------------------------------

@Composable
private fun PreviewTabBody(label: String, padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$label screen",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Main — Weekly selected"
)
@Composable
private fun MainScaffoldWeeklyPreview() {
    HabbitRabbitTheme(darkTheme = true, dynamicColor = false) {
        MainScaffold(
            selectedRoute = Routes.WEEKLY,
            showBottomBar = true,
            onTabSelected = {}
        ) { padding -> PreviewTabBody("Weekly", padding) }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Main — Monthly selected"
)
@Composable
private fun MainScaffoldMonthlyPreview() {
    HabbitRabbitTheme(darkTheme = true, dynamicColor = false) {
        MainScaffold(
            selectedRoute = Routes.MONTHLY,
            showBottomBar = true,
            onTabSelected = {}
        ) { padding -> PreviewTabBody("Monthly", padding) }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Main — Settings selected"
)
@Composable
private fun MainScaffoldSettingsPreview() {
    HabbitRabbitTheme(darkTheme = true, dynamicColor = false) {
        MainScaffold(
            selectedRoute = Routes.SETTINGS,
            showBottomBar = true,
            onTabSelected = {}
        ) { padding -> PreviewTabBody("Settings", padding) }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Main — bottom bar hidden (add/edit)"
)
@Composable
private fun MainScaffoldNoBarPreview() {
    HabbitRabbitTheme(darkTheme = true, dynamicColor = false) {
        MainScaffold(
            selectedRoute = Routes.WEEKLY,
            showBottomBar = false,
            onTabSelected = {}
        ) { padding -> PreviewTabBody("Add habbit", padding) }
    }
}
