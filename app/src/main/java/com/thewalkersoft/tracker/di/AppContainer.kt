package com.thewalkersoft.tracker.di

import android.content.Context
import com.thewalkersoft.tracker.data.local.AppDatabase
import com.thewalkersoft.tracker.data.repository.CycleRepositoryImpl
import com.thewalkersoft.tracker.domain.predictor.CyclePredictorEngine
import com.thewalkersoft.tracker.domain.repository.CycleRepository
import com.thewalkersoft.tracker.ui.security.SecurityPreferences

interface AppContainer {
    val cycleRepository: CycleRepository
    val cyclePredictorEngine: CyclePredictorEngine
    val securityPreferences: SecurityPreferences
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    private val database: AppDatabase by lazy {
        AppDatabase.getInstance(context)
    }

    override val cyclePredictorEngine: CyclePredictorEngine by lazy {
        CyclePredictorEngine()
    }

    override val securityPreferences: SecurityPreferences by lazy {
        SecurityPreferences(context)
    }

    override val cycleRepository: CycleRepository by lazy {
        CycleRepositoryImpl(
            periodLogDao = database.periodLogDao(),
            symptomLogDao = database.symptomLogDao(),
            predictorEngine = cyclePredictorEngine
        )
    }
}
