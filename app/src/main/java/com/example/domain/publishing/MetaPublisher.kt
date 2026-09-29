package com.example.domain.publishing

/**
 * Meta content publisher abstraction.
 *
 * SAFETY MANDATE (Phase 3 Foundation):
 * All publishing operations are strictly disabled to prevent accidental publishing.
 * Real publishing with verification checks will be introduced in subsequent phases.
 */
interface MetaPublisher {
    suspend fun publishFacebookPost(pageId: String, content: String): PublishResult
    suspend fun publishInstagramPhoto(instagramAccountId: String, imageUrl: String, caption: String): PublishResult
    suspend fun publishInstagramReel(instagramAccountId: String, videoUrl: String, caption: String): PublishResult
}

sealed interface PublishResult {
    data class Success(val postId: String) : PublishResult
    data class Failure(val error: String) : PublishResult
    data class Disabled(val message: String) : PublishResult
}

/**
 * Foundation implementation of MetaPublisher.
 * Strictly guarantees that publishing calls return safe Disabled results with zero network dispatch.
 */
class FoundationDisabledMetaPublisher : MetaPublisher {

    override suspend fun publishFacebookPost(pageId: String, content: String): PublishResult {
        return PublishResult.Disabled(
            "Facebook publishing is strictly disabled: Phase 3 connection foundation only."
        )
    }

    override suspend fun publishInstagramPhoto(
        instagramAccountId: String,
        imageUrl: String,
        caption: String
    ): PublishResult {
        return PublishResult.Disabled(
            "Instagram publishing is strictly disabled: Phase 3 connection foundation only."
        )
    }

    override suspend fun publishInstagramReel(
        instagramAccountId: String,
        videoUrl: String,
        caption: String
    ): PublishResult {
        return PublishResult.Disabled(
            "Instagram Reel publishing is strictly disabled: Phase 3 connection foundation only."
        )
    }
}
