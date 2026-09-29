package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.verification.FinalVerificationResult
import com.example.data.model.verification.FinalVerificationStatus
import com.example.data.model.verification.PublishReadiness

@Entity(
    tableName = "verification_records",
    indices = [
        Index(value = ["contentType"]),
        Index(value = ["finalStatus"]),
        Index(value = ["humanReviewRequired"]),
        Index(value = ["publishReadiness"])
    ]
)
data class FinalVerificationRecordEntity(
    @PrimaryKey
    val contentId: String,
    val contentType: String, // "POST", "MEME", "REEL"
    val finalStatus: String = FinalVerificationStatus.PENDING.name,
    val publishReadiness: String = PublishReadiness.NOT_READY.name,
    val checkedAt: Long = System.currentTimeMillis(),
    val sourceVerified: Boolean = false,
    val sourceUrlValid: Boolean = false,
    val sourceStillAvailable: Boolean = true,
    val factsConsistent: Boolean = false,
    val deadlineConsistent: Boolean = false,
    val eligibilityConsistent: Boolean = false,
    val safetyPassed: Boolean = false,
    val duplicateFree: Boolean = true,
    val politicalReviewRequired: Boolean = false,
    val humanReviewRequired: Boolean = false,
    val issues: String = "", // Delimited with '||'
    val warnings: String = "", // Delimited with '||'
    val approvedAt: Long? = null,
    val approvalReason: String? = null,
    val approvalType: String? = null, // "HUMAN"
    val rejectedAt: Long? = null,
    val rejectionReason: String? = null,
    val currentVersion: Int = 1
) {
    val finalStatusEnum: FinalVerificationStatus
        get() = FinalVerificationStatus.fromString(finalStatus)

    val publishReadinessEnum: PublishReadiness
        get() = PublishReadiness.fromString(publishReadiness)

    fun getIssuesList(): List<String> = if (issues.isBlank()) emptyList() else issues.split("||")
    fun getWarningsList(): List<String> = if (warnings.isBlank()) emptyList() else warnings.split("||")

    fun toDomainResult(): FinalVerificationResult {
        return FinalVerificationResult(
            contentId = contentId,
            status = finalStatusEnum,
            checkedAt = checkedAt,
            sourceVerified = sourceVerified,
            sourceUrlValid = sourceUrlValid,
            sourceStillAvailable = sourceStillAvailable,
            factsConsistent = factsConsistent,
            deadlineConsistent = deadlineConsistent,
            eligibilityConsistent = eligibilityConsistent,
            safetyPassed = safetyPassed,
            duplicateFree = duplicateFree,
            politicalReviewRequired = politicalReviewRequired,
            humanReviewRequired = humanReviewRequired,
            issues = getIssuesList(),
            warnings = getWarningsList()
        )
    }

    companion object {
        fun fromDomainResult(
            result: FinalVerificationResult,
            contentType: String,
            currentVersion: Int = 1,
            approvedAt: Long? = null,
            approvalReason: String? = null,
            approvalType: String? = null,
            rejectedAt: Long? = null,
            rejectionReason: String? = null
        ): FinalVerificationRecordEntity {
            return FinalVerificationRecordEntity(
                contentId = result.contentId,
                contentType = contentType,
                finalStatus = result.status.name,
                publishReadiness = result.publishReadiness.name,
                checkedAt = result.checkedAt,
                sourceVerified = result.sourceVerified,
                sourceUrlValid = result.sourceUrlValid,
                sourceStillAvailable = result.sourceStillAvailable,
                factsConsistent = result.factsConsistent,
                deadlineConsistent = result.deadlineConsistent,
                eligibilityConsistent = result.eligibilityConsistent,
                safetyPassed = result.safetyPassed,
                duplicateFree = result.duplicateFree,
                politicalReviewRequired = result.politicalReviewRequired,
                humanReviewRequired = result.humanReviewRequired,
                issues = result.issues.joinToString("||"),
                warnings = result.warnings.joinToString("||"),
                approvedAt = approvedAt,
                approvalReason = approvalReason,
                approvalType = approvalType,
                rejectedAt = rejectedAt,
                rejectionReason = rejectionReason,
                currentVersion = currentVersion
            )
        }
    }
}

@Entity(
    tableName = "content_versions",
    indices = [
        Index(value = ["contentId"]),
        Index(value = ["createdAt"])
    ]
)
data class ContentVersionEntity(
    @PrimaryKey
    val id: String,
    val contentId: String,
    val versionNumber: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val changeType: String,
    val previousStatus: String,
    val titleSnapshot: String = "",
    val bodySnapshot: String = "",
    val captionSnapshot: String = ""
)

@Entity(
    tableName = "verification_audit_logs",
    indices = [
        Index(value = ["contentId"]),
        Index(value = ["action"]),
        Index(value = ["timestamp"])
    ]
)
data class VerificationAuditLogEntity(
    @PrimaryKey
    val id: String,
    val contentId: String,
    val action: String,
    val timestamp: Long = System.currentTimeMillis(),
    val statusBefore: String,
    val statusAfter: String,
    val reason: String
)
