package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.GalleryScreen
import com.example.ui.screens.HealthMetricsScreen
import com.example.ui.screens.StudioEditorScreen
import com.example.ui.screens.WatchfaceEditorScreen
import com.example.ui.screens.WearableSyncScreen
import com.example.ui.theme.*
import com.example.ui.viewmodel.WatchStudioViewModel

enum class MainDestination(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    STUDIO("studio", "Studio", Icons.Filled.Watch, Icons.Outlined.Watch),
    GALLERY("gallery", "Galería", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    SYNC("sync", "Wearable", Icons.Filled.Sync, Icons.Outlined.Sync),
    HEALTH("health", "Salud", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder)
}

class MainActivity : ComponentActivity() {
    private val viewModel: WatchStudioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GalaxyWatchStudioTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: WatchStudioViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: MainDestination.STUDIO.route

    val snackbarHostState = remember { SnackbarHostState() }
    val userMessage by viewModel.userMessage.collectAsState()

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissUserMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = DarkSurface,
                    contentColor = TextPrimary,
                    actionColor = GalaxyCyan,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                contentColor = TextPrimary,
                tonalElevation = 8.dp
            ) {
                MainDestination.values().forEach { dest ->
                    val isSelected = currentRoute == dest.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != dest.route) {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) dest.selectedIcon else dest.unselectedIcon,
                                contentDescription = dest.title
                            )
                        },
                        label = {
                            Text(
                                text = dest.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBackground,
                            selectedTextColor = GalaxyCyan,
                            indicatorColor = GalaxyCyan,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_${dest.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MainDestination.STUDIO.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(MainDestination.STUDIO.route) {
                WatchfaceEditorScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable(MainDestination.GALLERY.route) {
                GalleryScreen(
                    viewModel = viewModel,
                    onNavigateToStudio = {
                        navController.navigate(MainDestination.STUDIO.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable(MainDestination.SYNC.route) {
                WearableSyncScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable(MainDestination.HEALTH.route) {
                HealthMetricsScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
