@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.ui.screens.generator

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.ContentEntity
import com.example.data.local.entity.MemeDraftEntity
import com.example.data.local.entity.OpportunityEntity
import com.example.data.model.opportunity.VerificationStatus
import com.example.data.model.reel.ReelLanguage
import com.example.data.model.reel.ReelTopic
import com.example.data.model.reel.ReelType
import com.example.ui.viewmodel.AppViewModel

@Composable
fun ReelGeneratorSection(
    viewModel: AppViewModel,
    opportunities: List<OpportunityEntity>,
    onNavigateToQueue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isGeneratingReel by viewModel.isGeneratingReel.collectAsState()
    val allContents by viewModel.contentQueue.collectAsState()
    val allMemes by viewModel.allMemes.collectAsState()

    var selectedSourceType by remember { mutableStateOf("Verified Opportunity") }
    val sourceOptions = listOf(
        "Creator & YouTuber News",
        "MSME & Government Schemes",
        "Verified Opportunity",
        "Verified News",
        "Verified Content",
        "Meme Draft",
        "General Topic"
    )

    var selectedOpportunity by remember { mutableStateOf<OpportunityEntity?>(null) }
    var selectedContent by remember { mutableStateOf<ContentEntity?>(null) }
    var selectedMeme by remember { mutableStateOf<MemeDraftEntity?>(null) }

    var generalTopicText by remember { mutableStateOf("Assam Tech & Skill Ecosystem 2026") }
    var generalContextText by remember { mutableStateOf("New incubators and digital skilling initiatives launched across Guwahati and upper Assam districts.") }

    var selectedReelType by remember { mutableStateOf(ReelType.OPPORTUNITY_REEL) }
    val reelTypes = listOf(
        Pair("Opportunity", ReelType.OPPORTUNITY_REEL),
        Pair("News", ReelType.NEWS_REEL),
        Pair("Job", ReelType.JOB_ALERT_REEL),
        Pair("Scholarship", ReelType.SCHOLARSHIP_REEL),
        Pair("Grant", ReelType.GRANT_REEL),
        Pair("Hackathon", ReelType.HACKATHON_REEL),
        Pair("Startup", ReelType.STARTUP_REEL),
        Pair("Assam Fact", ReelType.ASSAM_FACT_REEL),
        Pair("Tech", ReelType.TECH_REEL),
        Pair("Meme", ReelType.MEME_REEL)
    )

    var selectedDuration by remember { mutableIntStateOf(30) }
    val durations = listOf(15, 30, 45, 60)

    var selectedLanguage by remember { mutableStateOf(ReelLanguage.ENGLISH) }

    var showSourceDialog by remember { mutableStateOf(false) }

    // Auto-select initial entities
    if (selectedOpportunity == null && opportunities.isNotEmpty()) {
        selectedOpportunity = opportunities.firstOrNull { it.verificationStatus == VerificationStatus.VERIFIED.name }
            ?: opportunities.first()
    }
    if (selectedContent == null && allContents.isNotEmpty()) {
        selectedContent = allContents.firstOrNull()
    }
    if (selectedMeme == null && allMemes.isNotEmpty()) {
        selectedMeme = allMemes.firstOrNull()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("reel_generator_section"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "REEL GENERATOR",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Short-form vertical video plans (Facebook Reels)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Source Category Selection
            Text(
                text = "1. Choose Reel Source",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                sourceOptions.forEach { opt ->
                    FilterChip(
                        selected = selectedSourceType == opt,
                        onClick = {
                            selectedSourceType = opt
                            when (opt) {
                                "Creator & YouTuber News" -> {
                                    generalTopicText = "NewsBoy & Neon Man Style Creator Updates"
                                    generalContextText = "Fast short news update on top Indian YouTubers, viral controversies, CarryMinati, BB Ki Vines, and creator milestones."
                                    selectedReelType = ReelType.NEWS_REEL
                                }
                                "MSME & Government Schemes" -> {
                                    generalTopicText = "Bharat Sarkar MSME PMEGP & Subsidy Opportunity"
                                    generalContextText = "Official Government of India micro-enterprise loan subsidies up to ₹50 Lakhs and Udyam paperless registration."
                                    selectedReelType = ReelType.OPPORTUNITY_REEL
                                }
                                "Meme Draft" -> selectedReelType = ReelType.MEME_REEL
                                "Verified News" -> selectedReelType = ReelType.NEWS_REEL
                                "Verified Opportunity" -> selectedReelType = ReelType.OPPORTUNITY_REEL
                                else -> {}
                            }
                        },
                        label = { Text(opt) },
                        modifier = Modifier.testTag("reel_source_${opt.lowercase().replace(" ", "_")}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Source Selector Card or custom inputs
            when (selectedSourceType) {
                "Verified Opportunity", "Verified News" -> {
                    selectedOpportunity?.let { opp ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showSourceDialog = true }
                                .testTag("reel_opportunity_picker"),
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
                                        text = "Source: ${opp.sourceName} | ${opp.regionEnum.displayName}",
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
                }
                "Verified Content" -> {
                    selectedContent?.let { content ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showSourceDialog = true },
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
                                        text = content.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Source: ${content.sourceName} | ${content.platformEnum.displayName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = "Change")
                            }
                        }
                    } ?: run {
                        Text(
                            text = "No queued content drafts available yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                "Meme Draft" -> {
                    selectedMeme?.let { meme ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showSourceDialog = true },
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
                                        text = meme.topic,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Setup: ${meme.setupText}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = "Change")
                            }
                        }
                    } ?: run {
                        Text(
                            text = "No meme drafts available yet. Generate a meme first.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                else -> {
                    OutlinedTextField(
                        value = generalTopicText,
                        onValueChange = { generalTopicText = it },
                        label = { Text("Topic Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("reel_general_topic_input")
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = generalContextText,
                        onValueChange = { generalContextText = it },
                        label = { Text("Source Context / Facts") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("reel_general_context_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Reel Type
            Text(
                text = "2. Select Reel Type",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                reelTypes.forEach { (label, type) ->
                    FilterChip(
                        selected = selectedReelType == type,
                        onClick = { selectedReelType = type },
                        label = { Text(label) },
                        modifier = Modifier.testTag("reel_type_${type.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Duration & Language
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                // Duration
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "3. Duration",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        durations.forEach { dur ->
                            FilterChip(
                                selected = selectedDuration == dur,
                                onClick = { selectedDuration = dur },
                                label = { Text("${dur}s") }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Language
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "4. Language",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ReelLanguage.entries.forEach { lang ->
                            FilterChip(
                                selected = selectedLanguage == lang,
                                onClick = { selectedLanguage = lang },
                                label = { Text(lang.displayName) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Button: GENERATE REEL PLAN
            Button(
                onClick = {
                    val topic = when (selectedSourceType) {
                        "Verified Opportunity", "Verified News" -> {
                            selectedOpportunity?.let { ReelTopic.fromOpportunity(it) }
                                ?: ReelTopic.createGeneralTheme(generalTopicText, generalContextText)
                        }
                        "Verified Content" -> {
                            selectedContent?.let { ReelTopic.fromContent(it) }
                                ?: ReelTopic.createGeneralTheme(generalTopicText, generalContextText)
                        }
                        "Meme Draft" -> {
                            selectedMeme?.let { ReelTopic.fromMemeDraft(it) }
                                ?: ReelTopic.createGeneralTheme(generalTopicText, generalContextText)
                        }
                        else -> {
                            ReelTopic.createGeneralTheme(generalTopicText, generalContextText)
                        }
                    }

                    viewModel.generateReel(
                        topic = topic,
                        reelType = selectedReelType,
                        durationSeconds = selectedDuration,
                        language = selectedLanguage
                    ) { success ->
                        if (success) {
                            onNavigateToQueue()
                        }
                    }
                },
                enabled = !isGeneratingReel,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("generate_reel_plan_button")
            ) {
                if (isGeneratingReel) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generating Factual Reel Plan...")
                } else {
                    Icon(Icons.Default.Videocam, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("GENERATE REEL PLAN")
                }
            }
        }
    }

    // Source Selection Dialog
    if (showSourceDialog) {
        AlertDialog(
            onDismissRequest = { showSourceDialog = false },
            title = { Text("Choose $selectedSourceType") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    when (selectedSourceType) {
                        "Verified Opportunity", "Verified News" -> {
                            opportunities.forEach { opp ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedOpportunity = opp
                                            showSourceDialog = false
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selectedOpportunity?.id == opp.id) {
                                            MaterialTheme.colorScheme.secondaryContainer
                                        } else MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(opp.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(opp.sourceName, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                        "Verified Content" -> {
                            allContents.forEach { content ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedContent = content
                                            showSourceDialog = false
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selectedContent?.id == content.id) {
                                            MaterialTheme.colorScheme.secondaryContainer
                                        } else MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(content.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(content.sourceName, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                        "Meme Draft" -> {
                            allMemes.forEach { meme ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedMeme = meme
                                            showSourceDialog = false
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selectedMeme?.id == meme.id) {
                                            MaterialTheme.colorScheme.secondaryContainer
                                        } else MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(meme.topic, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(meme.punchlineText, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSourceDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
