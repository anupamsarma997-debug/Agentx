package com.example.data.remote.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
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
    private val modelName: String = "gemini-3.5-flash",
    private val apiKeyProvider: () -> String = { resolveApiKey() }
) {

    companion object {
        const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
        val FALLBACK_MODELS = listOf("gemini-3.5-flash", "gemini-3-flash-preview", "gemini-3.1-flash-lite-preview")

        fun resolveApiKey(): String {
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
            return AIResult.Error("Connection to Gemini timed out. Please check your internet connection.", e)
        } catch (e: UnknownHostException) {
            return AIResult.Error("Unable to reach Gemini servers. Please check your network connection.", e)
        } catch (e: Exception) {
            val sanitizedMsg = sanitizeExceptionMessage(e.localizedMessage ?: "Unknown error", apiKey)
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
}
