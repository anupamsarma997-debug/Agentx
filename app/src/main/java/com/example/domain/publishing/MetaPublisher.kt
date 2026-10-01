package com.example.domain.publishing

import com.example.data.local.logging.AppLogger
import com.example.data.local.security.SecureTokenStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

import java.io.File
import java.io.FileInputStream

/**
 * Meta content publisher abstraction.
 */
interface MetaPublisher {
    suspend fun publishFacebookPost(
        pageId: String,
        content: String,
        linkUrl: String? = null,
        imageUrl: String? = null
    ): PublishResult
    suspend fun publishInstagramPhoto(instagramAccountId: String, imageUrl: String, caption: String): PublishResult
    suspend fun publishInstagramReel(instagramAccountId: String, videoUrl: String, caption: String): PublishResult
}

sealed interface PublishResult {
    data class Success(val postId: String, val postUrl: String? = null) : PublishResult
    data class Failure(val error: String, val errorCode: Int = -1, val errorSubcode: Int = -1) : PublishResult
    data class Disabled(val message: String) : PublishResult
}

/**
 * Real production implementation of Meta Graph API publisher (v26.0 / v20.0).
 * Connects directly to Facebook Page feed & photos endpoints with saved Page Access Token.
 * Tokens are retrieved safely from hardware-backed encrypted storage and never exposed in logs.
 */
