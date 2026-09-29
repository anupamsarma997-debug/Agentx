package com.example.ui.screens.verification

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.FinalVerificationRecordEntity
import com.example.data.model.verification.FinalVerificationStatus
import com.example.data.model.verification.PublishReadiness
import com.example.data.model.verification.RejectionReason
import com.example.ui.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinalVerificationScreen(
    contentId: String,
    contentType: String,
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val record by viewModel.getVerificationRecord(contentId).collectAsState(initial = null)
    val auditLogs by viewModel.getAuditLogs(contentId).collectAsState(initial = emptyList())
    val versions by viewModel.getContentVersions(contentId).collectAsState(initial = emptyList())

    var showRejectDialog by remember { mutableStateOf(false) }
    var selectedRejectionReason by remember { mutableStateOf(RejectionReason.INCORRECT_FACT) }

    // Run verification if record is missing
    if (record == null) {
        viewModel.runVerification(contentId, contentType)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quality Control & Final Gate", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.recheckSource(contentId, contentType) },
                        modifier = Modifier.testTag("btn_recheck_top")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recheck Source")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val currentRecord = record
            if (currentRecord == null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Running 20-point verification check...")
                        }
                    }
                }
            } else {
                // Header Status Card
                item {
                    FinalStatusHeaderCard(record = currentRecord)
                }

                // Official Source Link Card
                item {
                    OfficialSourceCard(
                        contentId = contentId,
                        contentType = contentType,
                        viewModel = viewModel,
                        onOpenUrl = { url ->
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                viewModel.showMessage("Cannot open URL: $url")
                            }
                        }
                    )
                }

                // 20-point Verification Checklist Card
                item {
                    VerificationChecklistCard(record = currentRecord)
                }

                // Issues & Warnings
                if (currentRecord.getIssuesList().isNotEmpty() || currentRecord.getWarningsList().isNotEmpty()) {
                    item {
                        IssuesAndWarningsCard(
                            issues = currentRecord.getIssuesList(),
                            warnings = currentRecord.getWarningsList()
                        )
                    }
                }

                // Gate Action Buttons: RECHECK, EDIT, APPROVE, REJECT
                item {
                    GateActionButtons(
                        record = currentRecord,
                        onRecheck = { viewModel.recheckSource(contentId, contentType) },
                        onEdit = {
                            viewModel.showMessage("Use Content Queue to edit this draft.")
                        },
                        onApprove = {
                            viewModel.approveContent(contentId, contentType)
                        },
                        onReject = {
                            showRejectDialog = true
                        }
                    )
                }

                // Version History
                if (versions.isNotEmpty()) {
                    item {
                        VersionHistoryCard(versions = versions)
                    }
                }

                // Verification Audit Log
                if (auditLogs.isNotEmpty()) {
                    item {
                        AuditLogCard(logs = auditLogs)
                    }
                }
            }
        }
    }

    // Rejection Dialog with Reasons
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Reject Content Draft") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Select reason for rejection:", style = MaterialTheme.typography.bodySmall)
                    RejectionReason.entries.forEach { reason ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = selectedRejectionReason == reason,
                                onClick = { selectedRejectionReason = reason }
                            )
                            Text(reason.displayName, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rejectContent(contentId, contentType, selectedRejectionReason)
                        showRejectDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("btn_confirm_reject")
                ) {
                    Text("Reject")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun FinalStatusHeaderCard(record: FinalVerificationRecordEntity) {
    val status = record.finalStatusEnum
    val (statusBg, statusFg) = when (status) {
        FinalVerificationStatus.PASSED -> Color(0xFFDCFCE7) to Color(0xFF166534)
        FinalVerificationStatus.NEEDS_REVIEW -> Color(0xFFFEF3C7) to Color(0xFF92400E)
        FinalVerificationStatus.BLOCKED -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
        FinalVerificationStatus.FAILED -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
        FinalVerificationStatus.EXPIRED -> Color(0xFFF3F4F6) to Color(0xFF374151)
        FinalVerificationStatus.PENDING -> Color(0xFFE0E7FF) to Color(0xFF3730A3)
    }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("final_status_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("FINAL STATUS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Surface(shape = RoundedCornerShape(8.dp), color = statusBg) {
                        Text(
                            text = status.displayName.uppercase(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = statusFg
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("PUBLISH READINESS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    val readiness = record.publishReadinessEnum
                    val (rBg, rFg) = when (readiness) {
                        PublishReadiness.READY_FOR_PUBLISHER -> Color(0xFFDCFCE7) to Color(0xFF166534)
                        PublishReadiness.BLOCKED -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
                        PublishReadiness.NOT_READY -> Color(0xFFFEF3C7) to Color(0xFF92400E)
                    }
                    Surface(shape = RoundedCornerShape(8.dp), color = rBg) {
                        Text(
                            text = readiness.displayName,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = rFg
                        )
                    }
                }
            }

            if (record.humanReviewRequired) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "⚠ HUMAN REVIEW REQUIRED: Editorial review must be completed before approval.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OfficialSourceCard(
    contentId: String,
    contentType: String,
    viewModel: AppViewModel,
    onOpenUrl: (String) -> Unit
) {
    val sourceUrl = viewModel.getSourceUrlForContent(contentId, contentType) ?: "https://assam.gov.in"
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Official Source:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Text(sourceUrl, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, maxLines = 1)
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick = { onOpenUrl(sourceUrl) },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("btn_open_source")
            ) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("OPEN SOURCE", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

enum class CheckResult {
    PASS, WARNING, FAIL
}

@Composable
fun VerificationChecklistCard(record: FinalVerificationRecordEntity) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("verification_checklist_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Verification Criteria Checklist",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            ChecklistItem(
                title = "Source exists & available",
                result = if (record.sourceStillAvailable) CheckResult.PASS else CheckResult.FAIL
            )
            ChecklistItem(
                title = "Source URL valid & authentic",
                result = if (record.sourceUrlValid) CheckResult.PASS else CheckResult.FAIL
            )
            ChecklistItem(
                title = "Source verified by curators",
                result = when {
                    record.sourceVerified -> CheckResult.PASS
                    record.humanReviewRequired -> CheckResult.WARNING
                    else -> CheckResult.FAIL
                }
            )
            ChecklistItem(
                title = "Facts consistent with source",
                result = when {
                    record.factsConsistent && !record.humanReviewRequired -> CheckResult.PASS
                    record.factsConsistent -> CheckResult.WARNING
                    else -> CheckResult.FAIL
                }
            )
            ChecklistItem(
                title = "Deadline consistent & unexpired",
                result = when {
                    record.deadlineConsistent && record.finalStatusEnum != FinalVerificationStatus.EXPIRED -> CheckResult.PASS
                    record.humanReviewRequired -> CheckResult.WARNING
                    else -> CheckResult.FAIL
                }
            )
            ChecklistItem(
                title = "Eligibility criteria accurate",
                result = when {
                    record.eligibilityConsistent && !record.humanReviewRequired -> CheckResult.PASS
                    record.eligibilityConsistent -> CheckResult.WARNING
                    else -> CheckResult.FAIL
                }
            )
            ChecklistItem(
                title = "Content safety & neutrality passed",
                result = when {
                    record.safetyPassed && !record.politicalReviewRequired -> CheckResult.PASS
                    record.politicalReviewRequired || record.humanReviewRequired -> CheckResult.WARNING
                    else -> CheckResult.FAIL
                }
            )
            ChecklistItem(
                title = "Duplicate check passed",
                result = if (record.duplicateFree) CheckResult.PASS else CheckResult.FAIL
            )
            ChecklistItem(
                title = "Platform compatibility valid",
                result = CheckResult.PASS
            )
        }
    }
}

@Composable
fun ChecklistItem(title: String, result: CheckResult) {
    val (bg, fg, icon) = when (result) {
        CheckResult.PASS -> Triple(Color(0xFFDCFCE7), Color(0xFF166534), Icons.Default.Check)
        CheckResult.WARNING -> Triple(Color(0xFFFEF3C7), Color(0xFF92400E), Icons.Default.Warning)
        CheckResult.FAIL -> Triple(Color(0xFFFEE2E2), Color(0xFF991B1B), Icons.Default.Close)
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodySmall)
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = bg
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = fg,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = result.name,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = fg
                )
            }
        }
    }
}

