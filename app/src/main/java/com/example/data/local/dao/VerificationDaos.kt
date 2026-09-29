package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.ContentVersionEntity
import com.example.data.local.entity.FinalVerificationRecordEntity
import com.example.data.local.entity.VerificationAuditLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FinalVerificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: FinalVerificationRecordEntity)

    @Query("SELECT * FROM verification_records WHERE contentId = :contentId")
    suspend fun getByContentId(contentId: String): FinalVerificationRecordEntity?

    @Query("SELECT * FROM verification_records WHERE contentId = :contentId")
    fun observeByContentId(contentId: String): Flow<FinalVerificationRecordEntity?>

    @Query("SELECT * FROM verification_records")
    fun observeAllRecords(): Flow<List<FinalVerificationRecordEntity>>

    @Query("SELECT * FROM verification_records WHERE finalStatus = :status")
    fun observeByStatus(status: String): Flow<List<FinalVerificationRecordEntity>>

    @Query("SELECT COUNT(*) FROM verification_records WHERE finalStatus = :status")
    fun countByStatus(status: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM verification_records WHERE finalStatus = :status")
    suspend fun countByStatusDirect(status: String): Int

    @Query("SELECT COUNT(*) FROM verification_records WHERE humanReviewRequired = 1 AND finalStatus != 'BLOCKED' AND finalStatus != 'REJECTED'")
    fun countNeedsReview(): Flow<Int>

    @Query("DELETE FROM verification_records WHERE contentId = :contentId")
    suspend fun deleteByContentId(contentId: String)
}

@Dao
interface ContentVersionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersion(version: ContentVersionEntity)

    @Query("SELECT * FROM content_versions WHERE contentId = :contentId ORDER BY versionNumber DESC")
    fun observeVersionsForContent(contentId: String): Flow<List<ContentVersionEntity>>

    @Query("SELECT * FROM content_versions WHERE contentId = :contentId ORDER BY versionNumber DESC")
    suspend fun getVersionsForContent(contentId: String): List<ContentVersionEntity>

    @Query("SELECT MAX(versionNumber) FROM content_versions WHERE contentId = :contentId")
    suspend fun getLatestVersionNumber(contentId: String): Int?
}

@Dao
interface VerificationAuditLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: VerificationAuditLogEntity)

    @Query("SELECT * FROM verification_audit_logs WHERE contentId = :contentId ORDER BY timestamp DESC")
    fun observeLogsForContent(contentId: String): Flow<List<VerificationAuditLogEntity>>

    @Query("SELECT * FROM verification_audit_logs ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecentLogs(limit: Int = 50): Flow<List<VerificationAuditLogEntity>>

    @Query("SELECT * FROM verification_audit_logs WHERE contentId = :contentId ORDER BY timestamp DESC")
    suspend fun getLogsForContent(contentId: String): List<VerificationAuditLogEntity>
}
