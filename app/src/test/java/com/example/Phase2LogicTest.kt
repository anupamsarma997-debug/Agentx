package com.example

import com.example.data.local.settings.AppSettings
import com.example.domain.automation.DailyAutomationManager
import com.example.domain.automation.FreeTierGuard
import com.example.domain.automation.GenerationDecision
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Phase2LogicTest {

    private val freeTierGuard = FreeTierGuard()
    private val automationManager = DailyAutomationManager()

    @Test
    fun `default settings match requirements`() {
        val defaultSettings = AppSettings()
        assertTrue(defaultSettings.freeMode)
        assertTrue(defaultSettings.automationEnabled)
        assertEquals(10, defaultSettings.automationStartHour)
        assertEquals(0, defaultSettings.automationStartMinute)
        assertEquals(11, defaultSettings.automationEndHour)
        assertEquals(0, defaultSettings.automationEndMinute)
        assertEquals("Asia/Kolkata", defaultSettings.timezone)
        assertEquals(8, defaultSettings.dailyPostTarget)
        assertEquals(2, defaultSettings.dailyReelTarget)
    }

    @Test
    fun `free tier guard permits generation under limit`() {
        val settings = AppSettings(
            todayPostCount = 3,
            dailyPostTarget = 8,
            todayReelCount = 1,
            dailyReelTarget = 2
        )
        assertTrue(freeTierGuard.canGeneratePost(settings) is GenerationDecision.Allowed)
        assertTrue(freeTierGuard.canGenerateReel(settings) is GenerationDecision.Allowed)
    }

    @Test
    fun `free tier guard halts generation when limit reached`() {
        val settings = AppSettings(
            todayPostCount = 8,
            dailyPostTarget = 8,
            todayReelCount = 2,
            dailyReelTarget = 2
        )
        val postDecision = freeTierGuard.canGeneratePost(settings)
        val reelDecision = freeTierGuard.canGenerateReel(settings)

        assertTrue(postDecision is GenerationDecision.QuotaExhausted)
        assertTrue(reelDecision is GenerationDecision.QuotaExhausted)
    }

    @Test
    fun `automation manager safe timezone fallback`() {
        val settings = AppSettings(timezone = "Invalid/Zone_Name")
        // Should not crash, returns a boolean
        val isOpen = automationManager.isAutomationWindowOpen(settings)
        val status = automationManager.getWindowStatusDescription(settings)
        // Verify evaluation completed safely without exception
        assertTrue(isOpen == true || isOpen == false)
        assertTrue(status.windowText.isNotEmpty())
    }
}
