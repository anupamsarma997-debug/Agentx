package com.example.data.repository

import com.example.data.local.dao.ContentDao
import com.example.data.local.dao.ContentVersionDao
import com.example.data.local.dao.FinalVerificationDao
import com.example.data.local.dao.MemeDao
import com.example.data.local.dao.OpportunityDao
import com.example.data.local.dao.ReelDao
import com.example.data.local.dao.VerificationAuditLogDao
import com.example.data.local.entity.ContentEntity
import com.example.data.local.entity.ContentVersionEntity
import com.example.data.local.entity.FinalVerificationRecordEntity
import com.example.data.local.entity.MemeDraftEntity
import com.example.data.local.entity.OpportunityEntity
import com.example.data.local.entity.ReelDraftEntity
import com.example.data.local.entity.VerificationAuditLogEntity
import com.example.data.model.content.GenerationStatus
import com.example.data.model.meme.MemeGenerationStatus
import com.example.data.model.reel.ReelGenerationStatus
import com.example.data.model.verification.FinalVerificationResult
import com.example.data.model.verification.FinalVerificationStatus
import com.example.data.model.verification.GeneratedContentFact
import com.example.data.model.verification.PublishReadiness
import com.example.data.model.verification.RejectionReason
import com.example.data.model.verification.VerificationAction
import com.example.domain.engine.ApprovalDecision
import com.example.domain.engine.ApprovalEngine
import com.example.domain.engine.FinalVerificationEngine
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class VerificationRepository(
    private val verificationDao: FinalVerificationDao,
    private val versionDao: ContentVersionDao,
    private val auditLogDao: VerificationAuditLogDao,
    private val opportunityDao: OpportunityDao,
    private val contentDao: ContentDao,
    private val memeDao: MemeDao,
    private val reelDao: ReelDao,
    val verificationEngine: FinalVerificationEngine = FinalVerificationEngine(),
    val approvalEngine: ApprovalEngine = ApprovalEngine()
) {

    fun observeRecord(contentId: String): Flow<FinalVerificationRecordEntity?> {
        return verificationDao.observeByContentId(contentId)
    }

    fun observeAllRecords(): Flow<List<FinalVerificationRecordEntity>> {
        return verificationDao.observeAllRecords()
    }

    fun countByStatus(status: FinalVerificationStatus): Flow<Int> {
        return verificationDao.countByStatus(status.name)
    }

    fun countNeedsReview(): Flow<Int> {
        return verificationDao.countNeedsReview()
    }

    fun observeLogs(contentId: String): Flow<List<VerificationAuditLogEntity>> {
        return auditLogDao.observeLogsForContent(contentId)
    }

    fun observeVersions(contentId: String): Flow<List<ContentVersionEntity>> {
        return versionDao.observeVersionsForContent(contentId)
    }

    suspend fun getRecord(contentId: String): FinalVerificationRecordEntity? {
        return verificationDao.getByContentId(contentId)
    }

    /**
     * Verifies a Post content draft and updates/stores its verification record and audit log.
     */
    suspend fun verifyPost(content: ContentEntity): FinalVerificationResult {
        val source = opportunityDao.getOpportunityByIdSync(content.sourceOpportunityId)
        val contentFact = GeneratedContentFact(
            title = content.title,
            body = content.body,
            caption = content.caption,
            deadline = "",
            eligibility = "",
            organization = content.sourceName,
            sourceUrl = content.sourceUrl,
            hashtags = content.getHashtagList()
        )

        val previousRecord = verificationDao.getByContentId(content.id)
        val statusBefore = previousRecord?.finalStatus ?: FinalVerificationStatus.PENDING.name

        val result = verificationEngine.verifyContent(
            contentId = content.id,
            source = source,
            contentFact = contentFact,
            platform = content.platform
        )

        val record = FinalVerificationRecordEntity.fromDomainResult(
            result = result,
            contentType = "POST",
            currentVersion = previousRecord?.currentVersion ?: 1
        )
        verificationDao.insertOrUpdate(record)

        auditLogDao.insertLog(
            VerificationAuditLogEntity(
                id = UUID.randomUUID().toString(),
                contentId = content.id,
                action = VerificationAction.VERIFIED.name,
                statusBefore = statusBefore,
                statusAfter = result.status.name,
                reason = "Ran 20-point final verification check on Post."
            )
        )

        return result
    }

    /**
     * Verifies a Meme draft and updates/stores its verification record and audit log.
     */
    suspend fun verifyMeme(meme: MemeDraftEntity): FinalVerificationResult {
        val source = meme.sourceOpportunityId?.let { opportunityDao.getOpportunityByIdSync(it) }
        val contentFact = GeneratedContentFact(
            title = meme.topic,
            body = "${meme.setupText} | ${meme.punchlineText}",
            caption = meme.caption,
            deadline = "",
            eligibility = "",
            organization = meme.sourceName,
            sourceUrl = meme.sourceUrl,
            hashtags = meme.getHashtagList()
        )

        val previousRecord = verificationDao.getByContentId(meme.id)
        val statusBefore = previousRecord?.finalStatus ?: FinalVerificationStatus.PENDING.name

        val result = verificationEngine.verifyContent(
            contentId = meme.id,
            source = source,
            contentFact = contentFact,
            platform = "MEME"
        )

        val record = FinalVerificationRecordEntity.fromDomainResult(
            result = result,
            contentType = "MEME",
            currentVersion = previousRecord?.currentVersion ?: 1
        )
        verificationDao.insertOrUpdate(record)

        auditLogDao.insertLog(
            VerificationAuditLogEntity(
                id = UUID.randomUUID().toString(),
                contentId = meme.id,
                action = VerificationAction.VERIFIED.name,
                statusBefore = statusBefore,
                statusAfter = result.status.name,
                reason = "Ran 20-point final verification check on Meme."
            )
        )

        return result
    }

    /**
     * Verifies a Reel draft and updates/stores its verification record and audit log.
     */
    suspend fun verifyReel(reel: ReelDraftEntity): FinalVerificationResult {
        val source = reel.sourceOpportunityId?.let { opportunityDao.getOpportunityByIdSync(it) }
        val contentFact = GeneratedContentFact(
            title = reel.title,
            body = "${reel.hook} | ${reel.voiceover}",
            caption = reel.caption,
            deadline = "",
            eligibility = "",
            organization = reel.sourceName,
            sourceUrl = reel.sourceUrl,
            hashtags = reel.getHashtagList()
        )

        val previousRecord = verificationDao.getByContentId(reel.id)
        val statusBefore = previousRecord?.finalStatus ?: FinalVerificationStatus.PENDING.name

        val result = verificationEngine.verifyContent(
            contentId = reel.id,
            source = source,
            contentFact = contentFact,
            platform = "REEL"
        )

        val record = FinalVerificationRecordEntity.fromDomainResult(
            result = result,
            contentType = "REEL",
            currentVersion = previousRecord?.currentVersion ?: 1
        )
        verificationDao.insertOrUpdate(record)

        auditLogDao.insertLog(
            VerificationAuditLogEntity(
                id = UUID.randomUUID().toString(),
                contentId = reel.id,
                action = VerificationAction.VERIFIED.name,
                statusBefore = statusBefore,
                statusAfter = result.status.name,
                reason = "Ran 20-point final verification check on Reel."
            )
        )

        return result
    }

    /**
     * Rechecks the original source for a content item and updates verification status.
     * Manually triggered only, no continuous polling.
     */
    suspend fun recheckSource(contentId: String, contentType: String): FinalVerificationResult? {
        val previousRecord = verificationDao.getByContentId(contentId)
        val statusBefore = previousRecord?.finalStatus ?: FinalVerificationStatus.PENDING.name

        val result: FinalVerificationResult? = when (contentType.uppercase()) {
            "POST" -> {
                val post = contentDao.getContentByIdSync(contentId)
                if (post != null) verifyPost(post) else null
            }
            "MEME" -> {
                val meme = memeDao.getMemeByIdSync(contentId)
                if (meme != null) verifyMeme(meme) else null
            }
            "REEL" -> {
                val reel = reelDao.getReelById(contentId)
                if (reel != null) verifyReel(reel) else null
            }
            else -> null
        }

        if (result != null) {
            auditLogDao.insertLog(
                VerificationAuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    contentId = contentId,
                    action = VerificationAction.RECHECKED.name,
                    statusBefore = statusBefore,
                    statusAfter = result.status.name,
                    reason = "User manually triggered re-check of original source."
                )
            )
        }

        return result
    }

    /**
     * Records a content edit, creates a ContentVersion snapshot, resets verification to PENDING,
     * and triggers re-verification.
     */
    suspend fun recordContentEdit(
        contentId: String,
        contentType: String,
        changeType: String,
        titleSnapshot: String,
        bodySnapshot: String,
        captionSnapshot: String
    ) {
        val currentRecord = verificationDao.getByContentId(contentId)
        val previousStatus = currentRecord?.finalStatus ?: FinalVerificationStatus.PENDING.name
        val currentVersion = (currentRecord?.currentVersion ?: 1)
        val newVersion = currentVersion + 1

        // Insert version snapshot
        versionDao.insertVersion(
            ContentVersionEntity(
                id = UUID.randomUUID().toString(),
                contentId = contentId,
                versionNumber = currentVersion,
                changeType = changeType,
                previousStatus = previousStatus,
                titleSnapshot = titleSnapshot,
                bodySnapshot = bodySnapshot,
                captionSnapshot = captionSnapshot
            )
        )

        // Reset verification record status to PENDING
        if (currentRecord != null) {
            verificationDao.insertOrUpdate(
                currentRecord.copy(
                    finalStatus = FinalVerificationStatus.PENDING.name,
                    publishReadiness = PublishReadiness.NOT_READY.name,
                    currentVersion = newVersion
                )
            )
        }

        // Reset underlying item generation status if it was previously APPROVED
        when (contentType.uppercase()) {
            "POST" -> {
                contentDao.updateStatus(contentId, GenerationStatus.DRAFT.name)
                val post = contentDao.getContentByIdSync(contentId)
                if (post != null) verifyPost(post)
            }
            "MEME" -> {
                memeDao.updateStatus(contentId, MemeGenerationStatus.DRAFT.name)
                val meme = memeDao.getMemeByIdSync(contentId)
                if (meme != null) verifyMeme(meme)
            }
            "REEL" -> {
                reelDao.updateStatus(contentId, ReelGenerationStatus.DRAFT.name)
                val reel = reelDao.getReelById(contentId)
                if (reel != null) verifyReel(reel)
            }
        }

        auditLogDao.insertLog(
            VerificationAuditLogEntity(
                id = UUID.randomUUID().toString(),
                contentId = contentId,
                action = VerificationAction.EDITED.name,
                statusBefore = previousStatus,
                statusAfter = FinalVerificationStatus.PENDING.name,
                reason = "Factual edit: '$changeType'. Version incremented to v$newVersion. Returned to PENDING for re-verification."
            )
        )
    }

    /**
     * Approves content after verification passes or human editor manually approves.
     */
    suspend fun approveContent(
        contentId: String,
        contentType: String,
        reason: String = "Approved by human editor."
    ): Boolean {
        val currentRecord = verificationDao.getByContentId(contentId)
        if (currentRecord != null) {
            val currentResult = currentRecord.toDomainResult()
            if (!approvalEngine.canManuallyApprove(currentResult)) {
                return false
            }
        }

        val approvedRecord = currentRecord?.copy(
            finalStatus = FinalVerificationStatus.PASSED.name,
            publishReadiness = PublishReadiness.READY_FOR_PUBLISHER.name,
            approvedAt = System.currentTimeMillis(),
            approvalReason = reason,
            approvalType = "HUMAN",
            humanReviewRequired = false
        ) ?: FinalVerificationRecordEntity(
            contentId = contentId,
            contentType = contentType,
            finalStatus = FinalVerificationStatus.PASSED.name,
            publishReadiness = PublishReadiness.READY_FOR_PUBLISHER.name,
            checkedAt = System.currentTimeMillis(),
            approvedAt = System.currentTimeMillis(),
            approvalReason = reason,
            approvalType = "HUMAN",
            humanReviewRequired = false,
            sourceVerified = true,
            sourceUrlValid = true,
            safetyPassed = true
        )
        verificationDao.insertOrUpdate(approvedRecord)

        // Update target entity status
        when (contentType.uppercase()) {
            "POST" -> contentDao.updateStatus(contentId, GenerationStatus.APPROVED.name)
            "MEME" -> memeDao.updateStatus(contentId, MemeGenerationStatus.APPROVED.name)
            "REEL" -> reelDao.updateStatus(contentId, ReelGenerationStatus.APPROVED.name)
        }

        auditLogDao.insertLog(
            VerificationAuditLogEntity(
                id = UUID.randomUUID().toString(),
                contentId = contentId,
                action = VerificationAction.APPROVED.name,
                statusBefore = currentRecord?.finalStatus ?: FinalVerificationStatus.PENDING.name,
                statusAfter = FinalVerificationStatus.PASSED.name,
                reason = reason
            )
        )

        return true
    }

    /**
     * Rejects content with a specific reason.
     */
    suspend fun rejectContent(
        contentId: String,
        contentType: String,
        reason: RejectionReason,
        note: String = ""
    ) {
        val currentRecord = verificationDao.getByContentId(contentId)
        val statusBefore = currentRecord?.finalStatus ?: FinalVerificationStatus.PENDING.name

        val rejectionNote = note.ifBlank { reason.displayName }

        val rejectedRecord = (currentRecord ?: FinalVerificationRecordEntity(
            contentId = contentId,
            contentType = contentType
        )).copy(
            finalStatus = FinalVerificationStatus.FAILED.name,
            publishReadiness = PublishReadiness.NOT_READY.name,
            rejectedAt = System.currentTimeMillis(),
            rejectionReason = "${reason.name}: $rejectionNote"
        )
        verificationDao.insertOrUpdate(rejectedRecord)

        // Update target entity status
        when (contentType.uppercase()) {
            "POST" -> contentDao.updateStatus(contentId, GenerationStatus.REJECTED.name)
            "MEME" -> memeDao.updateStatus(contentId, MemeGenerationStatus.REJECTED.name)
            "REEL" -> reelDao.updateStatus(contentId, ReelGenerationStatus.REJECTED.name)
        }

        auditLogDao.insertLog(
            VerificationAuditLogEntity(
                id = UUID.randomUUID().toString(),
                contentId = contentId,
                action = VerificationAction.REJECTED.name,
                statusBefore = statusBefore,
                statusAfter = FinalVerificationStatus.FAILED.name,
                reason = "Rejected (${reason.displayName}): $rejectionNote"
            )
        )
    }
}
