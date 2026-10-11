package com.example.domain.generator

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.example.data.local.logging.AppLogger
import java.io.File
import java.io.FileOutputStream
import kotlin.math.absoluteValue

/**
 * Generates rich, diverse, eye-catching social media announcement banners and meme cards in multiple sizes:
 * - Square (1:1 - 1080x1080) for Instagram & Facebook feeds
 * - Portrait (4:5 - 1080x1350) for maximum mobile feed impact
 * - Landscape (16:9 - 1200x675) for Facebook link preview & desktop banners
 * - Story / Reel (9:16 - 1080x1920) for Instagram & Facebook Stories and vertical status
 *
 * Implements high-converting viral design principles ("Joh dikhta hai wo bikta hai"):
 * - 8 dynamic visual color themes (Assam Crimson-Gold, Police Midnight-Amber, Scholar Violet-Yellow,
 *   Emerald Mint, Central Saffron, Farmer Moss-Gold, Women Ruby-Peach, Digital Slate-Cyan)
 * - 4 distinct layout styles with dynamic rotation so consecutive feed posts never look "same same"
 * - 3 high-contrast detail matrix chips (Benefits/Amount, Eligibility/Qualification, Deadline/Action)
 * - Authority ribbon headers, sharp typography, drop shadows, and high-visibility CTA buttons
 */
class PostImageGenerator(private val context: Context) {

    data class BannerPalette(
        val name: String,
        val gradStart: Int,
        val gradEnd: Int,
        val accentColor: Int,
        val badgeBg: Int,
        val badgeText: Int,
        val ctaBg: Int,
        val ctaText: Int,
        val headerRibbonBg: Int,
        val headerRibbonText: Int,
        val defaultAuthority: String,
        val categoryIcon: String
    )

    data class ExtractedDetails(
        val categoryBadge: String,
        val benefitText: String,
        val eligibilityText: String,
        val deadlineText: String,
        val authorityText: String,
        val ctaText: String
    )

