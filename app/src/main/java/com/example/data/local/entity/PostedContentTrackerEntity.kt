package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tracks published social media posts by URL, content fingerprint, and opportunity ID
 * to guarantee that identical content is never published twice across platforms.
 */
@Entity(
    tableName = "posted_content_tracker",
    indices = [
        Index(value = ["contentUrl"], unique = false),
        Index(value = ["contentFingerprint"], unique = true),
        Index(value = ["opportunityId"], unique = false),
        Index(value = ["socialPlatform", "socialPostId"], unique = false)
    ]
)
data class PostedContentTrackerEntity(
    @PrimaryKey
    val id: String,
    val opportunityId: String?,
    val contentUrl: String?,
    val contentFingerprint: String,
    val title: String,
    val socialPlatform: String,
    val socialPostId: String?,
    val publishedAt: Long = System.currentTimeMillis(),
    val status: String = "PUBLISHED"
)
