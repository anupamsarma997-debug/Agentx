package com.example.ui.screens.settings

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.meta.InstagramAccountType
import com.example.data.model.meta.MetaConnectionStatus
import com.example.ui.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetaConnectionScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val connectionState by viewModel.metaConnection.collectAsState()
    val configDialogMessage by viewModel.metaConfigDialogMessage.collectAsState()
    val scrollState = rememberScrollState()

    var showDisconnectDialog by remember { mutableStateOf(false) }
    var showPreviewMockDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Social Accounts",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_meta_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // DEMO / SANDBOX Warning Banner (prominently shown when in simulation)
            if (connectionState.isDemoSandbox) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sandbox_mode_banner"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFD97706)
                        ) {
                            Text(
                                text = "DEMO / SANDBOX",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Simulated connection for testing. This is not a real Meta connection and cannot publish to live Facebook/Instagram servers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }

            // Overall Connection Status Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("meta_status_banner_card"),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        connectionState.isDemoSandbox -> Color(0xFFFEF3C7)
                        connectionState.status == MetaConnectionStatus.CONNECTED -> Color(0xFFDCFCE7)
                        connectionState.status == MetaConnectionStatus.PERMISSION_REQUIRED -> Color(0xFFFEF3C7)
                        connectionState.status == MetaConnectionStatus.CONFIGURATION_REQUIRED -> Color(0xFFEFF6FF)
                        connectionState.status == MetaConnectionStatus.EXPIRED || connectionState.status == MetaConnectionStatus.ERROR -> Color(0xFFFEE2E2)
                        else -> MaterialTheme.colorScheme.surfaceContainerHigh
                    }
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val (icon, tint) = when {
                        connectionState.isDemoSandbox -> Icons.Default.Info to Color(0xFFB45309)
                        connectionState.status == MetaConnectionStatus.CONNECTED -> Icons.Default.CheckCircle to Color(0xFF166534)
                        connectionState.status == MetaConnectionStatus.PERMISSION_REQUIRED -> Icons.Default.Warning to Color(0xFF92400E)
                        connectionState.status == MetaConnectionStatus.CONFIGURATION_REQUIRED -> Icons.Default.Info to Color(0xFF1E40AF)
                        connectionState.status == MetaConnectionStatus.EXPIRED || connectionState.status == MetaConnectionStatus.ERROR -> Icons.Default.ErrorOutline to Color(0xFF991B1B)
                        else -> Icons.Default.LinkOff to MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (connectionState.isDemoSandbox) {
                                "Connection Status: DEMO / SANDBOX"
                            } else {
                                "Connection Status: ${formatStatus(connectionState.status)}"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = tint
                        )
                        if (connectionState.errorMessage != null) {
                            Text(
                                text = connectionState.errorMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = tint.copy(alpha = 0.9f)
                            )
                        } else if (connectionState.status == MetaConnectionStatus.DISCONNECTED) {
                            Text(
                                text = "No Meta accounts connected. Connect your Facebook Page & Instagram account below.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Notice about Instagram Professional requirement
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Instagram publishing requires an eligible Professional (Business or Creator) account connected through Meta.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Facebook Page Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("facebook_page_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = "Facebook",
                                tint = Color(0xFF1877F2),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Facebook Page",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (connectionState.isDemoSandbox && connectionState.isFacebookConnected) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFEF3C7),
                                    modifier = Modifier.padding(end = 6.dp)
                                ) {
                                    Text(
                                        text = "DEMO / SANDBOX",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            StatusChip(
                                isConnected = connectionState.isFacebookConnected,
                                label = if (connectionState.isFacebookConnected) "Connected" else "Not connected"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (connectionState.facebookPage != null) {
                        val fb = connectionState.facebookPage!!
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = fb.pageName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Page ID: ${fb.maskedPageId}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.disconnectFacebook() },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_disconnect_facebook"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Disconnect")
                            }
                            OutlinedButton(
                                onClick = { viewModel.reconnectFacebook() },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_reconnect_facebook"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Reconnect")
                            }
                        }
                    } else {
                        Text(
                            text = "Connect the Facebook Page that administers your social channels.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.connectFacebookPage() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_connect_facebook"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2))
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Connect Facebook Page")
                        }
                    }
                }
            }

            // Instagram Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("instagram_account_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Instagram",
                                tint = Color(0xFFE1306C),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Instagram",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (connectionState.isDemoSandbox && connectionState.isInstagramConnected) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFEF3C7),
                                    modifier = Modifier.padding(end = 6.dp)
                                ) {
                                    Text(
                                        text = "DEMO / SANDBOX",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            StatusChip(
                                isConnected = connectionState.isInstagramConnected,
                                label = if (connectionState.isInstagramConnected) "Connected" else "Not connected"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (connectionState.instagramAccount != null) {
                        val ig = connectionState.instagramAccount!!
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "@${ig.username}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Type: ${ig.accountType.displayName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (ig.isEligibleForPublishing) MaterialTheme.colorScheme.primary else Color(0xFFDC2626)
                            )
                            Text(
                                text = "Account ID: ${ig.maskedAccountId}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.disconnectInstagram() },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_disconnect_instagram"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Disconnect")
                            }
                            OutlinedButton(
                                onClick = { viewModel.reconnectInstagram() },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_reconnect_instagram"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Reconnect")
                            }
                        }
                    } else {
                        Text(
                            text = "Link the Instagram Professional account associated with your Facebook Page.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.connectInstagram() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_connect_instagram"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE1306C))
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Connect Instagram")
                        }
                    }
                }
            }

            // Developer Testing Tool: Simulate Connection for UI Verification
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Architecture & Testing Sandbox",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Test connection states safely. Simulated sandbox connections are clearly marked as DEMO / SANDBOX and never mixed with real credentials.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showPreviewMockDialog = true },
                        modifier = Modifier.testTag("btn_open_test_sandbox")
                    ) {
                        Text("Simulate Verified Connection")
                    }
                }
            }
        }
    }

    // Meta Developer Configuration Required Dialog
    if (configDialogMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissMetaConfigDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Meta Configuration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Text(
                    text = configDialogMessage ?: "",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.dismissMetaConfigDialog() }) {
                    Text("OK")
                }
            }
        )
    }

    // Disconnect Confirmation Dialog
    if (showDisconnectDialog) {
        AlertDialog(
            onDismissRequest = { showDisconnectDialog = false },
            title = { Text("Disconnect Meta Accounts?") },
            text = {
                Text("This will remove connected Facebook Page and Instagram references and clear cached tokens from secure memory.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.disconnectMeta()
                        showDisconnectDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Disconnect")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisconnectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Testing Sandbox Dialog
    if (showPreviewMockDialog) {
        AlertDialog(
            onDismissRequest = { showPreviewMockDialog = false },
            title = { Text("Simulate Connection State (Sandbox)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select a sandbox scenario to verify UI & model compliance (explicitly marked as DEMO / SANDBOX):")
                    Button(
                        onClick = {
                            viewModel.setVerifiedMetaPreview(
                                pageName = "Assam Tech & Jobs",
                                pageId = "10492850239",
                                instagramUsername = "assamtechjobs",
                                instagramType = InstagramAccountType.PROFESSIONAL_BUSINESS
                            )
                            showPreviewMockDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Eligible Professional Account (DEMO / SANDBOX)")
                    }
                    Button(
                        onClick = {
                            viewModel.setVerifiedMetaPreview(
                                pageName = "Personal Tech Blog",
                                pageId = "20492850239",
                                instagramUsername = "rupam_personal",
                                instagramType = InstagramAccountType.PERSONAL
                            )
                            showPreviewMockDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) {
                        Text("Personal Account - Ineligible (DEMO / SANDBOX)")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPreviewMockDialog = false }) {
                    Text("Dismiss")
                }
            }
        )
    }
}

@Composable
private fun StatusChip(
    isConnected: Boolean,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isConnected) Color(0xFFDCFCE7) else Color(0xFFF3F4F6),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (isConnected) Color(0xFF166534) else Color(0xFF9CA3AF))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isConnected) Color(0xFF166534) else Color(0xFF4B5563)
            )
        }
    }
}

private fun formatStatus(status: MetaConnectionStatus): String {
    return when (status) {
        MetaConnectionStatus.DISCONNECTED -> "Not Connected"
        MetaConnectionStatus.CONNECTED -> "Connected"
        MetaConnectionStatus.PERMISSION_REQUIRED -> "Permission Required"
        MetaConnectionStatus.CONFIGURATION_REQUIRED -> "Configuration Required"
        MetaConnectionStatus.EXPIRED -> "Connection Expired"
        MetaConnectionStatus.ERROR -> "Error"
    }
}
