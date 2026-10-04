package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.OpportunityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OpportunityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOpportunity(opportunity: OpportunityEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOpportunities(opportunities: List<OpportunityEntity>): List<Long>

    @Update
    suspend fun updateOpportunity(opportunity: OpportunityEntity)

    @Query("SELECT * FROM opportunities ORDER BY discoveredAt DESC LIMIT :limit")
    fun getLatestOpportunities(limit: Int = 100): Flow<List<OpportunityEntity>>

    @Query("SELECT * FROM opportunities WHERE verificationStatus != 'REJECTED' ORDER BY discoveredAt DESC LIMIT :limit")
    fun getActiveOpportunities(limit: Int = 150): Flow<List<OpportunityEntity>>

    @Query("SELECT * FROM opportunities WHERE category = :category AND verificationStatus != 'REJECTED' ORDER BY discoveredAt DESC LIMIT :limit")
    fun getOpportunitiesByCategory(category: String, limit: Int = 100): Flow<List<OpportunityEntity>>

    @Query("SELECT * FROM opportunities WHERE region = :region AND verificationStatus != 'REJECTED' ORDER BY discoveredAt DESC LIMIT :limit")
    fun getOpportunitiesByRegion(region: String, limit: Int = 100): Flow<List<OpportunityEntity>>

    @Query("UPDATE opportunities SET isExpired = 0, verificationStatus = 'VERIFIED' WHERE verificationStatus = 'EXPIRED' OR isExpired = 1")
    suspend fun unexpireAllOpportunities(): Int

    @Query("SELECT * FROM opportunities WHERE verificationStatus = :status ORDER BY discoveredAt DESC LIMIT :limit")
    fun getOpportunitiesByVerificationStatus(status: String, limit: Int = 100): Flow<List<OpportunityEntity>>

    @Query("SELECT * FROM opportunities WHERE verificationStatus = :status ORDER BY discoveredAt DESC LIMIT :limit")
    suspend fun getOpportunitiesByVerificationStatusSync(status: String, limit: Int = 20): List<OpportunityEntity>

    @Query("SELECT * FROM opportunities WHERE id = :id LIMIT 1")
    fun getOpportunityById(id: String): Flow<OpportunityEntity?>

    @Query("SELECT * FROM opportunities WHERE id = :id LIMIT 1")
    suspend fun getOpportunityByIdSync(id: String): OpportunityEntity?

    @Query("SELECT * FROM opportunities WHERE contentHash = :hash LIMIT 1")
    suspend fun findByContentHash(hash: String): OpportunityEntity?

    @Query("SELECT contentHash FROM opportunities")
    suspend fun getAllContentHashes(): List<String>

    @Query("UPDATE opportunities SET verificationStatus = :status, lastCheckedAt = :checkedAt WHERE id = :id")
    suspend fun updateVerificationStatus(id: String, status: String, checkedAt: Long = System.currentTimeMillis())

    @Query("UPDATE opportunities SET isExpired = 1, verificationStatus = 'EXPIRED', lastCheckedAt = :checkedAt WHERE id = :id")
    suspend fun markExpired(id: String, checkedAt: Long = System.currentTimeMillis())

    @Query("UPDATE opportunities SET isExpired = 1, verificationStatus = 'EXPIRED', lastCheckedAt = :checkedAt WHERE deadlineEpochMillis IS NOT NULL AND deadlineEpochMillis < :nowMillis AND isExpired = 0")
    suspend fun markExpiredBefore(nowMillis: Long, checkedAt: Long = System.currentTimeMillis()): Int

    @Query("DELETE FROM opportunities WHERE isExpired = 1 AND discoveredAt < :cutoffMillis")
    suspend fun deleteExpiredOlderThan(cutoffMillis: Long): Int

    @Query("SELECT COUNT(*) FROM opportunities")
    fun countOpportunities(): Flow<Int>

    @Query("SELECT COUNT(*) FROM opportunities")
    suspend fun countOpportunitiesSync(): Int

    @Query("SELECT COUNT(*) FROM opportunities WHERE verificationStatus = :status")
    fun countByVerificationStatus(status: String): Flow<Int>
}
