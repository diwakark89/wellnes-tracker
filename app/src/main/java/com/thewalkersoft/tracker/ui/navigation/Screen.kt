package com.thewalkersoft.tracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Mood
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector)

data object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Favorite)
data object History : Screen("history", "History", Icons.AutoMirrored.Filled.List)
data object Symptoms : Screen("symptoms", "Symptoms", Icons.Default.Mood)
data object Export : Screen("export", "Doctor Report", Icons.Default.Assessment)

val screenItems = listOf(Dashboard, History, Symptoms, Export)