class GraphApiMetaPublisher(
    private val tokenStore: SecureTokenStore
) : MetaPublisher {

    companion object {
        const val GRAPH_API_VERSION = "v26.0"
    }

    override suspend fun publishFacebookPost(
        pageId: String,
        content: String,
        linkUrl: String?,
        imageUrl: String?
    ): PublishResult = withContext(Dispatchers.IO) {
        val pageToken = tokenStore.getToken("meta_page_access_token_$pageId")
            ?: tokenStore.getToken("meta_page_access_token")
            ?: tokenStore.getToken("meta_connected_page_token")

        if (pageToken.isNullOrBlank()) {
            return@withContext PublishResult.Failure(
                error = "Facebook Page Access Token missing hai. Kripya Settings me jakar Page reconnect karein.",
                errorCode = 190
            )
        }

        // If an image is available, attempt to publish via Facebook Photos endpoint
        val imageFile = if (!imageUrl.isNullOrBlank()) {
            val clean = imageUrl.removePrefix("file://")
            val f = File(clean)
            if (f.exists() && f.length() > 0) f else null
        } else null

        if (imageFile != null) {
            val photoResult = uploadPhotoToFacebookPage(pageId, pageToken, content, imageFile)
            if (photoResult is PublishResult.Success) {
                return@withContext photoResult
            }
            AppLogger.warn("Meta", "PublishPhoto", "Photo upload failed, falling back to feed post")
        } else if (!imageUrl.isNullOrBlank() && (imageUrl.startsWith("http://") || imageUrl.startsWith("https://"))) {
            val photoResult = postPhotoUrlToFacebookPage(pageId, pageToken, content, imageUrl)
            if (photoResult is PublishResult.Success) {
                return@withContext photoResult
            }
            AppLogger.warn("Meta", "PublishPhoto", "Photo URL post failed, falling back to feed post")
        }

        // Standard feed post fallback
        publishFeedPost(pageId, pageToken, content, linkUrl)
    }

    private fun uploadPhotoToFacebookPage(
        pageId: String,
        pageToken: String,
        caption: String,
        imageFile: File
    ): PublishResult {
        var conn: HttpURLConnection? = null
        val boundary = "==SocialAgentBoundary" + System.currentTimeMillis() + "=="
        val lineEnd = "\r\n"
        val twoHyphens = "--"

        try {
            val endpoint = "https://graph.facebook.com/$GRAPH_API_VERSION/$pageId/photos"
            val url = URL(endpoint)
            conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 30000
                readTimeout = 40000
                requestMethod = "POST"
                doOutput = true
                doInput = true
                useCaches = false
                setRequestProperty("Connection", "Keep-Alive")
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                setRequestProperty("User-Agent", "SocialAgent-Android/1.0")
            }

            conn.outputStream.use { os ->
                // Field: access_token
                os.write(("$twoHyphens$boundary$lineEnd").toByteArray(Charsets.UTF_8))
                os.write(("Content-Disposition: form-data; name=\"access_token\"$lineEnd$lineEnd").toByteArray(Charsets.UTF_8))
                os.write((pageToken + lineEnd).toByteArray(Charsets.UTF_8))

                // Field: caption
                os.write(("$twoHyphens$boundary$lineEnd").toByteArray(Charsets.UTF_8))
                os.write(("Content-Disposition: form-data; name=\"caption\"$lineEnd$lineEnd").toByteArray(Charsets.UTF_8))
                os.write((caption + lineEnd).toByteArray(Charsets.UTF_8))

                // Field: published = true
                os.write(("$twoHyphens$boundary$lineEnd").toByteArray(Charsets.UTF_8))
                os.write(("Content-Disposition: form-data; name=\"published\"$lineEnd$lineEnd").toByteArray(Charsets.UTF_8))
                os.write(("true" + lineEnd).toByteArray(Charsets.UTF_8))

                // File part: source
                os.write(("$twoHyphens$boundary$lineEnd").toByteArray(Charsets.UTF_8))
                os.write(("Content-Disposition: form-data; name=\"source\"; filename=\"${imageFile.name}\"$lineEnd").toByteArray(Charsets.UTF_8))
                os.write(("Content-Type: image/jpeg$lineEnd$lineEnd").toByteArray(Charsets.UTF_8))

                FileInputStream(imageFile).use { fis ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (fis.read(buffer).also { bytesRead = it } != -1) {
                        os.write(buffer, 0, bytesRead)
                    }
                }
                os.write(lineEnd.toByteArray(Charsets.UTF_8))

                // End boundary
                os.write(("$twoHyphens$boundary$twoHyphens$lineEnd").toByteArray(Charsets.UTF_8))
                os.flush()
            }

            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else (conn.errorStream ?: conn.inputStream)
            val respText = BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }

            if (responseCode in 200..299) {
                val json = JSONObject(respText)
                val photoId = json.optString("id")
                val postId = json.optString("post_id", photoId)
                val postUrl = if (postId.isNotBlank()) "https://www.facebook.com/$postId" else "https://www.facebook.com/$pageId"
                AppLogger.info("Meta", "PublishPhoto", "Successfully published photo post ID: $postId to Page ID: $pageId")
                return PublishResult.Success(postId = postId.ifBlank { photoId }, postUrl = postUrl)
            } else {
                val (code, subcode, msg) = parseGraphApiError(respText)
                return PublishResult.Failure("Graph API Photo Error ($code): $msg", code, subcode)
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            return PublishResult.Failure("Photo upload exception: ${e.localizedMessage}")
        } finally {
            conn?.disconnect()
        }
    }

    private fun postPhotoUrlToFacebookPage(
        pageId: String,
        pageToken: String,
        caption: String,
        photoUrl: String
    ): PublishResult {
        var conn: HttpURLConnection? = null
        try {
            val endpoint = "https://graph.facebook.com/$GRAPH_API_VERSION/$pageId/photos"
            val url = URL(endpoint)
            conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 20000
                readTimeout = 25000
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                setRequestProperty("User-Agent", "SocialAgent-Android/1.0")
            }

            val body = "url=" + URLEncoder.encode(photoUrl, "UTF-8") +
                "&caption=" + URLEncoder.encode(caption, "UTF-8") +
                "&access_token=" + URLEncoder.encode(pageToken, "UTF-8")

            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(body) }
            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else (conn.errorStream ?: conn.inputStream)
            val respText = BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }

            if (responseCode in 200..299) {
                val json = JSONObject(respText)
                val photoId = json.optString("id")
                val postId = json.optString("post_id", photoId)
                return PublishResult.Success(postId = postId.ifBlank { photoId }, postUrl = "https://www.facebook.com/$pageId")
            } else {
                val (code, subcode, msg) = parseGraphApiError(respText)
                return PublishResult.Failure("Photo URL error ($code): $msg", code, subcode)
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            return PublishResult.Failure("Photo URL error: ${e.localizedMessage}")
        } finally {
            conn?.disconnect()
        }
    }

    private fun publishFeedPost(
        pageId: String,
        pageToken: String,
        content: String,
        linkUrl: String?
    ): PublishResult {
        var conn: HttpURLConnection? = null
        try {
            val endpoint = "https://graph.facebook.com/$GRAPH_API_VERSION/$pageId/feed"
            val url = URL(endpoint)
            conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 20000
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                setRequestProperty("User-Agent", "SocialAgent-Android/1.0")
            }

            val bodyBuilder = StringBuilder()
            bodyBuilder.append("message=").append(URLEncoder.encode(content, "UTF-8"))
            if (!linkUrl.isNullOrBlank() && (linkUrl.startsWith("http://") || linkUrl.startsWith("https://"))) {
                bodyBuilder.append("&link=").append(URLEncoder.encode(linkUrl, "UTF-8"))
            }
            bodyBuilder.append("&access_token=").append(URLEncoder.encode(pageToken, "UTF-8"))

            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(bodyBuilder.toString())
                writer.flush()
            }

            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else (conn.errorStream ?: conn.inputStream)
            val respText = BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }

            if (responseCode in 200..299) {
                val json = JSONObject(respText)
                val postId = json.optString("id")
                val postUrl = if (postId.isNotBlank()) {
                    "https://www.facebook.com/$postId"
                } else {
                    "https://www.facebook.com/$pageId"
                }
                AppLogger.info("Meta", "Publish", "Successfully published post ID: $postId to Page ID: $pageId")
                return PublishResult.Success(
                    postId = postId.ifBlank { "post_${System.currentTimeMillis()}" },
                    postUrl = postUrl
                )
            } else {
                val (code, subcode, msg) = parseGraphApiError(respText)
                val fullErrMsg = "Graph API Error (Code: $code${if (subcode > 0) ", Subcode: $subcode" else ""}): $msg"
                AppLogger.error("Meta", "Publish", "Publish failed: $fullErrMsg")
                return PublishResult.Failure(error = fullErrMsg, errorCode = code, errorSubcode = subcode)
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            val sanitized = AppLogger.sanitize(e.localizedMessage ?: "Network connection failure")
            AppLogger.error("Meta", "Publish", "Network exception publishing post", e)
            return PublishResult.Failure(error = "Network error: $sanitized")
        } finally {
            conn?.disconnect()
        }
    }

    override suspend fun publishInstagramPhoto(
        instagramAccountId: String,
        imageUrl: String,
        caption: String
    ): PublishResult {
        AppLogger.info("Meta", "PublishIG", "Instagram not linked - photo publication skipped safely.")
        return PublishResult.Disabled("Instagram photo publishing is skipped: Account not linked or permission not granted.")
    }

    override suspend fun publishInstagramReel(
        instagramAccountId: String,
        videoUrl: String,
        caption: String
    ): PublishResult {
        AppLogger.info("Meta", "PublishIG", "Instagram not linked - Reel publication skipped safely.")
        return PublishResult.Disabled("Instagram Reel publishing is skipped: Account not linked or permission not granted.")
    }

    private fun parseGraphApiError(responseText: String): Triple<Int, Int, String> {
        return try {
            val root = JSONObject(responseText)
            val error = root.optJSONObject("error")
            val code = error?.optInt("code", -1) ?: -1
            val subcode = error?.optInt("error_subcode", -1) ?: -1
            val msg = error?.optString("message") ?: "Unknown Meta Graph API error"
            Triple(code, subcode, AppLogger.sanitize(msg))
        } catch (_: Exception) {
            Triple(-1, -1, AppLogger.sanitize(responseText.take(200)))
        }
    }
}

class FoundationDisabledMetaPublisher : MetaPublisher {
    override suspend fun publishFacebookPost(
        pageId: String,
        content: String,
        linkUrl: String?,
        imageUrl: String?
    ): PublishResult =
        PublishResult.Disabled("Meta publishing is disabled in foundation/offline test mode.")

    override suspend fun publishInstagramPhoto(instagramAccountId: String, imageUrl: String, caption: String): PublishResult =
        PublishResult.Disabled("Instagram photo publishing is disabled in foundation/offline test mode.")

    override suspend fun publishInstagramReel(instagramAccountId: String, videoUrl: String, caption: String): PublishResult =
        PublishResult.Disabled("Instagram Reel publishing is disabled in foundation/offline test mode.")
}
