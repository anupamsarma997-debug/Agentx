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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
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
import com.example.data.local.seed.CuratedOpportunityCatalog
import com.example.data.model.content.ContentLength
import com.example.data.model.content.ContentPlatform
import com.example.data.model.content.ContentType
import com.example.data.model.meme.MemeFormat
import com.example.data.model.meme.MemeTopic
import com.example.data.model.opportunity.OpportunityCategory
import com.example.data.model.opportunity.OpportunityRegion
import com.example.data.model.opportunity.VerificationStatus
import com.example.data.remote.scout.OfficialPortalDirectory
import com.example.data.remote.scout.OfficialPortalInfo
import com.example.domain.generator.PostImageSize
import com.example.ui.screens.opportunities.VerificationBadge
import com.example.ui.screens.opportunities.AddOpportunityDialog
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
    val postedOpportunityIds by viewModel.postedOpportunityIds.collectAsState()
    val freshSarkariList by viewModel.freshSarkari48hList.collectAsState()

    var selectedOpportunity by remember { mutableStateOf<OpportunityEntity?>(null) }
    var selectedContentType by remember { mutableStateOf(ContentType.OPPORTUNITY_POST) }
    var selectedPlatform by remember { mutableStateOf(ContentPlatform.BOTH) }
    var selectedLength by remember { mutableStateOf(ContentLength.SHORT) }
    var selectedImageSize by remember { mutableStateOf(PostImageSize.SQUARE) }
    var selectedLanguage by remember { mutableStateOf("অসমীয়া (Assamese)") }
    var showSourceSelectorDialog by remember { mutableStateOf(false) }

    // Dedicated state for relatable memes
    var selectedMemePresetKey by remember { mutableStateOf<String?>(null) }
    var selectedMemeTopic by remember { mutableStateOf<MemeTopic?>(null) }
    var selectedMemeFormat by remember { mutableStateOf(MemeFormat.ASSAM_RELATABLE) }

    // 100+ Official Websites / Portals Directory state
    var websiteSearchQuery by remember { mutableStateOf("") }
    var selectedWebsiteCategory by remember { mutableStateOf("ALL") }
    var showAllWebsites by remember { mutableStateOf(false) }
    var showAddOpportunityDialog by remember { mutableStateOf(false) }

    // Auto-select initial unposted / fresh 48h opportunity if none selected
    if (selectedOpportunity == null) {
        selectedOpportunity = freshSarkariList.firstOrNull()
            ?: opportunities.firstOrNull { it.verificationStatus == VerificationStatus.VERIFIED.name && !it.isPosted && !postedOpportunityIds.contains(it.id) }
            ?: opportunities.firstOrNull { !it.isPosted && !postedOpportunityIds.contains(it.id) }
            ?: opportunities.firstOrNull()
            ?: CuratedOpportunityCatalog.ORUNODOI_3
    }

    val isSelectedAlreadyPosted = selectedOpportunity != null && (selectedOpportunity!!.isPosted || postedOpportunityIds.contains(selectedOpportunity!!.id))

    if (showAddOpportunityDialog) {
        AddOpportunityDialog(
            onDismiss = { showAddOpportunityDialog = false },
            onSaveOpportunity = { title, desc, cat, reg, org, elig, url, dead ->
                viewModel.addCustomOpportunity(
                    title = title,
                    description = desc,
                    category = cat,
                    region = reg,
                    organization = org,
                    eligibility = elig,
                    sourceUrl = url,
                    deadline = dead,
                    onComplete = { entity ->
                        selectedOpportunity = entity
                        selectedContentType = when (entity.category) {
                            OpportunityCategory.MSME.name -> ContentType.MSME_ALERT
                            OpportunityCategory.GOVERNMENT_JOB.name, OpportunityCategory.JOB.name -> ContentType.JOB_ALERT
                            OpportunityCategory.HACKATHON.name -> ContentType.HACKATHON_ALERT
                            OpportunityCategory.SCHOLARSHIP.name -> ContentType.SCHOLARSHIP_ALERT
                            else -> ContentType.OPPORTUNITY_POST
                        }
                    }
                )
            }
        )
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
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Daily Quota: ${settings.todayPostCount} used • Unlimited Access Active",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .clickable { viewModel.resetTodayCounts() }
                                .testTag("btn_reset_quota_generator")
                        ) {
                            Text(
                                text = "🔄 Reset to 0",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
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
                                viewModel.batchGenerateAllOpportunities("SARKARI_48H", langParam) {
                                    onNavigateToQueue()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_batch_sarkari_48h")
                        ) {
                            Text("🔥 Daily 48h Sarkari (দৈনিক ৪৮ ঘণ্টীয়া চৰকাৰী)", style = MaterialTheme.typography.labelSmall)
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

                        OutlinedButton(
                            onClick = {
                                viewModel.batchGenerateAllOpportunities("PORTALS", langParam) {
                                    onNavigateToQueue()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_batch_portals")
                        ) {
                            Text("🌐 All 100+ Websites", style = MaterialTheme.typography.labelSmall)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
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
                    }
                    FilledTonalButton(
                        onClick = { showAddOpportunityDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("btn_generator_add_new_opp")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ ADD NEW", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }

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
                        "MSME PMEGP (₹50L Loan 35% Subsidy)" to ("PMEGP" to CuratedOpportunityCatalog.MSME_PMEGP),
                        "Udyam পঞ্জীয়ন (Zero Cost)" to ("Udyam" to CuratedOpportunityCatalog.MSME_UDYAM),
                        "PM বিশ্বকৰ্মা (Vishwakarma Loan)" to ("Vishwakarma" to CuratedOpportunityCatalog.MSME_VISHWAKARMA)
                    )
                    msmePresets.forEach { (label, pair) ->
                        val (keyword, fallback) = pair
                        val isSelected = selectedContentType == ContentType.MSME_ALERT &&
                            (selectedOpportunity?.title?.contains(keyword, ignoreCase = true) == true ||
                             selectedOpportunity?.id == fallback.id)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedOpportunity = opportunities.firstOrNull {
                                    it.title.contains(keyword, ignoreCase = true) || it.id.contains(keyword, ignoreCase = true)
                                } ?: fallback
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
                        "Smart India Hackathon (SIH 2026)" to ("Smart India" to CuratedOpportunityCatalog.SMART_INDIA_HACKATHON),
                        "MyGov Innovation Challenge" to ("MyGov" to CuratedOpportunityCatalog.MYGOV_HACKATHON)
                    )
                    hackathonPresets.forEach { (label, pair) ->
                        val (keyword, fallback) = pair
                        val isSelected = selectedContentType == ContentType.HACKATHON_ALERT &&
                            (selectedOpportunity?.title?.contains(keyword, ignoreCase = true) == true ||
                             selectedOpportunity?.id == fallback.id)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedOpportunity = opportunities.firstOrNull {
                                    it.title.contains(keyword, ignoreCase = true) || it.id.contains(keyword, ignoreCase = true)
                                } ?: fallback
                                selectedLanguage = "অসমীয়া (Assamese)"
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
                        Triple("গুৱাহাটী ট্ৰেফিক আৰু চাহ", "Guwahati", MemeFormat.ASSAM_RELATABLE to MemeTopic.createAssamTheme(
                            "গুৱাহাটীৰ জিএছ ৰোডৰ জাঁম বনাম সন্ধিয়াৰ বৰষুণৰ পিছৰ ৰঙা চাহ",
                            "অসমৰ দৈনন্দিন চিৰপৰিচিত অনুভূতি আৰু সন্ধিয়াৰ চাহৰ আড্ডা"
                        )),
                        Triple("MSME vs Angel Investors", "MSME", MemeFormat.SARKARI_SCHEME_RELATABLE to MemeTopic.createSarkariSchemeTheme(
                            "Bharat Sarkar MSME PMEGP vs Angel Investors",
                            "Looking for angel investors vs discovering 35% margin subsidy and ₹50 Lakh collateral-free loan"
                        )),
                        Triple("হেকাথন আৰু বাগ ফিক্স", "Hackathon", MemeFormat.TEXT_MEME to MemeTopic.createGeneralTheme(
                            "Hackathon Submission Deadline 11:59 PM vs Git Merge Conflict",
                            "36-hour college hackathon coding finale and team viva"
                        )),
                        Triple("Fresher 5 Yrs Experience", "Fresher", MemeFormat.JOB_RELATABLE to MemeTopic.createGeneralTheme(
                            "Fresher Applying For Job: Needs 5 Years Experience",
                            "HR requirement dilemma for entry level freshers"
                        )),
                        Triple("NewsBoy Creator Updates", "NewsBoy", MemeFormat.NEWSBOY_CREATOR_STYLE to MemeTopic.createCreatorNewsTheme(
                            "NewsBoy & Neon Man 3 AM Creator Updates",
                            "Faster than 5G breaking creator updates and milestones"
                        )),
                        Triple("Exam Night Preparation", "Exam", MemeFormat.STUDENT_RELATABLE to MemeTopic.createGeneralTheme(
                            "Studying 1 Night Before Exam vs Syllabus Coverage",
                            "Every college student during exam week"
                        ))
                    )
                    memePresets.forEach { (label, key, pair) ->
                        val isSelected = selectedContentType == ContentType.MEME_POST && selectedMemePresetKey == key
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedContentType = ContentType.MEME_POST
                                selectedMemePresetKey = key
                                selectedMemeFormat = pair.first
                                selectedMemeTopic = pair.second
                                selectedOpportunity = OpportunityEntity(
                                    id = "meme_${key.lowercase()}",
                                    title = pair.second.topic,
                                    description = pair.second.context,
                                    sourceName = "অসম সামাজিক মাধ্যম আৰু দৈনন্দিন জীৱন (Assam Relatable)",
                                    sourceUrl = "https://instagram.com/assam_creators",
                                    sourceDomain = "instagram.com",
                                    category = "MEME",
                                    region = OpportunityRegion.ASSAM.name,
                                    verificationStatus = VerificationStatus.VERIFIED.name,
                                    contentHash = "meme_${key.lowercase()}_hash"
                                )
                                selectedLanguage = "অসমীয়া (Assamese)"
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
                        "অৰুণোদয় ৩.০" to ("Orunodoi" to CuratedOpportunityCatalog.ORUNODOI_3),
                        "নিজুত মইনা" to ("Nijut" to CuratedOpportunityCatalog.NIJUT_MOINA),
                        "আত্মনিৰ্ভৰ অসম" to ("CMAAA" to CuratedOpportunityCatalog.CMAAA_2),
                        "স্কুটাৰ আঁচনি" to ("Scooty" to CuratedOpportunityCatalog.SCOOTY_SCHEME),
                        "স্বনিৰ্ভৰ নাৰী" to ("Swanirbhar" to CuratedOpportunityCatalog.SWANIRBHAR_NARI),
                        "অৰুন্ধতী সোণ" to ("Arundhati" to CuratedOpportunityCatalog.ARUNDHATI_GOLD)
                    )
                    assamPresets.forEach { (label, pair) ->
                        val (keyword, fallback) = pair
                        val isSelected = selectedContentType != ContentType.MEME_POST &&
                            (selectedOpportunity?.title?.contains(keyword, ignoreCase = true) == true ||
                             selectedOpportunity?.id == fallback.id)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedOpportunity = opportunities.firstOrNull {
                                    it.title.contains(keyword, ignoreCase = true) || it.id.contains(keyword, ignoreCase = true)
                                } ?: fallback
                                selectedLanguage = "অসমীয়া (Assamese)"
                                selectedContentType = if (keyword == "CMAAA") ContentType.MSME_ALERT else ContentType.OPPORTUNITY_POST
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
                        "অসম আৰক্ষী নিযুক্তি (Police)" to ("Police" to CuratedOpportunityCatalog.ASSAM_POLICE),
                        "ভাৰতীয় সেনা (Army Rally)" to ("Army" to CuratedOpportunityCatalog.INDIAN_ARMY),
                        "ভাৰতীয় নৌসেনা (Navy SSR/MR)" to ("Navy" to CuratedOpportunityCatalog.INDIAN_NAVY),
                        "মাৰ্চেন্ট নেভী (Merchant Navy)" to ("Merchant" to CuratedOpportunityCatalog.MERCHANT_NAVY)
                    )
                    defensePresets.forEach { (label, pair) ->
                        val (keyword, fallback) = pair
                        val isSelected = selectedContentType != ContentType.MEME_POST &&
                            (selectedOpportunity?.title?.contains(keyword, ignoreCase = true) == true ||
                             selectedOpportunity?.id == fallback.id)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedOpportunity = opportunities.firstOrNull {
                                    it.title.contains(keyword, ignoreCase = true) || it.id.contains(keyword, ignoreCase = true)
                                } ?: fallback
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
                        "AICTE প্ৰগতি বৃত্তি (Pragati Girls)" to ("Pragati" to CuratedOpportunityCatalog.AICTE_PRAGATI),
                        "NSP কেন্দ্ৰীয় বৃত্তি (NSP Portal)" to ("NSP" to CuratedOpportunityCatalog.NSP_SCHOLARSHIP)
                    )
                    scholarshipPresets.forEach { (label, pair) ->
                        val (keyword, fallback) = pair
                        val isSelected = selectedContentType != ContentType.MEME_POST &&
                            (selectedOpportunity?.title?.contains(keyword, ignoreCase = true) == true ||
                             selectedOpportunity?.id == fallback.id)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedOpportunity = opportunities.firstOrNull {
                                    it.title.contains(keyword, ignoreCase = true) || it.id.contains(keyword, ignoreCase = true)
                                } ?: fallback
                                selectedLanguage = "অসমীয়া (Assamese)"
                                selectedContentType = ContentType.SCHOLARSHIP_ALERT
                            },
                            label = { Text("🎓 $label") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 7. 100+ Official Websites & Portals Directory Selector
                Text(
                    text = "🌐 ১০০+ চৰকাৰী ৱেবচাইট আৰু পৰ্টেল (100+ Official Websites & Portals):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "অসম আৰু ভাৰত চৰকাৰৰ ১০৪টা অফিচিয়েল পৰ্টেলৰ পৰা যিকোনো এটা ক্লিক কৰক - সেই ৱেবচাইটৰ পৰা তথ্য সংগ্ৰহ কৰি পোষ্ট প্ৰস্তুত হ'ব:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Category pills for portals
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        "ALL" to "All (১০৪)",
                        "ASSAM" to "🏛️ অসম (৩৭)",
                        "MSME" to "🏢 MSME (১৮)",
                        "JOB" to "🛡️ চাকৰি (১৮)",
                        "HACKATHON" to "💻 হেকাথন (১৬)",
                        "SCHOLARSHIP" to "🎓 বৃত্তি (১৫)"
                    ).forEach { (catKey, catLabel) ->
                        FilterChip(
                            selected = selectedWebsiteCategory == catKey,
                            onClick = { selectedWebsiteCategory = catKey },
                            label = { Text(catLabel, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = websiteSearchQuery,
                    onValueChange = { websiteSearchQuery = it },
                    placeholder = { Text("Search 100+ websites (e.g. slprb, sih, kvic, nehu, isro)...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_search_websites")
                )
                Spacer(modifier = Modifier.height(8.dp))

                val filteredWebsites = remember(websiteSearchQuery, selectedWebsiteCategory) {
                    var list = OfficialPortalDirectory.ALL_PORTALS
                    if (selectedWebsiteCategory != "ALL") {
                        list = when (selectedWebsiteCategory) {
                            "ASSAM" -> list.filter { it.region == OpportunityRegion.ASSAM || it.category == OpportunityCategory.ASSAM }
                            "MSME" -> list.filter { it.category == OpportunityCategory.MSME }
                            "JOB" -> list.filter { it.category == OpportunityCategory.GOVERNMENT_JOB }
                            "HACKATHON" -> list.filter { it.category == OpportunityCategory.HACKATHON || it.category == OpportunityCategory.INTERNSHIP }
                            "SCHOLARSHIP" -> list.filter { it.category == OpportunityCategory.SCHOLARSHIP }
                            else -> list
                        }
                    }
                    if (websiteSearchQuery.isNotBlank()) {
                        val q = websiteSearchQuery.trim().lowercase()
                        list = list.filter {
                            it.name.lowercase().contains(q) ||
                            it.department.lowercase().contains(q) ||
                            it.url.lowercase().contains(q) ||
                            it.badgeText.lowercase().contains(q)
                        }
                    }
                    list
                }

                val displayWebsites = if (showAllWebsites || websiteSearchQuery.isNotBlank()) filteredWebsites else filteredWebsites.take(24)

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    displayWebsites.forEach { portal ->
                        val portalEntity = remember(portal.id) { OfficialPortalDirectory.toOpportunityEntity(portal) }
                        val isPortalSelected = selectedOpportunity?.id == portalEntity.id ||
                            selectedOpportunity?.sourceUrl == portal.url
                        FilterChip(
                            selected = isPortalSelected,
                            onClick = {
                                selectedOpportunity = portalEntity
                                selectedLanguage = "অসমীয়া (Assamese)"
                                selectedContentType = when (portal.category) {
                                    OpportunityCategory.GOVERNMENT_JOB -> ContentType.JOB_ALERT
                                    OpportunityCategory.SCHOLARSHIP -> ContentType.SCHOLARSHIP_ALERT
                                    OpportunityCategory.MSME -> ContentType.MSME_ALERT
                                    OpportunityCategory.HACKATHON -> ContentType.HACKATHON_ALERT
                                    else -> ContentType.OPPORTUNITY_POST
                                }
                            },
                            label = {
                                Text(
                                    text = "${portal.badgeText} • ${portal.name.take(24)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }

                if (filteredWebsites.size > 24 && websiteSearchQuery.isBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = { showAllWebsites = !showAllWebsites }
                    ) {
                        Text(
                            text = if (showAllWebsites) "▲ Show Fewer Portals" else "▼ Show All ${filteredWebsites.size} Websites & Portals",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
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
                                        selectedOpportunity = opportunities.firstOrNull {
                                            it.id == CuratedOpportunityCatalog.SMART_INDIA_HACKATHON.id
                                        } ?: opportunities.firstOrNull {
                                            it.category.contains("Hackathon", ignoreCase = true) ||
                                            it.title.contains("Hackathon", ignoreCase = true) ||
                                            it.title.contains("হেকাথন", ignoreCase = true) ||
                                            it.title.contains("SIH", ignoreCase = true)
                                        } ?: CuratedOpportunityCatalog.SMART_INDIA_HACKATHON
                                        selectedLanguage = "অসমীয়া (Assamese)"
                                    }
                                    ContentType.MSME_ALERT -> {
                                        selectedOpportunity = opportunities.firstOrNull {
                                            it.id == CuratedOpportunityCatalog.MSME_PMEGP.id
                                        } ?: opportunities.firstOrNull {
                                            it.category.contains("MSME", ignoreCase = true) ||
                                            it.title.contains("MSME", ignoreCase = true) ||
                                            it.title.contains("PMEGP", ignoreCase = true) ||
                                            it.title.contains("উদ্যোগ", ignoreCase = true)
                                        } ?: CuratedOpportunityCatalog.MSME_PMEGP
                                        selectedLanguage = "অসমীয়া (Assamese)"
                                    }
                                    ContentType.JOB_ALERT -> {
                                        selectedOpportunity = opportunities.firstOrNull {
                                            it.id == CuratedOpportunityCatalog.ASSAM_POLICE.id
                                        } ?: opportunities.firstOrNull {
                                            it.category.contains("Recruitment", ignoreCase = true) ||
                                            it.category.contains("Job", ignoreCase = true) ||
                                            it.title.contains("Police", ignoreCase = true) ||
                                            it.title.contains("আৰক্ষী", ignoreCase = true) ||
                                            it.title.contains("Army", ignoreCase = true)
                                        } ?: CuratedOpportunityCatalog.ASSAM_POLICE
                                        selectedLanguage = "অসমীয়া (Assamese)"
                                    }
                                    ContentType.SCHOLARSHIP_ALERT -> {
                                        selectedOpportunity = opportunities.firstOrNull {
                                            it.id == CuratedOpportunityCatalog.AICTE_PRAGATI.id
                                        } ?: opportunities.firstOrNull {
                                            it.category.contains("Scholarship", ignoreCase = true) ||
                                            it.title.contains("Scholarship", ignoreCase = true) ||
                                            it.title.contains("বৃত্তি", ignoreCase = true) ||
                                            it.title.contains("Pragati", ignoreCase = true)
                                        } ?: CuratedOpportunityCatalog.AICTE_PRAGATI
                                        selectedLanguage = "অসমীয়া (Assamese)"
                                    }
                                    ContentType.MEME_POST -> {
                                        selectedMemePresetKey = "Guwahati"
                                        selectedMemeFormat = MemeFormat.ASSAM_RELATABLE
                                        selectedMemeTopic = MemeTopic.createAssamTheme(
                                            "গুৱাহাটীৰ জিএছ ৰোডৰ জাঁম বনাম সন্ধিয়াৰ বৰষুণৰ পিছৰ ৰঙা চাহ",
                                            "অসমৰ দৈনন্দিন চিৰপৰিচিত অনুভূতি আৰু সন্ধিয়াৰ চাহৰ আড্ডা"
                                        )
                                        selectedOpportunity = OpportunityEntity(
                                            id = "meme_guwahati",
                                            title = "গুৱাহাটীৰ জিএছ ৰোডৰ জাঁম বনাম সন্ধিয়াৰ বৰষুণৰ পিছৰ ৰঙা চাহ",
                                            description = "অসমৰ দৈনন্দিন চিৰপৰিচিত অনুভূতি আৰু সন্ধিয়াৰ চাহৰ আড্ডা",
                                            sourceName = "অসম সামাজিক মাধ্যম আৰু দৈনন্দিন জীৱন (Assam Relatable)",
                                            sourceUrl = "https://instagram.com/assam_creators",
                                            sourceDomain = "instagram.com",
                                            category = "MEME",
                                            region = OpportunityRegion.ASSAM.name,
                                            verificationStatus = VerificationStatus.VERIFIED.name,
                                            contentHash = "meme_guwahati_hash"
                                        )
                                        selectedLanguage = "অসমীয়া (Assamese)"
                                    }
                                    else -> {
                                        selectedOpportunity = opportunities.firstOrNull {
                                            it.id == CuratedOpportunityCatalog.ORUNODOI_3.id
                                        } ?: opportunities.firstOrNull() ?: CuratedOpportunityCatalog.ORUNODOI_3
                                    }
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

                // Duplicate Prevention Warning Banner
                if (isSelectedAlreadyPosted && selectedContentType != ContentType.MEME_POST) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("banner_already_posted_warning")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "⚠️ এই সুযোগৰ পোষ্ট ইতিমধ্যে তৈয়াৰ কৰা হৈছে! (ALREADY POSTED)",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B),
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "একেটা পোষ্ট বাৰে বাৰে বনোৱা নহয় (No duplicate posts allowed). অনুগ্ৰহ কৰি আন এটা নতুন বা পোষ্ট নকৰা সুযোগ বাছক:",
                                color = Color(0xFF7F1D1D),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    val nextUnposted = freshSarkariList.firstOrNull()
                                        ?: opportunities.firstOrNull { !it.isPosted && !postedOpportunityIds.contains(it.id) }
                                    if (nextUnposted != null) {
                                        selectedOpportunity = nextUnposted
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("👉 বাছক নতুন ৪৮ ঘণ্টীয়া চৰকাৰী সুযোগ")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

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
                            val topic = selectedMemeTopic ?: selectedOpportunity?.let { MemeTopic.fromOpportunity(it) }
                                ?: MemeTopic.createAssamTheme(
                                    "গুৱাহাটীৰ জিএছ ৰোডৰ জাঁম বনাম সন্ধিয়াৰ বৰষুণৰ পিছৰ ৰঙা চাহ",
                                    "অসমৰ দৈনন্দিন চিৰপৰিচিত অনুভূতি আৰু সন্ধিয়াৰ চাহৰ আড্ডা"
                                )
                            val format = selectedMemeFormat
                            viewModel.generateMeme(topic, format) { success ->
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
                    enabled = !isGenerating && (!isSelectedAlreadyPosted || selectedContentType == ContentType.MEME_POST) && (selectedOpportunity != null || selectedContentType == ContentType.MEME_POST),
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
                    } else if (isSelectedAlreadyPosted && selectedContentType != ContentType.MEME_POST) {
                        Text("⚠️ ইতিমধ্যে পোষ্ট কৰা হ'ল (ALREADY POSTED)")
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

        Spacer(modifier = Modifier.height(16.dp))

        // AI IMAGE STUDIO (gemini-3.1-flash-image-preview)
        Text(
            text = "AI Image Studio",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        ImageStudioSection(
            viewModel = viewModel,
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
