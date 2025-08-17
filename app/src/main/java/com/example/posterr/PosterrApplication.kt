package com.example.posterr

import android.app.Application
import com.example.posterr.data.PosterrDatabase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class PosterrApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                val database = PosterrDatabase.getDatabase(this@PosterrApplication, scope)
                PosterrDatabase.populateDatabase(database)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}