package com.thewalkersoft.tracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Mood
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Favorite)
    object History : Screen("history", "History", Icons.AutoMirrored.Filled.List)
    object Symptoms : Screen("symptoms", "Symptoms", Icons.Default.Mood)
    object Export : Screen("export", "Doctor Report", Icons.Default.Assessment)

    companion object {
        val items = listOf(Dashboard, History, Symptoms, Export)
    }
}
