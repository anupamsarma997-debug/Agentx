package com.example.data.repository

import com.example.data.local.dao.ReelDao
import com.example.data.local.entity.ReelDraftEntity
import com.example.data.local.settings.AppSettings
import com.example.data.model.reel.ReelGenerationOutcome
import com.example.data.model.reel.ReelGenerationStatus
import com.example.data.model.reel.ReelLanguage
import com.example.data.model.reel.ReelTopic
import com.example.data.model.reel.ReelType
import com.example.domain.engine.ReelEngine
import kotlinx.coroutines.flow.Flow

class ReelRepository(
    private val reelDao: ReelDao,
    private val reelEngine: ReelEngine
) {

    val allReels: Flow<List<ReelDraftEntity>> = reelDao.getAllReelsFlow()
    val reelCount: Flow<Int> = reelDao.getReelCount()
    val reelNeedsReviewCount: Flow<Int> = reelDao.getReelCountByStatus(ReelGenerationStatus.REVIEW_REQUIRED.name)
    val reelApprovedCount: Flow<Int> = reelDao.getReelCountByStatus(ReelGenerationStatus.APPROVED.name)

    suspend fun getReelById(id: String): ReelDraftEntity? {
        return reelDao.getReelById(id)
    }

    suspend fun generateReel(
        topic: ReelTopic,
        reelType: ReelType,
        durationSeconds: Int = 30,
        language: ReelLanguage = ReelLanguage.ENGLISH,
        settings: AppSettings = AppSettings()
    ): ReelGenerationOutcome {
        return reelEngine.generateReel(
            topic = topic,
            reelType = reelType,
            durationSeconds = durationSeconds,
            language = language,
            settings = settings
        )
    }

    suspend fun updateStatus(id: String, status: ReelGenerationStatus) {
        reelDao.updateStatus(id, status.name)
    }

    suspend fun updateReelTexts(id: String, hook: String, caption: String, voiceover: String) {
        reelDao.updateReelTexts(id, hook, caption, voiceover)
    }

    suspend fun deleteReel(id: String) {
        reelDao.deleteReelById(id)
    }
}
