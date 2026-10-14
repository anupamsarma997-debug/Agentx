package com.example.domain.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.logging.AppLogger

/**
 * BroadcastReceiver for handling daily alarm triggers and device boot events.
 * Triggers the once-per-day Top 10 notification and re-arms the schedule for the next day.
 */
class DailyTop10NewsNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        AppLogger.info("NotificationReceiver", "Received", "Action: $action")

        when (action) {
            DailyTop10NewsNotificationManager.ACTION_TRIGGER_DAILY_NOTIFICATION -> {
                // Post the once-per-day Top 10 digest
                DailyTop10NewsNotificationManager.triggerDailyNotification(context)

                // Re-arm alarm for the next day at 09:00 AM
                DailyTop10NewsNotificationManager.scheduleDailyAlarm(context, hour = 9, minute = 0)
            }
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                // Restore schedule across device reboots or app upgrades
                DailyTop10NewsNotificationManager.createNotificationChannel(context)
                DailyTop10NewsNotificationManager.scheduleDailyAlarm(context, hour = 9, minute = 0)
                AppLogger.info("NotificationReceiver", "BootRestored", "Restored daily notification alarm after boot/update")
            }
        }
    }
}
