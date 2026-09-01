package com.thewalkersoft.tracker.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thewalkersoft.tracker.domain.model.CycleRecord
import com.thewalkersoft.tracker.domain.repository.CycleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repository: CycleRepository
) : ViewModel() {

    val cycleRecords: StateFlow<List<CycleRecord>> = repository.getAllCycleRecords()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteLog(id: Long) {
        viewModelScope.launch {
            repository.deletePeriodLogById(id)
        }
    }
}
