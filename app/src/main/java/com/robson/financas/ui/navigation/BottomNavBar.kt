package com.robson.financas.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.robson.financas.ui.designsystem.LiveDot
import com.robson.financas.ui.designsystem.drawTopDivider
import com.robson.financas.ui.theme.BorderSubtle
import com.robson.financas.ui.theme.HudCyanLight
import com.robson.financas.ui.theme.JetBrainsMonoFamily
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
                icon = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (selected) {
                            LiveDot(modifier = Modifier.height(6.dp))
                            Spacer(modifier = Modifier.height(3.dp))
                        } else {
                            Spacer(modifier = Modifier.height(9.dp))
                        }
                        Icon(tab.icon, contentDescription = tab.label)
                        if (selected) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Spacer(
                                modifier = Modifier
                                    .width(18.dp)
                                    .height(2.dp)
                                    .background(HudCyanLight),
                            )
                        }
                    }
                },
                label = {
                    Text(
                        tab.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = HudCyanLight,
                    selectedTextColor = HudCyanLight,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = TextTertiary,
                    unselectedTextColor = TextTertiary,
                ),
            )
        }
    }
}
