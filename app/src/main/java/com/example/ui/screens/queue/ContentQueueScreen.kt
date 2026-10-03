package com.example.ui.screens.queue

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.draw.clip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.ContentEntity
import com.example.data.model.content.GenerationStatus
import com.example.ui.screens.opportunities.VerificationBadge
import com.example.ui.viewmodel.AppViewModel
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material.icons.filled.Movie
import com.example.data.local.entity.MemeDraftEntity
import com.example.data.local.entity.ReelDraftEntity
import com.example.data.model.meme.MemeGenerationStatus
import com.example.data.model.meme.MemeSafetyStatus
import com.example.data.model.reel.ReelGenerationStatus
import com.example.data.model.reel.ReelSafetyStatus
import com.example.ui.screens.meme.MemePreviewDialog
import com.example.ui.screens.meme.MemeSafetyBadge
import com.example.ui.screens.reel.ReelPreviewDialog
import androidx.compose.material3.ScrollableTabRow
import com.example.data.model.verification.FinalVerificationStatus
import com.example.data.model.verification.RejectionReason
import com.example.data.local.entity.FinalVerificationRecordEntity
import com.example.ui.screens.queue.FinalVerificationBadge
import com.example.ui.screens.queue.HumanReviewRequiredBadge

@Composable
fun ContentQueueScreen(
    viewModel: AppViewModel,
    onNavigateToPreview: (String) -> Unit = {},
    onNavigateToVerification: (String, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val queue by viewModel.contentQueue.collectAsState()
    val memes by viewModel.allMemes.collectAsState()
    val reels by viewModel.allReels.collectAsState()
    val allVerificationRecords by viewModel.allVerificationRecords.collectAsState()
    val activeValidationResult by viewModel.activeValidationResult.collectAsState()
    val recordMap = remember(allVerificationRecords) {
        allVerificationRecords.associateBy { it.contentId }
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("ALL", "POSTS", "MEMES", "REELS", "REVIEW", "APPROVED", "REJECTED", "BLOCKED", "EXPIRED")

    // State for viewing/editing a meme or reel
    var activePreviewMeme by remember { mutableStateOf<MemeDraftEntity?>(null) }
    var memeToRegenerate by remember { mutableStateOf<MemeDraftEntity?>(null) }
    var activePreviewReel by remember { mutableStateOf<ReelDraftEntity?>(null) }
    var reelToRegenerate by remember { mutableStateOf<ReelDraftEntity?>(null) }

    // Transparent Approval Validation Dialog
    activeValidationResult?.let { (item, validation) ->
        ApprovalValidationDialog(
            item = item,
            validationResult = validation,
            onDismiss = { viewModel.dismissValidationDialog() },
            onEditField = { targetItem ->
                viewModel.dismissValidationDialog()
                onNavigateToPreview(targetItem.id)
            },
            onApproveAnyway = {
                viewModel.approveContent(item.id, "POST", overrideWarnings = true)
            }
        )
    }

    val postDrafts = when (selectedTabIndex) {
        1 -> queue // POSTS
        2 -> emptyList() // MEMES
        3 -> emptyList() // REELS
        4 -> queue.filter { recordMap[it.id]?.humanReviewRequired == true || it.generationStatus == GenerationStatus.REVIEW_REQUIRED.name || recordMap[it.id]?.finalStatus == FinalVerificationStatus.NEEDS_REVIEW.name }
        5 -> queue.filter { it.generationStatus == GenerationStatus.APPROVED.name || recordMap[it.id]?.finalStatus == FinalVerificationStatus.PASSED.name }
        6 -> queue.filter { it.generationStatus == GenerationStatus.REJECTED.name || recordMap[it.id]?.finalStatus == FinalVerificationStatus.FAILED.name }
        7 -> queue.filter { recordMap[it.id]?.finalStatus == FinalVerificationStatus.BLOCKED.name }
        8 -> queue.filter { recordMap[it.id]?.finalStatus == FinalVerificationStatus.EXPIRED.name }
        else -> queue // ALL
    }

    val memeDrafts = when (selectedTabIndex) {
        1 -> emptyList() // POSTS
        2 -> memes // MEMES
        3 -> emptyList() // REELS
        4 -> memes.filter { recordMap[it.id]?.humanReviewRequired == true || it.generationStatus == MemeGenerationStatus.REVIEW_REQUIRED.name || recordMap[it.id]?.finalStatus == FinalVerificationStatus.NEEDS_REVIEW.name || it.safetyStatus == MemeSafetyStatus.NEEDS_REVIEW.name }
        5 -> memes.filter { it.generationStatus == MemeGenerationStatus.APPROVED.name || recordMap[it.id]?.finalStatus == FinalVerificationStatus.PASSED.name }
        6 -> memes.filter { it.generationStatus == MemeGenerationStatus.REJECTED.name || recordMap[it.id]?.finalStatus == FinalVerificationStatus.FAILED.name }
        7 -> memes.filter { recordMap[it.id]?.finalStatus == FinalVerificationStatus.BLOCKED.name || it.safetyStatus == MemeSafetyStatus.BLOCKED.name }
        8 -> memes.filter { recordMap[it.id]?.finalStatus == FinalVerificationStatus.EXPIRED.name }
        else -> memes // ALL
    }

    val reelDrafts = when (selectedTabIndex) {
        1 -> emptyList() // POSTS
        2 -> emptyList() // MEMES
        3 -> reels // REELS
        4 -> reels.filter { recordMap[it.id]?.humanReviewRequired == true || it.generationStatus == ReelGenerationStatus.REVIEW_REQUIRED.name || recordMap[it.id]?.finalStatus == FinalVerificationStatus.NEEDS_REVIEW.name || it.safetyStatus == ReelSafetyStatus.NEEDS_REVIEW.name }
        5 -> reels.filter { it.generationStatus == ReelGenerationStatus.APPROVED.name || recordMap[it.id]?.finalStatus == FinalVerificationStatus.PASSED.name }
        6 -> reels.filter { it.generationStatus == ReelGenerationStatus.REJECTED.name || recordMap[it.id]?.finalStatus == FinalVerificationStatus.FAILED.name }
        7 -> reels.filter { recordMap[it.id]?.finalStatus == FinalVerificationStatus.BLOCKED.name || it.safetyStatus == ReelSafetyStatus.BLOCKED.name }
        8 -> reels.filter { recordMap[it.id]?.finalStatus == FinalVerificationStatus.EXPIRED.name }
        else -> reels // ALL
    }

    val totalItemsCount = postDrafts.size + memeDrafts.size + reelDrafts.size

    // Preview Dialog
    activePreviewMeme?.let { meme ->
        MemePreviewDialog(
            meme = meme,
            onDismiss = { activePreviewMeme = null },
            onApprove = {
                viewModel.updateMemeStatus(meme.id, MemeGenerationStatus.APPROVED)
                activePreviewMeme = null
            },
            onReject = {
                viewModel.updateMemeStatus(meme.id, MemeGenerationStatus.REJECTED)
                activePreviewMeme = null
            },
            onSaveEdit = { setup, punchline, caption, hashtags ->
                viewModel.updateMemeTexts(meme.id, setup, punchline, caption, hashtags)
            },
            onExportImage = { size ->
                viewModel.exportMemeImage(meme.id, size)
            }
        )
    }

    // Confirmation dialog for explicit user action before regeneration
    memeToRegenerate?.let { meme ->
        AlertDialog(
            onDismissRequest = { memeToRegenerate = null },
            title = { Text("Regenerate Meme Concept?") },
            text = {
                Text(
                    "This will consume a generation attempt from your daily post quota and generate an alternative concept for '${meme.topic}'."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = meme
                        memeToRegenerate = null
                        viewModel.regenerateMeme(target)
                    },
                    modifier = Modifier.testTag("confirm_regenerate_meme_button")
                ) {
                    Text("Regenerate")
                }
            },
            dismissButton = {
                TextButton(onClick = { memeToRegenerate = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Preview Dialog for Reel (Phase 7)
    activePreviewReel?.let { reel ->
        ReelPreviewDialog(
            reel = reel,
            onDismiss = { activePreviewReel = null },
            onApprove = {
                viewModel.updateReelStatus(reel.id, ReelGenerationStatus.APPROVED)
                activePreviewReel = null
            },
            onReject = {
                viewModel.updateReelStatus(reel.id, ReelGenerationStatus.REJECTED)
                activePreviewReel = null
            },
            onSaveEdit = { id, hook, caption, voiceover ->
                viewModel.updateReelTexts(id, hook, caption, voiceover)
            }
        )
    }

    // Confirmation dialog for explicit user action before Reel regeneration
    reelToRegenerate?.let { reel ->
        AlertDialog(
            onDismissRequest = { reelToRegenerate = null },
            title = { Text("Regenerate Reel Plan?") },
            text = {
                Text(
                    "This will request a new Reel plan for '${reel.title}'. Only a successfully generated plan consumes one Reel quota unit."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = reel
                        reelToRegenerate = null
                        viewModel.regenerateReel(target)
                    },
                    modifier = Modifier.testTag("confirm_regenerate_reel_button")
                ) {
                    Text("Regenerate")
                }
            },
            dismissButton = {
                TextButton(onClick = { reelToRegenerate = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CONTENT QUEUE",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$totalItemsCount items (${queue.size} posts, ${memes.size} memes, ${reels.size} reels) in queue",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tabs: ALL, POSTS, MEMES, REELS, REVIEW, APPROVED, REJECTED, BLOCKED, EXPIRED
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title, style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.testTag("tab_queue_${title.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (totalItemsCount == 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No Items in this Queue Tab",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Generate post drafts or memes from the Generator screen or Opportunity Scout.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Post Drafts
                items(postDrafts, key = { "post_${it.id}" }) { item ->
                    val record = recordMap[item.id]
                    QueueItemCard(
                        item = item,
                        record = record,
                        onView = { onNavigateToPreview(item.id) },
                        onVerify = { onNavigateToVerification(item.id, "POST") },
                        onApprove = { viewModel.approveContent(item.id, "POST") },
                        onPublish = { viewModel.publishToFacebook(item.id) },
                        onReject = { viewModel.rejectContent(item.id, "POST", RejectionReason.USER_REJECTED) },
                        onDelete = { viewModel.deleteContentDraft(item.id) }
                    )
                }

                // Meme Drafts
                items(memeDrafts, key = { "meme_${it.id}" }) { meme ->
                    val record = recordMap[meme.id]
                    MemeQueueItemCard(
                        meme = meme,
                        record = record,
                        onView = { activePreviewMeme = meme },
                        onVerify = { onNavigateToVerification(meme.id, "MEME") },
                        onEdit = { activePreviewMeme = meme },
                        onApprove = { viewModel.approveContent(meme.id, "MEME") },
                        onReject = { viewModel.rejectContent(meme.id, "MEME", RejectionReason.USER_REJECTED) },
                        onRegenerate = { memeToRegenerate = meme },
                        onDelete = { viewModel.deleteMeme(meme.id) }
                    )
                }

                // Reel Drafts (Phase 7)
                items(reelDrafts, key = { "reel_${it.id}" }) { reel ->
                    val record = recordMap[reel.id]
                    ReelQueueItemCard(
                        reel = reel,
                        record = record,
                        onView = { activePreviewReel = reel },
                        onVerify = { onNavigateToVerification(reel.id, "REEL") },
                        onEdit = { activePreviewReel = reel },
                        onApprove = { viewModel.approveContent(reel.id, "REEL") },
                        onReject = { viewModel.rejectContent(reel.id, "REEL", RejectionReason.USER_REJECTED) },
                        onRegenerate = { reelToRegenerate = reel },
                        onDelete = { viewModel.deleteReel(reel.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun MemeQueueItemCard(
    meme: MemeDraftEntity,
    record: FinalVerificationRecordEntity? = null,
    onView: () -> Unit,
    onVerify: () -> Unit = {},
    onEdit: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onRegenerate: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onView() }
            .testTag("meme_card_${meme.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.SentimentVerySatisfied,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = meme.memeFormatEnum.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    FinalVerificationBadge(status = record?.finalStatusEnum ?: FinalVerificationStatus.PENDING)
                    MemeSafetyBadge(safetyStatus = meme.safetyStatusEnum)
                    MemeStatusBadge(status = meme.generationStatusEnum)
                }
            }

            if (record?.humanReviewRequired == true) {
                Spacer(modifier = Modifier.height(6.dp))
                HumanReviewRequiredBadge()
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Topic headline
            Text(
                text = meme.topic,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Setup & Punchline Mini Box
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = meme.setupText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "👉 ${meme.punchlineText}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metadata row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Source: ${meme.sourceName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatQueueDate(meme.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: VIEW, EDIT, APPROVE, REJECT, REGENERATE, DELETE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onView,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_view_meme_${meme.id}")
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("VIEW", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = onVerify,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_verify_meme_${meme.id}")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("VERIFY", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = onEdit,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_edit_meme_${meme.id}")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("EDIT", style = MaterialTheme.typography.labelSmall)
                }

                val canApprove = record?.finalStatusEnum != FinalVerificationStatus.BLOCKED &&
                    record?.finalStatusEnum != FinalVerificationStatus.EXPIRED &&
                    record?.finalStatusEnum != FinalVerificationStatus.FAILED &&
                    record?.sourceUrlValid != false &&
                    record?.safetyPassed != false
                Button(
                    onClick = onApprove,
                    enabled = canApprove,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF166534)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_approve_meme_${meme.id}")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("APPROVE", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = onReject,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_reject_meme_${meme.id}")
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("REJECT", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = onRegenerate,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_regenerate_meme_${meme.id}")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("RETRY", style = MaterialTheme.typography.labelSmall)
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp).testTag("btn_delete_meme_${meme.id}")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun MemeStatusBadge(status: MemeGenerationStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (status) {
        MemeGenerationStatus.DRAFT -> Pair(Color(0xFFE3F2FD), Color(0xFF1565C0))
        MemeGenerationStatus.REVIEW_REQUIRED -> Pair(Color(0xFFFFF8E1), Color(0xFFF57F17))
        MemeGenerationStatus.APPROVED -> Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32))
        MemeGenerationStatus.REJECTED -> Pair(Color(0xFFFFEBEE), Color(0xFFC62828))
        MemeGenerationStatus.FAILED -> Pair(Color(0xFFFFEBEE), Color(0xFFB71C1C))
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Text(
            text = status.displayName.uppercase(),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun SourceVerificationBadge(isVerified: Boolean, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = if (isVerified) {
        Triple(Color(0xFFDCFCE7), Color(0xFF166534), "VERIFIED")
    } else {
        Triple(Color(0xFFFEF3C7), Color(0xFF92400E), "UNVERIFIED")
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
fun QueueItemCard(
    item: ContentEntity,
    record: FinalVerificationRecordEntity? = null,
    onView: () -> Unit,
    onVerify: () -> Unit = {},
    onApprove: () -> Unit,
    onPublish: () -> Unit = {},
    onReject: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onView() }
            .testTag("queue_card_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Badges row: Exactly TWO badges (Source Verification + Generation Status)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = item.contentTypeEnum.displayName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    SourceVerificationBadge(isVerified = item.isSourceVerified)
                    GenerationStatusBadge(status = item.generationStatusEnum)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title, Thumbnail & Body Preview
            Row(modifier = Modifier.fillMaxWidth()) {
                if (!item.imageUrl.isNullOrBlank()) {
                    val cleanPath = item.imageUrl.removePrefix("file://")
                    val imageFile = File(cleanPath)
                    AsyncImage(
                        model = if (imageFile.exists()) imageFile else item.imageUrl,
                        contentDescription = "Post Banner Thumbnail",
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    // Title
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    // Body Preview
                    Text(
                        text = item.body,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // If validation failures exist in DB and post is not approved
            if (!item.validationFailures.isNullOrBlank() && item.generationStatusEnum != GenerationStatus.APPROVED) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF2F2),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.validationFailures.lines().firstOrNull() ?: "Review required",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF991B1B),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Clean Source Domain display on its own metadata row (Task 3d)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Source: ${item.getSourceDomain()}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.platformEnum.displayName} • ${formatQueueDate(item.createdAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: VIEW, EDIT, APPROVE / PUBLISH TO FACEBOOK, REJECT, DELETE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onView,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_view_${item.id}")
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("VIEW", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = onView,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_edit_${item.id}")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("EDIT", style = MaterialTheme.typography.labelSmall)
                }

                if (item.generationStatusEnum == GenerationStatus.APPROVED) {
                    if (!item.facebookPostId.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFD1FAE5)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF065F46), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PUBLISHED", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                            }
                        }
                    } else {
                        Button(
                            onClick = onPublish,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_publish_${item.id}")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PUBLISH TO FB", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (item.generationStatusEnum == GenerationStatus.PUBLISHED) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFD1FAE5)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF065F46), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PUBLISHED", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                        }
                    }
                } else if (item.generationStatusEnum == GenerationStatus.FAILED) {
                    Button(
                        onClick = onPublish,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_retry_publish_${item.id}")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("RETRY FB", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                } else {
                    Button(
                        onClick = onApprove,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF166534)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_approve_${item.id}")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("APPROVE", style = MaterialTheme.typography.labelSmall)
                    }

                    OutlinedButton(
                        onClick = onReject,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_reject_${item.id}")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("REJECT", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp).testTag("btn_delete_${item.id}")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

private fun formatQueueDate(epochMillis: Long): String {
    return try {
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        sdf.format(Date(epochMillis))
    } catch (_: Exception) {
        "Recent"
    }
}

@Composable
fun ReelQueueItemCard(
    reel: ReelDraftEntity,
    record: FinalVerificationRecordEntity? = null,
    onView: () -> Unit,
    onVerify: () -> Unit = {},
    onEdit: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onRegenerate: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onView() }
            .testTag("reel_card_${reel.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = reel.reelTypeEnum.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "${reel.durationSeconds}s",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = reel.languageEnum.displayName,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    FinalVerificationBadge(status = record?.finalStatusEnum ?: FinalVerificationStatus.PENDING)
                    ReelSafetyBadge(safetyStatus = reel.safetyStatusEnum)
                    ReelStatusBadge(status = reel.generationStatusEnum)
                }
            }

            if (record?.humanReviewRequired == true) {
                Spacer(modifier = Modifier.height(6.dp))
                HumanReviewRequiredBadge()
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = reel.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Hook Box
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "HOOK (0-3s)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "“${reel.hook}”",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metadata info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Source: ${reel.sourceName} | ${reel.verificationStatus}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatQueueDate(reel.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: VIEW, EDIT, APPROVE, REJECT, REGENERATE, DELETE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onView,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_view_reel_${reel.id}")
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("VIEW", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = onVerify,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_verify_reel_${reel.id}")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("VERIFY", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = onEdit,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_edit_reel_${reel.id}")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("EDIT", style = MaterialTheme.typography.labelSmall)
                }

                val canApprove = record?.finalStatusEnum != FinalVerificationStatus.BLOCKED &&
                    record?.finalStatusEnum != FinalVerificationStatus.EXPIRED &&
                    record?.finalStatusEnum != FinalVerificationStatus.FAILED &&
                    record?.sourceUrlValid != false &&
                    record?.safetyPassed != false
                Button(
                    onClick = onApprove,
                    enabled = canApprove,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF166534)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_approve_reel_${reel.id}")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("APPROVE", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = onReject,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_reject_reel_${reel.id}")
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("REJECT", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = onRegenerate,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_regenerate_reel_${reel.id}")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("RETRY", style = MaterialTheme.typography.labelSmall)
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp).testTag("btn_delete_reel_${reel.id}")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun ReelSafetyBadge(safetyStatus: ReelSafetyStatus, modifier: Modifier = Modifier) {
    val (label, bg, fg) = when (safetyStatus) {
        ReelSafetyStatus.SAFE -> Triple("SAFE", Color(0xFFDCFCE7), Color(0xFF166534))
        ReelSafetyStatus.NEEDS_REVIEW -> Triple("REVIEW", Color(0xFFFEF3C7), Color(0xFF92400E))
        ReelSafetyStatus.BLOCKED -> Triple("BLOCKED", Color(0xFFFEE2E2), Color(0xFF991B1B))
    }
    Surface(shape = RoundedCornerShape(6.dp), color = bg, modifier = modifier) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fg
        )
    }
}

@Composable
fun ReelStatusBadge(status: ReelGenerationStatus, modifier: Modifier = Modifier) {
    val (bg, fg) = when (status) {
        ReelGenerationStatus.APPROVED -> Color(0xFFDCFCE7) to Color(0xFF166534)
        ReelGenerationStatus.REVIEW_REQUIRED -> Color(0xFFFEF3C7) to Color(0xFF92400E)
        ReelGenerationStatus.REJECTED -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
        ReelGenerationStatus.DRAFT, ReelGenerationStatus.GENERATED -> Color(0xFFE0E7FF) to Color(0xFF3730A3)
        ReelGenerationStatus.FAILED -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
        else -> Color(0xFFF3F4F6) to Color(0xFF374151)
    }
    Surface(shape = RoundedCornerShape(6.dp), color = bg, modifier = modifier) {
        Text(
            text = status.name,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fg
        )
    }
}
