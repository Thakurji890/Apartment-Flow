package com.example

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import com.example.core.util.FirebaseInitializer

@HiltAndroidApp
class ApartmentFlowApp : Application(), Configuration.Provider {
    override fun onCreate() {
        super.onCreate()
        FirebaseInitializer.ensureInitialized(this)
    }

    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
