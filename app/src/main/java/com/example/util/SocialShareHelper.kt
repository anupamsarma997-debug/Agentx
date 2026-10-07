package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.local.logging.AppLogger
import java.io.File

object SocialShareHelper {

    /**
     * Copies the post text to clipboard and launches the Android Share intent
     * pre-targeted to Facebook app or Facebook mobile web.
     */
    fun shareToFacebook(
        context: Context,
        title: String,
        body: String,
        imagePath: String? = null,
        sourceUrl: String? = null
    ): Boolean {
        return try {
            val fullText = buildString {
                appendLine(title)
                appendLine()
                appendLine(body)
                if (!sourceUrl.isNullOrBlank() && !body.contains(sourceUrl)) {
                    appendLine()
                    appendLine("🔗 Official Portal: $sourceUrl")
                }
            }.trim()

            // 1. Copy text to clipboard so user can easily paste in Facebook composer
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText("Social Agent Post", fullText)
            clipboard?.setPrimaryClip(clip)
            Toast.makeText(context, "Post text clipboard par copy ho gaya!", Toast.LENGTH_SHORT).show()

            // 2. Prepare image URI if available
            val imageUri: Uri? = if (!imagePath.isNullOrBlank()) {
                val clean = imagePath.removePrefix("file://")
                val f = File(clean)
                if (f.exists() && f.length() > 0) {
                    try {
                        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", f)
                    } catch (e: Exception) {
                        AppLogger.warn("Share", "FileProvider", "FileProvider error: ${e.localizedMessage}")
                        null
                    }
                } else null
            } else null

            // 3. Build Intent
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                if (imageUri != null) {
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, imageUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } else {
                    type = "text/plain"
                }
                putExtra(Intent.EXTRA_TEXT, fullText)
                putExtra(Intent.EXTRA_SUBJECT, title)
            }

            // Check if Facebook app is installed
            val packageManager = context.packageManager
            val fbPackage = when {
                isPackageInstalled("com.facebook.katana", packageManager) -> "com.facebook.katana"
                isPackageInstalled("com.facebook.lite", packageManager) -> "com.facebook.lite"
                else -> null
            }

            if (fbPackage != null) {
                shareIntent.setPackage(fbPackage)
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(shareIntent)
                Toast.makeText(context, "Facebook app khul raha hai...", Toast.LENGTH_SHORT).show()
            } else {
                // Fallback to System Chooser
                val chooser = Intent.createChooser(shareIntent, "Facebook ya Any App par Post karein").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            }
            true
        } catch (e: Exception) {
            AppLogger.error("Share", "Facebook", "Share error: ${e.localizedMessage}")
            Toast.makeText(context, "Post text copy ho gaya! Facebook browser par paste karein.", Toast.LENGTH_LONG).show()
            // Fallback to web Facebook
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://m.facebook.com")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            } catch (_: Exception) {}
            true
        }
    }

    private fun isPackageInstalled(packageName: String, packageManager: android.content.pm.PackageManager): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
}
