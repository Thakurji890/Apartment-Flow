package com.example.core.util

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

object FirebaseInitializer {
    private const val TAG = "FirebaseInitializer"

    @Synchronized
    fun ensureInitialized(context: Context): FirebaseApp? {
        return try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseApp.getInstance()
            } else {
                val app = FirebaseApp.initializeApp(context)
                if (app != null) {
                    app
                } else {
                    val fallbackOptions = FirebaseOptions.Builder()
                        .setApplicationId("1:184899352932:android:9d45e7f1a2345bc89012de")
                        .setApiKey("AIzaSyDummyKeyForApartmentFlowProject01")
                        .setProjectId("apartmentflow-lfsqkx")
                        .setStorageBucket("apartmentflow-lfsqkx.appspot.com")
                        .build()
                    FirebaseApp.initializeApp(context.applicationContext, fallbackOptions)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Notice during FirebaseApp initialization: ${e.message}")
            try {
                FirebaseApp.getInstance()
            } catch (ignored: Exception) {
                null
            }
        }
    }
}
