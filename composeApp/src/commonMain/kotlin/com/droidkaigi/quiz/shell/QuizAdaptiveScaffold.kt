package com.droidkaigi.quiz.shell

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
            NavItem(Route.Home, "ホーム"),
            NavItem(Route.Ranking, "ランキング"),
        )

        if (useRail) {
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(modifier = Modifier.fillMaxHeight()) {
                    navItems.forEach { item ->
                        NavigationRailItem(
                            selected = currentRoute == item.route || isQuizFlow(currentRoute) && item.route == Route.Home,
                            onClick = { onNavigate(item.route) },
                            icon = { Text(item.label.take(1)) },
                            label = { Text(item.label) },
                        )
                    }
                }
                BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    content()
                }
            }
        } else {
            Scaffold(
                bottomBar = {
                    if (currentRoute == Route.Home || currentRoute == Route.Ranking) {
                        NavigationBar {
                            navItems.forEach { item ->
                                NavigationBarItem(
                                    selected = currentRoute == item.route,
                                    onClick = { onNavigate(item.route) },
                                    icon = { Text(item.label.take(1)) },
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

private fun isQuizFlow(route: Route): Boolean =
    route == Route.Quiz || route == Route.Result

private data class NavItem(val route: Route, val label: String)
