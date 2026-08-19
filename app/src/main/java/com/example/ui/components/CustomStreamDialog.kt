package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.sp
import com.example.data.model.DolbyVisionProfile
import com.example.data.model.HdrFormat

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomStreamDialog(
    onDismiss: () -> Unit,
    onPlayCustomStream: (title: String, url: String, hdrFormat: HdrFormat, dvProfile: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var selectedFormat by remember { mutableStateOf(HdrFormat.DOLBY_VISION) }
    var selectedDvProfile by remember { mutableStateOf<String?>("Profile 5 (dvhe.05)") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        title = {
            Text(
                text = "Add Custom Stream / URL",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Stream Title") },
                    placeholder = { Text("e.g. My Dolby Vision Movie / Test Stream") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("stream_title_input")
                )

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Stream / Video URL (HLS / DASH / MP4)") },
                    placeholder = { Text("https://.../.m3u8, .mpd, or .mp4") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("stream_url_input")
                )

                Text(
                    text = "HDR Color Format:",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        HdrFormat.DOLBY_VISION,
                        HdrFormat.HDR10_PLUS,
                        HdrFormat.HDR10,
                        HdrFormat.HLG,
                        HdrFormat.AUTO,
                        HdrFormat.SDR
                    ).forEach { format ->
                        val isSelected = selectedFormat == format
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF1E293B) else Color(0xFF0B1120))
                                .border(
                                    1.dp,
                                    if (isSelected) format.badgeColor else Color(0xFF334155),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedFormat = format }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = format.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) format.badgeColor else Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                if (selectedFormat == HdrFormat.DOLBY_VISION) {
                    Text(
                        text = "Dolby Vision Profile:",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Profile 5 (dvhe.05)",
                            "Profile 8.1 (dvhe.08)",
                            "Profile 8.4 (dvhe.08)",
                            "Profile 7 (dvhe.07)"
                        ).forEach { profile ->
                            val isSelected = selectedDvProfile == profile
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF1E293B) else Color(0xFF0B1120))
                                    .border(
                                        1.dp,
                                        if (isSelected) Color(0xFFE5A93B) else Color(0xFF334155),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedDvProfile = profile }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = profile,
                                    fontSize = 11.sp,
                                    color = if (isSelected) Color(0xFFE5A93B) else Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (url.isNotBlank()) {
                        onPlayCustomStream(
                            title.ifBlank { "Custom Stream" },
                            url.trim(),
                            selectedFormat,
                            if (selectedFormat == HdrFormat.DOLBY_VISION) selectedDvProfile else null
                        )
                        onDismiss()
                    }
                },
                enabled = url.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier.testTag("play_stream_confirm_button")
            ) {
                Text("Play Stream")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        }
    )
}