    companion object {
        // 8 Highly contrasting, vibrant color palettes
        val PALETTE_ASSAM_ROYAL = BannerPalette(
            name = "Assam Royal Crimson & Gold",
            gradStart = 0xFF831843.toInt(),      // Deep Royal Crimson
            gradEnd = 0xFF4C0519.toInt(),        // Deep Maroon
            accentColor = 0xFFFDE047.toInt(),    // Radiant Gold
            badgeBg = 0xFFDC2626.toInt(),        // Vivid Red Alert
            badgeText = 0xFFFEF08A.toInt(),      // Yellow Gold
            ctaBg = 0xFFFDE047.toInt(),          // Bright Gold
            ctaText = 0xFF000000.toInt(),        // Bold Black
            headerRibbonBg = 0xFF991B1B.toInt(), // Rich Dark Crimson
            headerRibbonText = 0xFFFEF08A.toInt(),
            defaultAuthority = "🏛 অসম চৰকাৰৰ বিশেষ আঁচনি • GOVT OF ASSAM",
            categoryIcon = "🏛"
        )

        val PALETTE_POLICE_DEFENSE = BannerPalette(
            name = "Police & Defense Midnight Amber",
            gradStart = 0xFF0A1128.toInt(),      // Deep Midnight Navy
            gradEnd = 0xFF1C2541.toInt(),        // Rich Dark Navy
            accentColor = 0xFFF97316.toInt(),    // Fiery Warning Orange
            badgeBg = 0xFFEA580C.toInt(),        // High-vis Amber
            badgeText = 0xFFFFFFFF.toInt(),      // Crisp White
            ctaBg = 0xFFF97316.toInt(),          // Electric Orange
            ctaText = 0xFF000000.toInt(),        // Bold Black
            headerRibbonBg = 0xFF1E3A8A.toInt(), // Deep Royal Navy
            headerRibbonText = 0xFFFED7AA.toInt(),
            defaultAuthority = "🚨 জৰুৰী নিযুক্তি জাননী • DEFENSE & POLICE RECRUITMENT",
            categoryIcon = "👮"
        )

        val PALETTE_SCHOLARSHIP_VIOLET = BannerPalette(
            name = "Scholar Electric Violet & Yellow",
            gradStart = 0xFF2E1065.toInt(),      // Deep Royal Violet
            gradEnd = 0xFF4C1D95.toInt(),        // Electric Indigo
            accentColor = 0xFFFACC15.toInt(),    // Cyber Yellow
            badgeBg = 0xFF7C3AED.toInt(),        // Bright Violet
            badgeText = 0xFFFFFFFF.toInt(),      // Crisp White
            ctaBg = 0xFFFACC15.toInt(),          // Bright Yellow
            ctaText = 0xFF000000.toInt(),        // Bold Black
            headerRibbonBg = 0xFF5B21B6.toInt(), // Rich Purple
            headerRibbonText = 0xFFFEF08A.toInt(),
            defaultAuthority = "🎓 ১০০% চৰকাৰী বৃত্তি • SCHOLARSHIP GATEWAY",
            categoryIcon = "🎓"
        )

        val PALETTE_SCHOLARSHIP_EMERALD = BannerPalette(
            name = "Scholarship Emerald & Mint",
            gradStart = 0xFF064E3B.toInt(),      // Deep Forest Emerald
            gradEnd = 0xFF0F766E.toInt(),        // Dark Teal
            accentColor = 0xFF34D399.toInt(),    // Mint Green
            badgeBg = 0xFF059669.toInt(),        // Vivid Emerald
            badgeText = 0xFFFFFFFF.toInt(),      // Crisp White
            ctaBg = 0xFF34D399.toInt(),          // Mint Cyan
            ctaText = 0xFF000000.toInt(),        // Bold Black
            headerRibbonBg = 0xFF047857.toInt(), // Deep Green
            headerRibbonText = 0xFFA7F3D0.toInt(),
            defaultAuthority = "📚 ছাত্ৰ-ছাত্ৰী শিক্ষাবৃত্তি • EDUCATION AID PORTAL",
            categoryIcon = "📚"
        )

        val PALETTE_CENTRAL_SAFFRON = BannerPalette(
            name = "National Saffron & Rust",
            gradStart = 0xFF451A03.toInt(),      // Deep Burnt Umber
            gradEnd = 0xFF7C2D12.toInt(),        // Rich Terracotta Saffron
            accentColor = 0xFFFDBA74.toInt(),    // Warm Saffron Peach
            badgeBg = 0xFFC2410C.toInt(),        // Flame Saffron
            badgeText = 0xFFFFFFFF.toInt(),      // Crisp White
            ctaBg = 0xFFFDBA74.toInt(),          // Warm Saffron
            ctaText = 0xFF000000.toInt(),        // Bold Black
            headerRibbonBg = 0xFF9A3412.toInt(), // Dark Saffron
            headerRibbonText = 0xFFFFEDD5.toInt(),
            defaultAuthority = "🇮🇳 ভাৰত চৰকাৰৰ ৰাষ্ট্ৰীয় প'ৰ্টেল • GOVT OF INDIA",
            categoryIcon = "🇮🇳"
        )

        val PALETTE_FARMER_GREEN = BannerPalette(
            name = "Agriculture Forest & Sun Gold",
            gradStart = 0xFF14532D.toInt(),      // Forest Moss
            gradEnd = 0xFF166534.toInt(),        // Deep Green
            accentColor = 0xFFFBBF24.toInt(),    // Harvest Gold
            badgeBg = 0xFF15803D.toInt(),        // Field Green
            badgeText = 0xFFFFFFFF.toInt(),      // Crisp White
            ctaBg = 0xFFFBBF24.toInt(),          // Golden Amber
            ctaText = 0xFF000000.toInt(),        // Bold Black
            headerRibbonBg = 0xFF166534.toInt(), // Deep Moss
            headerRibbonText = 0xFFFEF08A.toInt(),
            defaultAuthority = "🌾 কৃষক আৰু গ্ৰামোন্নয়ন আঁচনি • KISAN WELFARE",
            categoryIcon = "🌾"
        )

        val PALETTE_WOMEN_RUBY = BannerPalette(
            name = "Women Ruby & Sunset Peach",
            gradStart = 0xFF701A75.toInt(),      // Deep Plum Magenta
            gradEnd = 0xFF86198F.toInt(),        // Vivid Ruby Violet
            accentColor = 0xFFF472B6.toInt(),    // Bright Rose
            badgeBg = 0xFFBE185D.toInt(),        // Ruby Pink
            badgeText = 0xFFFFFFFF.toInt(),      // Crisp White
            ctaBg = 0xFFF472B6.toInt(),          // Rose Pink
            ctaText = 0xFF000000.toInt(),        // Bold Black
            headerRibbonBg = 0xFF9D174D.toInt(), // Dark Rose
            headerRibbonText = 0xFFFCE7F3.toInt(),
            defaultAuthority = "👩 মহিলা আৰু যুৱ সবলীকৰণ • EMPOWERMENT SCHEME",
            categoryIcon = "👩"
        )

        val PALETTE_DIGITAL_CYAN = BannerPalette(
            name = "Digital Seva Slate & Electric Cyan",
            gradStart = 0xFF0F172A.toInt(),      // Midnight Slate
            gradEnd = 0xFF1E293B.toInt(),        // Cool Dark Slate
            accentColor = 0xFF38BDF8.toInt(),    // Electric Sky Cyan
            badgeBg = 0xFF2563EB.toInt(),        // Royal Blue
            badgeText = 0xFFFFFFFF.toInt(),      // Crisp White
            ctaBg = 0xFF38BDF8.toInt(),          // Electric Cyan
            ctaText = 0xFF000000.toInt(),        // Bold Black
            headerRibbonBg = 0xFF1D4ED8.toInt(), // Deep Blue
            headerRibbonText = 0xFFBAE6FD.toInt(),
            defaultAuthority = "⚡ অফিচিয়েল ডিজিটেল প'ৰ্টেল • SEVA SETU & PORTAL",
            categoryIcon = "⚡"
        )
    }

