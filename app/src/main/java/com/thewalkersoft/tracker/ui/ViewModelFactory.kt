package com.thewalkersoft.tracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.thewalkersoft.tracker.TrackerApplication
import com.thewalkersoft.tracker.di.AppContainer
import com.thewalkersoft.tracker.ui.dashboard.DashboardViewModel
import com.thewalkersoft.tracker.ui.export.ExportViewModel
import com.thewalkersoft.tracker.ui.history.HistoryViewModel
import com.thewalkersoft.tracker.ui.symptoms.SymptomsViewModel

object AppViewModelProvider {
    val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as TrackerApplication
            val appContainer: AppContainer = application.container

            return when {
                modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                    DashboardViewModel(
                        repository = appContainer.cycleRepository,
                        securityPreferences = appContainer.securityPreferences
                    ) as T
                }
                modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                    HistoryViewModel(
                        repository = appContainer.cycleRepository
                    ) as T
                }
                modelClass.isAssignableFrom(SymptomsViewModel::class.java) -> {
                    SymptomsViewModel(
                        repository = appContainer.cycleRepository
                    ) as T
                }
                modelClass.isAssignableFrom(ExportViewModel::class.java) -> {
                    ExportViewModel(
                        repository = appContainer.cycleRepository
                    ) as T
                }
                else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}
