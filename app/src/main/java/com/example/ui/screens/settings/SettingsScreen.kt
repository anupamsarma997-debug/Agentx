package com.example.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.AppViewModel
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    viewModel: AppViewModel,
    onNavigateToMetaConnection: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val metaConnection by viewModel.metaConnection.collectAsState()
    val scrollState = rememberScrollState()

    var showTimeDialog by remember { mutableStateOf(false) }
    var showTargetDialog by remember { mutableStateOf(false) }
    var showTimezoneDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings & Guardrails",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Configure safe operating windows and free-tier daily quotas.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // META SOCIAL ACCOUNTS Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings_meta_accounts_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "META SOCIAL ACCOUNTS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (metaConnection.isFullyConnected) {
                        "Connected: ${metaConnection.facebookPage?.pageName} & @${metaConnection.instagramAccount?.username}"
                    } else if (metaConnection.isFacebookConnected) {
                        "Connected: ${metaConnection.facebookPage?.pageName} (Instagram pending)"
                    } else {
                        "Connect your Facebook Page & Instagram Professional account for scheduled publishing."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onNavigateToMetaConnection,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_manage_meta_accounts"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Public, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Facebook Page + Instagram")
                }
            }
        }

        // General Switches Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Free Mode Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Free Mode",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Stops when free tier is exhausted. Never triggers billing.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = settings.freeMode,
                        onCheckedChange = { viewModel.setFreeMode(it) },
                        modifier = Modifier.testTag("settings_freemode_switch")
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Automation Enabled Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Automation Enabled",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Allows agent activities during the scheduled window.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = settings.automationEnabled,
                        onCheckedChange = { viewModel.setAutomationEnabled(it) },
                        modifier = Modifier.testTag("settings_automation_switch")
                    )
                }
            }
        }

        // SCOUT SETTINGS Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings_scout_section_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SCOUT SETTINGS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Filter regions and categories during opportunity discovery.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Assam Priority
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Assam Priority", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("Prioritize state government exams, schemes, and startup programs", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = settings.assamPriority,
                        onCheckedChange = {
                            viewModel.updateScoutSettings(
                                assam = it,
                                northeast = settings.northeastPriority,
                                india = settings.indiaOpportunities,
                                international = settings.internationalOpportunities,
                                news = settings.newsCollection
                            )
                        },
                        modifier = Modifier.testTag("switch_scout_assam")
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // Northeast Priority
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Northeast Priority", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("Include regional development initiatives, grants, and youth programs", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = settings.northeastPriority,
                        onCheckedChange = {
                            viewModel.updateScoutSettings(
                                assam = settings.assamPriority,
                                northeast = it,
                                india = settings.indiaOpportunities,
                                international = settings.internationalOpportunities,
                                news = settings.newsCollection
                            )
                        },
                        modifier = Modifier.testTag("switch_scout_northeast")
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // India Opportunities
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("India Opportunities", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("Nationwide central government schemes, SIH, AICTE fellowships", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = settings.indiaOpportunities,
                        onCheckedChange = {
                            viewModel.updateScoutSettings(
                                assam = settings.assamPriority,
                                northeast = settings.northeastPriority,
                                india = it,
                                international = settings.internationalOpportunities,
                                news = settings.newsCollection
                            )
                        },
                        modifier = Modifier.testTag("switch_scout_india")
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // International Opportunities
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("International Opportunities", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("Global hackathons and international fellowships (Default: OFF)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = settings.internationalOpportunities,
                        onCheckedChange = {
                            viewModel.updateScoutSettings(
                                assam = settings.assamPriority,
                                northeast = settings.northeastPriority,
                                india = settings.indiaOpportunities,
                                international = it,
                                news = settings.newsCollection
                            )
                        },
                        modifier = Modifier.testTag("switch_scout_international")
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // News Collection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("News Collection", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("Gather factual public announcements from verified entities", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = settings.newsCollection,
                        onCheckedChange = {
                            viewModel.updateScoutSettings(
                                assam = settings.assamPriority,
                                northeast = settings.northeastPriority,
                                india = settings.indiaOpportunities,
                                international = settings.internationalOpportunities,
                                news = it
                            )
                        },
                        modifier = Modifier.testTag("switch_scout_news")
                    )
                }
            }
        }

        // Timing & Targets Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Automation Window
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTimeDialog = true }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Automation Window",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${formatHour(settings.automationStartHour, settings.automationStartMinute)} – ${formatHour(settings.automationEndHour, settings.automationEndMinute)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Edit window",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Daily Targets
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTargetDialog = true }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PostAdd,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Daily Targets",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${settings.dailyPostTarget} Posts, ${settings.dailyReelTarget} Reels (Target: 10 items/day)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Edit targets",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Timezone
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTimezoneDialog = true }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Operating Timezone",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = settings.timezone,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Edit timezone",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Reset Quota Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Reset Daily Counters",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Reset today's recorded post (${settings.todayPostCount}) and reel (${settings.todayReelCount}) counters to 0.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = {
                        viewModel.resetTodayCounts()
                        viewModel.showMessage("Daily counters reset to 0.")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset counts",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    // Time Window Dialog
    if (showTimeDialog) {
        var startHour by remember { mutableIntStateOf(settings.automationStartHour) }
        var endHour by remember { mutableIntStateOf(settings.automationEndHour) }

        AlertDialog(
            onDismissRequest = { showTimeDialog = false },
            title = { Text("Set Automation Window") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Select the 1-hour window for daily autonomous processing:")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Start: ${formatHour(startHour, 0)}")
                        Row {
                            OutlinedButton(onClick = { if (startHour > 0) startHour-- }) { Text("-") }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = { if (startHour < 23) startHour++ }) { Text("+") }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("End: ${formatHour(endHour, 0)}")
                        Row {
                            OutlinedButton(onClick = { if (endHour > 0) endHour-- }) { Text("-") }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = { if (endHour < 24) endHour++ }) { Text("+") }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateAutomationWindow(startHour, 0, endHour, 0)
                        showTimeDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Daily Targets Dialog
    if (showTargetDialog) {
        var postSlider by remember { mutableFloatStateOf(settings.dailyPostTarget.toFloat()) }
        var reelSlider by remember { mutableFloatStateOf(settings.dailyReelTarget.toFloat()) }

        AlertDialog(
            onDismissRequest = { showTargetDialog = false },
            title = { Text("Daily Content Targets") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Posts Target: ${postSlider.roundToInt()} per day")
                    Slider(
                        value = postSlider,
                        onValueChange = { postSlider = it },
                        valueRange = 1f..15f,
                        steps = 13
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Reels Target: ${reelSlider.roundToInt()} per day")
                    Slider(
                        value = reelSlider,
                        onValueChange = { reelSlider = it },
                        valueRange = 0f..5f,
                        steps = 4
                    )
                    Text(
                        text = "Total daily goal: ${postSlider.roundToInt() + reelSlider.roundToInt()} items. (Target default: 8 posts + 2 reels = 10 items).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateDailyTargets(postSlider.roundToInt(), reelSlider.roundToInt())
                        showTargetDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTargetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Timezone Dialog
    if (showTimezoneDialog) {
        val availableTimezones = listOf(
            "Asia/Kolkata",
            "UTC",
            "Asia/Dubai",
            "America/New_York",
            "Europe/London"
        )

        AlertDialog(
            onDismissRequest = { showTimezoneDialog = false },
            title = { Text("Select Operating Timezone") },
            text = {
                Column {
                    availableTimezones.forEach { tz ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateTimezone(tz)
                                    showTimezoneDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tz,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (tz == settings.timezone) FontWeight.Bold else FontWeight.Normal,
                                color = if (tz == settings.timezone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTimezoneDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

private fun formatHour(hour: Int, minute: Int): String {
    val ampm = if (hour >= 12 && hour < 24) "PM" else "AM"
    val displayHour = when {
        hour == 0 || hour == 24 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return String.format("%02d:%02d %s", displayHour, minute, ampm)
}