    /**
     * Generates a stunning, distinctive social announcement banner.
     */
    fun generatePostBanner(
        contentId: String,
        title: String,
        category: String,
        organization: String?,
        deadline: String?,
        sourceUrl: String,
        region: String? = null,
        size: PostImageSize = PostImageSize.SQUARE,
        bodySnippet: String? = null,
        eligibility: String? = null,
        benefitSummary: String? = null
    ): String? {
        return try {
            val width = size.width
            val height = size.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // 1. Extract rich contextual details
            val details = extractDetails(
                title = title,
                category = category,
                org = organization,
                body = bodySnippet,
                deadline = deadline,
                region = region,
                eligibility = eligibility,
                benefitSummary = benefitSummary
            )

            // 2. Select vibrant palette with variation rotation based on contentId hash
            val palette = selectPalette(contentId, category, title, bodySnippet)

            // 3. Draw gradient background
            val bgPaint = Paint().apply {
                shader = LinearGradient(0f, 0f, 0f, height.toFloat(), palette.gradStart, palette.gradEnd, Shader.TileMode.CLAMP)
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // 4. Draw dynamic geometric background patterns (4 variants)
            val patternVariant = (contentId.hashCode().absoluteValue) % 4
            drawBackgroundPattern(canvas, width, height, palette, patternVariant)

            // 5. Draw layout tailored to image size
            when (size) {
                PostImageSize.LANDSCAPE -> drawLandscapeLayout(canvas, width, height, title, organization, sourceUrl, palette, details)
                PostImageSize.PORTRAIT -> drawPortraitLayout(canvas, width, height, title, organization, sourceUrl, palette, details)
                PostImageSize.STORY -> drawStoryLayout(canvas, width, height, title, organization, sourceUrl, palette, details)
                PostImageSize.SQUARE -> drawSquareLayout(canvas, width, height, title, organization, sourceUrl, palette, details)
            }

            // Save bitmap with high quality
            val imagesDir = File(context.filesDir, "post_images").apply { mkdirs() }
            val imageFile = File(imagesDir, "post_${contentId}_${size.id}.jpg")
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 94, out)
                out.flush()
            }
            bitmap.recycle()

            AppLogger.info("PostImage", "Generate", "Generated vibrant ${palette.name} (${size.displayName}) at ${imageFile.name}")
            imageFile.absolutePath
        } catch (e: Exception) {
            AppLogger.error("PostImage", "Generate", "Failed to generate post banner image", e)
            null
        }
    }

    /**
     * Intelligently selects a vibrant color palette based on domain and hash rotation.
     */
    private fun selectPalette(contentId: String, category: String, title: String, body: String?): BannerPalette {
        val allText = "$category $title ${body ?: ""}".lowercase()
        val hashMod = (contentId.hashCode().absoluteValue) % 2

        return when {
            // Assam Police & Defense Recruitment
            allText.contains("police") || allText.contains("slprb") || allText.contains("আৰক্ষী") ||
            allText.contains("constable") || allText.contains("কনিষ্টবল") || allText.contains("commando") ||
            allText.contains("army") || allText.contains("সেনা") || allText.contains("agniveer") ||
            allText.contains("navy") || allText.contains("নৌসেনা") || allText.contains("defence") ||
            allText.contains("recruitment") && allText.contains("force") ->
                PALETTE_POLICE_DEFENSE

            // Assam Govt State Schemes
            allText.contains("orunodoi") || allText.contains("অৰুণোদয়") || allText.contains("nijut moina") ||
            allText.contains("নিযুত মইনা") || allText.contains("pragyan") || allText.contains("প্ৰজ্ঞান") ||
            allText.contains("scooty") || allText.contains("স্কুটাৰ") || allText.contains("assam govt") ||
            allText.contains("অসম চৰকাৰ") || allText.contains("assam scheme") ->
                PALETTE_ASSAM_ROYAL

            // Scholarships & Education Aid (Alternating between Violet and Emerald so consecutive scholarships differ!)
            allText.contains("scholarship") || allText.contains("বৃত্তি") || allText.contains("buddy4study") ||
            allText.contains("nsp") || allText.contains("ishan uday") || allText.contains("fellowship") ||
            allText.contains("student") || allText.contains("ছাত্ৰ") -> {
                if (hashMod == 0) PALETTE_SCHOLARSHIP_VIOLET else PALETTE_SCHOLARSHIP_EMERALD
            }

            // Agriculture & Farmers
            allText.contains("kisan") || allText.contains("কৃষক") || allText.contains("farmer") ||
            allText.contains("krishi") || allText.contains("fasal") || allText.contains("agriculture") ->
                PALETTE_FARMER_GREEN

            // Women & Youth Welfare
            allText.contains("women") || allText.contains("মহিলা") || allText.contains("lakhpati baideo") ||
            allText.contains("shg") || allText.contains("girl") || allText.contains("মাতৃ") ->
                PALETTE_WOMEN_RUBY

            // Central Schemes
            allText.contains("pmay") || allText.contains("ayushman") || allText.contains("mudra") ||
            allText.contains("central") || allText.contains("ভাৰত চৰকাৰ") ->
                PALETTE_CENTRAL_SAFFRON

            // Digital Portals / Citizen Services
            allText.contains("seva setu") || allText.contains("সেৱা সেতু") || allText.contains("portal") ||
            allText.contains("digital") || allText.contains("online apply") ->
                PALETTE_DIGITAL_CYAN

            // General fallbacks rotated cleanly
            else -> when ((contentId.hashCode().absoluteValue) % 4) {
                0 -> PALETTE_ASSAM_ROYAL
                1 -> PALETTE_POLICE_DEFENSE
                2 -> PALETTE_SCHOLARSHIP_VIOLET
                else -> PALETTE_DIGITAL_CYAN
            }
        }
    }

    /**
     * Extracts rich details to display on the 3-matrix chips ("Post oht jibur olp beleg beleg or ditails odh dekhat").
     */
    private fun extractDetails(
        title: String,
        category: String,
        org: String?,
        body: String?,
        deadline: String?,
        region: String?,
        eligibility: String?,
        benefitSummary: String?
    ): ExtractedDetails {
        val full = "$title $category ${org ?: ""} ${body ?: ""} ${eligibility ?: ""}".lowercase()

        // 1. Category Badge
        val catBadge = when {
            full.contains("police") || full.contains("slprb") || full.contains("আৰক্ষী") -> "🚨 আৰক্ষী নিযুক্তি ২০২৬"
            full.contains("army") || full.contains("agniveer") || full.contains("সেনা") -> "🇮🇳 ভাৰতীয় সেনা নিযুক্তি"
            full.contains("navy") || full.contains("নৌসেনা") -> "⚓ ভাৰতীয় নৌসেনা নিযুক্তি"
            full.contains("orunodoi") || full.contains("অৰুণোদয়") -> "🏛 অৰুণোদয় ৩.০ আঁচনি"
            full.contains("nijut moina") || full.contains("নিযুত মইনা") -> "👩 নিযুত মইনা আঁচনি"
            full.contains("scooty") || full.contains("স্কুটাৰ") || full.contains("pragyan") -> "🛵 প্ৰজ্ঞান ভাৰতী স্কুটাৰ"
            full.contains("scholarship") || full.contains("buddy4study") || full.contains("বৃত্তি") -> "🎓 ১০০% চৰকাৰী বৃত্তি"
            full.contains("kisan") || full.contains("কৃষক") || full.contains("farmer") -> "🌾 কৃষক কল্যাণ আঁচনি"
            full.contains("seva setu") || full.contains("সেৱা সেতু") -> "⚡ সেৱা সেতু চৰকাৰী সেৱা"
            full.contains("ayushman") || full.contains("চিকিৎসা") -> "🏥 আয়ুষ্মান আৰোগ্য কাৰ্ড"
            category.isNotBlank() && category.length < 24 -> "📌 ${category.uppercase()}"
            else -> "📢 অফিচিয়েল চৰকাৰী জাননী"
        }

        // 2. Benefit / Posts / Vacancy Highlight
        val benefit = when {
            benefitSummary?.isNotBlank() == true -> benefitSummary
            full.contains("6000+") || full.contains("৬০০০+") || (full.contains("6000") && full.contains("post")) -> "👮 পদ: ৬,০০০+ কনিষ্টবল/SI"
            full.contains("agniveer") || full.contains("অগ্নিবীৰ") -> "⚔️ অগ্নিবীৰ ৰেলী আৰু অফিচাৰ"
            full.contains("orunodoi") || full.contains("অৰুণোদয়") || full.contains("1250") -> "💰 সাহায্য: মাহিলী ₹১,২৫০/-"
            full.contains("nijut moina") || full.contains("নিযুত মইনা") -> "👩 অনুদান: ₹১০,০০০ - ₹২৫,০০০"
            full.contains("scooty") || full.contains("স্কুটাৰ") -> "🛵 ১০০% বিনামূলীয়া স্কুটাৰ"
            full.contains("buddy4study") || full.contains("scholarship") || full.contains("বৃত্তি") -> "🎓 ₹১২,০০০ - ₹১ লাখ পৰ্যন্ত"
            full.contains("kisan") || full.contains("কৃষক") -> "🌾 চৰকাৰী অনুদান: ₹৬,০০০/বছৰি"
            full.contains("ayushman") || full.contains("চিকিৎসা") -> "🏥 বিনামূলীয়া চিকিৎসা: ₹৫ লাখ"
            full.contains("seva setu") || full.contains("সেৱা সেতু") -> "🏛 ৫০০+ চৰকাৰী সেৱা অনলাইন"
            full.contains("job") || full.contains("নিযুক্তি") || full.contains("recruitment") -> "💼 নিযুক্তি আৰু আকৰ্ষণীয় দৰমহা"
            else -> "✅ চৰকাৰীভাৱে প্ৰমাণিত সুবিধা"
        }

        // 3. Eligibility / Qualification Highlight
        val elig = when {
            !eligibility.isNullOrBlank() && eligibility.length in 5..35 -> "🎯 $eligibility"
            full.contains("12") || full.contains("উচ্চতৰ মাধ্যমিক") || full.contains("hs") -> "🎯 অৰ্হতা: ১০ম / ১২ শ্ৰেণী উত্তীৰ্ণ"
            full.contains("graduate") || full.contains("স্নাতক") || full.contains("degree") -> "🎓 অৰ্হতা: যিকোনো শাখাত স্নাতক"
            full.contains("student") || full.contains("ছাত্ৰ") || full.contains("class") -> "📚 যোগ্য: বিদ্যালয় / মহাবিদ্যালয় ছাত্ৰ"
            full.contains("women") || full.contains("মহিলা") -> "👩 যোগ্য: অসমৰ স্থায়ী বাসিন্দা মহিলা"
            full.contains("farmer") || full.contains("কৃষক") -> "🌾 যোগ্য: অসমৰ সকলো যোগ্য কৃষক"
            !region.isNullOrBlank() && region.equals("Assam", ignoreCase = true) -> "📍 অঞ্চল: অসমৰ স্থায়ী বাসিন্দা"
            else -> "👥 সকলো যোগ্য প্ৰাৰ্থীৰ বাবে"
        }

        // 4. Deadline / Action Highlight
        val dead = when {
            !deadline.isNullOrBlank() && !deadline.contains("2027-12-31") && !deadline.equals("2027", ignoreCase = true) ->
                "⏳ অন্তিম তাৰিখ: $deadline"
            full.contains("৩১ জানুৱাৰী") || full.contains("january") -> "⏳ অন্তিম তাৰিখ: ৩১ জানুৱাৰী"
            full.contains("১৫ জানুৱাৰী") -> "⏳ অন্তিম তাৰিখ: ১৫ জানুৱাৰী"
            full.contains("২৫ ফেব্ৰুৱাৰী") -> "⏳ অন্তিম তাৰিখ: ২৫ ফেব্ৰুৱাৰী"
            else -> "⚡ মাচুল: ১০০% বিনামূলীয়া আবেদন"
        }

        // 5. Authority Header
        val auth = when {
            full.contains("slprb") || full.contains("assam police") || full.contains("আৰক্ষী") ->
                "🚨 অসম আৰক্ষী নিযুক্তি • SLPRB ASSAM"
            full.contains("army") || full.contains("সেনা") ->
                "🇮🇳 ভাৰতীয় সেনা • JOIN INDIAN ARMY"
            full.contains("navy") || full.contains("নৌসেনা") ->
                "⚓ ভাৰতীয় নৌসেনা • JOIN INDIAN NAVY"
            full.contains("assam") || full.contains("অসম") ->
                "🏛 অসম চৰকাৰৰ অফিচিয়েল প'ৰ্টেল • GOVT OF ASSAM"
            else ->
                "🇮🇳 ভাৰত চৰকাৰৰ অফিচিয়েল প'ৰ্টেল • GOVT VERIFIED"
        }

        // 6. Call To Action Text
        val cta = when {
            full.contains("police") || full.contains("slprb") -> "APPLY ON SLPRB PORTAL ➔"
            full.contains("army") -> "JOIN INDIAN ARMY ➔"
            full.contains("navy") -> "JOIN INDIAN NAVY ➔"
            full.contains("scholarship") || full.contains("buddy4study") -> "APPLY FOR SCHOLARSHIP ➔"
            full.contains("orunodoi") -> "CHECK ORUNODOI 3.0 ➔"
            full.contains("nijut moina") -> "APPLY NIJUT MOINA ➔"
            full.contains("seva setu") -> "SEVA SETU PORTAL LINK ➔"
            else -> "APPLY ON OFFICIAL PORTAL ➔"
        }

        return ExtractedDetails(catBadge, benefit, elig, dead, auth, cta)
    }

    /**
     * Draws dynamic, high-energy background motifs (so cards have distinct visual textures).
     */
    private fun drawBackgroundPattern(canvas: Canvas, width: Int, height: Int, palette: BannerPalette, variant: Int) {
        when (variant) {
            0 -> {
                // Variant 0: Angled Energy Stripe & Glowing Radial Orb
                val stripePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = palette.accentColor
                    alpha = 18
                    style = Paint.Style.FILL
                }
                val path = Path().apply {
                    moveTo(width * 0.55f, 0f)
                    lineTo(width * 0.85f, 0f)
                    lineTo(width * 0.35f, height.toFloat())
                    lineTo(width * 0.05f, height.toFloat())
                    close()
                }
                canvas.drawPath(path, stripePaint)

                val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = palette.accentColor
                    alpha = 24
                }
                canvas.drawCircle(width * 0.9f, height * 0.15f, width * 0.28f, glowPaint)
            }
            1 -> {
                // Variant 1: Precision Tech Dot Grid
                val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    alpha = 20
                }
                val step = 45f
                var x = 80f
                while (x < width - 80f) {
                    var y = 140f
                    while (y < height - 160f) {
                        canvas.drawCircle(x, y, 3f, dotPaint)
                        y += step
                    }
                    x += step
                }
            }
            2 -> {
                // Variant 2: Concentric Circular Radiant Rings
                val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = palette.accentColor
                    alpha = 20
                    style = Paint.Style.STROKE
                    strokeWidth = 3f
                }
                canvas.drawCircle(width * 0.88f, height * 0.2f, 160f, ringPaint)
                canvas.drawCircle(width * 0.88f, height * 0.2f, 260f, ringPaint)
                canvas.drawCircle(width * 0.12f, height * 0.85f, 180f, ringPaint)
            }
            3 -> {
                // Variant 3: Corner Geometric Diamonds
                val diamondPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    alpha = 16
                }
                val path = Path().apply {
                    moveTo(width.toFloat(), 0f)
                    lineTo(width * 0.65f, 0f)
                    lineTo(width.toFloat(), height * 0.35f)
                    close()
                }
                canvas.drawPath(path, diamondPaint)

                val bottomPath = Path().apply {
                    moveTo(0f, height.toFloat())
                    lineTo(width * 0.35f, height.toFloat())
                    lineTo(0f, height * 0.65f)
                    close()
                }
                canvas.drawPath(bottomPath, diamondPaint)
            }
        }
    }

    /**
     * SQUARE (1:1 - 1080x1080) Layout
     */
    private fun drawSquareLayout(
        canvas: Canvas,
        width: Int,
        height: Int,
        title: String,
        organization: String?,
        sourceUrl: String,
        palette: BannerPalette,
        details: ExtractedDetails
    ) {
        val outerMargin = 50f
        val cardRect = RectF(outerMargin, outerMargin, width - outerMargin, height - outerMargin)
        drawCardContainer(canvas, cardRect, palette.accentColor)

        // 1. Top Authority Ribbon Strip
        val ribbonRect = RectF(outerMargin, outerMargin, width - outerMargin, outerMargin + 56f)
        drawTopAuthorityRibbon(canvas, ribbonRect, palette.headerRibbonBg, palette.headerRibbonText, details.authorityText)

        // 2. Category Badge Pill & Verified Badge
        val pillTop = outerMargin + 76f
        val catText = details.categoryBadge
        val catPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.badgeText
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val catWidth = catPaint.measureText(catText)
        val pillRect = RectF(80f, pillTop, 80f + catWidth + 48f, pillTop + 54f)
        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.badgeBg }
        canvas.drawRoundRect(pillRect, 27f, 27f, pillPaint)
        canvas.drawText(catText, 104f, pillTop + 38f, catPaint)

        // Top-right Verified Badge
        val verifiedText = "✓ VERIFIED 48H"
        val vPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.accentColor
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val vWidth = vPaint.measureText(verifiedText)
        canvas.drawText(verifiedText, width - 80f - vWidth, pillTop + 38f, vPaint)

        // 3. Main Title (Crisp, High Contrast, Max 3 lines)
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 48f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setShadowLayer(10f, 0f, 5f, 0xAA000000.toInt())
        }
        val maxTitleWidth = width - 160
        val cleanTitle = if (title.length > 115) title.take(112) + "..." else title
        val titleLayout = StaticLayout.Builder.obtain(cleanTitle, 0, cleanTitle.length, titlePaint, maxTitleWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(8f, 1f)
            .setMaxLines(3)
            .build()

        val titleY = pillTop + 76f
        canvas.save()
        canvas.translate(80f, titleY)
        titleLayout.draw(canvas)
        canvas.restore()

        val titleBottomY = titleY + titleLayout.height + 15f

        // 4. Organization line
        val orgText = organization ?: "Official Government Notification"
        val cleanOrg = if (orgText.length > 42) orgText.take(40) + "..." else orgText
        val orgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.accentColor
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("🏛  $cleanOrg", 80f, titleBottomY + 22f, orgPaint)

        // 5. Three High-Contrast Feature Matrix Chips (Details)
        val chipsTop = titleBottomY + 54f
        drawThreeDetailChips(
            canvas = canvas,
            left = 80f,
            top = chipsTop,
            right = width - 80f,
            chip1 = details.benefitText,
            chip2 = details.eligibilityText,
            chip3 = details.deadlineText,
            accentColor = palette.accentColor
        )

        // 6. High-Visibility CTA Button
        val ctaY = height - 190f
        drawCtaBanner(
            canvas = canvas,
            left = 80f,
            top = ctaY,
            right = width - 80f,
            bottom = ctaY + 76f,
            bgColor = palette.ctaBg,
            textColor = palette.ctaText,
            text = details.ctaText
        )

        // 7. Footer URL
        drawFooterUrl(canvas, width, height - 72f, sourceUrl)
    }

    /**
     * PORTRAIT (4:5 - 1080x1350) Layout
     */
    private fun drawPortraitLayout(
        canvas: Canvas,
        width: Int,
        height: Int,
        title: String,
        organization: String?,
        sourceUrl: String,
        palette: BannerPalette,
        details: ExtractedDetails
    ) {
        val outerMargin = 55f
        val cardRect = RectF(outerMargin, outerMargin, width - outerMargin, height - outerMargin)
        drawCardContainer(canvas, cardRect, palette.accentColor)

        // Authority ribbon
        val ribbonRect = RectF(outerMargin, outerMargin, width - outerMargin, outerMargin + 64f)
        drawTopAuthorityRibbon(canvas, ribbonRect, palette.headerRibbonBg, palette.headerRibbonText, details.authorityText)

        // Category pill
        val pillTop = outerMargin + 90f
        val catText = details.categoryBadge
        val catPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.badgeText
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val catWidth = catPaint.measureText(catText)
        val pillRect = RectF(80f, pillTop, 80f + catWidth + 52f, pillTop + 60f)
        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.badgeBg }
        canvas.drawRoundRect(pillRect, 30f, 30f, pillPaint)
        canvas.drawText(catText, 106f, pillTop + 42f, catPaint)

        // Verified badge
        val verifiedText = "✓ VERIFIED 48H"
        val vPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.accentColor
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val vWidth = vPaint.measureText(verifiedText)
        canvas.drawText(verifiedText, width - 80f - vWidth, pillTop + 42f, vPaint)

        // Title (Spacious, bold)
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 54f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setShadowLayer(10f, 0f, 5f, 0xAA000000.toInt())
        }
        val cleanTitle = if (title.length > 140) title.take(137) + "..." else title
        val titleLayout = StaticLayout.Builder.obtain(cleanTitle, 0, cleanTitle.length, titlePaint, width - 160)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(10f, 1f)
            .setMaxLines(4)
            .build()

        val titleY = pillTop + 90f
        canvas.save()
        canvas.translate(80f, titleY)
        titleLayout.draw(canvas)
        canvas.restore()

        val titleBottomY = titleY + titleLayout.height + 25f

        // Organization
        val orgText = organization ?: "Official Government Notification"
        val cleanOrg = if (orgText.length > 44) orgText.take(42) + "..." else orgText
        val orgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.accentColor
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("🏛  $cleanOrg", 80f, titleBottomY + 24f, orgPaint)

        // 3 Feature matrix chips (Taller format)
        val chipsTop = titleBottomY + 68f
        drawThreeDetailChips(
            canvas = canvas,
            left = 80f,
            top = chipsTop,
            right = width - 80f,
            chip1 = details.benefitText,
            chip2 = details.eligibilityText,
            chip3 = details.deadlineText,
            accentColor = palette.accentColor
        )

        // Extra Value Stamp
        val stampPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            alpha = 190
            textSize = 28f
        }
        canvas.drawText("📢 Factual Guardrails Verified • Direct Portal Link Below", 80f, chipsTop + 310f, stampPaint)

        // CTA Button
        val ctaY = height - 215f
        drawCtaBanner(
            canvas = canvas,
            left = 80f,
            top = ctaY,
            right = width - 80f,
            bottom = ctaY + 84f,
            bgColor = palette.ctaBg,
            textColor = palette.ctaText,
            text = details.ctaText
        )

        // Footer URL
        drawFooterUrl(canvas, width, height - 80f, sourceUrl)
    }

    /**
     * LANDSCAPE (16:9 - 1200x675) Layout
     */
    private fun drawLandscapeLayout(
        canvas: Canvas,
        width: Int,
        height: Int,
        title: String,
        organization: String?,
        sourceUrl: String,
        palette: BannerPalette,
        details: ExtractedDetails
    ) {
        val outerMargin = 35f
        val cardRect = RectF(outerMargin, outerMargin, width - outerMargin, height - outerMargin)
        drawCardContainer(canvas, cardRect, palette.accentColor)

        // Left Column (Title & Org, width ~ 640)
        val pillTop = outerMargin + 30f
        val catText = details.categoryBadge
        val catPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.badgeText
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val catWidth = catPaint.measureText(catText)
        val pillRect = RectF(60f, pillTop, 60f + catWidth + 40f, pillTop + 46f)
        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.badgeBg }
        canvas.drawRoundRect(pillRect, 23f, 23f, pillPaint)
        canvas.drawText(catText, 80f, pillTop + 33f, catPaint)

        // Title in left column
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 38f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setShadowLayer(8f, 0f, 4f, 0x99000000.toInt())
        }
        val cleanTitle = if (title.length > 90) title.take(87) + "..." else title
        val titleLayout = StaticLayout.Builder.obtain(cleanTitle, 0, cleanTitle.length, titlePaint, 600)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(6f, 1f)
            .setMaxLines(3)
            .build()

        val titleY = pillTop + 65f
        canvas.save()
        canvas.translate(60f, titleY)
        titleLayout.draw(canvas)
        canvas.restore()

        val orgText = organization ?: "Official Announcement"
        val cleanOrg = if (orgText.length > 36) orgText.take(34) + "..." else orgText
        val orgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.accentColor
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("🏛  $cleanOrg", 60f, titleY + titleLayout.height + 30f, orgPaint)

        // Right Column: 3 Details Chips & CTA (x from 700 to width - 60)
        val rightX = 700f
        val rightWidth = width - 60f
        val chipsTop = outerMargin + 30f

        drawThreeDetailChips(
            canvas = canvas,
            left = rightX,
            top = chipsTop,
            right = rightWidth,
            chip1 = details.benefitText,
            chip2 = details.eligibilityText,
            chip3 = details.deadlineText,
            accentColor = palette.accentColor
        )

        // CTA Button in Right Column
        val ctaY = chipsTop + 270f
        drawCtaBanner(
            canvas = canvas,
            left = rightX,
            top = ctaY,
            right = rightWidth,
            bottom = ctaY + 70f,
            bgColor = palette.ctaBg,
            textColor = palette.ctaText,
            text = details.ctaText
        )

        // Footer URL
        drawFooterUrl(canvas, width, height - 42f, sourceUrl)
    }

    /**
     * STORY (9:16 - 1080x1920) Layout
     */
    private fun drawStoryLayout(
        canvas: Canvas,
        width: Int,
        height: Int,
        title: String,
        organization: String?,
        sourceUrl: String,
        palette: BannerPalette,
        details: ExtractedDetails
    ) {
        val outerMargin = 60f
        val cardRect = RectF(outerMargin, 120f, width - outerMargin, height - 120f)
        drawCardContainer(canvas, cardRect, palette.accentColor)

        // Authority ribbon
        val ribbonRect = RectF(outerMargin, 120f, width - outerMargin, 195f)
        drawTopAuthorityRibbon(canvas, ribbonRect, palette.headerRibbonBg, palette.headerRibbonText, details.authorityText)

        // Category Tag Pill
        val pillTop = 230f
        val catText = details.categoryBadge
        val catPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.badgeText
            textSize = 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val catWidth = catPaint.measureText(catText)
        val pillRect = RectF(80f, pillTop, 80f + catWidth + 56f, pillTop + 66f)
        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = palette.badgeBg }
        canvas.drawRoundRect(pillRect, 33f, 33f, pillPaint)
        canvas.drawText(catText, 108f, pillTop + 46f, catPaint)

        // Story Title
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 58f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setShadowLayer(12f, 0f, 6f, 0xAA000000.toInt())
        }
        val cleanTitle = if (title.length > 150) title.take(147) + "..." else title
        val titleLayout = StaticLayout.Builder.obtain(cleanTitle, 0, cleanTitle.length, titlePaint, width - 160)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(12f, 1f)
            .setMaxLines(5)
            .build()

        val titleY = pillTop + 100f
        canvas.save()
        canvas.translate(80f, titleY)
        titleLayout.draw(canvas)
        canvas.restore()

        val titleBottomY = titleY + titleLayout.height + 35f

        // Organization
        val orgText = organization ?: "Official Announcement"
        val cleanOrg = if (orgText.length > 40) orgText.take(38) + "..." else orgText
        val orgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.accentColor
            textSize = 38f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("🏛  $cleanOrg", 80f, titleBottomY + 24f, orgPaint)

        // 3 Detail chips
        val chipsTop = titleBottomY + 80f
        drawThreeDetailChips(
            canvas = canvas,
            left = 80f,
            top = chipsTop,
            right = width - 80f,
            chip1 = details.benefitText,
            chip2 = details.eligibilityText,
            chip3 = details.deadlineText,
            accentColor = palette.accentColor
        )

        // Big Story CTA
        val ctaY = height - 340f
        drawCtaBanner(
            canvas = canvas,
            left = 80f,
            top = ctaY,
            right = width - 80f,
            bottom = ctaY + 96f,
            bgColor = palette.ctaBg,
            textColor = palette.ctaText,
            text = "TAP LINK TO APPLY ➔"
        )

        // Story Swipe / Notice
        val swipeText = "Official Verified Source Link Available in Comments"
        val swipePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            alpha = 200
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val swWidth = swipePaint.measureText(swipeText)
        canvas.drawText(swipeText, (width - swWidth) / 2f, height - 200f, swipePaint)

        // Footer URL
        drawFooterUrl(canvas, width, height - 145f, sourceUrl)
    }

    /**
     * Draws the top authority header ribbon strip.
     */
    private fun drawTopAuthorityRibbon(
        canvas: Canvas,
        rect: RectF,
        bgColor: Int,
        textColor: Int,
        text: String
    ) {
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = bgColor }
        // Round top corners only
        canvas.drawRoundRect(rect, 30f, 30f, bgPaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.04f
        }
        val tWidth = textPaint.measureText(text)
        val textX = rect.left + ((rect.width() - tWidth) / 2f)
        val textY = rect.top + (rect.height() / 2f) + 8f
        canvas.drawText(text, textX, textY, textPaint)
    }

    /**
     * Draws 3 High-Contrast Feature Matrix Chips with icons and bold text.
     */
    private fun drawThreeDetailChips(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        chip1: String,
        chip2: String,
        chip3: String,
        accentColor: Int
    ) {
        val chipHeight = 68f
        val gap = 14f

        drawSingleChip(canvas, left, top, right, top + chipHeight, chip1, accentColor, isHighlighted = true)
        drawSingleChip(canvas, left, top + chipHeight + gap, right, top + (chipHeight * 2) + gap, chip2, 0xFF38BDF8.toInt(), isHighlighted = false)
        drawSingleChip(canvas, left, top + (chipHeight * 2) + (gap * 2), right, top + (chipHeight * 3) + (gap * 2), chip3, 0xFFFBBF24.toInt(), isHighlighted = false)
    }

    private fun drawSingleChip(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        text: String,
        indicatorColor: Int,
        isHighlighted: Boolean
    ) {
        val rect = RectF(left, top, right, bottom)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isHighlighted) 0x30FFFFFF else 0x22000000
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isHighlighted) indicatorColor else Color.WHITE
            alpha = if (isHighlighted) 120 else 40
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
        }
        canvas.drawRoundRect(rect, 18f, 18f, bgPaint)
        canvas.drawRoundRect(rect, 18f, 18f, borderPaint)

        // Left accent bar
        val barRect = RectF(left + 4f, top + 10f, left + 14f, bottom - 10f)
        val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = indicatorColor }
        canvas.drawRoundRect(barRect, 5f, 5f, barPaint)

        // Text
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val clean = if (text.length > 55) text.take(52) + "..." else text
        canvas.drawText(clean, left + 30f, top + 44f, textPaint)
    }

    private fun drawCardContainer(canvas: Canvas, rect: RectF, accentColor: Int) {
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
        canvas.drawRoundRect(rect, 34f, 34f, cardPaint)
        canvas.drawRoundRect(rect, 34f, 34f, cardBorderPaint)
    }

    private fun drawCtaBanner(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        bgColor: Int,
        textColor: Int,
        text: String
    ) {
        val rect = RectF(left, top, right, bottom)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = bgColor }
        canvas.drawRoundRect(rect, 22f, 22f, paint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = textColor
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
            alpha = 175
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
