package com.example.ui.screens.opportunities

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.OpportunityEntity
import com.example.data.model.opportunity.OpportunityCategory
import com.example.data.model.opportunity.OpportunityRegion
import com.example.ui.viewmodel.AppViewModel

@Composable
fun OpportunitiesScreen(
    viewModel: AppViewModel,
    onNavigateToDetail: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val opportunities by viewModel.filteredOpportunities.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsState()
    val selectedRegion by viewModel.selectedRegionFilter.collectAsState()
    val lastScoutResult by viewModel.lastScoutResult.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Top Header with [ SCAN NOW ] Action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "OPPORTUNITIES",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${opportunities.size} active opportunities found",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { viewModel.triggerScoutScan() },
                enabled = !isScanning,
                modifier = Modifier.testTag("btn_scout_scan_now"),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Scanning...")
                } else {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SCAN NOW")
                }
            }
        }

        // Horizontal Filter Chips
        val filterScrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(filterScrollState)
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // "All" Chip
            FilterChip(
                selected = selectedCategory == null && selectedRegion == null,
                onClick = {
                    viewModel.setCategoryFilter(null)
                    viewModel.setRegionFilter(null)
                },
                label = { Text("All") },
                modifier = Modifier.testTag("filter_all")
            )

            // Regional Filter: Assam
            FilterChip(
                selected = selectedRegion == OpportunityRegion.ASSAM,
                onClick = {
                    viewModel.setRegionFilter(
                        if (selectedRegion == OpportunityRegion.ASSAM) null else OpportunityRegion.ASSAM
                    )
                },
                label = { Text("Assam") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.testTag("filter_assam")
            )

            // Regional Filter: Northeast
            FilterChip(
                selected = selectedRegion == OpportunityRegion.NORTHEAST_INDIA,
                onClick = {
                    viewModel.setRegionFilter(
                        if (selectedRegion == OpportunityRegion.NORTHEAST_INDIA) null else OpportunityRegion.NORTHEAST_INDIA
                    )
                },
                label = { Text("Northeast") },
                modifier = Modifier.testTag("filter_northeast")
            )

            // Regional Filter: India
            FilterChip(
                selected = selectedRegion == OpportunityRegion.INDIA,
                onClick = {
                    viewModel.setRegionFilter(
                        if (selectedRegion == OpportunityRegion.INDIA) null else OpportunityRegion.INDIA
                    )
                },
                label = { Text("India") },
                modifier = Modifier.testTag("filter_india")
            )

            // Category Filter: Jobs
            FilterChip(
                selected = selectedCategory == OpportunityCategory.JOB,
                onClick = {
                    viewModel.setCategoryFilter(
                        if (selectedCategory == OpportunityCategory.JOB) null else OpportunityCategory.JOB
                    )
                },
                label = { Text("Jobs") },
                modifier = Modifier.testTag("filter_jobs")
            )

            // Category Filter: Government Jobs
            FilterChip(
                selected = selectedCategory == OpportunityCategory.GOVERNMENT_JOB,
                onClick = {
                    viewModel.setCategoryFilter(
                        if (selectedCategory == OpportunityCategory.GOVERNMENT_JOB) null else OpportunityCategory.GOVERNMENT_JOB
                    )
                },
                label = { Text("Government Jobs") },
                modifier = Modifier.testTag("filter_gov_jobs")
            )

            // Category Filter: Internships
            FilterChip(
                selected = selectedCategory == OpportunityCategory.INTERNSHIP,
                onClick = {
                    viewModel.setCategoryFilter(
                        if (selectedCategory == OpportunityCategory.INTERNSHIP) null else OpportunityCategory.INTERNSHIP
                    )
                },
                label = { Text("Internships") },
                modifier = Modifier.testTag("filter_internships")
            )

            // Category Filter: Scholarships
            FilterChip(
                selected = selectedCategory == OpportunityCategory.SCHOLARSHIP,
                onClick = {
                    viewModel.setCategoryFilter(
                        if (selectedCategory == OpportunityCategory.SCHOLARSHIP) null else OpportunityCategory.SCHOLARSHIP
                    )
                },
                label = { Text("Scholarships") },
                modifier = Modifier.testTag("filter_scholarships")
            )

            // Category Filter: Grants
            FilterChip(
                selected = selectedCategory == OpportunityCategory.GRANT,
                onClick = {
                    viewModel.setCategoryFilter(
                        if (selectedCategory == OpportunityCategory.GRANT) null else OpportunityCategory.GRANT
                    )
                },
                label = { Text("Grants") },
                modifier = Modifier.testTag("filter_grants")
            )

            // Category Filter: Hackathons
            FilterChip(
                selected = selectedCategory == OpportunityCategory.HACKATHON,
                onClick = {
                    viewModel.setCategoryFilter(
                        if (selectedCategory == OpportunityCategory.HACKATHON) null else OpportunityCategory.HACKATHON
                    )
                },
                label = { Text("Hackathons") },
                modifier = Modifier.testTag("filter_hackathons")
            )

            // Category Filter: Startup
            FilterChip(
                selected = selectedCategory == OpportunityCategory.STARTUP,
                onClick = {
                    viewModel.setCategoryFilter(
                        if (selectedCategory == OpportunityCategory.STARTUP) null else OpportunityCategory.STARTUP
                    )
                },
                label = { Text("Startup") },
                modifier = Modifier.testTag("filter_startup")
            )

            // Category Filter: MSME & Business Schemes
            FilterChip(
                selected = selectedCategory == OpportunityCategory.BUSINESS,
                onClick = {
                    viewModel.setCategoryFilter(
                        if (selectedCategory == OpportunityCategory.BUSINESS) null else OpportunityCategory.BUSINESS
                    )
                },
                label = { Text("MSME & Schemes") },
                modifier = Modifier.testTag("filter_msme_business")
            )

            // Category Filter: News & Creator Updates
            FilterChip(
                selected = selectedCategory == OpportunityCategory.NEWS,
                onClick = {
                    viewModel.setCategoryFilter(
                        if (selectedCategory == OpportunityCategory.NEWS) null else OpportunityCategory.NEWS
                    )
                },
                label = { Text("News & Creator Updates") },
                modifier = Modifier.testTag("filter_news")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Empty State or List
        if (opportunities.isEmpty()) {
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
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No Opportunities Discovered Yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap 'SCAN NOW' to scout official government announcements, hackathons, and regional initiatives.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.triggerScoutScan() },
                            modifier = Modifier.testTag("btn_empty_scan")
                        ) {
                            Text("SCAN NOW")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(opportunities, key = { it.id }) { opportunity ->
                    OpportunityCard(
                        opportunity = opportunity,
                        onClick = { onNavigateToDetail(opportunity.id) },
                        onViewSource = {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(opportunity.sourceUrl))
                            context.startActivity(browserIntent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OpportunityCard(
    opportunity: OpportunityEntity,
    onClick: () -> Unit,
    onViewSource: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("opportunity_card_${opportunity.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Category Tag & Status Badge
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
                        text = opportunity.categoryEnum.displayName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                VerificationBadge(status = opportunity.verificationStatusEnum)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = opportunity.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Organization
            if (!opportunity.organization.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = opportunity.organization,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Region & Deadline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Region: ${opportunity.regionEnum.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = if (opportunity.isExpired) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Deadline: ${opportunity.deadline ?: "Not specified"}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (opportunity.isExpired) FontWeight.Bold else FontWeight.Normal,
                        color = if (opportunity.isExpired) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Source & View Source Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Source: ${opportunity.sourceName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                OutlinedButton(
                    onClick = onViewSource,
                    modifier = Modifier.testTag("btn_view_source_${opportunity.id}"),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("VIEW SOURCE", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
