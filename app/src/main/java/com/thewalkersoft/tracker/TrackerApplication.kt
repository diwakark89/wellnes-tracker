package com.thewalkersoft.tracker

import android.app.Application
import com.thewalkersoft.tracker.di.AppContainer
import com.thewalkersoft.tracker.di.DefaultAppContainer

class TrackerApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
