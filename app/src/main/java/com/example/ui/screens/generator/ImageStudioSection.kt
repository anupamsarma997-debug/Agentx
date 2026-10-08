package com.example.ui.screens.generator

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.ui.viewmodel.AppViewModel
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImageStudioSection(
    viewModel: AppViewModel,
    onNavigateToQueue: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isGenerating by viewModel.isGeneratingImage.collectAsState()
    val lastImagePath by viewModel.lastGeneratedImagePath.collectAsState()
    val lastDesc by viewModel.lastImageDescription.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Text-to-Image, 1: Edit Image
    var createPrompt by remember {
        mutableStateOf("A vibrant high-resolution digital poster celebrating an Assamese entrepreneur with Brahmaputra river in background, modern typography 'অসম উদ্যোগ ২০২৬'")
    }
    var editPrompt by remember {
        mutableStateOf("Add glowing golden morning sunlight over the Brahmaputra river and festive Bihu motifs")
    }
    var selectedRatio by remember { mutableStateOf("1:1") }
    var sourceImagePathForEdit by remember { mutableStateOf<String?>(null) }

    val ratios = listOf(
        "1:1" to "Square (1:1)",
        "4:5" to "Portrait (4:5)",
        "16:9" to "Landscape (16:9)",
        "9:16" to "Story (9:16)"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("image_studio_section"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "AI IMAGE STUDIO (gemini-3.1-flash-image-preview)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Create & Edit 1K images with text prompts",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub-tabs: Create vs Edit
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("✨ Create from Prompt") }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("🖌️ Edit Existing Image") }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Aspect Ratio selection
            Text(
                text = "Target Aspect Ratio:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ratios.forEach { (ratio, label) ->
                    FilterChip(
                        selected = selectedRatio == ratio,
                        onClick = { selectedRatio = ratio },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("ratio_chip_$ratio")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (activeTab == 0) {
                // CREATE MODE
                OutlinedTextField(
                    value = createPrompt,
                    onValueChange = { createPrompt = it },
                    label = { Text("Image Generation Prompt") },
                    placeholder = { Text("Describe the visual scene, subject, colors, and Assamese/Indian cultural details...") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth().testTag("input_create_image_prompt")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Suggestion chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val quickPrompts = listOf(
                        "অসম আৰক্ষী প্ৰশিক্ষণ বেনাৰ",
                        "MSME PMEGP ₹50L উদ্যোগী",
                        "Smart India Hackathon ক'ডিং দল",
                        "গুৱাহাটী সন্ধিয়াৰ চাহ আৰু বৰষুণ"
                    )
                    quickPrompts.forEach { qp ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.clickable {
                                createPrompt = when (qp) {
                                    "অসম আৰক্ষী প্ৰশিক্ষণ বেনাৰ" -> "A proud Assam Police recruitment official banner with uniform, Indian flag, and modern Guwahati police headquarters in background"
                                    "MSME PMEGP ₹50L উদ্যোগী" -> "An empowered Indian entrepreneur in tea processing startup receiving government MSME PMEGP award, vibrant cinematic lighting"
                                    "Smart India Hackathon ক'ডিং দল" -> "A college student team collaborating during Smart India Hackathon finale, laptops with code, high energy neon tech lights"
                                    else -> "Relatable monsoon evening in Guwahati with hot red tea and city lights reflecting on rain water"
                                }
                            }
                        ) {
                            Text(
                                text = "💡 $qp",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        viewModel.generateImageWithPrompt(createPrompt, selectedRatio)
                    },
                    enabled = !isGenerating && createPrompt.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_create_image")
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gemini 3.1 Flash Image প্ৰস্তুত কৰি আছে...")
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("CREATE IMAGE WITH GEMINI 3.1 FLASH")
                    }
                }
            } else {
                // EDIT MODE
                Text(
                    text = "Select an existing generated image to edit:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (lastImagePath != null && File(lastImagePath!!).exists()) {
                    sourceImagePathForEdit = lastImagePath
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(File(lastImagePath!!)),
                            contentDescription = "Current Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(60.dp).clip(RoundedCornerShape(6.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Current Active Image Selected",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = File(lastImagePath!!).name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Tip: Create an image above or generate a post banner first to enable visual editing.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = editPrompt,
                    onValueChange = { editPrompt = it },
                    label = { Text("Text Edit Instructions") },
                    placeholder = { Text("e.g. Add glowing sunset, change color scheme to vibrant amber, add festive elements...") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("input_edit_image_prompt")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val path = sourceImagePathForEdit ?: lastImagePath
                        if (path != null && File(path).exists()) {
                            val bitmap = BitmapFactory.decodeFile(path)
                            if (bitmap != null) {
                                viewModel.editImageWithPrompt(editPrompt, bitmap, selectedRatio)
                            }
                        }
                    },
                    enabled = !isGenerating && (sourceImagePathForEdit != null || lastImagePath != null) && editPrompt.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_edit_image")
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gemini 3.1 Flash Image এডিট কৰি আছে...")
                    } else {
                        Icon(Icons.Default.Brush, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("EDIT IMAGE WITH GEMINI 3.1 FLASH")
                    }
                }
            }

            // Result preview if generated
            lastImagePath?.let { path ->
                if (File(path).exists()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Generated Image Output (1K)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "gemini-3.1-flash-image-preview",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Image(
                                painter = rememberAsyncImagePainter(File(path)),
                                contentDescription = "AI Generated Image",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                            )

                            lastDesc?.let { desc ->
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.saveAiImageToQueue(
                                            imagePath = path,
                                            title = if (activeTab == 0) createPrompt.take(60) else editPrompt.take(60),
                                            caption = if (activeTab == 0) createPrompt else editPrompt
                                        )
                                        onNavigateToQueue()
                                    },
                                    modifier = Modifier.weight(1f).testTag("btn_save_image_queue")
                                ) {
                                    Icon(Icons.Default.Queue, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add to Queue & Publish", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
