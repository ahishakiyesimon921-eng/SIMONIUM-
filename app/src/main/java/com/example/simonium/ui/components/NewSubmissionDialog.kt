package com.example.simonium.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Science
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
import com.example.simonium.data.model.EvidenceType
import com.example.simonium.data.model.ModuleType
import com.example.ui.theme.SimoniumNavyDark
import com.example.ui.theme.SimoniumTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSubmissionDialog(
    initialModule: ModuleType = ModuleType.RESEARCH,
    onDismiss: () -> Unit,
    onSubmit: (
        title: String,
        description: String,
        module: ModuleType,
        evidenceType: EvidenceType,
        location: String?,
        tags: List<String>,
        sourceReferences: List<String>,
        isDraft: Boolean,
        isConfidential: Boolean
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedModule by remember { mutableStateOf(initialModule) }
    var selectedEvidenceType by remember { mutableStateOf(EvidenceType.RAW_OBSERVATION) }
    var location by remember { mutableStateOf("") }
    var tagsInput by remember { mutableStateOf("") }
    var referencesInput by remember { mutableStateOf("") }
    var isConfidential by remember { mutableStateOf(false) }

    var moduleMenuExpanded by remember { mutableStateOf(false) }
    var evidenceMenuExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("new_submission_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = SimoniumTeal
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "New Scientific Submission",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    label = { Text("Title / Observation Name *") },
                    placeholder = { Text("e.g., Anopheles Larval Density Survey") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submission_title_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Module Selection Dropdown
                ExposedDropdownMenuBox(
                    expanded = moduleMenuExpanded,
                    onExpandedChange = { moduleMenuExpanded = !moduleMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedModule.title,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Discipline / Module *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = moduleMenuExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = moduleMenuExpanded,
                        onDismissRequest = { moduleMenuExpanded = false }
                    ) {
                        listOf(
                            ModuleType.HEALTH,
                            ModuleType.EDUCATION,
                            ModuleType.RESEARCH,
                            ModuleType.COMMUNITY
                        ).forEach { mod ->
                            DropdownMenuItem(
                                text = { Text(mod.title) },
                                onClick = {
                                    selectedModule = mod
                                    moduleMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Evidence Type Selection Dropdown
                ExposedDropdownMenuBox(
                    expanded = evidenceMenuExpanded,
                    onExpandedChange = { evidenceMenuExpanded = !evidenceMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedEvidenceType.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Information Lifecycle Phase *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = evidenceMenuExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = evidenceMenuExpanded,
                        onDismissRequest = { evidenceMenuExpanded = false }
                    ) {
                        EvidenceType.values().forEach { ev ->
                            DropdownMenuItem(
                                text = { Text(ev.label) },
                                onClick = {
                                    selectedEvidenceType = ev
                                    evidenceMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        errorMessage = null
                    },
                    label = { Text("Description & Empirical Evidence *") },
                    placeholder = { Text("Describe methodology, observed data points, conditions...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("submission_desc_input"),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Location Input
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Geographic Location / Field Station") },
                    placeholder = { Text("e.g., Musanze, Kinigi Sector") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Tags Input
                OutlinedTextField(
                    value = tagsInput,
                    onValueChange = { tagsInput = it },
                    label = { Text("Tags (comma separated)") },
                    placeholder = { Text("e.g., WaterTesting, Turbidity, HighAltitude") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // References / Sources Input
                OutlinedTextField(
                    value = referencesInput,
                    onValueChange = { referencesInput = it },
                    label = { Text("Source Citations / Literature References") },
                    placeholder = { Text("e.g., WHO Vector Ecology 2024; RBC Highland Survey") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Confidentiality Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isConfidential,
                        onCheckedChange = { isConfidential = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Confidential / Patient Sensitive",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "Restricts viewing to authorized health professionals and researchers",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            if (title.isBlank()) {
                                errorMessage = "Please enter a title for your draft."
                                return@OutlinedButton
                            }
                            onSubmit(
                                title,
                                description,
                                selectedModule,
                                selectedEvidenceType,
                                location.ifBlank { null },
                                tagsInput.split(",").map { it.trim() }.filter { it.isNotBlank() },
                                referencesInput.split(";").map { it.trim() }.filter { it.isNotBlank() },
                                true, // isDraft
                                isConfidential
                            )
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f).testTag("save_draft_button")
                    ) {
                        Text("Save Draft", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            if (title.isBlank() || description.isBlank()) {
                                errorMessage = "Title and description are required."
                                return@Button
                            }
                            onSubmit(
                                title,
                                description,
                                selectedModule,
                                selectedEvidenceType,
                                location.ifBlank { null },
                                tagsInput.split(",").map { it.trim() }.filter { it.isNotBlank() },
                                referencesInput.split(";").map { it.trim() }.filter { it.isNotBlank() },
                                false, // submit for review
                                isConfidential
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SimoniumTeal),
                        modifier = Modifier.weight(1f).testTag("submit_record_button")
                    ) {
                        Text("Submit for Review", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
