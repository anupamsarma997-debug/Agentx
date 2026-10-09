package com.example.domain.engine

import com.example.data.local.dao.OpportunityDao
import com.example.data.local.entity.OpportunityEntity
import com.example.data.model.opportunity.OpportunityCategory
import com.example.data.model.opportunity.OpportunityRegion
import com.example.data.model.opportunity.VerificationStatus
import com.example.data.remote.scout.OfficialCuratedSourceProvider
import com.example.data.remote.scout.SourceProvider
import java.util.UUID

data class ScoutResult(
    val discovered: Int = 0,
    val newItems: Int = 0,
    val duplicates: Int = 0,
    val expired: Int = 0,
    val needsReview: Int = 0,
    val verified: Int = 0,
    val rejected: Int = 0,
    val errors: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

class OpportunityScoutEngine(
    private val dao: OpportunityDao,
    private val providers: List<SourceProvider> = listOf(OfficialCuratedSourceProvider()),
    private val verificationEngine: VerificationEngine = VerificationEngine()
) {

    suspend fun runScout(
        assamEnabled: Boolean = true,
        northeastEnabled: Boolean = true,
        indiaEnabled: Boolean = true,
        internationalEnabled: Boolean = false,
        newsEnabled: Boolean = true
    ): ScoutResult {
        var totalDiscovered = 0
        var totalNew = 0
        var totalDuplicates = 0
        var totalExpired = 0
        var totalNeedsReview = 0
        var totalVerified = 0
        var totalRejected = 0
        var totalErrors = 0

        // Unexpire opportunities so user always sees active opportunities
        dao.unexpireAllOpportunities()

        val now = System.currentTimeMillis()
        val existingHashes = dao.getAllContentHashes().toMutableSet()
        val inBatchHashes = mutableSetOf<String>()
        val itemsToInsert = mutableListOf<OpportunityEntity>()

        for (provider in providers) {
            try {
                val rawItems = provider.fetchItems()
                totalDiscovered += rawItems.size

                for (item in rawItems) {
                    // Check region filters
                    val region = item.regionHint
                    val allowedByRegion = when (region) {
                        OpportunityRegion.ASSAM -> assamEnabled
                        OpportunityRegion.NORTHEAST_INDIA -> northeastEnabled
                        OpportunityRegion.INDIA -> indiaEnabled
                        OpportunityRegion.INTERNATIONAL -> internationalEnabled
                        OpportunityRegion.UNKNOWN -> true
                    }
                    if (!allowedByRegion) continue

                    // Check news filter
                    if (item.categoryHint == OpportunityCategory.NEWS && !newsEnabled) {
                        continue
                    }

                    // Normalization
                    val normTitle = OpportunityNormalizer.normalizeTitle(item.title)
                    val normOrg = OpportunityNormalizer.normalizeOrganization(item.organization)
                    val normUrl = OpportunityNormalizer.normalizeUrl(item.sourceUrl)
                    val domain = OpportunityNormalizer.extractDomain(normUrl)
                    val sourceTier = OpportunityNormalizer.determineSourceTier(normUrl)

                    val parsedDeadline = OpportunityNormalizer.parseDeadline(item.deadline)

                    // Deterministic Hash
                    val contentHash = OpportunityNormalizer.computeContentHash(
                        title = normTitle,
                        domain = domain,
                        deadline = parsedDeadline.normalizedString,
                        organization = normOrg
                    )

                    // In-batch deduplication check
                    if (inBatchHashes.contains(contentHash)) {
                        totalDuplicates++
                        continue
                    }
                    inBatchHashes.add(contentHash)

                    val isNew = !existingHashes.contains(contentHash)
                    if (isNew) totalNew++ else totalDuplicates++

                    // Verification Engine execution
                    val verification = verificationEngine.verify(
                        title = normTitle,
                        description = item.description,
                        sourceUrl = normUrl,
                        sourceTier = sourceTier,
                        category = item.categoryHint,
                        parsedDeadline = parsedDeadline
                    )

                    if (verification.status == VerificationStatus.REJECTED) {
                        totalRejected++
                        continue // Do not persist rejected items
                    }

                    if (verification.status == VerificationStatus.VERIFIED) {
                        totalVerified++
                    } else if (verification.status == VerificationStatus.NEEDS_REVIEW) {
                        totalNeedsReview++
                    }

                    val existingEntity = if (!isNew) dao.findByContentHash(contentHash) else null
                    val entity = OpportunityEntity(
                        id = existingEntity?.id ?: UUID.randomUUID().toString(),
                        title = normTitle,
                        description = OpportunityNormalizer.normalizeText(item.description),
                        category = item.categoryHint.name,
                        region = region.name,
                        sourceName = item.sourceName,
                        sourceUrl = normUrl,
                        sourceDomain = domain,
                        publishedAt = item.publishedAt,
                        deadline = parsedDeadline.normalizedString,
                        deadlineEpochMillis = parsedDeadline.epochMillis,
                        eligibility = OpportunityNormalizer.normalizeText(item.eligibility).takeIf { it.isNotBlank() },
                        organization = normOrg.takeIf { it.isNotBlank() },
                        sourceTier = sourceTier.name,
                        verificationStatus = verification.status.name,
                        discoveredAt = existingEntity?.discoveredAt ?: now,
                        lastCheckedAt = now,
                        contentHash = contentHash,
                        isExpired = false,
                        isPosted = existingEntity?.isPosted ?: false,
                        postedAt = existingEntity?.postedAt
                    )

                    itemsToInsert.add(entity)
                }
            } catch (_: Exception) {
                totalErrors++
            }
        }

        if (itemsToInsert.isNotEmpty()) {
            dao.upsertPreservingPostedStatus(itemsToInsert)
        }

        return ScoutResult(
            discovered = totalDiscovered,
            newItems = totalNew,
            duplicates = totalDuplicates,
            expired = totalExpired,
            needsReview = totalNeedsReview,
            verified = totalVerified,
            rejected = totalRejected,
            errors = totalErrors,
            timestamp = now
        )
    }
}
