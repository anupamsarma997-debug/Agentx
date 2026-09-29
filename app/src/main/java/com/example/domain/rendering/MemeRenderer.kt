package com.example.domain.rendering

import com.example.data.model.meme.MemeDraft
import com.example.data.model.meme.MemeFormat

enum class MemeVisualTheme(val displayName: String) {
    DARK_CLASSIC("Dark Card"),
    CLEAN_MINIMAL("Clean Minimal"),
    VIBRANT_ACCENT("Vibrant Accent"),
    NEWS_TICKER("Editorial Ticker")
}

data class MemePreviewLayout(
    val topText: String,
    val bottomText: String,
    val visualTheme: MemeVisualTheme,
    val formatBadge: String,
    val aspectRatio: String = "1:1",
    val canRenderLocalBitmap: Boolean = false,
    val requiresExternalProvider: Boolean = false
)

interface MemeRenderer {
    /**
     * Prepares layout and style parameters for Compose preview without executing external paid calls.
     */
    fun createPreviewLayout(draft: MemeDraft, theme: MemeVisualTheme = MemeVisualTheme.DARK_CLASSIC): MemePreviewLayout

    /**
     * Future extension point: renders a native bitmap on-device from Compose canvas or local template.
     */
    suspend fun renderLocalBitmap(draft: MemeDraft): Result<ByteArray>

    /**
     * Future extension point: provider hook for user-selected image background templates.
     */
    suspend fun applyImageTemplate(draft: MemeDraft, templateUri: String): Result<Boolean>
}

/**
 * Default zero-cost Compose-first meme renderer.
 */
class DefaultComposeMemeRenderer : MemeRenderer {

    override fun createPreviewLayout(draft: MemeDraft, theme: MemeVisualTheme): MemePreviewLayout {
        val top = when (draft.memeFormat) {
            MemeFormat.EXPECTATION_REALITY -> "EXPECTATION: ${draft.setupText}"
            MemeFormat.BEFORE_AFTER -> "BEFORE: ${draft.setupText}"
            MemeFormat.TWO_PANEL -> "PANEL 1: ${draft.setupText}"
            else -> draft.setupText
        }

        val bottom = when (draft.memeFormat) {
            MemeFormat.EXPECTATION_REALITY -> "REALITY: ${draft.punchlineText}"
            MemeFormat.BEFORE_AFTER -> "AFTER: ${draft.punchlineText}"
            MemeFormat.TWO_PANEL -> "PANEL 2: ${draft.punchlineText}"
            else -> draft.punchlineText
        }

        return MemePreviewLayout(
            topText = top,
            bottomText = bottom,
            visualTheme = theme,
            formatBadge = draft.memeFormat.displayName,
            aspectRatio = "1:1",
            canRenderLocalBitmap = true,
            requiresExternalProvider = false
        )
    }

    override suspend fun renderLocalBitmap(draft: MemeDraft): Result<ByteArray> {
        // Placeholder for future local canvas bitmap capture
        return Result.failure(UnsupportedOperationException("Local bitmap export scheduled for future rendering module."))
    }

    override suspend fun applyImageTemplate(draft: MemeDraft, templateUri: String): Result<Boolean> {
        // Hook for future local template overlay
        return Result.success(true)
    }
}
