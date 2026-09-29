package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ContentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContent(content: ContentEntity): Long

    @Update
    suspend fun updateContent(content: ContentEntity)

    @Query("SELECT * FROM content_items ORDER BY createdAt DESC")
    fun getAllContent(): Flow<List<ContentEntity>>

    @Query("SELECT * FROM content_items WHERE generationStatus = :status ORDER BY createdAt DESC")
    fun getContentByStatus(status: String): Flow<List<ContentEntity>>

    @Query("SELECT * FROM content_items WHERE id = :id LIMIT 1")
    fun getContentById(id: String): Flow<ContentEntity?>

    @Query("SELECT * FROM content_items WHERE id = :id LIMIT 1")
    suspend fun getContentByIdSync(id: String): ContentEntity?

    @Query("UPDATE content_items SET generationStatus = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE content_items SET title = :title, body = :body, caption = :caption, hashtags = :hashtags, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateContentDraft(
        id: String,
        title: String,
        body: String,
        caption: String,
        hashtags: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM content_items WHERE id = :id")
    suspend fun deleteContentById(id: String)

    @Query("SELECT COUNT(*) FROM content_items")
    fun countTotal(): Flow<Int>

    @Query("SELECT COUNT(*) FROM content_items WHERE generationStatus = :status")
    fun countByStatus(status: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM content_items WHERE createdAt >= :startOfDayMillis AND generationStatus IN ('GENERATED', 'APPROVED', 'REVIEW_REQUIRED')")
    fun countGeneratedSince(startOfDayMillis: Long): Flow<Int>
}
