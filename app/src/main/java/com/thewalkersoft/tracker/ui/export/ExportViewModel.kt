package com.thewalkersoft.tracker.ui.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thewalkersoft.tracker.data.local.entity.SymptomLogEntity
import com.thewalkersoft.tracker.domain.model.CycleRecord
import com.thewalkersoft.tracker.domain.model.PredictionResult
import com.thewalkersoft.tracker.domain.repository.CycleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

sealed class ExportState {
    object Idle : ExportState()
    object Generating : ExportState()
    data class Success(val file: File, val uri: Uri) : ExportState()
    data class Error(val message: String) : ExportState()
}

class ExportViewModel(
    private val repository: CycleRepository
) : ViewModel() {

    val cycles: StateFlow<List<CycleRecord>> = repository.getAllCycleRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val symptoms: StateFlow<List<SymptomLogEntity>> = repository.getAllSymptoms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val prediction: StateFlow<PredictionResult?> = repository.getPrediction()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    fun generatePdfReport(context: Context) {
        viewModelScope.launch {
            _exportState.value = ExportState.Generating
            try {
                val generator = DoctorPdfGenerator(context)
                val file = generator.generateClinicalReport(
                    cycles = cycles.value,
                    symptoms = symptoms.value,
                    prediction = prediction.value
                )

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                _exportState.value = ExportState.Success(file, uri)
            } catch (e: Exception) {
                _exportState.value = ExportState.Error(e.localizedMessage ?: "Failed to generate report")
            }
        }
    }

    fun shareReport(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Menstrual Cycle & Health Clinical Report")
            putExtra(Intent.EXTRA_TEXT, "Attached is my confidential menstrual cycle health report generated from Adaptive Cycle Tracker.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Clinical Report with Doctor"))
    }

    fun viewReport(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Open Medical Report"))
    }

    fun resetExportState() {
        _exportState.value = ExportState.Idle
    }
}
