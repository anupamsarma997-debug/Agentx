package com.example.data.model.content

enum class ContentType(val displayName: String) {
    OPPORTUNITY_POST("Opportunity Post"),
    NEWS_POST("News Summary"),
    JOB_ALERT("Job Alert"),
    SCHOLARSHIP_ALERT("Scholarship Alert"),
    GRANT_ALERT("Grant Alert"),
    HACKATHON_ALERT("Hackathon Alert"),
    MSME_ALERT("MSME Scheme Alert"),
    MEME_POST("Meme Post"),
    STARTUP_ALERT("Startup Alert"),
    GENERAL_INFORMATION("General Information");

    companion object {
        fun fromString(value: String): ContentType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: OPPORTUNITY_POST
        }
    }
}

enum class ContentPlatform(val displayName: String) {
    FACEBOOK("Facebook"),
    INSTAGRAM("Instagram"),
    BOTH("Facebook & Instagram");

    companion object {
        fun fromString(value: String): ContentPlatform {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: BOTH
        }
    }
}

enum class GenerationStatus(val displayName: String) {
    DRAFT("Draft"),
    NEEDS_REVIEW("Needs Review"),
    REVIEW_REQUIRED("Needs Review"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    PUBLISHING("Publishing"),
    PUBLISHED("Published"),
    FAILED("Failed"),
    GENERATED("Draft"),
    GENERATING("Generating");

    companion object {
        fun fromString(value: String): GenerationStatus {
            return when (value.uppercase()) {
                "DRAFT", "GENERATED" -> DRAFT
                "NEEDS_REVIEW", "REVIEW_REQUIRED" -> NEEDS_REVIEW
                "APPROVED" -> APPROVED
                "REJECTED" -> REJECTED
                "PUBLISHING" -> PUBLISHING
                "PUBLISHED" -> PUBLISHED
                "FAILED" -> FAILED
                else -> entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: DRAFT
            }
        }
    }
}

enum class ContentLength(val displayName: String, val wordCountGuide: String, val targetWords: Int) {
    SHORT("Short", "50–100 words", 80),
    MEDIUM("Medium", "100–180 words", 140),
    LONG("Long", "180–300 words", 240);

    companion object {
        fun fromString(value: String): ContentLength {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: SHORT
        }
    }
}

enum class PublishTargetPlatform(val displayName: String, val subtitle: String) {
    FACEBOOK_ONLY("Facebook Only", "Publish only to connected Facebook Page"),
    INSTAGRAM_ONLY("Instagram Only", "Publish only to connected Instagram account"),
    BOTH("Both (Facebook & Instagram)", "Publish simultaneously to both platforms")
}

