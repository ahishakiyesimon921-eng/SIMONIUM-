package com.example.simonium.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.simonium.data.local.SubmissionEntity
import com.example.simonium.ui.viewmodel.SimoniumViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OfflineSyncQueueDialog(
    viewModel: SimoniumViewModel,
    onDismiss: () -> Unit
) {
    val isOnline by viewModel.isOnline.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val scope = rememberCoroutineScope()
    var pendingItems by remember { mutableStateOf<List<SubmissionEntity>>(emptyList()) }
    var syncMessage by remember { mutableStateOf<String?>(null) }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    LaunchedEffect(Unit, isOnline, isSyncing) {
        pendingItems = viewModel.getPendingSyncQueue()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("offline_sync_queue_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth()
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = if (isOnline) SimoniumTeal.copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = if (isOnline) SimoniumTeal else Color(0xFFEF4444),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Offline Sync Engine",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = if (isOnline) "Network Connected (Online)" else "Offline Mode (Local SQLite Queue)",
                                fontSize = 11.sp,
                                color = if (isOnline) SimoniumTeal else Color(0xFFEF4444),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Conflict Resolution & Security Strategy Notice
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Conflict Resolution & Provenance Protocol",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SimoniumTeal
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "• Local SQLite cache queues new observations while offline.\n" +
                                   "• Monotonic timestamp ordering prevents overwrites.\n" +
                                   "• Verified scientific reviews remain server-authoritative.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Pending Queue Count & Status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Pending Synchronizations (${pendingItems.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = SimoniumTeal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (pendingItems.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = null,
                                tint = StatusVerified,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "All records synchronized with database.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(pendingItems) { entity ->
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = entity.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${entity.moduleName} • Modified: ${dateFormat.format(Date(entity.updatedAt))}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFD97706).copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = entity.syncStateName,
                                            color = Color(0xFFD97706),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (syncMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = syncMessage!!,
                        fontSize = 11.sp,
                        color = SimoniumTeal,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.toggleNetworkStatus()
                            scope.launch {
                                pendingItems = viewModel.getPendingSyncQueue()
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("toggle_offline_sync_btn")
                    ) {
                        Icon(
                            imageVector = if (isOnline) Icons.Default.CloudOff else Icons.Default.CloudQueue,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isOnline) "Simulate Offline" else "Go Online", fontSize = 10.sp)
                    }

                    Button(
                        onClick = {
                            if (!isOnline) {
                                syncMessage = "Cannot sync while in offline mode. Enable network first."
                            } else {
                                viewModel.syncNow()
                                scope.launch {
                                    kotlinx.coroutines.delay(1400)
                                    pendingItems = viewModel.getPendingSyncQueue()
                                    syncMessage = "Sync completed. Database is up to date."
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SimoniumTeal),
                        modifier = Modifier.weight(1f).testTag("force_sync_now_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sync Now", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }
    }
}
