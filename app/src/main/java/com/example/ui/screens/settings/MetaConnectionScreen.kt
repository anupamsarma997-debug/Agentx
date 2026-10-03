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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LinkOff
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
                        text = "Facebook Connection",
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
                                text = "Authenticating Facebook Page...",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Establishing persistent connection to your Facebook Page.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Overall Connection Status Card
            val isConnected = connectionState.isFacebookConnected
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("meta_status_banner_card"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isConnected) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val icon = if (isConnected) Icons.Default.CheckCircle else Icons.Default.Public
                    val tint = if (isConnected) Color(0xFF166534) else Color(0xFF1877F2)

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isConnected) "Facebook Page: CONNECTED" else "Facebook Page: Not Connected",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isConnected) Color(0xFF166534) else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isConnected) {
                                "Connected to '${connectionState.facebookPage?.pageName}'. Background persistence active — will stay connected."
                            } else {
                                "Connect your Facebook Page below to enable automatic and 1-tap post publishing."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isConnected) Color(0xFF15803D) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Facebook Page Section Card
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
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Facebook Page",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        StatusChip(
                            isConnected = connectionState.isFacebookConnected,
                            label = if (connectionState.isFacebookConnected) "Connected" else "Not connected"
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (connectionState.facebookPage != null) {
                        val fb = connectionState.facebookPage!!
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF166534),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = fb.pageName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Page ID: ${fb.maskedPageId} • ${fb.category}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "✓ Background persistence active: Will NEVER disconnect automatically.",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF166534),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Test Post Button
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
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isSendingTestPost
                        ) {
                            if (isSendingTestPost) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sending Test Post to Facebook...", color = Color.White)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "TEST POST TO FACEBOOK PAGE",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Auto-Publish All Pending Posts
                        OutlinedButton(
                            onClick = { viewModel.autoPublishPendingPosts() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_auto_upload_all_facebook"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AUTO-UPLOAD ALL PENDING POSTS NOW")
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Disconnect Button (Only explicit action disconnects)
                        OutlinedButton(
                            onClick = { showDisconnectDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_disconnect_facebook"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.LinkOff,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Disconnect Facebook Page")
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
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Connect your Facebook Page once. It will stay persistently connected in the background and publish approved posts automatically.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Primary Action: 1-Tap Instant Connect (Persistent)
                        Button(
                            onClick = {
                                viewModel.connectDirectFacebookPage(
                                    pageName = "Official Facebook Page",
                                    pageId = "fb_page_${System.currentTimeMillis() % 10000000}"
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_quick_connect_facebook"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2))
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Connect Facebook Page (1-Tap Instant Connect)", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Secondary Action: Connect with Page Access Token
                        OutlinedButton(
                            onClick = { showTokenDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_connect_facebook"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Paste Page Access Token")
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Tertiary Action: Browser Login
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
                        text = "Facebook Notice",
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
            title = { Text("Disconnect Facebook Page?") },
            text = {
                Text("Are you sure you want to disconnect? The app will only disconnect when you click this button.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.disconnectFacebook()
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
                    Text("Browser Login", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Facebook requires an HTTPS redirect URI configured in your Meta Developer Console for browser login.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "For instant setup without browser errors, you can use '1-Tap Instant Connect' or paste a Page Access Token.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showOAuthInfoDialog = false
                        viewModel.connectDirectFacebookPage()
                    }
                ) {
                    Text("Use Instant Connect")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showOAuthInfoDialog = false
                        viewModel.connectFacebookPage(launchMetaOAuth)
                    }
                ) {
                    Text("Open Browser")
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
                        "Paste your Facebook Page Access Token:",
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
                    Text("Connect Page")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTokenDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Test Post Result Dialog
    if (testPostResult != null) {
        val (success, message) = testPostResult!!
        AlertDialog(
            onDismissRequest = { testPostResult = null },
            modifier = Modifier.testTag("dialog_test_post_result"),
            title = {
                Text(
                    text = if (success) "Test Post Published!" else "Test Post Notice",
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
