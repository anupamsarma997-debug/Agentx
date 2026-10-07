package com.example.data.model.opportunity

enum class OpportunityCategory(val displayName: String) {
    JOB("Jobs"),
    GOVERNMENT_JOB("Government Jobs"),
    GOVERNMENT_SCHEME("Government Schemes"),
    INTERNSHIP("Internships"),
    SCHOLARSHIP("Scholarships"),
    MSME("MSME Schemes"),
    FELLOWSHIP("Fellowships"),
    GRANT("Grants"),
    HACKATHON("Hackathons"),
    COMPETITION("Competitions"),
    STARTUP("Startup Competitions"),
    INCUBATOR("Incubators"),
    ACCELERATOR("Accelerators"),
    BUSINESS("Business Opportunities"),
    EVENT("Events"),
    NEWS("Factual News"),
    ASSAM("Assam Initiatives"),
    NORTHEAST("Northeast Initiatives"),
    TECHNOLOGY("Technology"),
    EDUCATION("Education"),
    OTHER("Other Opportunities");

    companion object {
        fun fromString(value: String): OpportunityCategory {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: OTHER
        }
    }
}

enum class OpportunityRegion(val displayName: String, val sortOrder: Int) {
    ASSAM("Assam", 1),
    NORTHEAST_INDIA("Northeast India", 2),
    INDIA("India", 3),
    INTERNATIONAL("International", 4),
    UNKNOWN("General", 5);

    companion object {
        fun fromString(value: String): OpportunityRegion {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
        }
    }
}

enum class VerificationStatus(val displayName: String) {
    VERIFIED("Verified"),
    NEEDS_REVIEW("Needs Review"),
    EXPIRED("Expired"),
    FAILED("Failed"),
    REJECTED("Rejected");

    companion object {
        fun fromString(value: String): VerificationStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: NEEDS_REVIEW
        }
    }
}

enum class SourceTier(val displayName: String) {
    TIER_1_OFFICIAL("Tier 1 - Official Entity"),
    TIER_2_REPUTABLE("Tier 2 - Reputable Portal"),
    TIER_3_COMMUNITY("Tier 3 - Community Feed"),
    UNKNOWN("Unknown Source");

    companion object {
        fun fromString(value: String): SourceTier {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
        }
    }
}
