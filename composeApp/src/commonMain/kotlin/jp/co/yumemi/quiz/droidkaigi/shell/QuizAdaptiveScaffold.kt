package jp.co.yumemi.quiz.droidkaigi.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import jp.co.yumemi.quiz.droidkaigi.core.ui.generated.resources.Res
import jp.co.yumemi.quiz.droidkaigi.core.ui.generated.resources.nav_home
import jp.co.yumemi.quiz.droidkaigi.core.ui.generated.resources.nav_ranking
import jp.co.yumemi.quiz.droidkaigi.navigation.Route
import org.jetbrains.compose.resources.stringResource

@Composable
fun QuizAdaptiveScaffold(currentRoute: Route, onNavigate: (Route) -> Unit, content: @Composable () -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp
        val navItems = listOf(
            NavItem(
                route = Route.Home,
                label = stringResource(Res.string.nav_home),
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
            ),
            NavItem(
                route = Route.Ranking,
                label = stringResource(Res.string.nav_ranking),
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
                                (isQuizFlow(currentRoute) && item.route == Route.Home)
                        NavigationRailItem(
                            selected = selected,
                            onClick = { onNavigate(item.route) },
                            icon = { NavIcon(item = item, selected = selected) },
                            label = { Text(item.label) },
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .consumeWindowInsets(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Start),
                        ),
                ) {
                    content()
                }
            }
        } else {
            // Custom shell (no Scaffold): content draws edge-to-edge; bottom bar overlays.
            // Screens apply safeDrawing insets to interactive content themselves.
            val showBottomBar = currentRoute == Route.Home || currentRoute == Route.Ranking
            val density = LocalDensity.current
            var bottomBarHeightPx by remember { mutableIntStateOf(0) }

            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (showBottomBar && bottomBarHeightPx > 0) {
                                val bottomPad = with(density) { bottomBarHeightPx.toDp() }
                                Modifier
                                    .padding(bottom = bottomPad)
                                    .consumeWindowInsets(PaddingValues(bottom = bottomPad))
                            } else {
                                Modifier
                            },
                        ),
                ) {
                    content()
                }
                if (showBottomBar) {
                    NavigationBar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                bottomBarHeightPx = coordinates.size.height
                            },
                    ) {
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

private fun isQuizFlow(route: Route): Boolean = route == Route.Quiz || route == Route.Result

private data class NavItem(
    val route: Route,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)
