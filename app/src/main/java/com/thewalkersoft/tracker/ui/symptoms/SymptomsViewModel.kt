package com.thewalkersoft.tracker.ui.symptoms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thewalkersoft.tracker.data.local.entity.SymptomLogEntity
import com.thewalkersoft.tracker.domain.repository.CycleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SymptomsViewModel(
    private val repository: CycleRepository
) : ViewModel() {

    val symptoms: StateFlow<List<SymptomLogEntity>> = repository.getAllSymptoms()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteSymptom(id: Long) {
        viewModelScope.launch {
            repository.deleteSymptomById(id)
        }
    }
}
