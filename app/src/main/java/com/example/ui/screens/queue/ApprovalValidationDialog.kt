package com.example.ui.screens.queue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.ContentEntity
import com.example.domain.validator.RuleFailure
import com.example.domain.validator.ValidationResult

@Composable
fun ApprovalValidationDialog(
    item: ContentEntity,
    validationResult: ValidationResult,
    onDismiss: () -> Unit,
    onEditField: (ContentEntity) -> Unit,
    onApproveAnyway: () -> Unit,
    modifier: Modifier = Modifier
) {
    var warningAcknowledged by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("dialog_approval_validation"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = if (validationResult.hasBlockingFailures) Color(0xFFFEE2E2) else Color(0xFFFEF3C7),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (validationResult.hasBlockingFailures) Icons.Default.Close else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (validationResult.hasBlockingFailures) Color(0xFFDC2626) else Color(0xFFD97706),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (validationResult.hasBlockingFailures) "Approval Blocked" else "Approval Warnings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (validationResult.hasBlockingFailures) {
                            "${validationResult.failures.size} blocking failure(s) found"
                        } else {
                            "${validationResult.warnings.size} warning(s) require review"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Post: ${item.title.take(50)}${if (item.title.length > 50) "..." else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. Blocking Failures
                    items(validationResult.failures) { failure ->
                        RuleFailureCard(
                            failure = failure,
                            onEdit = { onEditField(item) }
                        )
                    }

                    // 2. Warnings
                    items(validationResult.warnings) { warning ->
                        RuleFailureCard(
                            failure = warning,
                            onEdit = { onEditField(item) }
                        )
                    }
                }

                // If ONLY warnings exist, allow "Approve anyway" checkbox
                if (validationResult.hasOnlyWarnings) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = Color(0xFFFEF3C7).copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = warningAcknowledged,
                                onCheckedChange = { warningAcknowledged = it },
                                modifier = Modifier.testTag("checkbox_approve_anyway")
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Warnings ko verify kiya hai, fir bhi approve karein.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF92400E)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (validationResult.hasOnlyWarnings) {
                Button(
                    onClick = onApproveAnyway,
                    enabled = warningAcknowledged,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF166534),
                        disabledContainerColor = Color(0xFFE5E7EB)
                    ),
                    modifier = Modifier.testTag("btn_confirm_approve_anyway")
                ) {
                    Text("Approve Anyway", color = Color.White)
                }
            } else {
                Button(
                    onClick = { onEditField(item) },
                    modifier = Modifier.testTag("btn_fix_in_editor")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Fix in Editor")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_dismiss_validation_dialog")
            ) {
                Text("Close")
            }
        }
    )
}

@Composable
fun RuleFailureCard(
    failure: RuleFailure,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (failure.isBlocking) Color(0xFFFCA5A5) else Color(0xFFFDE68A)
    val badgeBg = if (failure.isBlocking) Color(0xFFFEE2E2) else Color(0xFFFEF3C7)
    val badgeText = if (failure.isBlocking) Color(0xFFDC2626) else Color(0xFFD97706)

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = badgeBg
                    ) {
                        Text(
                            text = if (failure.isBlocking) "BLOCKING" else "WARNING",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Rule ${failure.ruleId}: ${failure.ruleName}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .height(28.dp)
                        .testTag("btn_edit_rule_${failure.ruleId}"),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Edit", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = failure.reason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
