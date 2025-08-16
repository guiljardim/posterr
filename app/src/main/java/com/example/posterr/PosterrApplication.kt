package com.example.posterr

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PosterrApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}