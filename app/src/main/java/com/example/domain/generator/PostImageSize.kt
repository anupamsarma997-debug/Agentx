package com.example.domain.generator

/**
 * Standard social media aspect ratios and image dimensions.
 */
enum class PostImageSize(
    val id: String,
    val displayName: String,
    val width: Int,
    val height: Int,
    val aspectRatioLabel: String,
    val aspectFloat: Float,
    val description: String
) {
    SQUARE(
        id = "square",
        displayName = "Square (1:1)",
        width = 1080,
        height = 1080,
        aspectRatioLabel = "1:1",
        aspectFloat = 1.0f,
        description = "Ideal for Facebook & Instagram feed posts"
    ),
    PORTRAIT(
        id = "portrait",
        displayName = "Portrait (4:5)",
        width = 1080,
        height = 1350,
        aspectRatioLabel = "4:5",
        aspectFloat = 0.8f,
        description = "Optimal vertical feed real-estate on mobile"
    ),
    LANDSCAPE(
        id = "landscape",
        displayName = "Landscape (16:9)",
        width = 1200,
        height = 675,
        aspectRatioLabel = "16:9",
        aspectFloat = 1.777f,
        description = "Facebook Link Preview & desktop wide banner"
    ),
    STORY(
        id = "story",
        displayName = "Story / Reel (9:16)",
        width = 1080,
        height = 1920,
        aspectRatioLabel = "9:16",
        aspectFloat = 0.5625f,
        description = "Full-screen vertical Stories & Reels cover"
    );

    companion object {
        fun fromId(id: String?): PostImageSize {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: SQUARE
        }
    }
}
