package com.example.domain.engine

import com.example.data.model.opportunity.SourceTier
import java.net.URI
import java.security.MessageDigest
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

object OpportunityNormalizer {

    private val TIER_1_DOMAINS_SUFFIXES = listOf(
        ".gov.in",
        ".nic.in",
        ".gov",
        ".mil",
        ".ac.in",
        ".edu.in",
        ".edu"
    )

    private val TIER_1_EXACT_DOMAINS = listOf(
        "mygov.in",
        "innovateindia.mygov.in",
        "aicte-india.org",
        "ugc.gov.in",
        "ncs.gov.in",
        "assam.gov.in",
        "apsc.nic.in",
        "dst.gov.in",
        "startupindia.gov.in",
        "devpost.com",
        "unstop.com",
        "hackerearth.com"
    )

    private val TRACKING_PARAMS = setOf(
        "utm_source",
        "utm_medium",
        "utm_campaign",
        "utm_term",
        "utm_content",
        "fbclid",
        "gclid",
        "ref",
        "source"
    )

    private val DATE_FORMATTERS = listOf(
        DateTimeFormatter.ISO_LOCAL_DATE,                     // 2026-09-28
        DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.US), // 28/09/2026
        DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.US), // 28-09-2026
        DateTimeFormatter.ofPattern("d/M/yyyy", Locale.US),   // 5/9/2026
        DateTimeFormatter.ofPattern("d-M-yyyy", Locale.US),   // 5-9-2026
        DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US),// Sep 28, 2026
        DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US)// September 28, 2026
    )

    fun normalizeText(input: String?): String {
        if (input.isNullOrBlank()) return ""
        return input
            .replace("\\s+".toRegex(), " ")
            .trim()
    }

    fun normalizeTitle(title: String?): String {
        return normalizeText(title)
    }

    fun normalizeOrganization(org: String?): String {
        return normalizeText(org)
    }

    /**
     * Cleans URLs by normalizing scheme/host to lowercase, removing tracking query params,
     * and stripping fragments. Pure standard library implementation (runs on both JVM and Android).
     */
    fun normalizeUrl(rawUrl: String?): String {
        if (rawUrl.isNullOrBlank()) return ""
        return try {
            val uri = URI(rawUrl.trim())
            val scheme = (uri.scheme ?: "https").lowercase(Locale.ROOT)
            val host = (uri.host ?: "").lowercase(Locale.ROOT)
            val port = uri.port
            val path = uri.path ?: ""
            val rawQuery = uri.query

            val cleanQuery = if (!rawQuery.isNullOrBlank()) {
                rawQuery.split("&")
                    .mapNotNull { param ->
                        val parts = param.split("=", limit = 2)
                        val key = parts[0].lowercase(Locale.ROOT)
                        if (TRACKING_PARAMS.contains(key)) null else param
                    }
                    .joinToString("&")
            } else ""

            val authority = if (port != -1) "$host:$port" else host
            val queryString = if (cleanQuery.isNotBlank()) "?$cleanQuery" else ""
            "$scheme://$authority$path$queryString"
        } catch (_: Exception) {
            rawUrl.trim()
        }
    }

    fun extractDomain(url: String?): String {
        if (url.isNullOrBlank()) return ""
        return try {
            val uri = URI(normalizeUrl(url))
            val host = uri.host ?: ""
            if (host.startsWith("www.")) host.substring(4) else host
        } catch (_: Exception) {
            ""
        }
    }

    fun determineSourceTier(url: String?): SourceTier {
        val domain = extractDomain(url).lowercase(Locale.ROOT)
        if (domain.isBlank()) return SourceTier.UNKNOWN

        if (TIER_1_EXACT_DOMAINS.any { domain == it || domain.endsWith(".$it") }) {
            return SourceTier.TIER_1_OFFICIAL
        }
        if (TIER_1_DOMAINS_SUFFIXES.any { domain.endsWith(it) }) {
            return SourceTier.TIER_1_OFFICIAL
        }

        // Tier 2: Reputable tech, university or news publications
        if (domain.endsWith(".org") || domain.contains("thehindu") || domain.contains("timesofindia") ||
            domain.contains("hindustantimes") || domain.contains("assamtribune") || domain.contains("sentinelassam") ||
            domain.contains("indiatoday") || domain.contains("careers360") || domain.contains("shiksha")
        ) {
            return SourceTier.TIER_2_REPUTABLE
        }

        return SourceTier.TIER_3_COMMUNITY
    }

    /**
     * Parses deadline string into normalized (ISO yyyy-MM-dd) string and epoch millis.
     */
    fun parseDeadline(rawDeadline: String?): ParsedDeadline {
        if (rawDeadline.isNullOrBlank()) {
            return ParsedDeadline(normalizedString = null, epochMillis = null, isExpired = false)
        }

        val cleaned = normalizeText(rawDeadline)

        // Try direct date patterns
        for (formatter in DATE_FORMATTERS) {
            try {
                val date = LocalDate.parse(cleaned, formatter)
                val epochMillis = date.atStartOfDay(ZoneId.of("Asia/Kolkata"))
                    .plusHours(23).plusMinutes(59).toInstant().toEpochMilli()
                val isExpired = LocalDate.now(ZoneId.of("Asia/Kolkata")).isAfter(date)
                return ParsedDeadline(
                    normalizedString = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    epochMillis = epochMillis,
                    isExpired = isExpired
                )
            } catch (_: DateTimeParseException) {
                // Try next
            }
        }

        return ParsedDeadline(
            normalizedString = cleaned,
            epochMillis = null,
            isExpired = false
        )
    }

    /**
     * Generates a deterministic content hash from stable identity fields.
     */
    fun computeContentHash(
        title: String,
        domain: String,
        deadline: String?,
        organization: String?
    ): String {
        val normTitle = normalizeTitle(title).lowercase(Locale.ROOT)
        val normDomain = domain.lowercase(Locale.ROOT).trim()
        val normDeadline = normalizeText(deadline).lowercase(Locale.ROOT)
        val normOrg = normalizeOrganization(organization).lowercase(Locale.ROOT)

        val rawSignature = "$normTitle|$normDomain|$normDeadline|$normOrg"
        val bytes = MessageDigest.getInstance("SHA-256").digest(rawSignature.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

data class ParsedDeadline(
    val normalizedString: String?,
    val epochMillis: Long?,
    val isExpired: Boolean
)
