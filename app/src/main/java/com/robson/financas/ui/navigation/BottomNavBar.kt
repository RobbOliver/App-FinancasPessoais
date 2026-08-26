package com.robson.financas.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.robson.financas.ui.designsystem.drawTopDivider
import com.robson.financas.ui.theme.AccentMutedSurface
import com.robson.financas.ui.theme.BorderSubtle
import com.robson.financas.ui.theme.HudCyanLight
import com.robson.financas.ui.theme.SurfaceElevated
import com.robson.financas.ui.theme.TextTertiary

private data class BottomTab(val screen: Screen, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val bottomTabs = listOf(
    BottomTab(Screen.Dashboard, "Resumo", Icons.Filled.Home),
    BottomTab(Screen.Transactions, "Transações", Icons.Filled.Receipt),
    BottomTab(Screen.Goals, "Metas", Icons.Filled.Flag),
    BottomTab(Screen.More, "Mais", Icons.Filled.MoreHoriz),
)

val bottomTabRoutes: Set<String> = bottomTabs.map { it.screen.route }.toSet()

@Composable
fun FinanceBottomNavBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    NavigationBar(
        modifier = Modifier.drawTopDivider(BorderSubtle),
        containerColor = SurfaceElevated,
        tonalElevation = 0.dp,
    ) {
        bottomTabs.forEach { tab ->
            val selected = currentDestination?.hierarchy?.any { it.route == tab.screen.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(tab.screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = HudCyanLight,
                    selectedTextColor = HudCyanLight,
                    indicatorColor = AccentMutedSurface,
                    unselectedIconColor = TextTertiary,
                    unselectedTextColor = TextTertiary,
                ),
            )
        }
    }
}
