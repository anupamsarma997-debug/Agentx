package com.example.ui.screens.settings

import android.content.Intent
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
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    val connectionState by viewModel.metaConnection.collectAsState()
    val configDialogMessage by viewModel.metaConfigDialogMessage.collectAsState()
    val scrollState = rememberScrollState()

    var showDisconnectDialog by remember { mutableStateOf(false) }
    var showPreviewMockDialog by remember { mutableStateOf(false) }
    var showTokenDialog by remember { mutableStateOf(false) }
    var showOAuthInfoDialog by remember { mutableStateOf(false) }
    var directTokenInput by remember { mutableStateOf("") }
    var isSendingTestPost by remember { mutableStateOf(false) }
    var testPostResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    val launchMetaOAuth: (android.net.Uri) -> Unit = { authUri ->
        try {
            val intent = Intent(Intent.ACTION_VIEW, authUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            viewModel.showMessage("Could not open browser: ${e.localizedMessage}")
        }
    }

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
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshMetaConnection() },
                        modifier = Modifier.testTag("btn_meta_refresh")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Connection Status"
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
            // Authenticating Progress Card
            if (connectionState.isAuthenticating) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("meta_authenticating_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Authenticating with Meta...",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Connecting your Facebook Page and Instagram accounts.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
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
                        connectionState.status == MetaConnectionStatus.OPENING_META ||
                        connectionState.status == MetaConnectionStatus.WAITING_FOR_AUTHORIZATION ||
                        connectionState.status == MetaConnectionStatus.CONNECTING -> Color(0xFFDBEAFE)
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
                        connectionState.status == MetaConnectionStatus.OPENING_META ||
                        connectionState.status == MetaConnectionStatus.WAITING_FOR_AUTHORIZATION ||
                        connectionState.status == MetaConnectionStatus.CONNECTING -> Icons.Default.Info to Color(0xFF1D4ED8)
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
                        } else when (connectionState.status) {
                            MetaConnectionStatus.DISCONNECTED -> {
                                Text(
                                    text = "No Meta accounts connected. Connect your Facebook Page & Instagram account below.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            MetaConnectionStatus.OPENING_META -> {
                                Text(
                                    text = "Launching system browser for Meta login...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = tint.copy(alpha = 0.9f)
                                )
                            }
                            MetaConnectionStatus.WAITING_FOR_AUTHORIZATION -> {
                                Text(
                                    text = "Waiting for authorization in browser. Return here once finished.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = tint.copy(alpha = 0.9f)
                                )
                            }
                            MetaConnectionStatus.CONNECTING -> {
                                Text(
                                    text = "Connecting and validating with Meta Graph API...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = tint.copy(alpha = 0.9f)
                                )
                            }
                            else -> {}
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
                                text = "Page ID: ${fb.maskedPageId} • ${fb.category}",
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
                                onClick = { viewModel.reconnectFacebook(launchMetaOAuth) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_reconnect_facebook"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Reconnect")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                isSendingTestPost = true
                                viewModel.sendTestPostToFacebookPage { success, message ->
                                    isSendingTestPost = false
                                    testPostResult = Pair(success, message)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_test_post_facebook"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isSendingTestPost
                        ) {
                            if (isSendingTestPost) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sending Test Post...", color = Color.White)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "TEST POST TO FACEBOOK PAGE",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Connect directly with a Page Access Token for instant setup without browser redirect errors.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Primary Action: Connect with Page Access Token (Recommended & Instant)
                        Button(
                            onClick = { showTokenDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_connect_facebook"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2))
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Connect Facebook Page (Token)")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Secondary Action: Browser Login with Guidance Dialog
                        OutlinedButton(
                            onClick = { showOAuthInfoDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_connect_page_token"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Browser Login (OAuth)")
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

    // Browser OAuth Guidance Dialog
    if (showOAuthInfoDialog) {
        AlertDialog(
            onDismissRequest = { showOAuthInfoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Browser Login Notice", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Facebook requires an HTTPS redirect URI configured in your Meta Developer Console for browser login. If unconfigured, Facebook shows:\n\"প্যারামিটারে কোনো রিডাইরেক্ট URI নেই: এই URI-তে কোনো রিডাইরেক্ট নেই\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Recommended: Use 'Connect with Page Token' below for an immediate, reliable connection without browser configuration.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showOAuthInfoDialog = false
                        showTokenDialog = true
                    }
                ) {
                    Text("Use Page Token (Instant)")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showOAuthInfoDialog = false
                        viewModel.connectFacebookPage(launchMetaOAuth)
                    }
                ) {
                    Text("Open Browser Anyway")
                }
            }
        )
    }

    // Direct Page Access Token Dialog
    if (showTokenDialog) {
        AlertDialog(
            onDismissRequest = { showTokenDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Connect via Page Token", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Paste a Facebook Page Access Token (from Meta Graph API Explorer or Meta Business Suite):",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = directTokenInput,
                        onValueChange = { directTokenInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_page_token"),
                        placeholder = { Text("EAA...") },
                        singleLine = false,
                        maxLines = 4,
                        label = { Text("Page Access Token") }
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                "How to get a Page Token in 30 seconds:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "1. Visit developers.facebook.com/tools/explorer\n2. In 'User or Page', select your Facebook Page\n3. Add permissions: pages_show_list, pages_read_engagement, pages_manage_posts\n4. Click 'Generate Access Token' and paste here.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.connectWithDirectPageToken(directTokenInput) { success ->
                            if (success) {
                                showTokenDialog = false
                                directTokenInput = ""
                            }
                        }
                    },
                    modifier = Modifier.testTag("btn_submit_token")
                ) {
                    Text("Verify & Connect")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTokenDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (testPostResult != null) {
        val (success, message) = testPostResult!!
        AlertDialog(
            onDismissRequest = { testPostResult = null },
            modifier = Modifier.testTag("dialog_test_post_result"),
            title = {
                Text(
                    text = if (success) "Test Post Successful!" else "Test Post Failed",
                    fontWeight = FontWeight.Bold,
                    color = if (success) Color(0xFF166534) else Color(0xFFDC2626)
                )
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { testPostResult = null },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (success) Color(0xFF166534) else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("btn_close_test_post_dialog")
                ) {
                    Text("OK")
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
        MetaConnectionStatus.DISCONNECTED -> "Not connected"
        MetaConnectionStatus.OPENING_META -> "Opening Meta"
        MetaConnectionStatus.WAITING_FOR_AUTHORIZATION -> "Waiting for authorization"
        MetaConnectionStatus.CONNECTING -> "Connecting"
        MetaConnectionStatus.CONNECTED -> "Connected"
        MetaConnectionStatus.PERMISSION_REQUIRED -> "Permission Required"
        MetaConnectionStatus.CONFIGURATION_REQUIRED -> "Configuration Required"
        MetaConnectionStatus.EXPIRED -> "Token expired/revoked"
        MetaConnectionStatus.ERROR -> "Failed"
    }
}
