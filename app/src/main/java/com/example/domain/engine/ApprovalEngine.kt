package com.example.domain.engine

import com.example.data.model.verification.FinalVerificationResult
import com.example.data.model.verification.FinalVerificationStatus
import com.example.data.model.verification.PublishReadiness
import com.example.data.model.verification.RejectionReason

sealed interface ApprovalDecision {
    data class Allowed(val readiness: PublishReadiness) : ApprovalDecision
    data class RequiresHumanReview(val reasons: List<String>) : ApprovalDecision
    data class Disallowed(val reasons: List<String>) : ApprovalDecision
}

data class ManualApprovalRecord(
    val approvedAt: Long = System.currentTimeMillis(),
    val approvalReason: String,
    val approvalType: String = "HUMAN"
)

data class RejectionRecord(
    val rejectedAt: Long = System.currentTimeMillis(),
    val reason: RejectionReason,
    val note: String = ""
)

class ApprovalEngine {

    /**
     * Checks if automatic or standard approval is allowed for the given verification result.
     * Enforces all 10 strict gate rules.
     */
    fun evaluateApproval(result: FinalVerificationResult): ApprovalDecision {
        val blockingReasons = mutableListOf<String>()

        if (result.status == FinalVerificationStatus.BLOCKED) {
            blockingReasons.add("Content is blocked by safety engine. Approval is prohibited.")
        }
        if (result.status == FinalVerificationStatus.EXPIRED) {
            blockingReasons.add("Source opportunity has expired. Promotional publishing is blocked.")
        }
        if (result.status == FinalVerificationStatus.FAILED) {
            blockingReasons.add("Content failed verification checks.")
        }
        if (!result.sourceVerified) {
            blockingReasons.add("Source is not verified.")
        }
        if (!result.sourceUrlValid) {
            blockingReasons.add("Source URL is invalid or altered.")
        }
        if (!result.factsConsistent) {
            blockingReasons.add("Content facts are inconsistent with official source.")
        }
        if (!result.deadlineConsistent) {
            blockingReasons.add("Deadline is inconsistent with official source.")
        }
        if (!result.eligibilityConsistent) {
            blockingReasons.add("Eligibility criteria do not match official source.")
        }
        if (!result.safetyPassed) {
            blockingReasons.add("Content failed safety criteria.")
        }
        if (!result.duplicateFree) {
            blockingReasons.add("Content is suspected duplicate.")
        }

        if (blockingReasons.isNotEmpty()) {
            return ApprovalDecision.Disallowed(blockingReasons)
        }

        if (result.humanReviewRequired) {
            val reviewReasons = result.warnings.ifEmpty { listOf("Human editorial review required before approval.") }
            return ApprovalDecision.RequiresHumanReview(reviewReasons)
        }

        return ApprovalDecision.Allowed(PublishReadiness.READY_FOR_PUBLISHER)
    }

    /**
     * Evaluates whether a human editor can manually approve after inspecting warnings.
     * Manual approval is strictly prohibited for BLOCKED or EXPIRED or FAILED content.
     */
    fun canManuallyApprove(result: FinalVerificationResult): Boolean {
        if (result.status == FinalVerificationStatus.BLOCKED) return false
        if (result.status == FinalVerificationStatus.EXPIRED) return false
        if (result.status == FinalVerificationStatus.FAILED) return false
        if (!result.sourceUrlValid) return false
        if (!result.safetyPassed) return false
        return true
    }

    /**
     * Constructs a human approval record.
     */
    fun buildManualApproval(reason: String = "Verified and approved by human editor."): ManualApprovalRecord {
        return ManualApprovalRecord(
            approvedAt = System.currentTimeMillis(),
            approvalReason = reason,
            approvalType = "HUMAN"
        )
    }

    /**
     * Constructs a rejection record.
     */
    fun buildRejection(reason: RejectionReason, note: String = ""): RejectionRecord {
        return RejectionRecord(
            rejectedAt = System.currentTimeMillis(),
            reason = reason,
            note = note.ifBlank { reason.displayName }
        )
    }
}
