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
import java.net.URL

open class GeminiClient(
    private val modelName: String = "gemini-2.5-flash",
    private val apiKeyProvider: () -> String = { resolveApiKey() }
) {

    companion object {
        const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

        fun resolveApiKey(): String {
            // Safely resolve from BuildConfig reflection or environment without compilation errors
            val fromBuildConfig = try {
                val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
                field.get(null) as? String
            } catch (_: Exception) {
                null
            }
            if (!fromBuildConfig.isNullOrBlank() && fromBuildConfig != "MY_GEMINI_API_KEY") {
                return fromBuildConfig.trim()
            }

            val fromEnv = System.getenv("GEMINI_API_KEY")
            if (!fromEnv.isNullOrBlank() && fromEnv != "MY_GEMINI_API_KEY") {
                return fromEnv.trim()
            }

            return ""
        }
    }

    open suspend fun generateContent(systemPrompt: String, userPrompt: String): AIResult = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext AIResult.ConfigurationRequired(
                "Gemini API key is not configured. Please add GEMINI_API_KEY in the AI Studio Secrets panel."
            )
        }

        var connection: HttpURLConnection? = null
        try {
            val endpointUrl = "$BASE_URL/$modelName:generateContent?key=$apiKey"
            val url = URL(endpointUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 12000
                readTimeout = 20000
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("User-Agent", "SocialAgent-Android/1.0")
            }

            // Construct payload with JSON structured output configuration
            val requestJson = JSONObject().apply {
                // Contents
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", userPrompt)
                            })
                        })
                    })
                })
                // System Instruction
                if (systemPrompt.isNotBlank()) {
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", systemPrompt)
                            })
                        })
                    })
                }
                // Generation Config
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
                connection.errorStream
            }

            val responseText = BufferedReader(InputStreamReader(responseStream)).use { it.readText() }

            if (responseCode !in 200..299) {
                val safeError = try {
                    val errJson = JSONObject(responseText)
                    val errorObj = errJson.optJSONObject("error")
                    errorObj?.optString("message") ?: "API Error ($responseCode)"
                } catch (_: Exception) {
                    "API Error ($responseCode)"
                }
                return@withContext AIResult.Error("Gemini API call failed: $safeError")
            }

            // Parse candidates[0].content.parts[0].text
            val root = JSONObject(responseText)
            val candidates = root.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext AIResult.Error("Gemini returned no candidates.")
            }

            val content = candidates.getJSONObject(0).optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (text.isNullOrBlank()) {
                return@withContext AIResult.Error("Gemini candidate contained empty text.")
            }

            AIResult.Success(text)
        } catch (e: Exception) {
            AIResult.Error("Network error calling Gemini API: ${e.localizedMessage ?: "Unknown error"}", e)
        } finally {
            connection?.disconnect()
        }
    }
}
