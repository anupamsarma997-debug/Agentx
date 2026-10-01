package com.example.data.remote.firebase

import android.content.Context
import com.example.BuildConfig
import com.example.data.local.logging.AppLogger
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

/**
 * Production Firebase App Check manager.
 *
 * Enforces security boundary:
 * - DEBUG: Uses DebugAppCheckProviderFactory (logs debug token safely in Android Studio)
 * - RELEASE: Uses PlayIntegrityAppCheckProviderFactory (production attestation)
 *
 * Invariant: App Check failures or unconfigured Firebase projects NEVER crash the app.
 */
object FirebaseAppCheckManager {

    fun init(context: Context) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                AppLogger.info("AppCheck", "Init", "FirebaseApp not initialized, skipping App Check")
                return
            }

            val appCheck = FirebaseAppCheck.getInstance()

            if (BuildConfig.DEBUG) {
                appCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
                )
                AppLogger.info("AppCheck", "Init", "App Check initialized with DebugAppCheckProviderFactory")
            } else {
                try {
                    appCheck.installAppCheckProviderFactory(
                        PlayIntegrityAppCheckProviderFactory.getInstance()
                    )
                    AppLogger.info("AppCheck", "Init", "App Check initialized with PlayIntegrityAppCheckProviderFactory")
                } catch (e: Exception) {
                    AppLogger.warn("AppCheck", "Init", "Play Integrity App Check provider registration skipped", e)
                }
            }
        } catch (e: Exception) {
            AppLogger.warn("AppCheck", "Init", "App Check initialization error: ${e.message}", e)
        }
    }
}
