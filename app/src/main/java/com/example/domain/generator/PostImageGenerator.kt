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
 * Generates crisp, modern social media announcement banners and meme cards in multiple sizes:
 * - Square (1:1 - 1080x1080) for Instagram & Facebook feeds
 * - Portrait (4:5 - 1080x1350) for maximum mobile feed impact
 * - Landscape (16:9 - 1200x675) for Facebook link preview & desktop banners
 * - Story / Reel (9:16 - 1080x1920) for Instagram & Facebook Stories and vertical status
 */
class PostImageGenerator(private val context: Context) {

    fun generatePostBanner(
        contentId: String,
        title: String,
        category: String,
        organization: String?,
        deadline: String?,
        sourceUrl: String,
        region: String? = null,
        size: PostImageSize = PostImageSize.SQUARE
    ): String? {
        return try {
            val width = size.width
            val height = size.height
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

            // 1. Background gradient
            val bgPaint = Paint().apply {
                shader = LinearGradient(0f, 0f, 0f, height.toFloat(), gradStart, gradEnd, Shader.TileMode.CLAMP)
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // 2. Decorative background geometric circles
            val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                alpha = 14
            }
            canvas.drawCircle(width * 0.9f, height * 0.12f, width * 0.3f, circlePaint)
            canvas.drawCircle(width * 0.1f, height * 0.88f, width * 0.25f, circlePaint)

            when (size) {
                PostImageSize.LANDSCAPE -> drawLandscapeLayout(
                    canvas, width, height, title, category, organization, deadline, sourceUrl, region, accentColor
                )
                PostImageSize.PORTRAIT -> drawPortraitLayout(
                    canvas, width, height, title, category, organization, deadline, sourceUrl, region, accentColor
                )
                PostImageSize.STORY -> drawStoryLayout(
                    canvas, width, height, title, category, organization, deadline, sourceUrl, region, accentColor
                )
                PostImageSize.SQUARE -> drawSquareLayout(
                    canvas, width, height, title, category, organization, deadline, sourceUrl, region, accentColor
                )
            }

            // Save bitmap to app private storage
            val imagesDir = File(context.filesDir, "post_images").apply { mkdirs() }
            val imageFile = File(imagesDir, "post_${contentId}_${size.id}.jpg")
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
                out.flush()
            }
            bitmap.recycle()

            AppLogger.info("PostImage", "Generate", "Generated ${size.displayName} (${width}x${height}) banner at ${imageFile.absolutePath}")
            imageFile.absolutePath
        } catch (e: Exception) {
            AppLogger.error("PostImage", "Generate", "Failed to generate post banner image", e)
            null
        }
    }

    private fun drawSquareLayout(
        canvas: Canvas,
        width: Int,
        height: Int,
        title: String,
        category: String,
        organization: String?,
        deadline: String?,
        sourceUrl: String,
        region: String?,
        accentColor: Int
    ) {
        val cardRect = RectF(60f, 60f, width - 60f, height - 60f)
        drawCardContainer(canvas, cardRect)

        // Category & Verification Pill
        val catText = (if (category.isNotBlank()) category else "OFFICIAL OPPORTUNITY").uppercase()
        val catPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val catWidth = catPaint.measureText(catText)
        val pillRect = RectF(100f, 100f, 100f + catWidth + 48f, 154f)
        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accentColor }
        canvas.drawRoundRect(pillRect, 27f, 27f, pillPaint)
        canvas.drawText(catText, 124f, 138f, catPaint)

        // Verified Badge
        val verifiedText = "✓ VERIFIED SOURCE"
        val vPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            alpha = 220
        }
        val vWidth = vPaint.measureText(verifiedText)
        canvas.drawText(verifiedText, width - 100f - vWidth, 136f, vPaint)

        // Title
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

        // Organization
        val orgText = organization ?: "Official Announcement"
        val orgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val cleanOrg = if (orgText.length > 40) orgText.take(38) + "..." else orgText
        canvas.drawText("🏛  $cleanOrg", 100f, titleBottomY + 20f, orgPaint)

        // Info Card
        val infoY = titleBottomY + 65f
        drawInfoBox(canvas, 100f, infoY, width - 100f, infoY + 210f, region, deadline)

        // CTA Banner
        val ctaY = height - 210f
        drawCtaBanner(canvas, 100f, ctaY, width - 100f, ctaY + 76f, accentColor, "APPLY VIA OFFICIAL PORTAL ➔")

        // Footer URL
        drawFooterUrl(canvas, width, height - 90f, sourceUrl)
    }

    private fun drawPortraitLayout(
        canvas: Canvas,
        width: Int,
        height: Int,
        title: String,
        category: String,
        organization: String?,
        deadline: String?,
        sourceUrl: String,
        region: String?,
        accentColor: Int
    ) {
        val cardRect = RectF(60f, 60f, width - 60f, height - 60f)
        drawCardContainer(canvas, cardRect)

        // Category & Verification Pill
        val catText = (if (category.isNotBlank()) category else "OFFICIAL OPPORTUNITY").uppercase()
        val catPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val catWidth = catPaint.measureText(catText)
        val pillRect = RectF(100f, 110f, 100f + catWidth + 52f, 170f)
        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accentColor }
        canvas.drawRoundRect(pillRect, 30f, 30f, pillPaint)
        canvas.drawText(catText, 126f, 152f, catPaint)

        // Verified Badge
        val verifiedText = "✓ VERIFIED OFFICIAL"
        val vPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            alpha = 230
        }
        val vWidth = vPaint.measureText(verifiedText)
        canvas.drawText(verifiedText, width - 100f - vWidth, 150f, vPaint)

        // Title (Spacious, bold)
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 54f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setShadowLayer(8f, 0f, 4f, 0x88000000.toInt())
        }
        val maxTitleWidth = width - 200
        val cleanTitle = if (title.length > 140) title.take(137) + "..." else title
        val titleLayout = StaticLayout.Builder.obtain(cleanTitle, 0, cleanTitle.length, titlePaint, maxTitleWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(10f, 1f)
            .setMaxLines(5)
            .build()

        canvas.save()
        canvas.translate(100f, 215f)
        titleLayout.draw(canvas)
        canvas.restore()

        val titleBottomY = 215f + titleLayout.height + 35f

        // Organization
        val orgText = organization ?: "Official Announcement"
        val orgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = 36f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val cleanOrg = if (orgText.length > 42) orgText.take(40) + "..." else orgText
        canvas.drawText("🏛  $cleanOrg", 100f, titleBottomY + 20f, orgPaint)

        // Large Info Card
        val infoY = titleBottomY + 80f
        drawInfoBox(canvas, 100f, infoY, width - 100f, infoY + 250f, region, deadline)

        // Extra Value Stamp
        val stampPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            alpha = 190
            textSize = 28f
        }
        canvas.drawText("📢 Verified by SocialAgent Factual Pipeline", 100f, infoY + 310f, stampPaint)

        // CTA Banner
        val ctaY = height - 230f
        drawCtaBanner(canvas, 100f, ctaY, width - 100f, ctaY + 84f, accentColor, "APPLY NOW - OFFICIAL LINK ➔")

        // Footer URL
        drawFooterUrl(canvas, width, height - 90f, sourceUrl)
    }

    private fun drawLandscapeLayout(
        canvas: Canvas,
        width: Int,
        height: Int,
        title: String,
        category: String,
        organization: String?,
        deadline: String?,
        sourceUrl: String,
        region: String?,
        accentColor: Int
    ) {
        val cardRect = RectF(40f, 30f, width - 40f, height - 30f)
        drawCardContainer(canvas, cardRect)

        // Category Pill
        val catText = (if (category.isNotBlank()) category else "OFFICIAL OPPORTUNITY").uppercase()
        val catPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val catWidth = catPaint.measureText(catText)
        val pillRect = RectF(70f, 60f, 70f + catWidth + 40f, 106f)
        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accentColor }
        canvas.drawRoundRect(pillRect, 23f, 23f, pillPaint)
        canvas.drawText(catText, 90f, 92f, catPaint)

        // Verified Badge
        val verifiedText = "✓ VERIFIED OFFICIAL PORTAL"
        val vPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            alpha = 220
        }
        canvas.drawText(verifiedText, 70f + catWidth + 60f, 92f, vPaint)

        // Left Column: Title & Org (Width ~ 620)
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 42f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setShadowLayer(6f, 0f, 3f, 0x88000000.toInt())
        }
        val cleanTitle = if (title.length > 95) title.take(92) + "..." else title
        val titleLayout = StaticLayout.Builder.obtain(cleanTitle, 0, cleanTitle.length, titlePaint, 620)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(6f, 1f)
            .setMaxLines(3)
            .build()

        canvas.save()
        canvas.translate(70f, 130f)
        titleLayout.draw(canvas)
        canvas.restore()

        val orgText = organization ?: "Official Announcement"
        val orgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val cleanOrg = if (orgText.length > 40) orgText.take(38) + "..." else orgText
        canvas.drawText("🏛  $cleanOrg", 70f, 130f + titleLayout.height + 35f, orgPaint)

        // Right Column: Info Box & CTA
        val rightX = 720f
        val rightWidth = width - 70f
        val infoY = 70f
        drawInfoBox(canvas, rightX, infoY, rightWidth, infoY + 280f, region, deadline)

        // CTA Button in Right Column
        val ctaY = infoY + 310f
        drawCtaBanner(canvas, rightX, ctaY, rightWidth, ctaY + 70f, accentColor, "APPLY ON OFFICIAL PORTAL ➔")

        // Bottom footer
        drawFooterUrl(canvas, width, height - 50f, sourceUrl)
    }

    private fun drawStoryLayout(
        canvas: Canvas,
        width: Int,
        height: Int,
        title: String,
        category: String,
        organization: String?,
        deadline: String?,
        sourceUrl: String,
        region: String?,
        accentColor: Int
    ) {
        val cardRect = RectF(50f, 100f, width - 50f, height - 100f)
        drawCardContainer(canvas, cardRect)

        // Story Tag Pill
        val tagText = "⚡ NEW OPPORTUNITY ALERT"
        val tagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val tagWidth = tagPaint.measureText(tagText)
        val tagRect = RectF(90f, 160f, 90f + tagWidth + 48f, 216f)
        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accentColor }
        canvas.drawRoundRect(tagRect, 28f, 28f, pillPaint)
        canvas.drawText(tagText, 114f, 198f, tagPaint)

        // Category Badge
        val catText = (if (category.isNotBlank()) category else "OFFICIAL NOTICE").uppercase()
        val catPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            alpha = 240
        }
        canvas.drawText("📌 $catText", 90f, 290f, catPaint)

        // Title
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 58f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setShadowLayer(10f, 0f, 5f, 0x99000000.toInt())
        }
        val cleanTitle = if (title.length > 150) title.take(147) + "..." else title
        val titleLayout = StaticLayout.Builder.obtain(cleanTitle, 0, cleanTitle.length, titlePaint, width - 180)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(12f, 1f)
            .setMaxLines(6)
            .build()

        canvas.save()
        canvas.translate(90f, 350f)
        titleLayout.draw(canvas)
        canvas.restore()

        val titleBottomY = 350f + titleLayout.height + 45f

        // Organization
        val orgText = organization ?: "Official Announcement"
        val orgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = 40f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val cleanOrg = if (orgText.length > 36) orgText.take(34) + "..." else orgText
        canvas.drawText("🏛  $cleanOrg", 90f, titleBottomY + 20f, orgPaint)

        // Info Card
        val infoY = titleBottomY + 80f
        drawInfoBox(canvas, 90f, infoY, width - 90f, infoY + 300f, region, deadline)

        // Big CTA Button for Stories
        val ctaY = height - 340f
        drawCtaBanner(canvas, 90f, ctaY, width - 90f, ctaY + 96f, accentColor, "TAP LINK TO APPLY ➔")

        // Story Swipe / Link Notice
        val swipeText = "Official Verified Source Link Available in Bio / Comments"
        val swipePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            alpha = 200
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val swWidth = swipePaint.measureText(swipeText)
        canvas.drawText(swipeText, (width - swWidth) / 2f, height - 200f, swipePaint)

        // Footer URL
        drawFooterUrl(canvas, width, height - 140f, sourceUrl)
    }

    private fun drawCardContainer(canvas: Canvas, rect: RectF) {
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
        canvas.drawRoundRect(rect, 36f, 36f, cardPaint)
        canvas.drawRoundRect(rect, 36f, 36f, cardBorderPaint)
    }

    private fun drawInfoBox(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        region: String?,
        deadline: String?
    ) {
        val rect = RectF(left, top, right, bottom)
        val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x28000000 }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            alpha = 35
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(rect, 20f, 20f, boxPaint)
        canvas.drawRoundRect(rect, 20f, 20f, borderPaint)

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            alpha = 180
            textSize = 26f
        }
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val locText = if (!region.isNullOrBlank()) region else "All Eligible Candidates"
        val cleanLoc = if (locText.length > 40) locText.take(38) + "..." else locText
        canvas.drawText("Eligibility / Region:", left + 30f, top + 52f, labelPaint)
        canvas.drawText("📍 $cleanLoc", left + 30f, top + 94f, valuePaint)

        val dText = if (!deadline.isNullOrBlank()) deadline else "Check Official Portal"
        val cleanDead = if (dText.length > 38) dText.take(36) + "..." else dText
        canvas.drawText("Last Date to Apply:", left + 30f, top + 155f, labelPaint)
        val deadlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (!deadline.isNullOrBlank()) 0xFFFBBF24.toInt() else Color.WHITE
            textSize = 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("⏳ $cleanDead", left + 30f, top + 197f, deadlinePaint)
    }

    private fun drawCtaBanner(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        color: Int,
        text: String
    ) {
        val rect = RectF(left, top, right, bottom)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
        canvas.drawRoundRect(rect, 20f, 20f, paint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.BLACK
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val tWidth = textPaint.measureText(text)
        val tY = top + (bottom - top) / 2f + 10f
        canvas.drawText(text, left + ((right - left) - tWidth) / 2f, tY, textPaint)
    }

    private fun drawFooterUrl(canvas: Canvas, width: Int, y: Float, sourceUrl: String) {
        val cleanUrl = if (sourceUrl.length > 55) sourceUrl.take(52) + "..." else sourceUrl
        val urlPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            alpha = 160
            textSize = 22f
        }
        val urlWidth = urlPaint.measureText(cleanUrl)
        canvas.drawText(cleanUrl, (width - urlWidth) / 2f, y, urlPaint)
    }

    /**
     * Generates a high-contrast visual meme card image in any selected social media size.
     */
    fun generateMemeBanner(
        memeId: String,
        topic: String,
        setup: String,
        punchline: String,
        formatName: String,
        size: PostImageSize = PostImageSize.SQUARE
    ): String? {
        return try {
            val width = size.width
            val height = size.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Deep dark sleek card background
            val bgPaint = Paint().apply {
                shader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    0xFF121217.toInt(), 0xFF1E1E26.toInt(), Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // Outer card border
            val cardRect = RectF(50f, 50f, width - 50f, height - 50f)
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF38384A.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }
            canvas.drawRoundRect(cardRect, 32f, 32f, borderPaint)

            // Top Header: Topic & Format Badge
            val topicPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF38BDF8.toInt()
                textSize = 28f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                letterSpacing = 0.05f
            }
            canvas.drawText("MEME: ${topic.uppercase()}", 90f, 130f, topicPaint)

            val badgeText = formatName.uppercase()
            val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFA78BFA.toInt()
                textSize = 24f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val bWidth = badgePaint.measureText(badgeText)
            canvas.drawText(badgeText, width - 90f - bWidth, 130f, badgePaint)

            // Horizontal divider
            canvas.drawLine(90f, 165f, width - 90f, 165f, borderPaint)

            // Setup Text (Top Panel)
            val setupPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 46f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val maxTextWidth = width - 180
            val setupLayout = StaticLayout.Builder.obtain(setup, 0, setup.length, setupPaint, maxTextWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(8f, 1f)
                .build()

            val setupY = (height * 0.35f) - (setupLayout.height / 2f)
            canvas.save()
            canvas.translate(90f, setupY)
            setupLayout.draw(canvas)
            canvas.restore()

            // Punchline Text (Bottom Panel with Highlight)
            val punchDividerY = height * 0.55f
            canvas.drawLine(150f, punchDividerY, width - 150f, punchDividerY, borderPaint)

            val punchPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFFBBF24.toInt() // Vibrant Amber
                textSize = 52f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                setShadowLayer(8f, 0f, 4f, 0x88000000.toInt())
            }
            val punchLayout = StaticLayout.Builder.obtain(punchline, 0, punchline.length, punchPaint, maxTextWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(10f, 1f)
                .build()

            val punchY = (height * 0.72f) - (punchLayout.height / 2f)
            canvas.save()
            canvas.translate(90f, punchY)
            punchLayout.draw(canvas)
            canvas.restore()

            // Footer Branding & Safety Seal
            val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                alpha = 150
                textSize = 22f
            }
            val footerText = "SocialAgent Original • 100% Brand Safe Relatable Concept"
            val fWidth = footerPaint.measureText(footerText)
            canvas.drawText(footerText, (width - fWidth) / 2f, height - 90f, footerPaint)

            // Save image
            val memeDir = File(context.filesDir, "meme_images").apply { mkdirs() }
            val memeFile = File(memeDir, "meme_${memeId}_${size.id}.jpg")
            FileOutputStream(memeFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
                out.flush()
            }
            bitmap.recycle()

            AppLogger.info("MemeImage", "Generate", "Generated ${size.displayName} meme image at ${memeFile.absolutePath}")
            memeFile.absolutePath
        } catch (e: Exception) {
            AppLogger.error("MemeImage", "Generate", "Failed to generate meme banner", e)
            null
        }
    }
}
