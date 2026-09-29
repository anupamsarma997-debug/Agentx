package com.example.data.model.verification

enum class FinalVerificationStatus(val displayName: String) {
    PENDING("Pending Verification"),
    PASSED("Verification Passed"),
    NEEDS_REVIEW("Needs Human Review"),
    BLOCKED("Blocked by Safety"),
    FAILED("Verification Failed"),
    EXPIRED("Source Expired");

    companion object {
        fun fromString(value: String?): FinalVerificationStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: PENDING
        }
    }
}

enum class PublishReadiness(val displayName: String) {
    NOT_READY("Not Ready"),
    READY_FOR_PUBLISHER("Ready for Publisher"),
    BLOCKED("Publishing Blocked");

    companion object {
        fun fromString(value: String?): PublishReadiness {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: NOT_READY
        }
    }
}

enum class RejectionReason(val displayName: String) {
    INCORRECT_FACT("Incorrect Facts"),
    WRONG_DEADLINE("Wrong Deadline"),
    WRONG_ELIGIBILITY("Wrong Eligibility Criteria"),
    UNSAFE("Unsafe or Prohibited Content"),
    DUPLICATE("Duplicate Content"),
    SOURCE_UNAVAILABLE("Source Unavailable"),
    LOW_QUALITY("Low Editorial Quality"),
    USER_REJECTED("Rejected by Editor"),
    OTHER("Other Issue");

    companion object {
        fun fromString(value: String?): RejectionReason {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: OTHER
        }
    }
}

enum class VerificationAction(val displayName: String) {
    GENERATED("Generated"),
    VERIFIED("Verified"),
    RECHECKED("Rechecked Source"),
    EDITED("Edited by User"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    BLOCKED("Blocked"),
    EXPIRED("Expired");

    companion object {
        fun fromString(value: String?): VerificationAction {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: VERIFIED
        }
    }
}

data class FinalVerificationResult(
    val contentId: String,
    val status: FinalVerificationStatus,
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
    val issues: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
) {
    val isApprovedAllowed: Boolean
        get() = sourceVerified &&
                sourceUrlValid &&
                factsConsistent &&
                deadlineConsistent &&
                eligibilityConsistent &&
                safetyPassed &&
                duplicateFree &&
                !humanReviewRequired &&
                status != FinalVerificationStatus.EXPIRED &&
                status != FinalVerificationStatus.BLOCKED &&
                status != FinalVerificationStatus.FAILED

    val publishReadiness: PublishReadiness
        get() = when {
            status == FinalVerificationStatus.BLOCKED -> PublishReadiness.BLOCKED
            isApprovedAllowed && status == FinalVerificationStatus.PASSED -> PublishReadiness.READY_FOR_PUBLISHER
            else -> PublishReadiness.NOT_READY
        }
}

data class SourceFact(
    val title: String = "",
    val organization: String = "",
    val description: String = "",
    val category: String = "",
    val region: String = "",
    val eligibility: String = "",
    val deadline: String = "",
    val publishedAt: String = "",
    val sourceName: String = "",
    val sourceUrl: String = ""
)

data class GeneratedContentFact(
    val title: String = "",
    val body: String = "",
    val caption: String = "",
    val deadline: String = "",
    val eligibility: String = "",
    val organization: String = "",
    val sourceUrl: String = "",
    val hashtags: List<String> = emptyList()
)

data class ContentVersion(
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

data class VerificationAuditLog(
    val id: String,
    val contentId: String,
    val action: String,
    val timestamp: Long = System.currentTimeMillis(),
    val statusBefore: String,
    val statusAfter: String,
    val reason: String
)
