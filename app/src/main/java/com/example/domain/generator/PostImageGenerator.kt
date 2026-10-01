package com.example.domain.generator

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.example.data.local.logging.AppLogger
import java.io.File
import java.io.FileOutputStream

/**
 * Generates crisp, modern 1080x1080 social media announcement banners for opportunities.
 * Formatted perfectly for Facebook and Instagram feeds with high-contrast typography,
 * category badges, organization branding, deadline alerts, and official verification markers.
 */
class PostImageGenerator(private val context: Context) {

    fun generatePostBanner(
        contentId: String,
        title: String,
        category: String,
        organization: String?,
        deadline: String?,
        sourceUrl: String,
        region: String? = null
    ): String? {
        return try {
            val width = 1080
            val height = 1080
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Select color palette based on category
            val (gradStart, gradEnd, accentColor) = when {
                category.contains("Job", ignoreCase = true) ||
                category.contains("Career", ignoreCase = true) ||
                category.contains("Recruitment", ignoreCase = true) ->
                    Triple(0xFF0F172A.toInt(), 0xFF1E3A8A.toInt(), 0xFF38BDF8.toInt()) // Deep Navy to Royal Blue with Cyan accent

                category.contains("Scholarship", ignoreCase = true) ||
                category.contains("Education", ignoreCase = true) ||
                category.contains("Fellowship", ignoreCase = true) ||
                category.contains("Student", ignoreCase = true) ->
                    Triple(0xFF064E3B.toInt(), 0xFF0F766E.toInt(), 0xFF34D399.toInt()) // Deep Emerald to Teal with Mint accent

                category.contains("Scheme", ignoreCase = true) ||
                category.contains("Government", ignoreCase = true) ||
                category.contains("Yojana", ignoreCase = true) ||
                category.contains("Welfare", ignoreCase = true) ->
                    Triple(0xFF3B0764.toInt(), 0xFF581C87.toInt(), 0xFFFBBF24.toInt()) // Deep Purple to Violet with Amber/Gold accent

                else ->
                    Triple(0xFF1E1B4B.toInt(), 0xFF312E81.toInt(), 0xFF60A5FA.toInt()) // Deep Indigo with Vibrant Sky Blue accent
            }

            // 1. Draw rich background gradient
            val bgPaint = Paint().apply {
                shader = LinearGradient(0f, 0f, 0f, height.toFloat(), gradStart, gradEnd, Shader.TileMode.CLAMP)
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // 2. Decorative background geometric circles
            val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                alpha = 14
            }
            canvas.drawCircle(width * 0.9f, height * 0.12f, 320f, circlePaint)
            canvas.drawCircle(width * 0.1f, height * 0.88f, 260f, circlePaint)

            // 3. Inner Content Card Container
            val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                alpha = 24
            }
            val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                alpha = 50
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }
            val cardRect = RectF(60f, 60f, width - 60f, height - 60f)
            canvas.drawRoundRect(cardRect, 36f, 36f, cardPaint)
            canvas.drawRoundRect(cardRect, 36f, 36f, cardBorderPaint)