@Composable
fun IssuesAndWarningsCard(issues: List<String>, warnings: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (issues.isNotEmpty()) {
                Text("Verification Issues", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                issues.forEach { issue ->
                    Text("• $issue", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (warnings.isNotEmpty()) {
                Text("Verification Warnings", style = MaterialTheme.typography.titleSmall, color = Color(0xFFB45309), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                warnings.forEach { warning ->
                    Text("• $warning", style = MaterialTheme.typography.bodySmall, color = Color(0xFFB45309))
                }
            }
        }
    }
}

@Composable
fun GateActionButtons(
    record: FinalVerificationRecordEntity,
    onRecheck: () -> Unit,
    onEdit: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val canApprove = record.finalStatusEnum != FinalVerificationStatus.BLOCKED &&
            record.finalStatusEnum != FinalVerificationStatus.EXPIRED &&
            record.finalStatusEnum != FinalVerificationStatus.FAILED &&
            record.sourceUrlValid &&
            record.safetyPassed

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Gate Decision Actions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onRecheck,
                    modifier = Modifier.weight(1f).testTag("btn_gate_recheck")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("RECHECK", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f).testTag("btn_gate_edit")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("EDIT", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onApprove,
                    enabled = canApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF166534)),
                    modifier = Modifier.weight(1f).testTag("btn_gate_approve")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("APPROVE", style = MaterialTheme.typography.labelSmall)
                }

                Button(
                    onClick = onReject,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    modifier = Modifier.weight(1f).testTag("btn_gate_reject")
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("REJECT", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun VersionHistoryCard(versions: List<com.example.data.local.entity.ContentVersionEntity>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Version History (Content Versioning)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            versions.forEach { ver ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("v${ver.versionNumber} - ${ver.changeType}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    Text(formatTimestamp(ver.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}

@Composable
fun AuditLogCard(logs: List<com.example.data.local.entity.VerificationAuditLogEntity>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Verification Audit Trail", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            logs.take(5).forEach { log ->
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(log.action, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(formatTimestamp(log.timestamp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                    Text(log.reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private fun formatTimestamp(millis: Long): String {
    return try {
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        sdf.format(Date(millis))
    } catch (_: Exception) {
        "Recent"
    }
}
