package com.example.domain.validator

import com.example.data.local.entity.ContentEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.util.regex.Pattern

enum class ContentField {
    TITLE,
    FACEBOOK_BODY,
    SOURCE_URL,
    DEADLINE,
    HASHTAGS,
    SAFETY,
    INSTAGRAM_CAPTION
}

data class RuleFailure(
    val ruleId: Int,
    val ruleName: String,
    val reason: String,
    val field: ContentField,
    val isBlocking: Boolean
)

data class ValidationResult(
    val passed: Boolean,
    val failures: List<RuleFailure>,
    val warnings: List<RuleFailure>,
    val pageTitle: String? = null,
    val isSourceVerified: Boolean = false
) {
    val allIssues: List<RuleFailure>
        get() = failures + warnings

    val hasBlockingFailures: Boolean
        get() = failures.isNotEmpty()

    val hasOnlyWarnings: Boolean
        get() = failures.isEmpty() && warnings.isNotEmpty()
}

class ContentApprovalValidator(
    private val httpTimeoutMs: Int = 8000,
    private val customUrlChecker: ((url: String, expectedTitle: String) -> Pair<Int, String?>)? = null
) {

    companion object {
        private val SENSATIONAL_OR_MISLEADING_PATTERNS = listOf(
            "guaranteed job without exam",
            "100% free money",
            "forwarded as received",
            "shocking truth leaked",
            "viral secret trick",
            "vote for",
            "destroy opposition"
        )

        private val SAFETY_HATE_OR_ADULT_PATTERNS = listOf(
            "kill",
            "attack",
            "hate speech",
            "porn",
            "adult content",
            "gambling",
            "casino"
        )
    }

    /**
     * Executes the comprehensive 8-rule validation check.
     * Rules are evaluated independently; each failure or warning is itemized with actionable details.
     */
    suspend fun validate(
        content: ContentEntity,
        skipNetworkCheck: Boolean = false,
        sourcePageContext: String? = null
    ): ValidationResult = withContext(Dispatchers.IO) {
        val failures = mutableListOf<RuleFailure>()
        val warnings = mutableListOf<RuleFailure>()

        // ----------------------------------------------------
        // Rule 1 (Blocking): Title is not empty and has substance
        // ----------------------------------------------------
        val cleanTitle = content.title.trim()
        if (cleanTitle.isBlank()) {
            failures.add(
                RuleFailure(
                    ruleId = 1,
                    ruleName = "Title Required",
                    reason = "Title khali hai. Post ka headline hona zaroori hai.",
                    field = ContentField.TITLE,
                    isBlocking = true
                )
            )
        } else if (cleanTitle.length < 5) {
            failures.add(
                RuleFailure(
                    ruleId = 1,
                    ruleName = "Title Too Short",
                    reason = "Title bohot chhota hai (kam se kam 5 characters hone chahiye).",
                    field = ContentField.TITLE,
                    isBlocking = true
                )
            )
        } else if (cleanTitle == "..." || cleanTitle.endsWith("...")) {
            failures.add(
                RuleFailure(
                    ruleId = 1,
                    ruleName = "Title Truncated",
                    reason = "Title '...' par cut ho gaya hai. Poora headline likhein.",
                    field = ContentField.TITLE,
                    isBlocking = true
                )
            )
        }

        // ----------------------------------------------------
        // Rule 2 (Blocking): Facebook body >= 80 chars & not truncated ("...")
        // ----------------------------------------------------
        val cleanBody = content.body.trim()
        if (cleanBody.isBlank()) {
            failures.add(
                RuleFailure(
                    ruleId = 2,
                    ruleName = "Body Required",
                    reason = "Post body khali hai. Kam se kam 80 characters hone chahiye.",
                    field = ContentField.FACEBOOK_BODY,
                    isBlocking = true
                )
            )
        } else if (cleanBody.length < 80) {
            failures.add(
                RuleFailure(
                    ruleId = 2,
                    ruleName = "Body Too Short",
                    reason = "Body me sirf ${cleanBody.length} characters hain. Minimum 80 characters required hain.",
                    field = ContentField.FACEBOOK_BODY,
                    isBlocking = true
                )
            )
        } else {
            // Check for literal "..." truncation in the body text
            val containsEllipsisTruncation = cleanBody.contains("...") || cleanBody.contains("…")
            val isBodyCutOff = cleanBody.endsWith("...") || cleanBody.endsWith("…") ||
                    cleanBody.lines().any { line -> line.trim() == "..." || line.trim() == "…" }

            if (isBodyCutOff || cleanBody == "...") {
                failures.add(
                    RuleFailure(
                        ruleId = 2,
                        ruleName = "Body Truncated",
                        reason = "Body me '...' ya adhoori line mili hai. Content truncate ho gaya hai.",
                        field = ContentField.FACEBOOK_BODY,
                        isBlocking = true
                    )
                )
            }
        }

        // ----------------------------------------------------
        // Rule 3 (Blocking): Source URL present and valid format
        // ----------------------------------------------------
        val cleanUrl = content.sourceUrl.trim()
        var isUrlSyntaxValid = false
        if (cleanUrl.isBlank() || cleanUrl == "..." || cleanUrl.contains("...")) {
            failures.add(
                RuleFailure(
                    ruleId = 3,
                    ruleName = "Source URL Missing",
                    reason = "Source URL missing ya invalid hai.",
                    field = ContentField.SOURCE_URL,
                    isBlocking = true
                )
            )
        } else if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            failures.add(
                RuleFailure(
                    ruleId = 3,
                    ruleName = "Invalid URL Protocol",
                    reason = "Source URL https:// ya http:// se shuru honi chahiye ($cleanUrl).",
                    field = ContentField.SOURCE_URL,
                    isBlocking = true
                )
            )
        } else {
            try {
                val uri = URI(cleanUrl)
                if (uri.host.isNullOrBlank()) {
                    failures.add(
                        RuleFailure(
                            ruleId = 3,
                            ruleName = "Invalid URL Domain",
                            reason = "Source URL ka host/domain galat hai ($cleanUrl).",
                            field = ContentField.SOURCE_URL,
                            isBlocking = true
                        )
                    )
                } else {
                    isUrlSyntaxValid = true
                }
            } catch (e: Exception) {
                failures.add(
                    RuleFailure(
                        ruleId = 3,
                        ruleName = "Malformed URL",
                        reason = "Source URL syntax invalid hai: ${e.message}",
                        field = ContentField.SOURCE_URL,
                        isBlocking = true
                    )
                )
            }
        }

        // ----------------------------------------------------
        // Rule 4 (Blocking): Source URL HTTP GET check returns 200 (redirects followed, 8s timeout)
        // ----------------------------------------------------
        var pageTitle: String? = null
        var isSourceVerified = false
        var fetchedPageText: String? = sourcePageContext

        if (isUrlSyntaxValid && !skipNetworkCheck) {
            val netCheck = if (customUrlChecker != null) {
                val (code, title) = customUrlChecker.invoke(cleanUrl, content.title)
                HttpCheckResult(
                    statusCode = code,
                    pageTitle = title,
                    bodySnippet = title ?: "",
                    titleMatched = title != null && checkKeywordMatch(content.title, title, title),
                    errorMessage = if (code != 200) "HTTP $code" else null
                )
            } else {
                checkUrlLiveStatus(cleanUrl, content.title)
            }
            pageTitle = netCheck.pageTitle
            if (netCheck.statusCode != 200) {
                failures.add(
                    RuleFailure(
                        ruleId = 4,
                        ruleName = "Source URL Dead",
                        reason = if (netCheck.statusCode > 0) {
                            "Source link ne HTTP ${netCheck.statusCode} error diya (Dead link). Status 200 hona chahiye."
                        } else {
                            "Source URL tak connection nahi ho paya: ${netCheck.errorMessage ?: "Timeout/Network error"}"
                        },
                        field = ContentField.SOURCE_URL,
                        isBlocking = true
                    )
                )
            } else {
                fetchedPageText = netCheck.bodySnippet
                isSourceVerified = netCheck.titleMatched
            }
        } else if (isUrlSyntaxValid && skipNetworkCheck) {
            // Assume reachable in offline test mode
            isSourceVerified = true
        }

        // ----------------------------------------------------
        // Rule 5 (Warning): Deadline / Date line verification against source
        // ----------------------------------------------------
        val datePattern = Pattern.compile("(?i)(deadline|last date|due date|tarikh|antim tithi)[:\\s]+([0-9a-zA-Z\\s,/.-]{5,30})")
        val dateMatcher = datePattern.matcher(cleanBody)
        if (dateMatcher.find()) {
            val matchedDate = dateMatcher.group(2)?.trim() ?: ""
            // Check if this date string exists in source context if available
            val sourceText = fetchedPageText ?: ""
            if (sourceText.isNotBlank()) {
                val cleanDateForSearch = matchedDate.replace(Regex("[^0-9a-zA-Z]"), "").lowercase()
                val cleanSource = sourceText.replace(Regex("[^0-9a-zA-Z]"), "").lowercase()
                if (cleanDateForSearch.length >= 4 && !cleanSource.contains(cleanDateForSearch)) {
                    warnings.add(
                        RuleFailure(
                            ruleId = 5,
                            ruleName = "Deadline Unverified",
                            reason = "Post me deadline '$matchedDate' likhi hai, lekin source page par confirm nahi hui. Kripya verify karein.",
                            field = ContentField.DEADLINE,
                            isBlocking = false
                        )
                    )
                }
            } else {
                warnings.add(
                    RuleFailure(
                        ruleId = 5,
                        ruleName = "Deadline Unverified",
                        reason = "Post me deadline likhi hai. Kripya check karein ki ye official source se verified hai.",
                        field = ContentField.DEADLINE,
                        isBlocking = false
                    )
                )
            }
        }

        // ----------------------------------------------------
        // Rule 6 (Warning): Hashtags count <= 8
        // ----------------------------------------------------
        val hashtags = content.getHashtagList()
        if (hashtags.size > 8) {
            warnings.add(
                RuleFailure(
                    ruleId = 6,
                    ruleName = "Too Many Hashtags",
                    reason = "Hashtags ki sankhya 8 se zyada hai (${hashtags.size} tags). Maximum 8 recommended hain.",
                    field = ContentField.HASHTAGS,
                    isBlocking = false
                )
            )
        }

        // ----------------------------------------------------
        // Rule 7 (Blocking): Safety check (hate speech, adult, misleading claims)
        // ----------------------------------------------------
        val combinedText = "${cleanTitle.lowercase()} ${cleanBody.lowercase()}"
        val foundMisleading = SENSATIONAL_OR_MISLEADING_PATTERNS.firstOrNull { combinedText.contains(it) }
        val foundSafety = SAFETY_HATE_OR_ADULT_PATTERNS.firstOrNull { combinedText.contains(it) }

        if (foundMisleading != null) {
            failures.add(
                RuleFailure(
                    ruleId = 7,
                    ruleName = "Misleading Content Flag",
                    reason = "Content me misleading claim mila: '$foundMisleading'. Factual accuracy mandatory hai.",
                    field = ContentField.SAFETY,
                    isBlocking = true
                )
            )
        }
        if (foundSafety != null) {
            failures.add(
                RuleFailure(
                    ruleId = 7,
                    ruleName = "Safety Policy Violation",
                    reason = "Content me prohibited word/phrase mila: '$foundSafety'.",
                    field = ContentField.SAFETY,
                    isBlocking = true
                )
            )
        }

        // ----------------------------------------------------
        // Rule 8 (Warning): Instagram caption <= 2200 characters
        // ----------------------------------------------------
        val caption = content.caption.trim()
        if (caption.length > 2200) {
            warnings.add(
                RuleFailure(
                    ruleId = 8,
                    ruleName = "Instagram Caption Too Long",
                    reason = "Instagram caption 2200 characters se zyada hai (${caption.length} chars). Truncate ho jayega.",
                    field = ContentField.INSTAGRAM_CAPTION,
                    isBlocking = false
                )
            )
        }

        ValidationResult(
            passed = failures.isEmpty(),
            failures = failures,
            warnings = warnings,
            pageTitle = pageTitle,
            isSourceVerified = isSourceVerified
        )
    }

    private data class HttpCheckResult(
        val statusCode: Int,
        val pageTitle: String?,
        val bodySnippet: String?,
        val titleMatched: Boolean,
        val errorMessage: String? = null
    )

    private fun checkUrlLiveStatus(urlString: String, expectedTitle: String): HttpCheckResult {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = httpTimeoutMs
                readTimeout = httpTimeoutMs
                instanceFollowRedirects = true
                requestMethod = "GET"
                setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0 Mobile Safari/537.36 SocialAgent/1.0"
                )
                setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            }

            val code = connection.responseCode
            if (code != 200) {
                return HttpCheckResult(
                    statusCode = code,
                    pageTitle = null,
                    bodySnippet = null,
                    titleMatched = false,
                    errorMessage = "HTTP $code"
                )
            }

            // Read up to 32KB of content to inspect title & keywords
            val html = try {
                connection.inputStream.bufferedReader().use { reader ->
                    val buffer = CharArray(32768)
                    val read = reader.read(buffer, 0, buffer.size)
                    if (read > 0) String(buffer, 0, read) else ""
                }
            } catch (_: Exception) {
                ""
            }

            val pageTitle = extractTitleFromHtml(html)
            val titleMatched = checkKeywordMatch(expectedTitle, pageTitle, html)

            HttpCheckResult(
                statusCode = 200,
                pageTitle = pageTitle,
                bodySnippet = html.take(4000),
                titleMatched = titleMatched
            )
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            HttpCheckResult(
                statusCode = -1,
                pageTitle = null,
                bodySnippet = null,
                titleMatched = false,
                errorMessage = e.localizedMessage ?: "Connection error"
            )
        } finally {
            connection?.disconnect()
        }
    }

    private fun extractTitleFromHtml(html: String): String? {
        val matcher = Pattern.compile("<title[^>]*>(.*?)</title>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL).matcher(html)
        return if (matcher.find()) {
            matcher.group(1)?.replace(Regex("<[^>]+>"), "")?.trim()
        } else {
            null
        }
    }

    private fun checkKeywordMatch(expectedTitle: String, pageTitle: String?, bodyText: String): Boolean {
        val stopWords = setOf("the", "and", "for", "with", "from", "in", "on", "at", "to", "of", "a", "an", "is", "by", "or", "alert", "portal", "registration")
        val keywords = expectedTitle.lowercase()
            .split(Regex("[^a-zA-Z0-9]+"))
            .filter { it.length >= 3 && it !in stopWords }

        if (keywords.isEmpty()) return true

        val searchSpace = "${pageTitle?.lowercase() ?: ""} ${bodyText.take(10000).lowercase()}"
        val matches = keywords.count { searchSpace.contains(it) }
        return matches >= 1
    }
}
