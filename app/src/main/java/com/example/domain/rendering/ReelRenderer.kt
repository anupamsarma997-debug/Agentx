package com.example.domain.rendering

import com.example.data.model.reel.ReelDraft

/**
 * Video Rendering Abstraction for SocialAgent Reel Engine.
 *
 * For Phase 7:
 * - Provides preview representation abstraction.
 * - Video rendering and exporting remain disabled/unimplemented without adding FFmpeg or heavy dependencies.
 */
interface ReelRenderer {

    /**
     * Prepares a lightweight in-app preview descriptor for UI composition.
     */
    suspend fun createPreview(draft: ReelDraft): ReelPreviewModel

    /**
     * Video rendering engine hook.
     * STRICTLY disabled in Phase 7. Throws UnsupportedOperationException.
     */
    suspend fun renderVideo(draft: ReelDraft): String {
        throw UnsupportedOperationException("Full video rendering is deactivated in Phase 7.")
    }

    /**
     * Video export hook.
     * STRICTLY disabled in Phase 7. Throws UnsupportedOperationException.
     */
    suspend fun exportVideo(draft: ReelDraft, outputPath: String): Boolean {
        throw UnsupportedOperationException("Video export is deactivated in Phase 7.")
    }
}

data class ReelPreviewModel(
    val title: String,
    val totalDurationSeconds: Int,
    val sceneCount: Int,
    val isReadyForPreview: Boolean = true
)

class DefaultComposeReelRenderer : ReelRenderer {
    override suspend fun createPreview(draft: ReelDraft): ReelPreviewModel {
        return ReelPreviewModel(
            title = draft.title,
            totalDurationSeconds = draft.durationSeconds,
            sceneCount = draft.scenes.size,
            isReadyForPreview = true
        )
    }
}
