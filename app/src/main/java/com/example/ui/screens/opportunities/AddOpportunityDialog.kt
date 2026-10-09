package com.example.ui.screens.opportunities

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.OpportunityEntity
import com.example.data.model.opportunity.OpportunityCategory
import com.example.data.model.opportunity.OpportunityRegion

data class OpportunityTemplate(
    val label: String,
    val title: String,
    val category: String,
    val region: String,
    val org: String,
    val eligibility: String,
    val url: String,
    val deadline: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddOpportunityDialog(
    onDismiss: () -> Unit,
    onSaveOpportunity: (
        title: String,
        description: String,
        category: String,
        region: String,
        org: String,
        eligibility: String,
        url: String,
        deadline: String
    ) -> Unit
) {
    val quickTemplates = remember {
        listOf(
            OpportunityTemplate(
                label = "👮 অসম আৰক্ষী SI ২০২৬",
                title = "অসম আৰক্ষী উপ-পৰিদৰ্শক (SI) আৰু কমাণ্ডো বেটেলিয়ন নতুন নিযুক্তি ২০২৬",
                category = OpportunityCategory.GOVERNMENT_JOB.name,
                region = OpportunityRegion.ASSAM.name,
                org = "SLPRB Assam (slprbassam.in)",
                eligibility = "স্নাতক উত্তীৰ্ণ, বয়স: ২০-২৪ বছৰ, শাৰীৰিক সক্ষমতা পৰীক্ষা।",
                url = "https://slprbassam.in",
                deadline = "31/12/2026"
            ),
            OpportunityTemplate(
                label = "💼 CMAAA ৩.০ উদ্যোগ",
                title = "মুখ্যমন্ত্ৰীৰ আত্মনিৰ্ভৰশীল অসম অভিযান ৩.০ (CMAAA) - যুৱ উদ্যোগীলৈ ₹২ ৰ পৰা ₹৫ লাখ সাহাৰ্য",
                category = OpportunityCategory.MSME.name,
                region = OpportunityRegion.ASSAM.name,
                org = "উদ্যোগ আৰু বাণিজ্য বিভাগ, অসম",
                eligibility = "অসমৰ নিবনুৱা যুৱক-যুৱতী, নূন্যতম উচ্চতৰ মাধ্যমিক উত্তীৰ্ণ।",
                url = "https://cmaaa.assam.gov.in",
                deadline = "31/01/2027"
            ),
            OpportunityTemplate(
                label = "🎓 NSP পোষ্ট-মেট্ৰিক বৃত্তি",
                title = "ৰাষ্ট্ৰীয় বৃত্তি পৰ্টেল (NSP) - মহাবিদ্যালয় আৰু বিশ্ববিদ্যালয়ৰ ছাত্ৰ-ছাত্ৰীৰ বৃত্তি",
                category = OpportunityCategory.SCHOLARSHIP.name,
                region = OpportunityRegion.INDIA.name,
                org = "Ministry of Social Justice & MeitY",
                eligibility = "নূন্যতম ৫০% নম্বৰসহ নিয়মীয়া পাঠ্যক্ৰমত অধ্যয়নৰত ছাত্ৰ-ছাত্ৰী।",
                url = "https://scholarships.gov.in",
                deadline = "15/01/2027"
            ),
            OpportunityTemplate(
                label = "💻 Smart India Hackathon",
                title = "Smart India Hackathon (SIH) ২০২৬ - ছাত্ৰ-ছাত্ৰীৰ উদ্ভাৱনী প্ৰকল্প প্ৰতিযোগিতা",
                category = OpportunityCategory.HACKATHON.name,
                region = OpportunityRegion.INDIA.name,
                org = "AICTE & Ministry of Education",
                eligibility = "সকলো অভিযান্ত্ৰিক আৰু স্নাতক মহাবিদ্যালয়ৰ দল।",
                url = "https://sih.gov.in",
                deadline = "28/02/2027"
            ),
            OpportunityTemplate(
                label = "☀️ পিএম সূৰ্য্য ঘৰ",
                title = "পিএম সূৰ্য্য ঘৰ বিনামূলীয়া বিজুলী যোজনা (PM Surya Ghar) - ₹৭৮,০০০ পৰ্যন্ত ৰাজসাহায্য",
                category = OpportunityCategory.GOVERNMENT_SCHEME.name,
                region = OpportunityRegion.INDIA.name,
                org = "Ministry of New and Renewable Energy",
                eligibility = "নিজস্ব পকা ঘৰৰ ছাদ থকা আৱাসিক উপভোক্তা।",
                url = "https://pmsuryaghar.gov.in",
                deadline = "31/12/2026"
            )
        )
    }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(OpportunityCategory.GOVERNMENT_SCHEME.name) }
    var selectedRegion by remember { mutableStateOf(OpportunityRegion.ASSAM.name) }
    var organization by remember { mutableStateOf("Govt of Assam") }
    var eligibility by remember { mutableStateOf("") }
    var sourceUrl by remember { mutableStateOf("https://assam.gov.in") }
    var deadline by remember { mutableStateOf("31/12/2026") }
    var titleError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .testTag("add_opportunity_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PostAdd,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "নতুন সুযোগ যোগ কৰক",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "ADD NEW SCHEME / JOB / OPPORTUNITY",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick One-Tap Templates
                Text(
                    text = "⚡ ১-ক্লিক প্ৰস্তুত টেমপ্লেট (QUICK PREFILL):",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickTemplates.forEach { template ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.clickable {
                                title = template.title
                                selectedCategory = template.category
                                selectedRegion = template.region
                                organization = template.org
                                eligibility = template.eligibility
                                sourceUrl = template.url
                                deadline = template.deadline
                                description = "${template.title}\n${template.eligibility}"
                                titleError = false
                            }
                        ) {
                            Text(
                                text = template.label,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = false
                    },
                    label = { Text("সুযোগ বা আঁচনিৰ নাম (Title) *") },
                    placeholder = { Text("যেনে: অসম আৰক্ষী নতুন নিযুক্তি / অৰুণোদয় ৩.০ / MSME ঋণ") },
                    isError = titleError,
                    supportingText = {
                        if (titleError) {
                            Text("অনুগ্ৰহ কৰি সুযোগৰ নাম লিখক", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_opportunity_title"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category Selection
                Text(
                    text = "বিভাগ বা শ্ৰেণী (Category):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val categories = listOf(
                        OpportunityCategory.GOVERNMENT_SCHEME to "🏛️ চৰকাৰী আঁচনি",
                        OpportunityCategory.GOVERNMENT_JOB to "👮 সেনা / আৰক্ষী চাকৰি",
                        OpportunityCategory.MSME to "💼 MSME ব্যৱসায় সাহাৰ্য",
                        OpportunityCategory.HACKATHON to "💻 হেকাথন & উদ্ভাৱন",
                        OpportunityCategory.SCHOLARSHIP to "🎓 ছাত্ৰ বৃত্তি (Scholarship)"
                    )
                    categories.forEach { (cat, label) ->
                        FilterChip(
                            selected = selectedCategory == cat.name,
                            onClick = { selectedCategory = cat.name },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Region Selection
                Text(
                    text = "অঞ্চল (Region):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val regions = listOf(
                        OpportunityRegion.ASSAM to "🟢 অসম (Assam)",
                        OpportunityRegion.NORTHEAST_INDIA to "🔵 উত্তৰ-পূৰ্ব (NE)",
                        OpportunityRegion.INDIA to "🟠 সমগ্ৰ ভাৰত (India)"
                    )
                    regions.forEach { (reg, label) ->
                        FilterChip(
                            selected = selectedRegion == reg.name,
                            onClick = { selectedRegion = reg.name },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Organization & Deadline
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = organization,
                        onValueChange = { organization = it },
                        label = { Text("বিভাগ / অনুষ্ঠান (Dept)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = deadline,
                        onValueChange = { deadline = it },
                        label = { Text("শেষ তাৰিখ (Deadline)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Eligibility / Details
                OutlinedTextField(
                    value = eligibility,
                    onValueChange = { eligibility = it },
                    label = { Text("যোগ্যতা আৰু গুৰুত্বপূৰ্ণ নিয়ম (Eligibility / Rules)") },
                    placeholder = { Text("যেনে: শিক্ষাগত অৰ্হতা, বয়সৰ সীমা, বাৰ্ষিক আয়, আৱশ্যকীয় নথিপত্ৰ") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Official URL / Apply Link
                OutlinedTextField(
                    value = sourceUrl,
                    onValueChange = { sourceUrl = it },
                    label = { Text("অফিচিয়েল ৱেবছাইট বা আবেদন লিংক (Official URL)") },
                    placeholder = { Text("https://assam.gov.in বা অফিচিয়েল পৰ্টেল") },
                    leadingIcon = {
                        Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(20.dp))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                titleError = true
                            } else {
                                onSaveOpportunity(
                                    title,
                                    description.ifBlank { "$title\n$eligibility" },
                                    selectedCategory,
                                    selectedRegion,
                                    organization,
                                    eligibility,
                                    sourceUrl,
                                    deadline
                                )
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("btn_save_custom_opportunity"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("যোগ কৰক & ব্যৱহাৰ কৰক")
                    }
                }
            }
        }
    }
}
