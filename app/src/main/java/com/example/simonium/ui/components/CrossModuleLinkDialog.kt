package com.example.simonium.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.simonium.data.model.*
import com.example.ui.theme.*

@Composable
fun CrossModuleLinkDialog(
    submission: SubmissionItem,
    currentUser: UserProfile,
    onDismiss: () -> Unit,
    onLinkToEducation: (taskTitle: String, subject: String, prompt: String) -> Unit,
    onLinkToResearch: (questionTitle: String, discipline: String, description: String) -> Unit,
    onLinkToHealth: (conditionName: String, region: String, clinicalNotes: String) -> Unit
) {
    // Determine possible actions based on current user role
    val allowedTargets = remember(currentUser.role) {
        val targets = mutableListOf<ModuleType>()
        if (currentUser.role == UserRole.TEACHER || currentUser.role == UserRole.ADMINISTRATOR) {
            targets.add(ModuleType.EDUCATION)
        }
        if (currentUser.role == UserRole.RESEARCHER || currentUser.role == UserRole.DISCOVERER || currentUser.role == UserRole.ADMINISTRATOR) {
            targets.add(ModuleType.RESEARCH)
        }
        if (currentUser.role == UserRole.HEALTHCARE_PRO || currentUser.role == UserRole.ADMINISTRATOR) {
            targets.add(ModuleType.HEALTH)
        }
        if (targets.isEmpty()) targets.add(ModuleType.RESEARCH)
        targets
    }

    var selectedTargetModule by remember { mutableStateOf(allowedTargets.first()) }

    // Education inputs
    var taskTitle by remember { mutableStateOf("Inquiry Task: Practical Investigation of ${submission.title.take(30)}") }
    var taskSubject by remember { mutableStateOf("Environmental & Biological Sciences") }
    var taskPrompt by remember { mutableStateOf("Collect local ground-truth samples and compare with evidence reported in record #${submission.id}") }

    // Research inputs
    var questionTitle by remember { mutableStateOf("What are the systemic factors driving ${submission.title.take(30)}?") }
    var questionDiscipline by remember { mutableStateOf("Applied Multidisciplinary Science") }
    var questionDesc by remember { mutableStateOf("Scientific inquiry formulated from verified evidence: ${submission.description.take(120)}") }

    // Health inputs
    var conditionName by remember { mutableStateOf("Cluster Observation from ${submission.title.take(25)}") }
    var healthRegion by remember { mutableStateOf(submission.location ?: "National Health Surveillance") }
    var clinicalNotes by remember { mutableStateOf("Clinical follow-up initiated based on field observation #${submission.id}") }

    var deIdentificationConfirmed by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("cross_module_link_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Hub,
                            contentDescription = null,
                            tint = SimoniumTeal
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Connect Across Modules",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "Transform this verified finding into actionable learning, formal research, or health alerts.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Source Reference Card
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Source Evidence: #${submission.id}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SimoniumTeal
                        )
                        Text(
                            text = submission.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Module: ${submission.module.title} • By ${submission.authorName}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Target Module Selector
                Text(
                    text = "Select Target Domain:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    allowedTargets.forEach { mod ->
                        FilterChip(
                            selected = selectedTargetModule == mod,
                            onClick = { selectedTargetModule = mod; errorMessage = null },
                            label = { Text(mod.title, fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = when (mod) {
                                        ModuleType.EDUCATION -> Icons.Default.School
                                        ModuleType.RESEARCH -> Icons.Default.Science
                                        ModuleType.HEALTH -> Icons.Default.LocalHospital
                                        else -> Icons.Default.People
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action form depending on selected target module
                when (selectedTargetModule) {
                    ModuleType.EDUCATION -> {
                        Text(
                            text = "Educational Classroom & Fieldwork Task",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7C3AED)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = taskTitle,
                            onValueChange = { taskTitle = it },
                            label = { Text("Task Title *") },
                            modifier = Modifier.fillMaxWidth().testTag("link_task_title")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = taskSubject,
                            onValueChange = { taskSubject = it },
                            label = { Text("Subject Area") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = taskPrompt,
                            onValueChange = { taskPrompt = it },
                            label = { Text("Student Fieldwork Prompt *") },
                            modifier = Modifier.fillMaxWidth().height(80.dp)
                        )
                    }
                    ModuleType.RESEARCH -> {
                        Text(
                            text = "Formal Scientific Research Question",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = questionTitle,
                            onValueChange = { questionTitle = it },
                            label = { Text("Research Question *") },
                            modifier = Modifier.fillMaxWidth().testTag("link_question_title")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = questionDiscipline,
                            onValueChange = { questionDiscipline = it },
                            label = { Text("Scientific Discipline") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = questionDesc,
                            onValueChange = { questionDesc = it },
                            label = { Text("Hypothesis & Context *") },
                            modifier = Modifier.fillMaxWidth().height(80.dp)
                        )
                    }
                    ModuleType.HEALTH -> {
                        Text(
                            text = "De-Identified Epidemiological Surveillance",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = conditionName,
                            onValueChange = { conditionName = it },
                            label = { Text("Condition / Cluster Name *") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = healthRegion,
                            onValueChange = { healthRegion = it },
                            label = { Text("Reporting Region / District") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = clinicalNotes,
                            onValueChange = { clinicalNotes = it },
                            label = { Text("Clinical Justification *") },
                            modifier = Modifier.fillMaxWidth().height(80.dp)
                        )
                    }
                    else -> {}
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Confidentiality & Consent Assurance
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = deIdentificationConfirmed,
                        onCheckedChange = { deIdentificationConfirmed = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "I verify this cross-module transfer contains no personal or identifiable private data.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (successMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = successMessage!!,
                        color = StatusVerified,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (!deIdentificationConfirmed) {
                                errorMessage = "You must confirm patient / individual de-identification."
                                return@Button
                            }

                            when (selectedTargetModule) {
                                ModuleType.EDUCATION -> {
                                    if (taskTitle.isBlank() || taskPrompt.isBlank()) {
                                        errorMessage = "Please enter task title and fieldwork prompt."
                                        return@Button
                                    }
                                    onLinkToEducation(taskTitle, taskSubject, taskPrompt)
                                }
                                ModuleType.RESEARCH -> {
                                    if (questionTitle.isBlank() || questionDesc.isBlank()) {
                                        errorMessage = "Please enter research question and hypothesis context."
                                        return@Button
                                    }
                                    onLinkToResearch(questionTitle, questionDiscipline, questionDesc)
                                }
                                ModuleType.HEALTH -> {
                                    if (conditionName.isBlank() || clinicalNotes.isBlank()) {
                                        errorMessage = "Please enter condition name and clinical notes."
                                        return@Button
                                    }
                                    onLinkToHealth(conditionName, healthRegion, clinicalNotes)
                                }
                                else -> {}
                            }
                            successMessage = "Cross-module connection created and recorded in audit trail."
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SimoniumTeal),
                        modifier = Modifier.weight(1.3f).testTag("confirm_cross_module_link_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Link, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Create Link", color = Color.White)
                    }
                }
            }
        }
    }
}
