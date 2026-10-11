package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.PostedContentTrackerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostedContentTrackerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tracker: PostedContentTrackerEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM posted_content_tracker WHERE contentFingerprint = :fingerprint)")
    suspend fun isFingerprintPosted(fingerprint: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM posted_content_tracker WHERE contentUrl = :url AND contentUrl IS NOT NULL AND contentUrl != '')")
    suspend fun isUrlPosted(url: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM posted_content_tracker WHERE opportunityId = :opportunityId)")
    suspend fun isOpportunityPosted(opportunityId: String): Boolean

    @Query("SELECT * FROM posted_content_tracker WHERE contentFingerprint = :fingerprint LIMIT 1")
    suspend fun getByFingerprint(fingerprint: String): PostedContentTrackerEntity?

    @Query("SELECT * FROM posted_content_tracker WHERE contentUrl = :url LIMIT 1")
    suspend fun getByUrl(url: String): PostedContentTrackerEntity?

    @Query("SELECT * FROM posted_content_tracker ORDER BY publishedAt DESC")
    fun getAllTrackedPosts(): Flow<List<PostedContentTrackerEntity>>

    @Query("SELECT * FROM posted_content_tracker ORDER BY publishedAt DESC LIMIT :limit")
    suspend fun getRecentTrackedPostsSync(limit: Int = 100): List<PostedContentTrackerEntity>

    @Query("SELECT socialPostId FROM posted_content_tracker WHERE socialPostId IS NOT NULL")
    suspend fun getAllSocialPostIdsSync(): List<String>

    @Query("SELECT COUNT(*) FROM posted_content_tracker")
    suspend fun getCount(): Int
}