            // 4. Category & Verification Pill
            val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = accentColor
            }
            val catText = (if (category.isNotBlank()) category else "OFFICIAL OPPORTUNITY").uppercase()
            val catPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                textSize = 28f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val catWidth = catPaint.measureText(catText)
            val pillRect = RectF(100f, 100f, 100f + catWidth + 48f, 154f)
            canvas.drawRoundRect(pillRect, 27f, 27f, pillPaint)
            canvas.drawText(catText, 124f, 138f, catPaint)

            // Official Verified Badge on right
            val verifiedText = "✓ VERIFIED SOURCE"
            val vPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 26f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                alpha = 220
            }
            val vWidth = vPaint.measureText(verifiedText)
            canvas.drawText(verifiedText, width - 100f - vWidth, 136f, vPaint)

            // 5. Title (Bold, Multi-line with StaticLayout)
            val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 50f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                setShadowLayer(8f, 0f, 4f, 0x88000000.toInt())
            }
            val maxTitleWidth = width - 200
            val cleanTitle = if (title.length > 120) title.take(117) + "..." else title
            val titleLayout = StaticLayout.Builder.obtain(cleanTitle, 0, cleanTitle.length, titlePaint, maxTitleWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(8f, 1f)
                .setMaxLines(4)
                .build()

            canvas.save()
            canvas.translate(100f, 195f)
            titleLayout.draw(canvas)
            canvas.restore()

            val titleBottomY = 195f + titleLayout.height + 25f

            // 6. Organization Banner
            val orgText = organization ?: "Official Announcement"
            val orgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = accentColor
                textSize = 34f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val cleanOrg = if (orgText.length > 40) orgText.take(38) + "..." else orgText
            canvas.drawText("🏛  $cleanOrg", 100f, titleBottomY + 20f, orgPaint)

            // 7. Key Information Card / Grid
            val infoY = titleBottomY + 65f
            val infoBoxRect = RectF(100f, infoY, width - 100f, infoY + 210f)
            val infoBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0x22000000
            }
            val infoBoxBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                alpha = 30
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            canvas.drawRoundRect(infoBoxRect, 20f, 20f, infoBoxPaint)
            canvas.drawRoundRect(infoBoxRect, 20f, 20f, infoBoxBorder)

            val infoLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                alpha = 180
                textSize = 26f
            }
            val infoValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 32f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            // Row 1: Region / Scope
            val locText = if (!region.isNullOrBlank()) region else "All Eligible Candidates"
            val cleanLoc = if (locText.length > 42) locText.take(40) + "..." else locText
            canvas.drawText("Eligibility / Region:", 130f, infoY + 50f, infoLabelPaint)
            canvas.drawText("📍 $cleanLoc", 130f, infoY + 90f, infoValuePaint)

            // Row 2: Deadline
            val dText = if (!deadline.isNullOrBlank()) deadline else "Check Official Portal"
            val cleanDead = if (dText.length > 40) dText.take(38) + "..." else dText
            canvas.drawText("Last Date to Apply:", 130f, infoY + 145f, infoLabelPaint)
            val deadlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (!deadline.isNullOrBlank()) 0xFFFBBF24.toInt() else Color.WHITE
                textSize = 32f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("⏳ $cleanDead", 130f, infoY + 185f, deadlinePaint)

            // 8. Call to Action Banner at bottom of inner card
            val ctaY = height - 210f
            val ctaRect = RectF(100f, ctaY, width - 100f, ctaY + 76f)
            val ctaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = accentColor
            }
            canvas.drawRoundRect(ctaRect, 18f, 18f, ctaPaint)

            val ctaText = "APPLY VIA OFFICIAL PORTAL ➔"
            val ctaTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                textSize = 30f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val ctaTextWidth = ctaTextPaint.measureText(ctaText)
            canvas.drawText(ctaText, (width - ctaTextWidth) / 2f, ctaY + 48f, ctaTextPaint)

            // 9. Official URL & Footer Watermark
            val cleanUrl = if (sourceUrl.length > 55) sourceUrl.take(52) + "..." else sourceUrl
            val urlPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                alpha = 160
                textSize = 22f
            }
            val urlWidth = urlPaint.measureText(cleanUrl)
            canvas.drawText(cleanUrl, (width - urlWidth) / 2f, height - 90f, urlPaint)

            // Save bitmap to app private storage
            val imagesDir = File(context.filesDir, "post_images").apply { mkdirs() }
            val imageFile = File(imagesDir, "post_${contentId}.jpg")
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
                out.flush()
            }
            bitmap.recycle()

            AppLogger.info("PostImage", "Generate", "Generated post banner image at ${imageFile.absolutePath}")
            imageFile.absolutePath
        } catch (e: Exception) {
            AppLogger.error("PostImage", "Generate", "Failed to generate post banner image", e)
            null
        }
    }
}
