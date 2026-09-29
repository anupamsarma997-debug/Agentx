package com.example.data.repository

import com.example.data.local.dao.MemeDao
import com.example.data.local.entity.MemeDraftEntity
import com.example.data.model.meme.MemeDraft
import com.example.data.model.meme.MemeFormat
import com.example.data.model.meme.MemeGenerationStatus
import com.example.data.model.meme.MemeSafetyStatus
import com.example.data.model.meme.MemeTopic
import com.example.domain.engine.MemeEngine
import com.example.domain.engine.MemeGenerationOutcome
import kotlinx.coroutines.flow.Flow

class MemeRepository(
    private val memeDao: MemeDao,
    private val memeEngine: MemeEngine
) {

    fun observeAllMemes(): Flow<List<MemeDraftEntity>> = memeDao.getAllMemes()

    fun getMemesByStatus(status: MemeGenerationStatus): Flow<List<MemeDraftEntity>> =
        memeDao.getMemesByStatus(status.name)

    fun getMemeById(id: String): Flow<MemeDraftEntity?> = memeDao.getMemeById(id)

    suspend fun getMemeByIdSync(id: String): MemeDraftEntity? = memeDao.getMemeByIdSync(id)

    fun countTotal(): Flow<Int> = memeDao.countTotal()

    fun countByStatus(status: MemeGenerationStatus): Flow<Int> = memeDao.countByGenerationStatus(status.name)

    fun countNeedsReview(): Flow<Int> = memeDao.countBySafetyStatus(MemeSafetyStatus.NEEDS_REVIEW.name)

    fun countApproved(): Flow<Int> = memeDao.countByGenerationStatus(MemeGenerationStatus.APPROVED.name)

    fun countGeneratedSince(startOfDayMillis: Long): Flow<Int> = memeDao.countGeneratedSince(startOfDayMillis)

    suspend fun saveMemeDraft(draft: MemeDraft): Long {
        return memeDao.insertMeme(MemeDraftEntity.fromDraft(draft))
    }

    suspend fun updateMemeStatus(id: String, status: MemeGenerationStatus) {
        memeDao.updateStatus(id, status.name)
    }

    suspend fun updateMemeTexts(
        id: String,
        setupText: String,
        punchlineText: String,
        caption: String,
        hashtags: String
    ) {
        memeDao.updateMemeDraftTexts(id, setupText, punchlineText, caption, hashtags)
    }

    suspend fun deleteMeme(id: String) {
        memeDao.deleteMemeById(id)
    }

    /**
     * Executes generation through MemeEngine and automatically saves successful drafts to the database.
     */
    suspend fun generateAndSaveMeme(
        topic: MemeTopic,
        format: MemeFormat = MemeFormat.TEXT_MEME
    ): MemeGenerationOutcome {
        val outcome = memeEngine.generateMeme(topic, format)
        if (outcome is MemeGenerationOutcome.Success) {
            memeDao.insertMeme(MemeDraftEntity.fromDraft(outcome.draft))
        }
        return outcome
    }
}
