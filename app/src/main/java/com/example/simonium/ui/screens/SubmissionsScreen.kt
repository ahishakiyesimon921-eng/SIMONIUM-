package com.example.simonium.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simonium.data.model.LifecycleStatus
import com.example.simonium.data.model.ModuleType
import com.example.simonium.data.model.SyncState
import com.example.simonium.ui.components.EvidenceTypeBadge
import com.example.simonium.ui.components.LifecycleBadge
import com.example.ui.theme.SimoniumTeal
import com.example.simonium.ui.viewmodel.SimoniumViewModel

@Composable
fun SubmissionsScreen(
    viewModel: SimoniumViewModel,
    onOpenNewSubmission: () -> Unit
) {
    val filteredSubmissions by viewModel.filteredSubmissions.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedModuleFilter by viewModel.selectedModuleFilter.collectAsState()
    val selectedStatusFilter by viewModel.selectedStatusFilter.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("submissions_screen")
    ) {
        // Search & Filter Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search by title, tags, author, keyword...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = SimoniumTeal)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_submissions_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Module Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedModuleFilter == null,
                            onClick = { viewModel.setModuleFilter(null) },
                            label = { Text("All Disciplines", fontSize = 11.sp) }
                        )
                    }
                    items(listOf(ModuleType.HEALTH, ModuleType.EDUCATION, ModuleType.RESEARCH, ModuleType.COMMUNITY)) { mod ->
                        FilterChip(
                            selected = selectedModuleFilter == mod,
                            onClick = {
                                viewModel.setModuleFilter(if (selectedModuleFilter == mod) null else mod)
                            },
                            label = { Text(mod.title, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Lifecycle Status Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedStatusFilter == null,
                            onClick = { viewModel.setStatusFilter(null) },
                            label = { Text("All Statuses", fontSize = 11.sp) }
                        )
                    }
                    items(LifecycleStatus.values()) { st ->
                        FilterChip(
                            selected = selectedStatusFilter == st,
                            onClick = {
                                viewModel.setStatusFilter(if (selectedStatusFilter == st) null else st)
                            },
                            label = { Text(st.label, fontSize = 11.sp) }
                        )
                    }
                }
            }
        }

        // Submissions List
        if (filteredSubmissions.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FilterListOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No records match the current filters.",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try clearing filters or create a new scientific submission.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onOpenNewSubmission,
                        colors = ButtonDefaults.buttonColors(containerColor = SimoniumTeal)
                    ) {
                        Text("+ New Submission", color = Color.White)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Matching Submissions (${filteredSubmissions.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = onOpenNewSubmission) {
                            Text("+ Create Record", color = SimoniumTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                items(filteredSubmissions) { item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectSubmission(item) }
                            .testTag("lifecycle_item_${item.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = SimoniumTeal.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = item.module.title,
                                            color = SimoniumTeal,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    if (item.syncState == SyncState.PENDING_SYNC) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.CloudSync,
                                            contentDescription = "Pending Sync",
                                            tint = Color(0xFFEA580C),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                LifecycleBadge(status = item.status)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.description,
                                fontSize = 12.sp,
                                maxLines = 2,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                EvidenceTypeBadge(evidenceType = item.evidenceType)
                                Text(
                                    text = "by ${item.authorName} (${item.authorRole.displayName})",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
