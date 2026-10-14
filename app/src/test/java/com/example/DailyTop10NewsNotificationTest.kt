package com.example

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.example.domain.notification.DailyTop10NewsNotificationManager
import com.example.domain.notification.DailyTop10NewsNotificationReceiver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DailyTop10NewsNotificationTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        val app = context as Application
        Shadows.shadowOf(app).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS)
        // Reset shared preferences before each test
        val prefs = context.getSharedPreferences("daily_top10_notification_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
    }

    @Test
    fun testNotificationChannelCreation() {
        DailyTop10NewsNotificationManager.createNotificationChannel(context)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = notificationManager.getNotificationChannel(DailyTop10NewsNotificationManager.CHANNEL_ID)

        assertNotNull("Daily Top 10 notification channel must exist", channel)
        assertEquals("Daily Top 10 Big News", channel.name)
    }

    @Test
    fun testTriggerNotificationDeliveredAndEnforcesOncePerDayAntiSpam() {
        // Initial state: not delivered today
        assertFalse("Should not be marked as delivered today initially", DailyTop10NewsNotificationManager.isDeliveredToday(context))

        // First trigger with preview/test mode
        val firstDelivery = DailyTop10NewsNotificationManager.triggerDailyNotification(context, forcePreview = true)
        assertTrue("First notification trigger should succeed", firstDelivery)
        assertTrue("Should be marked as delivered today after successful post", DailyTop10NewsNotificationManager.isDeliveredToday(context))

        // Second automated trigger on the same day must be rejected by anti-spam guard
        val secondDelivery = DailyTop10NewsNotificationManager.triggerDailyNotification(context, forcePreview = false)
        assertFalse("Second trigger on same day must be blocked by once-per-day anti-spam guard", secondDelivery)
    }

    @Test
    fun testNotificationReceiverProcessesAction() {
        DailyTop10NewsNotificationManager.createNotificationChannel(context)
        val receiver = DailyTop10NewsNotificationReceiver()

        // Send boot completed action
        val bootIntent = Intent(Intent.ACTION_BOOT_COMPLETED)
        receiver.onReceive(context, bootIntent)

        // Send trigger action
        val triggerIntent = Intent(DailyTop10NewsNotificationManager.ACTION_TRIGGER_DAILY_NOTIFICATION)
        receiver.onReceive(context, triggerIntent)

        // Notification should have been delivered
        assertTrue("Notification should be marked delivered after receiver action", DailyTop10NewsNotificationManager.isDeliveredToday(context))
    }
}
