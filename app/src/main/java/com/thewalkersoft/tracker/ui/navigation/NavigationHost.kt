package com.thewalkersoft.tracker.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.thewalkersoft.tracker.ui.AppViewModelProvider
import com.thewalkersoft.tracker.ui.dashboard.DashboardScreen
import com.thewalkersoft.tracker.ui.dashboard.DashboardViewModel
import com.thewalkersoft.tracker.ui.dashboard.components.QuickLogButton
import com.thewalkersoft.tracker.ui.export.ExportScreen
import com.thewalkersoft.tracker.ui.export.ExportViewModel
import com.thewalkersoft.tracker.ui.history.HistoryScreen
import com.thewalkersoft.tracker.ui.history.HistoryViewModel
import com.thewalkersoft.tracker.ui.logging.LogPeriodBottomSheet
import com.thewalkersoft.tracker.ui.symptoms.SymptomsScreen
import com.thewalkersoft.tracker.ui.symptoms.SymptomsViewModel

@Composable
fun MainAppNavigation(
    modifier: Modifier = Modifier
) {
    val navController: NavHostController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Dashboard.route

    var showLoggingSheet by remember { mutableStateOf(false) }
    var loggingInitialDate by remember { mutableStateOf<java.time.LocalDate?>(null) }

    val dashboardViewModel: DashboardViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val historyViewModel: HistoryViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val symptomsViewModel: SymptomsViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val exportViewModel: ExportViewModel = viewModel(factory = AppViewModelProvider.Factory)

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                screenItems.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(imageVector = screen.icon, contentDescription = screen.title)
                        },
                        label = { Text(screen.title) }
                    )
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = currentRoute == Dashboard.route,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                QuickLogButton(
                    onClick = {
                        loggingInitialDate = null
                        showLoggingSheet = true
                    }
                )
            }
        },
        modifier = modifier
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Dashboard.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            composable(Dashboard.route) {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToHistory = { navController.navigate(History.route) },
                    onNavigateToSymptoms = { navController.navigate(Symptoms.route) },
                    onOpenLoggingSheet = {
                        loggingInitialDate = null
                        showLoggingSheet = true
                    },
                    onSelectDate = { selectedDate ->
                        loggingInitialDate = selectedDate
                        showLoggingSheet = true
                    }
                )
            }

            composable(History.route) {
                HistoryScreen(
                    viewModel = historyViewModel
                )
            }

            composable(Symptoms.route) {
                SymptomsScreen(
                    viewModel = symptomsViewModel
                )
            }

            composable(Export.route) {
                ExportScreen(
                    viewModel = exportViewModel
                )
            }
        }

        if (showLoggingSheet) {
            LogPeriodBottomSheet(
                initialStartDate = loggingInitialDate ?: java.time.LocalDate.now(),
                onDismiss = { showLoggingSheet = false },
                onSave = { startDate, endDate, flow, notes, isReset, bbt, cramps, mood, ovulation ->
                    dashboardViewModel.savePeriodAndSymptoms(
                        startDate = startDate,
                        endDate = endDate,
                        flowIntensity = flow,
                        notes = notes,
                        isPostpartumReset = isReset,
                        bbt = bbt,
                        cramps = cramps,
                        mood = mood,
                        ovulation = ovulation
                    )
                }
            )
        }
    }
}
