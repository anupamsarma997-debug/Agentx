@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.ui.screens.meme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.MemeDraftEntity
import com.example.data.model.meme.MemeGenerationStatus
import com.example.data.model.meme.MemeSafetyStatus

@Composable
fun MemeSafetyBadge(safetyStatus: MemeSafetyStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (safetyStatus) {
        MemeSafetyStatus.SAFE -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "SAFE")
        MemeSafetyStatus.NEEDS_REVIEW -> Triple(Color(0xFFFFF8E1), Color(0xFFF57F17), "NEEDS REVIEW")
        MemeSafetyStatus.BLOCKED -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "BLOCKED")
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = textColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MemePreviewDialog(
    meme: MemeDraftEntity,
    onDismiss: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onSaveEdit: (setup: String, punchline: String, caption: String, hashtags: String) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editedSetup by remember(meme) { mutableStateOf(meme.setupText) }
    var editedPunchline by remember(meme) { mutableStateOf(meme.punchlineText) }
    var editedCaption by remember(meme) { mutableStateOf(meme.caption) }
    var editedHashtags by remember(meme) { mutableStateOf(meme.hashtags) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("meme_preview_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with Format and Safety Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "MEME PREVIEW",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = meme.memeFormatEnum.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    MemeSafetyBadge(safetyStatus = meme.safetyStatusEnum)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // The Visual Meme Canvas Preview (Compose Native Text Meme)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF33333F), RoundedCornerShape(16.dp))
                        .testTag("meme_canvas_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Topic banner
                        Text(
                            text = meme.topic.uppercase(),
                            color = Color(0xFFAAAAAA),
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Setup text (Top Text)
                        Text(
                            text = if (isEditing) editedSetup else meme.setupText,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("meme_setup_text")
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = Color(0xFF33333F), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(20.dp))

                        // Punchline text (Bottom Text - highlighted)
                        Text(
                            text = if (isEditing) editedPunchline else meme.punchlineText,
                            color = Color(0xFFFFD54F), // Amber punchline accent
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.SansSerif,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("meme_punchline_text")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "SocialAgent Local Preview",
                            color = Color(0xFF666666),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Editable Fields / Metadata View
                if (isEditing) {
                    OutlinedTextField(
                        value = editedSetup,
                        onValueChange = { if (it.length <= 120) editedSetup = it },
                        label = { Text("Setup Text (max 120 chars)") },
                        supportingText = { Text("${editedSetup.length}/120") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_setup_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editedPunchline,
                        onValueChange = { if (it.length <= 160) editedPunchline = it },
                        label = { Text("Punchline Text (max 160 chars)") },
                        supportingText = { Text("${editedPunchline.length}/160") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_punchline_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editedCaption,
                        onValueChange = { if (it.length <= 500) editedCaption = it },
                        label = { Text("Caption (max 500 chars)") },
                        supportingText = { Text("${editedCaption.length}/500") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_caption_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editedHashtags,
                        onValueChange = { editedHashtags = it },
                        label = { Text("Hashtags (comma-separated, max 8)") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_hashtags_input")
                    )
                } else {
                    // Caption & Hashtags preview
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Caption:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = meme.caption,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            meme.getHashtagList().forEach { tag ->
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = tag,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Source: ${meme.sourceName} (${meme.sourceUrl})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isEditing) {
                        OutlinedButton(
                            onClick = { isEditing = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                onSaveEdit(editedSetup, editedPunchline, editedCaption, editedHashtags)
                                isEditing = false
                            },
                            modifier = Modifier.weight(1f).testTag("save_meme_edit_button")
                        ) {
                            Text("Save")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { isEditing = true },
                            modifier = Modifier.weight(1f).testTag("edit_meme_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit")
                        }

                        Button(
                            onClick = onApprove,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            modifier = Modifier.weight(1f).testTag("approve_meme_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Approve")
                        }

                        OutlinedButton(
                            onClick = onReject,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f).testTag("reject_meme_button")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reject")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterHorizontally).testTag("close_meme_preview_button")
                ) {
                    Text("Close Preview")
                }
            }
        }
    }
}
