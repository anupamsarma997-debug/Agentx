package com.example.ui.screens.queue

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.content.GenerationStatus
import com.example.ui.screens.opportunities.VerificationBadge
import com.example.ui.viewmodel.AppViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentPreviewScreen(
    contentId: String,
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val queue by viewModel.contentQueue.collectAsState()
    val allOpportunities by viewModel.allOpportunities.collectAsState()
    val activeValidationResult by viewModel.activeValidationResult.collectAsState()
    val isPublishing by viewModel.isPublishing.collectAsState()
    val content = queue.firstOrNull { it.id == contentId }
    val scrollState = rememberScrollState()

    var isEditing by remember { mutableStateOf(false) }
    var editedTitle by remember(content) { mutableStateOf(content?.title ?: "") }
    var editedBody by remember(content) { mutableStateOf(content?.body ?: "") }
    var editedCaption by remember(content) { mutableStateOf(content?.caption ?: "") }
    var editedHashtags by remember(content) { mutableStateOf(content?.hashtags ?: "") }
    var isDeadlineConfirmedByUser by remember { mutableStateOf(false) }

    // Transparent Approval Validation Dialog
    activeValidationResult?.let { (item, validation) ->
        ApprovalValidationDialog(
            item = item,
            validationResult = validation,
            onDismiss = { viewModel.dismissValidationDialog() },
            onEditField = {
                viewModel.dismissValidationDialog()
                isEditing = true
            },
            onApproveAnyway = {
                viewModel.approveContent(item.id, "POST", overrideWarnings = true)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Content Preview & Editor",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_preview_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (content != null) {
                        if (isEditing) {
                            IconButton(
                                onClick = {
                                    viewModel.updateContentDraft(
                                        id = content.id,
                                        title = editedTitle,
                                        body = editedBody,
                                        caption = editedCaption,
                                        hashtags = editedHashtags
                                    )
                                    isEditing = false
                                },
                                modifier = Modifier.testTag("btn_save_edit")
                            ) {
                                Icon(Icons.Default.Save, contentDescription = "Save Draft", tint = MaterialTheme.colorScheme.primary)
                            }
                        } else {
                            IconButton(
                                onClick = { isEditing = true },
                                modifier = Modifier.testTag("btn_start_edit")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Draft")
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        if (content == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Content item not found.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Metadata Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = content.contentTypeEnum.displayName,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                SourceVerificationBadge(isVerified = content.isSourceVerified)
                                GenerationStatusBadge(status = content.generationStatusEnum)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Platform: ${content.platformEnum.displayName} | Model: ${content.aiModel}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Source: ${content.sourceName} (${content.sourceUrl})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Generated Post Banner Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("preview_image_banner_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "POST IMAGE BANNER",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(
                                onClick = { viewModel.regeneratePostImage(content.id) },
                                modifier = Modifier.testTag("btn_regenerate_banner")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Regenerate")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (!content.imageUrl.isNullOrBlank()) {
                            val cleanPath = content.imageUrl.removePrefix("file://")
                            val imageFile = File(cleanPath)
                            if (imageFile.exists()) {
                                AsyncImage(
                                    model = imageFile,
                                    contentDescription = "Generated Post Banner",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                AsyncImage(
                                    model = content.imageUrl,
                                    contentDescription = "Generated Post Banner",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No banner image generated yet",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.regeneratePostImage(content.id) }
                                    ) {
                                        Text("Generate Image Banner")
                                    }
                                }
                            }
                        }
                    }
                }

                // Title & Post Body (View or Edit mode)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (isEditing) {
                            OutlinedTextField(
                                value = editedTitle,
                                onValueChange = { editedTitle = it },
                                label = { Text("Title") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("edit_content_title")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = editedBody,
                                onValueChange = { editedBody = it },
                                label = { Text("Post Body (Facebook / General)") },
                                minLines = 5,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("edit_content_body")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = editedCaption,
                                onValueChange = { editedCaption = it },
                                label = { Text("Caption (Instagram / Short Feed)") },
                                minLines = 3,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("edit_content_caption")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = editedHashtags,
                                onValueChange = { editedHashtags = it },
                                label = { Text("Hashtags (Max 8, comma separated)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("edit_content_hashtags")
                            )
                        } else {
                            Text(
                                text = "Post Title",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = content.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            Text(
                                text = "Facebook Post Body",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = content.body,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            Text(
                                text = "Instagram Caption",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE1306C)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = content.caption,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (content.hashtags.isNotBlank()) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                                Text(
                                    text = "Hashtags (${content.getHashtagList().size}/8)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = content.hashtags,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }

                // Deadline / Date Check Warning
                val hasDateMention = content.body.contains("deadline", ignoreCase = true) ||
                        content.body.contains("last date", ignoreCase = true) ||
                        content.body.contains("tarikh", ignoreCase = true)

                if (hasDateMention) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEF3C7),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isDeadlineConfirmedByUser,
                                onCheckedChange = { isDeadlineConfirmedByUser = it },
                                modifier = Modifier.testTag("checkbox_confirm_deadline_preview")
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Deadline / Date Verification",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                                Text(
                                    text = if (!isDeadlineConfirmedByUser) {
                                        "⚠️ Post me date/deadline hai jo source se unverified ho sakti hai. Kripya official portal se match karke tick karein."
                                    } else {
                                        "✅ Editor dwara date verified mark ki gayi hai."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF92400E)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Meta Facebook Publishing Card (Task 5)
                if (content.generationStatusEnum == GenerationStatus.APPROVED) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Facebook Publishing",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E40AF)
                            )
                            Text(
                                text = "Post approved hai. Ise Facebook Page par publish karein.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF1E3A8A)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            if (!content.facebookPostId.isNullOrBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFD1FAE5),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF065F46), modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Published to Facebook", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF065F46))
                                            Text("Post ID: ${content.facebookPostId}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF065F46))
                                        }
                                        val uriHandler = LocalUriHandler.current
                                        TextButton(onClick = { uriHandler.openUri("https://www.facebook.com/${content.facebookPostId}") }) {
                                            Text("VIEW POST")
                                        }
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.publishToFacebook(content.id) },
                                    enabled = !isPublishing,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("btn_preview_publish_facebook"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (isPublishing) "Publishing..." else "PUBLISH TO FACEBOOK", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                } else if (content.generationStatusEnum == GenerationStatus.FAILED) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEE2E2),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Publishing / Review Failed", fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                            Text(content.validationFailures ?: "Error during processing.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF991B1B))
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { viewModel.publishToFacebook(content.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("btn_preview_retry_publish")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("RETRY PUBLISH TO FACEBOOK", color = Color.White)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Workflow Actions: APPROVE, REJECT, REGENERATE
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Editorial Workflow",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Moderation controls. Transparent validation runs upon clicking Approve.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.approveContent(content.id, "POST") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_approve_content"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF166534)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("APPROVE")
                            }

                            OutlinedButton(
                                onClick = { viewModel.rejectContent(content.id, "POST", com.example.data.model.verification.RejectionReason.USER_REJECTED) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_reject_content"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("REJECT")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Regenerate button
                        val sourceOpportunity = allOpportunities.firstOrNull { it.id == content.sourceOpportunityId }
                        if (sourceOpportunity != null) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.generateContentForOpportunity(
                                        opportunity = sourceOpportunity,
                                        contentType = content.contentTypeEnum,
                                        platform = content.platformEnum
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_regenerate_content"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("REGENERATE (Creates New Draft)")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GenerationStatusBadge(status: GenerationStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (status) {
        GenerationStatus.APPROVED -> Color(0xFFDCFCE7) to Color(0xFF166534)
        GenerationStatus.NEEDS_REVIEW, GenerationStatus.REVIEW_REQUIRED -> Color(0xFFFEF3C7) to Color(0xFF92400E)
        GenerationStatus.PUBLISHING -> Color(0xFFDBEAFE) to Color(0xFF1E40AF)
        GenerationStatus.PUBLISHED -> Color(0xFFD1FAE5) to Color(0xFF065F46)
        GenerationStatus.FAILED, GenerationStatus.REJECTED -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
        GenerationStatus.GENERATED, GenerationStatus.DRAFT -> Color(0xFFF3F4F6) to Color(0xFF4B5563)
        GenerationStatus.GENERATING -> Color(0xFFE0E7FF) to Color(0xFF4338CA)
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Text(
            text = status.displayName.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
