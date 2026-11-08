package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.LiveNeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed

@Composable
fun YouTubeConfigCard(
    serverUrl: String,
    streamKey: String,
    isStreaming: Boolean,
    onServerUrlChange: (String) -> Unit,
    onStreamKeyChange: (String) -> Unit,
    onClearKey: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isKeyVisible by remember { mutableStateOf(false) }
    var isHelpExpanded by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkCardBorder))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF2E1010), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Podcasts,
                            contentDescription = "YouTube Ingestion",
                            tint = YouTubeRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "YOUTUBE LIVE CONNECTION",
                            style = MaterialTheme.typography.labelSmall,
                            color = YouTubeRed,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "RTMPS Ingestion Settings",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                IconButton(onClick = { isHelpExpanded = !isHelpExpanded }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Help Guide",
                        tint = TextSecondary
                    )
                }
            }

            // Help Drawer
            if (isHelpExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "How to get your YouTube Stream Key:",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "1. Open YouTube Studio (studio.youtube.com)\n" +
                                    "2. Click 'Create' (camera icon) -> 'Go Live'\n" +
                                    "3. In the Stream tab, find 'Stream key'\n" +
                                    "4. Copy it and tap Paste button below.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // RTMPS Server URL
            Text(
                text = "RTMPS Server URL",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = serverUrl,
                onValueChange = onServerUrlChange,
                enabled = !isStreaming,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("server_url_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = YouTubeRed,
                    unfocusedBorderColor = DarkCardBorder,
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant
                )
            )

            // Server URL presets
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = serverUrl.contains("a.rtmp.youtube.com"),
                    onClick = { onServerUrlChange("rtmps://a.rtmp.youtube.com/live2") },
                    enabled = !isStreaming,
                    label = { Text("Primary (live2)", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF3B1818),
                        selectedLabelColor = YouTubeRed
                    )
                )
                FilterChip(
                    selected = serverUrl.contains("b.rtmp.youtube.com"),
                    onClick = { onServerUrlChange("rtmps://b.rtmp.youtube.com/live2?backup=1") },
                    enabled = !isStreaming,
                    label = { Text("Backup Server", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF3B1818),
                        selectedLabelColor = YouTubeRed
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // YouTube Stream Key
            Text(
                text = "YouTube Stream Key",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = streamKey,
                onValueChange = onStreamKeyChange,
                enabled = !isStreaming,
                placeholder = { Text("Paste YouTube Stream Key (e.g. xxxx-xxxx-xxxx-xxxx)", color = TextMuted) },
                singleLine = true,
                visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (streamKey.isNotEmpty() && !isStreaming) {
                            IconButton(onClick = onClearKey) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Clear key",
                                    tint = TextMuted
                                )
                            }
                        }
                        IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                            Icon(
                                imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isKeyVisible) "Hide stream key" else "Show stream key",
                                tint = if (isKeyVisible) LiveNeonGreen else TextSecondary
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stream_key_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = YouTubeRed,
                    unfocusedBorderColor = DarkCardBorder,
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant
                )
            )

            // Paste button row
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Keystore Security Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Hardware Security",
                        tint = LiveNeonGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Hardware KeyStore Encrypted",
                        style = MaterialTheme.typography.labelSmall,
                        color = LiveNeonGreen
                    )
                }

                if (!isStreaming) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DarkSurfaceVariant,
                        modifier = Modifier.testTag("paste_key_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .clickable {
                                    val clip = clipboardManager.getText()?.text?.trim()
                                    if (!clip.isNullOrBlank()) {
                                        onStreamKeyChange(clip)
                                    }
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Paste",
                                tint = TextPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Paste Key",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
