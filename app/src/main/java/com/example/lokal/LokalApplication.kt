package com.example.lokal

import android.app.Application
import com.example.lokal.analytics.AnalyticsLogger

class LokalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AnalyticsLogger.initialize()
    }
}