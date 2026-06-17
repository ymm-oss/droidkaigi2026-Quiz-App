package com.droidkaigi.quiz.shell

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.droidkaigi.quiz.navigation.Route

@Composable
fun QuizAdaptiveScaffold(
    currentRoute: Route,
    onNavigate: (Route) -> Unit,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp
        val navItems = listOf(
            NavItem(
                route = Route.Home,
                label = "ホーム",
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
            ),
            NavItem(
                route = Route.Ranking,
                label = "ランキング",
                selectedIcon = Icons.Filled.Leaderboard,
                unselectedIcon = Icons.Outlined.Leaderboard,
            ),
        )

        if (useRail) {
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(modifier = Modifier.fillMaxHeight()) {
                    navItems.forEach { item ->
                        val selected =
                            currentRoute == item.route ||
                                isQuizFlow(currentRoute) && item.route == Route.Home
                        NavigationRailItem(
                            selected = selected,
                            onClick = { onNavigate(item.route) },
                            icon = { NavIcon(item = item, selected = selected) },
                            label = { Text(item.label) },
                        )
                    }
                }
                BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    content()
                }
            }
        } else {
            val imeInsets = WindowInsets.ime
            val isImeVisible by remember { derivedStateOf { imeInsets.getBottom(Density(1f)) > 0 } }
            val showBottomBar =
                !isImeVisible && (currentRoute == Route.Home || currentRoute == Route.Ranking)
            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    if (showBottomBar) {
                        NavigationBar {
                            navItems.forEach { item ->
                                val selected = currentRoute == item.route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { onNavigate(item.route) },
                                    icon = { NavIcon(item = item, selected = selected) },
                                    label = { Text(item.label) },
                                )
                            }
                        }
                    }
                },
            ) { padding ->
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun NavIcon(item: NavItem, selected: Boolean) {
    Icon(
        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
        contentDescription = item.label,
    )
}

private fun isQuizFlow(route: Route): Boolean =
    route == Route.Quiz || route == Route.Result

private data class NavItem(
    val route: Route,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)
