package com.example.data.repository

import com.example.data.local.dao.OpportunityDao
import com.example.data.local.entity.OpportunityEntity
import com.example.data.model.opportunity.OpportunityCategory
import com.example.data.model.opportunity.OpportunityRegion
import com.example.data.model.opportunity.VerificationStatus
import com.example.domain.engine.OpportunityScoutEngine
import com.example.domain.engine.ScoutResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OpportunityRepository(
    private val dao: OpportunityDao,
    private val scoutEngine: OpportunityScoutEngine
) {

    private val _lastScoutResult = MutableStateFlow<ScoutResult?>(null)
    val lastScoutResult: StateFlow<ScoutResult?> = _lastScoutResult.asStateFlow()

    fun getActiveOpportunities(): Flow<List<OpportunityEntity>> {
        return dao.getActiveOpportunities(500)
    }

    fun getAllOpportunities(): Flow<List<OpportunityEntity>> {
        return dao.getLatestOpportunities(500)
    }

    fun getOpportunitiesByCategory(category: OpportunityCategory): Flow<List<OpportunityEntity>> {
        return dao.getOpportunitiesByCategory(category.name, 500)
    }

    fun getOpportunitiesByRegion(region: OpportunityRegion): Flow<List<OpportunityEntity>> {
        return dao.getOpportunitiesByRegion(region.name, 500)
    }

    fun getOpportunityById(id: String): Flow<OpportunityEntity?> {
        return dao.getOpportunityById(id)
    }

    fun countTotal(): Flow<Int> = dao.countOpportunities()

    fun countVerified(): Flow<Int> = dao.countByVerificationStatus(VerificationStatus.VERIFIED.name)

    fun countNeedsReview(): Flow<Int> = dao.countByVerificationStatus(VerificationStatus.NEEDS_REVIEW.name)

    suspend fun runScoutScan(
        assamEnabled: Boolean = true,
        northeastEnabled: Boolean = true,
        indiaEnabled: Boolean = true,
        internationalEnabled: Boolean = false,
        newsEnabled: Boolean = true
    ): ScoutResult {
        val result = scoutEngine.runScout(
            assamEnabled = assamEnabled,
            northeastEnabled = northeastEnabled,
            indiaEnabled = indiaEnabled,
            internationalEnabled = internationalEnabled,
            newsEnabled = newsEnabled
        )
        _lastScoutResult.value = result
        return result
    }

    suspend fun updateVerification(id: String, status: VerificationStatus) {
        dao.updateVerificationStatus(id, status.name)
    }
}
