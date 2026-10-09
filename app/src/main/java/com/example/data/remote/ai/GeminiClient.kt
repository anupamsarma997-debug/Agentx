package com.example.data.remote.ai

import com.example.BuildConfig
import com.example.data.local.logging.AppLogger
import com.example.data.model.chat.ChatMessage
import com.example.data.model.chat.ChatResult
import com.example.data.model.chat.GroundingWebSource
import com.example.data.model.chat.ImageResult
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException

/**
 * Client for Google AI Studio Gemini API content generation.
 *
 * ARCHITECTURAL NOTE:
 * In Google AI Studio development and testing environments, the client-side REST endpoint
 * communicates directly with Generative Language API using GEMINI_API_KEY injected via
 * AI Studio Secrets.
 * For production deployment, client-side API keys should be migrated to a backend proxy
 * or Firebase AI with App Check to eliminate key exposure risks in distributed APKs.
 */
open class GeminiClient(
    private val modelName: String = "gemini-2.5-flash",
    private val apiKeyProvider: () -> String = { resolveApiKey() }
) {

    companion object {
        const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
        val FALLBACK_MODELS = listOf("gemini-2.5-flash", "gemini-1.5-flash")

        @Volatile
        var customApiKey: String? = null

        fun resolveApiKey(): String {
            // 0. Custom API key saved in app Settings
            val custom = customApiKey?.trim()
            if (!custom.isNullOrBlank() && !custom.startsWith("YOUR_") && custom != "MY_GEMINI_API_KEY") {
                return custom
            }

            // 1. Direct BuildConfig resolution from Secrets Gradle Plugin / buildConfigField
            val fromBuildConfig = try {
                val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
                field.get(null) as? String
            } catch (_: Throwable) {
                null
            }
            if (!fromBuildConfig.isNullOrBlank() &&
                fromBuildConfig != "MY_GEMINI_API_KEY" &&
                !fromBuildConfig.startsWith("YOUR_")
            ) {
                return fromBuildConfig.trim()
            }

            // 2. Runtime environment variable check (Google AI Studio environment)
            val fromEnv = try {
                System.getenv("GEMINI_API_KEY")
            } catch (_: Throwable) {
                null
            }
            if (!fromEnv.isNullOrBlank() &&
                fromEnv != "MY_GEMINI_API_KEY" &&
                !fromEnv.startsWith("YOUR_")
            ) {
                return fromEnv.trim()
            }

            return ""
        }

        suspend fun testApiKeyConnection(keyToTest: String? = null): Pair<Boolean, String> = withContext(Dispatchers.IO) {
            val key = keyToTest?.trim()?.ifBlank { null } ?: resolveApiKey()
            if (key.isBlank() || key == "MY_GEMINI_API_KEY" || key.startsWith("YOUR_")) {
                return@withContext Pair(false, "API key is not configured or blank.")
            }
            try {
                val testUrl = "$BASE_URL/gemini-2.5-flash:generateContent?key=$key"
                val payload = JSONObject().apply {
                    put("contents", JSONArray().put(JSONObject().apply {
                        put("parts", JSONArray().put(JSONObject().put("text", "Ping")))
                    }))
                }
                val conn = (URL(testUrl).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 10000
                    readTimeout = 10000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                }
                OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
                val code = conn.responseCode
                if (code in 200..299) {
                    Pair(true, "Connected successfully to Google Gemini 2.5 Flash!")
                } else {
                    val errStream = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                    val errMsg = when {
                        code == 400 && errStream.contains("API_KEY_INVALID") -> "Invalid API key."
                        code in listOf(401, 403) -> "Unauthorized key. Check permissions in Google AI Studio."
                        code == 429 -> "Rate limit reached (HTTP 429)."
                        else -> "HTTP $code error. Response: ${errStream.take(100)}"
                    }
                    Pair(false, errMsg)
                }
            } catch (e: Exception) {
                Pair(false, "Connection error: ${e.localizedMessage ?: "Network failed"}")
            }
        }
    }

    open suspend fun generateContent(systemPrompt: String, userPrompt: String): AIResult = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey.startsWith("YOUR_")) {
            return@withContext AIResult.ConfigurationRequired(
                "Gemini API key is not configured. Please add GEMINI_API_KEY in the AI Studio Secrets panel."
            )
        }

        // Try primary model, fallback if transient 404 or 503 occurs
        val modelsToTry = if (modelName in FALLBACK_MODELS) {
            listOf(modelName) + FALLBACK_MODELS.filter { it != modelName }
        } else {
            listOf(modelName) + FALLBACK_MODELS
        }

        var lastErrorResult: AIResult.Error? = null

        for (candidateModel in modelsToTry) {
            val result = executeRequest(candidateModel, apiKey, systemPrompt, userPrompt)
            when (result) {
                is AIResult.Success -> return@withContext result
                is AIResult.ConfigurationRequired -> return@withContext result
                is AIResult.Error -> {
                    lastErrorResult = result
                    // Only try fallback if error is model-specific (404 Not Found, 503 High Demand)
                    val isModelTransient = result.message.contains("503") ||
                            result.message.contains("404") ||
                            result.message.contains("not available") ||
                            result.message.contains("high demand")
                    if (!isModelTransient) {
                        return@withContext result
                    }
                }
            }
        }

        return@withContext lastErrorResult ?: AIResult.Error("Gemini generation failed. Please try again.")
    }

    private fun executeRequest(
        model: String,
        apiKey: String,
        systemPrompt: String,
        userPrompt: String
    ): AIResult {
        var connection: HttpURLConnection? = null
        try {
            val endpointUrl = "$BASE_URL/$model:generateContent?key=$apiKey"
            val url = URL(endpointUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 30000
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("User-Agent", "SocialAgent-Android/1.0")
            }

            // Construct payload with JSON structured output configuration
            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", userPrompt)
                            })
                        })
                    })
                })
                if (systemPrompt.isNotBlank()) {
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", systemPrompt)
                            })
                        })
                    })
                }
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.2)
                    put("maxOutputTokens", 2048)
                })
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestJson.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            val responseStream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream ?: connection.inputStream
            }

            val responseText = responseStream?.let { stream ->
                BufferedReader(InputStreamReader(stream)).use { it.readText() }
            } ?: ""

            if (responseCode !in 200..299) {
                val safeErrorMessage = extractSafeErrorMessage(responseCode, responseText, apiKey)
                return AIResult.Error(safeErrorMessage)
            }

            val root = JSONObject(responseText)

            // Check prompt feedback block
            val promptFeedback = root.optJSONObject("promptFeedback")
            val blockReason = promptFeedback?.optString("blockReason")
            if (!blockReason.isNullOrBlank()) {
                return AIResult.Error("Generation blocked by Gemini safety guidelines: $blockReason")
            }

            val candidates = root.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return AIResult.Error("Gemini returned no candidates.")
            }

            val firstCandidate = candidates.getJSONObject(0)
            val finishReason = firstCandidate.optString("finishReason")
            if (finishReason.equals("SAFETY", ignoreCase = true)) {
                return AIResult.Error("Content was blocked by Gemini safety filters.")
            }

            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (text.isNullOrBlank()) {
                return AIResult.Error("Gemini response contained empty text.")
            }

            return AIResult.Success(text)
        } catch (e: SocketTimeoutException) {
            AppLogger.warn("Gemini", "Generate", "Connection to Gemini timed out", e)
            return AIResult.Error("Gemini service temporarily unavailable hai (timeout). Please try again.", e)
        } catch (e: UnknownHostException) {
            AppLogger.warn("Gemini", "Generate", "Internet host unreachable", e)
            return AIResult.Error("Internet connection nahi hai. Please check your network connection.", e)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            val sanitizedMsg = sanitizeExceptionMessage(e.localizedMessage ?: "Unknown error", apiKey)
            AppLogger.error("Gemini", "Generate", "Network error calling Gemini: $sanitizedMsg", e)
            return AIResult.Error("Network error calling Gemini: $sanitizedMsg", e)
        } finally {
            connection?.disconnect()
        }
    }

    private fun extractSafeErrorMessage(responseCode: Int, responseText: String, apiKey: String): String {
        val rawMessage = try {
            val errJson = JSONObject(responseText)
            val errorObj = errJson.optJSONObject("error")
            val status = errorObj?.optString("status") ?: ""
            val msg = errorObj?.optString("message") ?: ""
            "$status: $msg".trim().removePrefix(":").trim()
        } catch (_: Exception) {
            ""
        }

        return when {
            responseCode == 400 && (rawMessage.contains("API_KEY_INVALID", ignoreCase = true) || rawMessage.contains("API key not valid", ignoreCase = true)) ->
                "Invalid Gemini API key. Please verify GEMINI_API_KEY in the AI Studio Secrets panel."
            responseCode in listOf(401, 403) ->
                "Unauthorized Gemini API key. Please check your GEMINI_API_KEY in the AI Studio Secrets panel."
            responseCode == 429 ->
                "Gemini API rate limit or quota exceeded. Please try again in a few moments."
            responseCode in listOf(500, 503) ->
                "Gemini service is temporarily experiencing high demand ($responseCode). Please try again."
            rawMessage.isNotBlank() ->
                "Gemini API call failed ($responseCode): ${sanitizeExceptionMessage(rawMessage, apiKey)}"
            else ->
                "Gemini API call failed with HTTP $responseCode."
        }
    }

    private fun sanitizeExceptionMessage(msg: String, apiKey: String): String {
        return if (apiKey.isNotBlank()) {
            msg.replace(apiKey, "[REDACTED]")
        } else {
            msg
        }
    }

    /**
     * Safely fetches live source page HTML text (first ~4000 characters) via HTTP GET.
     * Respects 8-second timeout, follows redirects, and strips raw HTML tags.
     */
    suspend fun fetchLiveSourcePageText(urlString: String, timeoutMs: Int = 8000): String? = withContext(Dispatchers.IO) {
        if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) return@withContext null
        var conn: HttpURLConnection? = null
        try {
            val url = URL(urlString)
            conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = timeoutMs
                readTimeout = timeoutMs
                instanceFollowRedirects = true
                requestMethod = "GET"
                setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0 Mobile Safari/537.36 SocialAgent/1.0"
                )
                setRequestProperty("Accept", "text/html,text/plain")
            }
            if (conn.responseCode == 200) {
                val rawHtml = conn.inputStream.bufferedReader().use { r ->
                    val buf = CharArray(16384)
                    val len = r.read(buf, 0, buf.size)
                    if (len > 0) String(buf, 0, len) else ""
                }
                // Strip tags and compress whitespace
                rawHtml.replace(Regex("<script[^>]*>.*?</script>", RegexOption.DOT_MATCHES_ALL), " ")
                    .replace(Regex("<style[^>]*>.*?</style>", RegexOption.DOT_MATCHES_ALL), " ")
                    .replace(Regex("<[^>]+>"), " ")
                    .replace(Regex("\\s+"), " ")
                    .trim()
                    .take(4000)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }

    /**
     * Executes multi-turn conversation with role history and optional Google Search Grounding.
     * Supports gemini-3.5-flash (general), gemini-3.1-pro-preview (complex), gemini-3.1-flash-lite-preview (fast).
     */
    suspend fun chatConversation(
        messages: List<ChatMessage>,
        systemInstruction: String = "You are SocialAgent AI Copilot, an expert social media and government scheme consultant for Assam and India.",
        model: String = "gemini-3.5-flash",
        enableGoogleSearch: Boolean = false
    ): ChatResult = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey.startsWith("YOUR_")) {
            return@withContext ChatResult.ConfigurationRequired(
                "Gemini API key is not configured. Please add GEMINI_API_KEY in the AI Studio Secrets panel."
            )
        }

        var connection: HttpURLConnection? = null
        try {
            val endpointUrl = "$BASE_URL/$model:generateContent?key=$apiKey"
            val url = URL(endpointUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 30000
                readTimeout = 60000
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("User-Agent", "SocialAgent-Android/1.0")
            }

            val requestJson = JSONObject().apply {
                // Multi-turn contents array
                val contentsArray = JSONArray()
                messages.forEach { msg ->
                    contentsArray.put(JSONObject().apply {
                        put("role", if (msg.role == "user") "user" else "model")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", msg.text)
                            })
                        })
                    })
                }
                put("contents", contentsArray)

                if (systemInstruction.isNotBlank()) {
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", systemInstruction)
                            })
                        })
                    })
                }

                // Google Search Grounding tool
                if (enableGoogleSearch) {
                    put("tools", JSONArray().apply {
                        put(JSONObject().apply {
                            put("googleSearch", JSONObject())
                        })
                    })
                }

                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 2048)
                })
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestJson.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            val responseStream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream ?: connection.inputStream
            }

            val responseText = responseStream?.let { stream ->
                BufferedReader(InputStreamReader(stream)).use { it.readText() }
            } ?: ""

            if (responseCode !in 200..299) {
                val safeErr = extractSafeErrorMessage(responseCode, responseText, apiKey)
                return@withContext ChatResult.Error(safeErr)
            }

            val root = JSONObject(responseText)
            val candidates = root.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext ChatResult.Error("Gemini returned no response.")
            }

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val textBuilder = StringBuilder()
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val p = parts.getJSONObject(i)
                    val t = p.optString("text")
                    if (t.isNotBlank()) {
                        textBuilder.append(t)
                    }
                }
            }

            val responseBody = textBuilder.toString().ifBlank { "No text content returned." }

            // Extract Google Search Grounding Metadata
            val groundingSources = mutableListOf<GroundingWebSource>()
            val groundingMetadata = firstCandidate.optJSONObject("groundingMetadata")
            if (groundingMetadata != null) {
                val groundingChunks = groundingMetadata.optJSONArray("groundingChunks")
                if (groundingChunks != null) {
                    for (i in 0 until groundingChunks.length()) {
                        val chunk = groundingChunks.optJSONObject(i)
                        val web = chunk?.optJSONObject("web")
                        if (web != null) {
                            val uri = web.optString("uri")
                            val title = web.optString("title").ifBlank { uri }
                            if (uri.isNotBlank()) {
                                groundingSources.add(GroundingWebSource(title = title, url = uri))
                            }
                        }
                    }
                }
            }

            ChatResult.Success(
                ChatMessage(
                    role = "model",
                    text = responseBody,
                    modelUsed = model,
                    groundingSources = groundingSources.distinctBy { it.url }
                )
            )
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            val sanitized = sanitizeExceptionMessage(e.localizedMessage ?: "Unknown error", apiKey)
            ChatResult.Error("Chat error: $sanitized", e)
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * Creates an image from a text prompt using gemini-3.1-flash-image-preview.
     * Supports aspect ratios: 1:1, 4:5, 16:9, 9:16.
     */
    suspend fun createImage(
        prompt: String,
        aspectRatio: String = "1:1",
        storageDir: File
    ): ImageResult = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey.startsWith("YOUR_")) {
            return@withContext ImageResult.ConfigurationRequired(
                "Gemini API key is not configured. Please add GEMINI_API_KEY in the AI Studio Secrets panel."
            )
        }

        var connection: HttpURLConnection? = null
        try {
            val model = "gemini-3.1-flash-image-preview"
            val endpointUrl = "$BASE_URL/$model:generateContent?key=$apiKey"
            val url = URL(endpointUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 45000
                readTimeout = 90000
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("User-Agent", "SocialAgent-Android/1.0")
            }

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("imageConfig", JSONObject().apply {
                        put("aspectRatio", aspectRatio)
                        put("imageSize", "1K")
                    })
                    put("responseModalities", JSONArray().apply {
                        put("TEXT")
                        put("IMAGE")
                    })
                })
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestJson.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            val responseStream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream ?: connection.inputStream
            }

            val responseText = responseStream?.let { stream ->
                BufferedReader(InputStreamReader(stream)).use { it.readText() }
            } ?: ""

            if (responseCode !in 200..299) {
                val safeErr = extractSafeErrorMessage(responseCode, responseText, apiKey)
                return@withContext ImageResult.Error(safeErr)
            }

            val root = JSONObject(responseText)
            val candidates = root.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext ImageResult.Error("No image candidates returned by Gemini.")
            }

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            var savedPath: String? = null
            var textDescription: String? = null

            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.getJSONObject(i)
                    val inlineData = part.optJSONObject("inlineData")
                    if (inlineData != null) {
                        val base64Data = inlineData.optString("data")
                        if (base64Data.isNotBlank()) {
                            val imageBytes = Base64.decode(base64Data, Base64.DEFAULT)
                            if (!storageDir.exists()) storageDir.mkdirs()
                            val imageFile = File(storageDir, "ai_image_${System.currentTimeMillis()}.png")
                            FileOutputStream(imageFile).use { fos ->
                                fos.write(imageBytes)
                                fos.flush()
                            }
                            savedPath = imageFile.absolutePath
                        }
                    } else if (part.has("text")) {
                        textDescription = part.optString("text")
                    }
                }
            }

            if (savedPath != null) {
                ImageResult.Success(savedPath, textDescription)
            } else {
                ImageResult.Error(textDescription ?: "Gemini did not return an image part. Please try rephrasing prompt.")
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            val sanitized = sanitizeExceptionMessage(e.localizedMessage ?: "Unknown error", apiKey)
            ImageResult.Error("Image generation error: $sanitized", e)
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * Edits an existing image based on text instruction using gemini-3.1-flash-image-preview.
     */
    suspend fun editImage(
        prompt: String,
        inputBitmap: Bitmap,
        aspectRatio: String = "1:1",
        storageDir: File
    ): ImageResult = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey.startsWith("YOUR_")) {
            return@withContext ImageResult.ConfigurationRequired(
                "Gemini API key is not configured. Please add GEMINI_API_KEY in the AI Studio Secrets panel."
            )
        }

        var connection: HttpURLConnection? = null
        try {
            val model = "gemini-3.1-flash-image-preview"
            val endpointUrl = "$BASE_URL/$model:generateContent?key=$apiKey"
            val url = URL(endpointUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 45000
                readTimeout = 90000
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("User-Agent", "SocialAgent-Android/1.0")
            }

            val baos = ByteArrayOutputStream()
            inputBitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos)
            val base64Input = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                            put(JSONObject().apply {
                                put("inlineData", JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Input)
                                })
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("imageConfig", JSONObject().apply {
                        put("aspectRatio", aspectRatio)
                        put("imageSize", "1K")
                    })
                    put("responseModalities", JSONArray().apply {
                        put("TEXT")
                        put("IMAGE")
                    })
                })
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestJson.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            val responseStream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream ?: connection.inputStream
            }

            val responseText = responseStream?.let { stream ->
                BufferedReader(InputStreamReader(stream)).use { it.readText() }
            } ?: ""

            if (responseCode !in 200..299) {
                val safeErr = extractSafeErrorMessage(responseCode, responseText, apiKey)
                return@withContext ImageResult.Error(safeErr)
            }

            val root = JSONObject(responseText)
            val candidates = root.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext ImageResult.Error("No edited image candidates returned by Gemini.")
            }

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            var savedPath: String? = null
            var textDescription: String? = null

            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.getJSONObject(i)
                    val inlineData = part.optJSONObject("inlineData")
                    if (inlineData != null) {
                        val base64Data = inlineData.optString("data")
                        if (base64Data.isNotBlank()) {
                            val imageBytes = Base64.decode(base64Data, Base64.DEFAULT)
                            if (!storageDir.exists()) storageDir.mkdirs()
                            val imageFile = File(storageDir, "ai_edit_${System.currentTimeMillis()}.png")
                            FileOutputStream(imageFile).use { fos ->
                                fos.write(imageBytes)
                                fos.flush()
                            }
                            savedPath = imageFile.absolutePath
                        }
                    } else if (part.has("text")) {
                        textDescription = part.optString("text")
                    }
                }
            }

            if (savedPath != null) {
                ImageResult.Success(savedPath, textDescription)
            } else {
                ImageResult.Error(textDescription ?: "Gemini did not return an edited image part. Try adjusting prompt.")
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            val sanitized = sanitizeExceptionMessage(e.localizedMessage ?: "Unknown error", apiKey)
            ImageResult.Error("Image edit error: $sanitized", e)
        } finally {
            connection?.disconnect()
        }
    }
}
