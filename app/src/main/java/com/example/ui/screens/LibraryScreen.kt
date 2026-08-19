package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HdrFormat
import com.example.data.model.MediaItemEntity
import com.example.player.PlayerViewModel
import com.example.ui.components.CustomStreamDialog
import com.example.ui.components.HdrBadge
import com.example.ui.components.MediaItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: PlayerViewModel,
    onPlayMedia: (MediaItemEntity) -> Unit,
    onOpenDiagnostics: () -> Unit
) {
    val context = LocalContext.current
    val allMedia by viewModel.allMedia.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val recentHistory by viewModel.recentHistory.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showCustomStreamDialog by remember { mutableStateOf(false) }

    // System File Picker for Local HDR / Dolby Vision Videos
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = it.lastPathSegment?.substringAfterLast("/") ?: "Local Video"
            viewModel.addLocalVideo(it.toString(), fileName)
        }
    }

    val categories = listOf("All", "Dolby Vision", "HDR10", "HLG", "Favorites", "History")

    val filteredList = when (selectedCategory) {
        "Dolby Vision" -> allMedia.filter { it.hdrFormat == HdrFormat.DOLBY_VISION }
        "HDR10" -> allMedia.filter { it.hdrFormat == HdrFormat.HDR10 || it.hdrFormat == HdrFormat.HDR10_PLUS }
        "HLG" -> allMedia.filter { it.hdrFormat == HdrFormat.HLG }
        "Favorites" -> favorites
        "History" -> recentHistory
        else -> allMedia
    }.filter {
        if (searchQuery.isBlank()) true else {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E1B4B))
                                .border(1.dp, Color(0xFFE5A93B), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = "App Logo",
                                tint = Color(0xFFE5A93B),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "HDR Vision Player",
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp,
                                color = Color.White
                            )
                            Text(
                                text = "ExoPlayer Dolby Vision & HDR10 Pipeline",
                                fontSize = 10.sp,
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                actions = {
                    // Display Diagnostics
                    IconButton(
                        onClick = onOpenDiagnostics,
                        modifier = Modifier.testTag("display_diagnostics_action")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DisplaySettings,
                            contentDescription = "Display Diagnostics",
                            tint = Color(0xFF00E5FF)
                        )
                    }

                    // Open Local Video File
                    IconButton(
                        onClick = {
                            filePickerLauncher.launch(arrayOf("video/*"))
                        },
                        modifier = Modifier.testTag("open_local_file_action")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Open Local Video",
                            tint = Color(0xFF38BDF8)
                        )
                    }

                    // Add Custom Stream URL
                    IconButton(
                        onClick = { showCustomStreamDialog = true },
                        modifier = Modifier.testTag("add_custom_stream_action")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddLink,
                            contentDescription = "Add Stream URL",
                            tint = Color(0xFFE5A93B)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A0C10)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCustomStreamDialog = true },
                containerColor = Color(0xFF0284C7),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_stream")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.AddLink, contentDescription = "Add Stream")
                    Text("Play URL / Stream", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        },
        containerColor = Color(0xFF0A0C10)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("library_list"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Feature Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(Color(0xFFE5A93B), Color(0xFF06B6D4), Color(0xFF10B981))
                        )
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF1E1B4B).copy(alpha = 0.8f),
                                        Color(0xFF0B0F19)
                                    )
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    HdrBadge(hdrFormat = HdrFormat.DOLBY_VISION, dvProfile = "Profile 5 / 8.4", isGlow = true)
                                    HdrBadge(hdrFormat = HdrFormat.HDR10)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF0284C7).copy(alpha = 0.3f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        "10-Bit Pipeline",
                                        color = Color(0xFF38BDF8),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = "Ultra High Dynamic Range Cinema Engine",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = Color.White,
                                lineHeight = 22.sp
                            )

                            Text(
                                text = "Built on modern Media3 ExoPlayer with native Dolby Vision profile 5 & 8 metadata parsing, ST.2084 PQ tone curves, and wide BT.2020 color gamut hardware rendering.",
                                color = Color(0xFFCBD5E1),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1E293B))
                                        .clickable { filePickerLauncher.launch(arrayOf("video/*")) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                                        Text("Open Device Video", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1E293B))
                                        .clickable(onClick = onOpenDiagnostics)
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.DisplaySettings, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(14.dp))
                                        Text("Display Check", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search HDR showcase, Dolby Vision streams...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF94A3B8)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF1E293B),
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("library_search_input")
                )
            }

            // Filter Tabs Row
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategory == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0xFF0284C7) else Color(0xFF0F172A))
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFF38BDF8) else Color(0xFF1E293B),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedCategory = category }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("filter_chip_$category")
                        ) {
                            Text(
                                text = category,
                                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$selectedCategory (${filteredList.size})",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            // Video Cards
            if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = null,
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "No media found in this filter",
                                color = Color(0xFF94A3B8),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                items(filteredList, key = { it.id }) { mediaItem ->
                    MediaItemCard(
                        item = mediaItem,
                        onClick = {
                            viewModel.playMedia(mediaItem)
                            onPlayMedia(mediaItem)
                        },
                        onToggleFavorite = {
                            viewModel.toggleFavorite(mediaItem)
                        }
                    )
                }
            }

            // Bottom space for FAB
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    if (showCustomStreamDialog) {
        CustomStreamDialog(
            onDismiss = { showCustomStreamDialog = false },
            onPlayCustomStream = { title, url, format, dvProfile ->
                viewModel.addCustomStream(title, url, format, dvProfile)
            }
        )
    }
}
