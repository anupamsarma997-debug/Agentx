package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.local.logging.AppLogger
import com.example.data.remote.firebase.FirebaseAppCheckManager
import com.google.firebase.FirebaseApp

/**
 * Main Application class for SocialAgent.
 *
 * Initializes security logging, Firebase, and Firebase App Check safely.
 * Any missing remote configurations (like missing google-services.json) are handled
 * gracefully and never crash app startup.
 */
class SocialAgentApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 1. Initialize In-App Logging with Room persistence
        try {
            val db = AppDatabase.getInstance(this)
            AppLogger.init(db.appLogDao())
            AppLogger.info("App", "Startup", "SocialAgent Application initialized successfully")
        } catch (e: Exception) {
            android.util.Log.e("SocialAgentApp", "Database/Logger initialization issue", e)
        }

        // 2. Initialize Firebase gracefully if google-services.json is present
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
            AppLogger.info("Firebase", "Startup", "FirebaseApp initialized")
        } catch (e: Exception) {
            AppLogger.warn("Firebase", "Startup", "FirebaseApp initialization deferred: ${e.message}")
        }

        // 3. Initialize Firebase App Check with Debug/Release safety boundary
        try {
            FirebaseAppCheckManager.init(this)
        } catch (e: Exception) {
            AppLogger.warn("AppCheck", "Startup", "App Check initialization warning: ${e.message}")
        }
    }
}
