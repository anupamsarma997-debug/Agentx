@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.ui.screens.generator

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.OpportunityEntity
import com.example.data.model.content.ContentLength
import com.example.data.model.content.ContentPlatform
import com.example.data.model.content.ContentType
import com.example.data.model.meme.MemeFormat
import com.example.data.model.meme.MemeTopic
import com.example.data.model.opportunity.VerificationStatus
import com.example.domain.generator.PostImageSize
import com.example.ui.screens.opportunities.VerificationBadge
import com.example.ui.viewmodel.AppViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GeneratorScreen(
    viewModel: AppViewModel,
    onNavigateToQueue: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val opportunities by viewModel.allOpportunities.collectAsState()
    val isGenerating by viewModel.isGeneratingContent.collectAsState()
    val isBatchGenerating by viewModel.isBatchGenerating.collectAsState()
    val batchProgressText by viewModel.batchProgressText.collectAsState()
    val batchProgressRatio by viewModel.batchProgressRatio.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var selectedOpportunity by remember { mutableStateOf<OpportunityEntity?>(null) }
    var selectedContentType by remember { mutableStateOf(ContentType.OPPORTUNITY_POST) }
    var selectedPlatform by remember { mutableStateOf(ContentPlatform.BOTH) }
    var selectedLength by remember { mutableStateOf(ContentLength.SHORT) }
    var selectedImageSize by remember { mutableStateOf(PostImageSize.SQUARE) }
    var selectedLanguage by remember { mutableStateOf("অসমীয়া (Assamese)") }
    var showSourceSelectorDialog by remember { mutableStateOf(false) }

    // Auto-select first verified opportunity if none selected
    if (selectedOpportunity == null && opportunities.isNotEmpty()) {
        selectedOpportunity = opportunities.firstOrNull { it.verificationStatus == VerificationStatus.VERIFIED.name }
            ?: opportunities.first()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Studio Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AI GENERATOR STUDIO",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Factual AI social copy with source-attribution",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                ) {
                    Text(
                        text = "Daily Post Quota: ${settings.todayPostCount} / ${settings.dailyPostTarget} used",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // ⚡ BATCH GENERATOR (GENERATE EVERYTHING) Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("batch_generator_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "⚡ এক-ক্লিকত সকলোবোৰ প্ৰস্তুত কৰক",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "BATCH GENERATE ALL: সকলো আঁচনি, আৰক্ষী, সেনা আৰু নৌসেনাৰ ফটো আৰু পোষ্ট একেলগে বনাওক (Square, Portrait, Landscape, Story)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                )

                if (isBatchGenerating) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = batchProgressText ?: "প্ৰস্তুত হৈ আছে...",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { batchProgressRatio },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val langParam = when {
                                selectedLanguage.startsWith("অসমীয়া") -> "ASSAMESE"
                                selectedLanguage.startsWith("বাংলা") -> "BENGALI"
                                selectedLanguage.startsWith("Hindi") -> "HINDI"
                                else -> "ENGLISH"
                            }
                            viewModel.batchGenerateAllOpportunities("ALL", langParam) {
                                onNavigateToQueue()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_batch_generate_all")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🚀 GENERATE ALL (ALL 10+ POSTS & ALL 4 SIZES)",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val langParam = when {
                            selectedLanguage.startsWith("অসমীয়া") -> "ASSAMESE"
                            selectedLanguage.startsWith("বাংলা") -> "BENGALI"
                            selectedLanguage.startsWith("Hindi") -> "HINDI"
                            else -> "ENGLISH"
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.batchGenerateAllOpportunities("MSME_SCHEMES", langParam) {
                                    onNavigateToQueue()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_batch_msme")
                        ) {
                            Text("🏢 All MSME Schemes", style = MaterialTheme.typography.labelSmall)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.batchGenerateAllOpportunities("HACKATHONS", langParam) {
                                    onNavigateToQueue()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_batch_hackathons")
                        ) {
                            Text("💻 All Hackathons", style = MaterialTheme.typography.labelSmall)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.batchGenerateAllOpportunities("MEMES", langParam) {
                                    onNavigateToQueue()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_batch_memes")
                        ) {
                            Text("😂 All Memes", style = MaterialTheme.typography.labelSmall)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.batchGenerateAllOpportunities("ASSAM_SCHEMES", langParam) {
                                    onNavigateToQueue()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_batch_assam_schemes")
                        ) {
                            Text("🏛️ All Assam Schemes", style = MaterialTheme.typography.labelSmall)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.batchGenerateAllOpportunities("DEFENSE_POLICE", langParam) {
                                    onNavigateToQueue()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_batch_defense")
                        ) {
                            Text("🛡️ Police & Defense (আৰক্ষী আৰু সেনা)", style = MaterialTheme.typography.labelSmall)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.batchGenerateAllOpportunities("SCHOLARSHIPS", langParam) {
                                    onNavigateToQueue()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_batch_scholarships")
                        ) {
                            Text("🎓 All Scholarships", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // CREATE CONTENT Configuration Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("generator_config_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CREATE CONTENT",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Choose an opportunity source, format, length, and destination platform.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 1-Tap Quick Presets for All Categories
                Text(
                    text = "🌟 ১-টেপ বাছনি (1-Tap Selection): আঁচনি, উদ্যোগ, হেকাথন আৰু মিম",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "যিকোনো এটা ক্লিক কৰক - সেই শিতানৰ তথ্য, ফৰ্মেট আৰু সকলো ৪টা আকাৰৰ বেনাৰ লোড হ'ব:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                // 1. MSME & Business Schemes Chips
                Text(
                    text = "🏢 MSME চৰকাৰী ঋণ আৰু উদ্যোগ (MSME & Business Schemes):",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val msmePresets = listOf(
                        "MSME PMEGP (₹50L Loan 35% Subsidy)" to "PMEGP",
                        "Udyam পঞ্জীয়ন (Zero Cost)" to "Udyam",
                        "PM বিশ্বকৰ্মা (Vishwakarma Loan)" to "Vishwakarma"
                    )
                    msmePresets.forEach { (label, keyword) ->
                        val isSelected = selectedOpportunity?.title?.contains(keyword, ignoreCase = true) == true
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val match = opportunities.firstOrNull {
                                    it.title.contains(keyword, ignoreCase = true) || it.category.contains("MSME", ignoreCase = true)
                                }
                                if (match != null) {
                                    selectedOpportunity = match
                                }
                                selectedLanguage = "অসমীয়া (Assamese)"
                                selectedContentType = ContentType.MSME_ALERT
                            },
                            label = { Text("🏢 $label") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 2. Hackathons & Tech Challenges Chips
                Text(
                    text = "💻 হেকাথন আৰু উদ্ভাৱনী প্ৰতিযোগিতা (Hackathons & Tech):",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val hackathonPresets = listOf(
                        "Smart India Hackathon (SIH 2026)" to "Smart India",
                        "MyGov Innovation Challenge" to "MyGov"
                    )
                    hackathonPresets.forEach { (label, keyword) ->
                        val isSelected = selectedOpportunity?.title?.contains(keyword, ignoreCase = true) == true
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val match = opportunities.firstOrNull {
                                    it.title.contains(keyword, ignoreCase = true) || it.category.contains("Hackathon", ignoreCase = true)
                                }
                                if (match != null) {
                                    selectedOpportunity = match
                                }
                                selectedContentType = ContentType.HACKATHON_ALERT
                            },
                            label = { Text("💻 $label") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 3. Relatable Memes Chips
                Text(
                    text = "😂 ৰিলেটেবল মিম আৰু হাস্যৰস (Relatable Humor & Memes):",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD97706)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val memePresets = listOf(
                        "MSME vs Angel Investors" to "MSME",
                        "Fresher 5 Yrs Experience" to "Fresher",
                        "NewsBoy Creator Updates" to "NewsBoy",
                        "Exam Night Preparation" to "Exam"
                    )
                    memePresets.forEach { (label, _) ->
                        FilterChip(
                            selected = selectedContentType == ContentType.MEME_POST && selectedOpportunity?.title?.contains(label) == true,
                            onClick = {
                                selectedContentType = ContentType.MEME_POST
                                val match = opportunities.firstOrNull { it.category.contains("MSME", ignoreCase = true) }
                                    ?: opportunities.firstOrNull()
                                if (match != null) {
                                    selectedOpportunity = match
                                }
                            },
                            label = { Text("😂 $label") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 4. Assam Schemes Chips
                Text(
                    text = "🏛️ অসমৰ মুখ্য আঁচনিসমূহ (Assam Flagship Asoni):",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val assamPresets = listOf(
                        "অৰুণোদয় ৩.০" to "Orunodoi",
                        "নিজুত মইনা" to "Nijut Moina",
                        "আত্মনিৰ্ভৰ অসম" to "CMAAA",
                        "স্কুটাৰ আঁচনি" to "Scooty",
                        "স্বনিৰ্ভৰ নাৰী" to "Swanirbhar",
                        "অৰুন্ধতী সোণ" to "Arundhati"
                    )
                    assamPresets.forEach { (label, keyword) ->
                        FilterChip(
                            selected = selectedOpportunity?.title?.contains(keyword, ignoreCase = true) == true ||
                                       selectedOpportunity?.title?.contains(label) == true,
                            onClick = {
                                val match = opportunities.firstOrNull {
                                    it.title.contains(keyword, ignoreCase = true) || it.title.contains(label)
                                }
                                if (match != null) {
                                    selectedOpportunity = match
                                }
                                selectedLanguage = "অসমীয়া (Assamese)"
                                selectedContentType = ContentType.OPPORTUNITY_POST
                            },
                            label = { Text("🏛️ $label") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 5. Defense & Police Chips
                Text(
                    text = "🛡️ আৰক্ষী আৰু প্ৰতিৰক্ষা বাহিনী (Police, Army, Navy & Merchant Navy):",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val defensePresets = listOf(
                        "অসম আৰক্ষী নিযুক্তি (Police)" to "Police",
                        "ভাৰতীয় সেনা (Army Rally)" to "Army",
                        "ভাৰতীয় নৌসেনা (Navy SSR/MR)" to "Navy",
                        "মাৰ্চেন্ট নেভী (Merchant Navy)" to "Merchant"
                    )
                    defensePresets.forEach { (label, keyword) ->
                        FilterChip(
                            selected = selectedOpportunity?.title?.contains(keyword, ignoreCase = true) == true,
                            onClick = {
                                val match = opportunities.firstOrNull {
                                    it.title.contains(keyword, ignoreCase = true)
                                }
                                if (match != null) {
                                    selectedOpportunity = match
                                }
                                selectedLanguage = "অসমীয়া (Assamese)"
                                selectedContentType = ContentType.JOB_ALERT
                            },
                            label = { Text("🛡️ $label") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 6. Scholarships Chips
                Text(
                    text = "🎓 ছাত্ৰ-ছাত্ৰীৰ বৃত্তি (Scholarships):",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val scholarshipPresets = listOf(
                        "AICTE প্ৰগতি বৃত্তি (Pragati Girls)" to "Pragati",
                        "NSP কেন্দ্ৰীয় বৃত্তি (NSP Portal)" to "NSP",
                        "সক্ষম বিশেষ বৃত্তি (Saksham)" to "Saksham"
                    )
                    scholarshipPresets.forEach { (label, keyword) ->
                        FilterChip(
                            selected = selectedOpportunity?.title?.contains(keyword, ignoreCase = true) == true,
                            onClick = {
                                val match = opportunities.firstOrNull {
                                    it.title.contains(keyword, ignoreCase = true) || it.category.contains("Scholarship", ignoreCase = true)
                                }
                                if (match != null) {
                                    selectedOpportunity = match
                                }
                                selectedContentType = ContentType.SCHOLARSHIP_ALERT
                            },
                            label = { Text("🎓 $label") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Post Language Selector
                Text(
                    text = "পোষ্টৰ ভাষা (Post Language Script)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("অসমীয়া (Assamese)", "English / Hinglish", "বাংলা (Bengali)", "Hindi").forEach { lang ->
                        FilterChip(
                            selected = selectedLanguage == lang,
                            onClick = { selectedLanguage = lang },
                            label = { Text(lang) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Source Selector
                Text(
                    text = "Source Opportunity",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))

                selectedOpportunity?.let { opp ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSourceSelectorDialog = true }
                            .testTag("generator_source_picker"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = opp.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(Icons.Default.ChevronRight, contentDescription = "Change")
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${opp.sourceName} | ${opp.regionEnum.displayName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (!opp.eligibility.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "🎯 কি কি যোগ্যতা লাগিব: ${opp.eligibility}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (opp.sourceUrl.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "🔗 ক'ত আবেদন কৰিব: ${opp.sourceUrl}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                } ?: run {
                    Text(
                        text = "No opportunities discovered yet. Run Opportunity Scout first.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Format Selector
                Text(
                    text = "Content Format",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))

                val formats = listOf(
                    ContentType.OPPORTUNITY_POST,
                    ContentType.MSME_ALERT,
                    ContentType.HACKATHON_ALERT,
                    ContentType.MEME_POST,
                    ContentType.JOB_ALERT,
                    ContentType.SCHOLARSHIP_ALERT,
                    ContentType.NEWS_POST,
                    ContentType.STARTUP_ALERT
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    formats.forEach { format ->
                        FilterChip(
                            selected = selectedContentType == format,
                            onClick = {
                                selectedContentType = format
                                when (format) {
                                    ContentType.HACKATHON_ALERT -> {
                                        val match = opportunities.firstOrNull {
                                            it.category.contains("Hackathon", ignoreCase = true) ||
                                            it.title.contains("Hackathon", ignoreCase = true) ||
                                            it.title.contains("SIH", ignoreCase = true)
                                        }
                                        if (match != null) selectedOpportunity = match
                                    }
                                    ContentType.MSME_ALERT -> {
                                        val match = opportunities.firstOrNull {
                                            it.category.contains("MSME", ignoreCase = true) ||
                                            it.title.contains("MSME", ignoreCase = true) ||
                                            it.title.contains("PMEGP", ignoreCase = true) ||
                                            it.title.contains("Udyam", ignoreCase = true)
                                        }
                                        if (match != null) selectedOpportunity = match
                                    }
                                    ContentType.JOB_ALERT -> {
                                        val match = opportunities.firstOrNull {
                                            it.category.contains("Recruitment", ignoreCase = true) ||
                                            it.category.contains("Job", ignoreCase = true) ||
                                            it.title.contains("Police", ignoreCase = true) ||
                                            it.title.contains("Army", ignoreCase = true)
                                        }
                                        if (match != null) selectedOpportunity = match
                                    }
                                    ContentType.SCHOLARSHIP_ALERT -> {
                                        val match = opportunities.firstOrNull {
                                            it.category.contains("Scholarship", ignoreCase = true) ||
                                            it.title.contains("Scholarship", ignoreCase = true) ||
                                            it.title.contains("Pragati", ignoreCase = true)
                                        }
                                        if (match != null) selectedOpportunity = match
                                    }
                                    ContentType.MEME_POST -> {
                                        val match = opportunities.firstOrNull { it.category.contains("MSME", ignoreCase = true) }
                                            ?: opportunities.firstOrNull()
                                        if (match != null) selectedOpportunity = match
                                    }
                                    else -> {}
                                }
                            },
                            label = { Text(format.displayName) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Platform Selector
                Text(
                    text = "Target Platform",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ContentPlatform.entries.forEach { platform ->
                        FilterChip(
                            selected = selectedPlatform == platform,
                            onClick = { selectedPlatform = platform },
                            label = { Text(platform.displayName) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Length Target Selector
                Text(
                    text = "Length Target",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ContentLength.entries.forEach { len ->
                        FilterChip(
                            selected = selectedLength == len,
                            onClick = { selectedLength = len },
                            label = { Text("${len.displayName} (~${len.targetWords}w)") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Banner Image Size / Aspect Ratio Selector
                Text(
                    text = "Banner Size & Aspect Ratio",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PostImageSize.entries.forEach { size ->
                        FilterChip(
                            selected = selectedImageSize == size,
                            onClick = { selectedImageSize = size },
                            label = { Text(size.displayName) },
                            modifier = Modifier.testTag("size_chip_${size.id}")
                        )
                    }
                }
                Text(
                    text = "${selectedImageSize.description} • ✨ All 4 sizes (1:1, 4:5, 16:9, 9:16) are generated automatically!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Action Button: GENERATE DRAFT
                Button(
                    onClick = {
                        val langParam = when {
                            selectedLanguage.startsWith("অসমীয়া") -> "ASSAMESE"
                            selectedLanguage.startsWith("বাংলা") -> "BENGALI"
                            selectedLanguage.startsWith("Hindi") -> "HINDI"
                            else -> "ENGLISH"
                        }

                        if (selectedContentType == ContentType.MEME_POST) {
                            val topic = selectedOpportunity?.let { MemeTopic.fromOpportunity(it) }
                                ?: MemeTopic.createSarkariSchemeTheme(
                                    "Bharat Sarkar MSME PMEGP vs Investor Pitch",
                                    "Looking for angel investors vs discovering 35% margin subsidy and ₹50 Lakh collateral-free loan"
                                )
                            viewModel.generateMeme(topic, MemeFormat.SARKARI_SCHEME_RELATABLE) { success ->
                                if (success) {
                                    onNavigateToQueue()
                                }
                            }
                        } else {
                            selectedOpportunity?.let { opp ->
                                viewModel.generateContentForOpportunity(
                                    opportunity = opp,
                                    contentType = selectedContentType,
                                    platform = selectedPlatform,
                                    length = selectedLength,
                                    imageSize = selectedImageSize,
                                    language = langParam
                                ) { success ->
                                    if (success) {
                                        onNavigateToQueue()
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isGenerating && (selectedOpportunity != null || selectedContentType == ContentType.MEME_POST),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("generate_draft_button")
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("পোষ্ট আৰু ফটো প্ৰস্তুত হৈ আছে...")
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        val btnLabel = if (selectedContentType == ContentType.MEME_POST) {
                            "GENERATE MEME POST & ALL 4 SIZES"
                        } else {
                            "GENERATE ${selectedContentType.displayName.uppercase()} ($selectedLanguage)"
                        }
                        Text(btnLabel)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        val langParam = when {
                            selectedLanguage.startsWith("অসমীয়া") -> "ASSAMESE"
                            selectedLanguage.startsWith("বাংলা") -> "BENGALI"
                            selectedLanguage.startsWith("Hindi") -> "HINDI"
                            else -> "ENGLISH"
                        }
                        viewModel.batchGenerateAllOpportunities("ALL", langParam) {
                            onNavigateToQueue()
                        }
                    },
                    enabled = !isBatchGenerating && !isGenerating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_batch_generate_all_secondary")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("⚡ GENERATE ALL OPPORTUNITIES ($selectedLanguage)", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        // Multi-Format Creative Pipelines Header
        Text(
            text = "Relatable Meme Generator",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // MEME GENERATOR
        MemeGeneratorSection(
            viewModel = viewModel,
            opportunities = opportunities,
            onNavigateToQueue = onNavigateToQueue
        )
    }

    // Source Selection Dialog
    if (showSourceSelectorDialog) {
        var dialogSearchQuery by remember { mutableStateOf("") }
        var dialogCategoryFilter by remember { mutableStateOf("ALL") }

        AlertDialog(
            onDismissRequest = { showSourceSelectorDialog = false },
            title = { Text("Choose Opportunity Source") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = dialogSearchQuery,
                        onValueChange = { dialogSearchQuery = it },
                        placeholder = { Text("Search schemes, hackathons, MSME...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_search_opportunity_dialog")
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            "ALL" to "All",
                            "MSME" to "MSME",
                            "HACKATHONS" to "Hackathons",
                            "ASSAM" to "Assam Schemes",
                            "DEFENSE" to "Police/Defense",
                            "SCHOLARSHIPS" to "Scholarships"
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = dialogCategoryFilter == key,
                                onClick = { dialogCategoryFilter = key },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val filteredList = opportunities.filter { opp ->
                        val matchesSearch = dialogSearchQuery.isBlank() ||
                            opp.title.contains(dialogSearchQuery, ignoreCase = true) ||
                            opp.category.contains(dialogSearchQuery, ignoreCase = true) ||
                            opp.sourceName.contains(dialogSearchQuery, ignoreCase = true)

                        val matchesCategory = when (dialogCategoryFilter) {
                            "MSME" -> opp.category.contains("MSME", ignoreCase = true) || opp.title.contains("MSME", ignoreCase = true) || opp.title.contains("PMEGP", ignoreCase = true) || opp.title.contains("Udyam", ignoreCase = true)
                            "HACKATHONS" -> opp.category.contains("Hackathon", ignoreCase = true) || opp.title.contains("Hackathon", ignoreCase = true) || opp.title.contains("SIH", ignoreCase = true)
                            "ASSAM" -> opp.region.contains("Assam", ignoreCase = true) || opp.title.contains("Orunodoi", ignoreCase = true) || opp.title.contains("Nijut", ignoreCase = true)
                            "DEFENSE" -> opp.title.contains("Police", ignoreCase = true) || opp.title.contains("Army", ignoreCase = true) || opp.title.contains("Navy", ignoreCase = true) || opp.category.contains("Recruitment", ignoreCase = true)
                            "SCHOLARSHIPS" -> opp.category.contains("Scholarship", ignoreCase = true) || opp.title.contains("Scholarship", ignoreCase = true) || opp.title.contains("Pragati", ignoreCase = true)
                            else -> true
                        }
                        matchesSearch && matchesCategory
                    }

                    if (filteredList.isEmpty()) {
                        Text(
                            text = "No matching opportunities found.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                        ) {
                            items(filteredList, key = { it.id }) { opp ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedOpportunity = opp
                                            when {
                                                opp.category.contains("Hackathon", ignoreCase = true) || opp.title.contains("Hackathon", ignoreCase = true) -> {
                                                    selectedContentType = ContentType.HACKATHON_ALERT
                                                }
                                                opp.category.contains("MSME", ignoreCase = true) || opp.title.contains("MSME", ignoreCase = true) || opp.title.contains("PMEGP", ignoreCase = true) -> {
                                                    selectedContentType = ContentType.MSME_ALERT
                                                }
                                                opp.title.contains("Police", ignoreCase = true) || opp.title.contains("Army", ignoreCase = true) || opp.title.contains("Navy", ignoreCase = true) -> {
                                                    selectedContentType = ContentType.JOB_ALERT
                                                }
                                                opp.category.contains("Scholarship", ignoreCase = true) || opp.title.contains("Scholarship", ignoreCase = true) -> {
                                                    selectedContentType = ContentType.SCHOLARSHIP_ALERT
                                                }
                                            }
                                            showSourceSelectorDialog = false
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selectedOpportunity?.id == opp.id) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        }
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = opp.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${opp.sourceName} | ${opp.regionEnum.displayName}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            VerificationBadge(status = opp.verificationStatusEnum)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSourceSelectorDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MemeGeneratorSection(
    viewModel: AppViewModel,
    opportunities: List<OpportunityEntity>,
    onNavigateToQueue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isGeneratingMeme by viewModel.isGeneratingMeme.collectAsState()

    var selectedSourceType by remember { mutableStateOf("Assam Topic") }
    val sourceOptions = listOf(
        "YouTuber & Creator News (NewsBoy / Neon Man)",
        "MSME & Sarkari Schemes",
        "Verified Opportunity",
        "Verified News",
        "Assam Topic",
        "Northeast Topic",
        "General Topic"
    )

    var customTopicText by remember { mutableStateOf("Guwahati Traffic vs Monsoon Rains") }
    var customContextText by remember { mutableStateOf("Everyday relatable situations commuting across the city during sudden evening rain.") }
    var selectedOpportunityForMeme by remember { mutableStateOf<OpportunityEntity?>(null) }
    var showMemeOppSelector by remember { mutableStateOf(false) }

    var selectedFormat by remember { mutableStateOf(MemeFormat.NEWSBOY_CREATOR_STYLE) }
    val formatStyles = listOf(
        Pair("NewsBoy & Neon Man Style", MemeFormat.NEWSBOY_CREATOR_STYLE),
        Pair("Sarkari Scheme Relatable", MemeFormat.SARKARI_SCHEME_RELATABLE),
        Pair("Assam Relatable", MemeFormat.ASSAM_RELATABLE),
        Pair("Job Relatable", MemeFormat.JOB_RELATABLE),
        Pair("Student Relatable", MemeFormat.STUDENT_RELATABLE),
        Pair("Startup Relatable", MemeFormat.STARTUP_RELATABLE),
        Pair("Tech Relatable", MemeFormat.TEXT_MEME),
        Pair("Expectation vs Reality", MemeFormat.EXPECTATION_REALITY),
        Pair("Chat Style", MemeFormat.CHAT_STYLE),
        Pair("Two Panel", MemeFormat.TWO_PANEL),
        Pair("Text Meme", MemeFormat.TEXT_MEME)
    )

    // Auto-select opportunity for meme if needed
    if (selectedOpportunityForMeme == null && opportunities.isNotEmpty()) {
        selectedOpportunityForMeme = opportunities.firstOrNull { it.verificationStatus == VerificationStatus.VERIFIED.name }
            ?: opportunities.first()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("meme_generator_section"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.SentimentVerySatisfied,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "MEME GENERATOR",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "Safe, 100% original relatable humor concepts",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Source Selector Type
            Text(
                text = "1. Choose Source Category",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                sourceOptions.forEach { option ->
                    FilterChip(
                        selected = selectedSourceType == option,
                        onClick = {
                            selectedSourceType = option
                            when (option) {
                                "YouTuber & Creator News (NewsBoy / Neon Man)" -> {
                                    customTopicText = "NewsBoy & Neon Man Creator Updates"
                                    customContextText = "Fast breaking updates on top Indian YouTubers, viral controversies, milestones, and creator buzz."
                                    selectedFormat = MemeFormat.NEWSBOY_CREATOR_STYLE
                                }
                                "MSME & Sarkari Schemes" -> {
                                    customTopicText = "Bharat Sarkar MSME PMEGP & Subsidy Opportunities"
                                    customContextText = "Student and entrepreneur reactions discovering official ₹50 Lakh project loans and Udyam benefits."
                                    selectedFormat = MemeFormat.SARKARI_SCHEME_RELATABLE
                                }
                                "Assam Topic" -> {
                                    customTopicText = "Guwahati Traffic vs Monsoon Rains"
                                    customContextText = "Everyday situations navigating city waterlogging and tea breaks."
                                }
                                "Northeast Topic" -> {
                                    customTopicText = "Hill Road Travel Realities"
                                    customContextText = "Shared sumo journeys, scenic mountain vistas, and chai stops."
                                }
                                "General Topic" -> {
                                    customTopicText = "Startup Pitch vs First Client"
                                    customContextText = "Optimism when building pitch decks vs debugging production crashes."
                                }
                            }
                        },
                        label = { Text(option) },
                        modifier = Modifier.testTag("source_option_${option.lowercase().replace(" ", "_")}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details input or opportunity picker
            if (selectedSourceType == "Verified Opportunity" || selectedSourceType == "Verified News") {
                selectedOpportunityForMeme?.let { opp ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showMemeOppSelector = true }
                            .testTag("meme_source_opportunity_picker"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = opp.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Source: ${opp.sourceName} | ${opp.category}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = "Change")
                        }
                    }
                } ?: run {
                    Text(
                        text = "No opportunities discovered yet. Run Opportunity Scout first.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                OutlinedTextField(
                    value = customTopicText,
                    onValueChange = { customTopicText = it },
                    label = { Text("Meme Topic") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("meme_topic_input")
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = customContextText,
                    onValueChange = { customContextText = it },
                    label = { Text("Relatable Context / Situation") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("meme_context_input")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Style Selector
            Text(
                text = "2. Select Meme Style",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                formatStyles.forEach { (label, format) ->
                    FilterChip(
                        selected = selectedFormat == format,
                        onClick = { selectedFormat = format },
                        label = { Text(label) },
                        modifier = Modifier.testTag("meme_style_${format.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button: GENERATE MEME
            Button(
                onClick = {
                    val topic = if (selectedSourceType == "Verified Opportunity" || selectedSourceType == "Verified News") {
                        selectedOpportunityForMeme?.let { MemeTopic.fromOpportunity(it) }
                            ?: MemeTopic.createAssamTheme(customTopicText, customContextText)
                    } else if (selectedSourceType == "YouTuber & Creator News (NewsBoy / Neon Man)") {
                        MemeTopic.createCreatorNewsTheme(customTopicText, customContextText)
                    } else if (selectedSourceType == "MSME & Sarkari Schemes") {
                        MemeTopic.createSarkariSchemeTheme(customTopicText, customContextText)
                    } else if (selectedSourceType == "Northeast Topic") {
                        MemeTopic.createNortheastTheme(customTopicText, customContextText)
                    } else if (selectedSourceType == "General Topic") {
                        MemeTopic.createGeneralTheme(customTopicText, customContextText)
                    } else {
                        MemeTopic.createAssamTheme(customTopicText, customContextText)
                    }

                    viewModel.generateMeme(topic, selectedFormat) { success ->
                        if (success) {
                            onNavigateToQueue()
                        }
                    }
                },
                enabled = !isGeneratingMeme,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("generate_meme_button")
            ) {
                if (isGeneratingMeme) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generating Meme Concept...")
                } else {
                    Icon(Icons.Default.SentimentVerySatisfied, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("GENERATE MEME")
                }
            }
        }
    }

    // Opportunity Selector Dialog for Memes
    if (showMemeOppSelector) {
        AlertDialog(
            onDismissRequest = { showMemeOppSelector = false },
            title = { Text("Choose Source for Meme") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    opportunities.forEach { opp ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedOpportunityForMeme = opp
                                    showMemeOppSelector = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedOpportunityForMeme?.id == opp.id) {
                                    MaterialTheme.colorScheme.tertiaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = opp.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${opp.sourceName} | ${opp.regionEnum.displayName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showMemeOppSelector = false }) {
                    Text("Close")
                }
            }
        )
    }
}
