package com.example.simonium.ui.components

import androidx.compose.foundation.background
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SubmissionDetailDialog(
    submission: SubmissionItem,
    currentUser: UserProfile,
    comments: List<ReviewComment>,
    auditLogs: List<AuditLogEntry>,
    connections: List<CrossModuleConnection> = emptyList(),
    onDismiss: () -> Unit,
    onStatusChange: (newStatus: LifecycleStatus, notes: String?, commentText: String?) -> Unit,
    onAddComment: (comment: String, isCorrection: Boolean) -> Unit,
    onDelete: (id: String) -> Unit,
    onUpdateSubmission: ((id: String, title: String, description: String, evidenceType: EvidenceType, location: String?, tags: List<String>, sourceReferences: List<String>, isDraft: Boolean) -> Unit)? = null,
    onOpenCrossModuleLink: (() -> Unit)? = null
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Overview, 1: Reviews & Feedback, 2: Audit History
    var newCommentText by remember { mutableStateOf("") }
    var verificationNotesInput by remember { mutableStateOf("") }
    var showActionPrompt by remember { mutableStateOf<LifecycleStatus?>(null) }

    // Author editing state
    var isEditing by remember { mutableStateOf(false) }
    var editTitle by remember(submission) { mutableStateOf(submission.title) }
    var editDescription by remember(submission) { mutableStateOf(submission.description) }
    var editEvidenceType by remember(submission) { mutableStateOf(submission.evidenceType) }
    var editLocation by remember(submission) { mutableStateOf(submission.location ?: "") }
    var editTagsText by remember(submission) { mutableStateOf(submission.tags.joinToString(", ")) }
    var editError by remember { mutableStateOf<String?>(null) }

    val canReview = currentUser.role in setOf(
        UserRole.TEACHER,
        UserRole.RESEARCHER,
        UserRole.HEALTHCARE_PRO,
        UserRole.ADMINISTRATOR
    )

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("submission_detail_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth()
            ) {
                // Header with Module and Close
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SimoniumTeal.copy(alpha = 0.15f),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = submission.module.title,
                                color = SimoniumTeal,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        if (submission.isConfidential) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFEF4444).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Confidential",
                                    color = Color(0xFFEF4444),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = submission.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    LifecycleBadge(status = submission.status)
                    EvidenceTypeBadge(evidenceType = submission.evidenceType)
                }

                val submissionConnections = connections.filter { it.sourceSubmissionId == submission.id }

                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    contentColor = SimoniumTeal
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Evidence", fontSize = 11.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Peer Review (${comments.size})", fontSize = 11.sp) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Linkages (${submissionConnections.size})", fontSize = 11.sp) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("Audit Trail", fontSize = 11.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Content
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .heightIn(max = 350.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (selectedTab) {
                        0 -> {
                            if (isEditing) {
                                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                    Text("Edit Evidence & Correct Data", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SimoniumTeal)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = editTitle,
                                        onValueChange = { editTitle = it; editError = null },
                                        label = { Text("Title *") },
                                        modifier = Modifier.fillMaxWidth().testTag("edit_submission_title")
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = editDescription,
                                        onValueChange = { editDescription = it; editError = null },
                                        label = { Text("Scientific Findings & Evidence Description *") },
                                        modifier = Modifier.fillMaxWidth().height(110.dp).testTag("edit_submission_desc"),
                                        maxLines = 5
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = editLocation,
                                        onValueChange = { editLocation = it },
                                        label = { Text("Field Location (Optional)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = editTagsText,
                                        onValueChange = { editTagsText = it },
                                        label = { Text("Tags (comma-separated)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    if (editError != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(editError!!, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                        OutlinedButton(
                                            onClick = { isEditing = false },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Cancel")
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                if (editTitle.isBlank() || editDescription.isBlank()) {
                                                    editError = "Title and description cannot be blank."
                                                    return@OutlinedButton
                                                }
                                                val tags = editTagsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                                onUpdateSubmission?.invoke(
                                                    submission.id,
                                                    editTitle,
                                                    editDescription,
                                                    editEvidenceType,
                                                    editLocation.ifBlank { null },
                                                    tags,
                                                    submission.sourceReferences,
                                                    true
                                                )
                                                isEditing = false
                                            },
                                            modifier = Modifier.weight(1.1f).testTag("save_draft_submission_btn")
                                        ) {
                                            Text("Save Draft", fontSize = 10.sp)
                                        }
                                        Button(
                                            onClick = {
                                                if (editTitle.isBlank() || editDescription.isBlank()) {
                                                    editError = "Title and description cannot be blank."
                                                    return@Button
                                                }
                                                val tags = editTagsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                                onUpdateSubmission?.invoke(
                                                    submission.id,
                                                    editTitle,
                                                    editDescription,
                                                    editEvidenceType,
                                                    editLocation.ifBlank { null },
                                                    tags,
                                                    submission.sourceReferences,
                                                    false
                                                )
                                                isEditing = false
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SimoniumTeal),
                                            modifier = Modifier.weight(1.2f).testTag("resubmit_submission_btn")
                                        ) {
                                            Text("Resubmit", fontSize = 11.sp, color = Color.White)
                                        }
                                    }
                                }
                            } else {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Author Info Card
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "Author & Provenance:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "${submission.authorName} (${submission.authorRole.displayName})",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        if (submission.authorInstitution != null) {
                                            Text(
                                                text = "Institution: ${submission.authorInstitution}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (submission.location != null) {
                                            Text(
                                                text = "Location: ${submission.location}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = "Submitted: ${dateFormat.format(Date(submission.createdAt))}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "Description & Data:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = submission.description,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (submission.tags.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Tags: " + submission.tags.joinToString(", ") { "#$it" },
                                        fontSize = 11.sp,
                                        color = SimoniumTeal,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                if (submission.sourceReferences.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Source References:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    submission.sourceReferences.forEach { ref ->
                                        Text(
                                            text = "• $ref",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (submission.verificationNotes != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = StatusVerified.copy(alpha = 0.12f)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Verified,
                                                    contentDescription = null,
                                                    tint = StatusVerified,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Independent Verification Decision",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = StatusVerified
                                                )
                                            }
                                            Text(
                                                text = "Verified By: ${submission.reviewerName ?: "Authorized Reviewer"}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = submission.verificationNotes,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                        1 -> {
                            // Review comments thread
                            Column(modifier = Modifier.fillMaxWidth()) {
                                if (comments.isEmpty()) {
                                    Text(
                                        text = "No review comments yet. Reviewers can request corrections or leave verification notes below.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                } else {
                                    comments.forEach { comment ->
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (comment.isCorrectionRequest)
                                                    StatusCorrection.copy(alpha = 0.12f)
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = "${comment.authorName} (${comment.authorRole.displayName})",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    if (comment.isCorrectionRequest) {
                                                        Text(
                                                            text = "Correction",
                                                            fontSize = 10.sp,
                                                            color = StatusCorrection,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = comment.comment,
                                                    fontSize = 11.sp,
                                                    lineHeight = 15.sp
                                                )
                                                Text(
                                                    text = dateFormat.format(Date(comment.timestamp)),
                                                    fontSize = 9.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = newCommentText,
                                        onValueChange = { newCommentText = it },
                                        placeholder = { Text("Add review comment...", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = {
                                            if (newCommentText.isNotBlank()) {
                                                onAddComment(newCommentText, false)
                                                newCommentText = ""
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Send,
                                            contentDescription = "Send Comment",
                                            tint = SimoniumTeal
                                        )
                                    }
                                }
                            }
                        }
                        2 -> {
                            // Cross-Module Connected Knowledge Graph
                            Column(modifier = Modifier.fillMaxWidth()) {
                                if (submission.linkedSourceId != null) {
                                    Card(
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = SimoniumTeal.copy(alpha = 0.1f)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = SimoniumTeal, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Derived From Source Evidence", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SimoniumTeal)
                                            }
                                            Text(text = "Parent ID: #${submission.linkedSourceId}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            if (submission.linkedSourceTitle != null) {
                                                Text(text = submission.linkedSourceTitle, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                }

                                Text(
                                    text = "Connected Downstream Activities (${submissionConnections.size}):",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                if (submissionConnections.isEmpty()) {
                                    Text(
                                        text = "No downstream tasks or research questions linked to this observation yet. Authorized reviewers and researchers can establish multidisciplinary links.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                } else {
                                    submissionConnections.forEach { conn ->
                                        Card(
                                            shape = RoundedCornerShape(8.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = SimoniumTeal.copy(alpha = 0.15f)
                                                    ) {
                                                        Text(
                                                            text = conn.targetModule.title,
                                                            color = SimoniumTeal,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                    Text(
                                                        text = dateFormat.format(Date(conn.timestamp)),
                                                        fontSize = 9.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(text = conn.targetTitle, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                Text(
                                                    text = "Type: ${conn.connectionType} • Linked by ${conn.initiatedBy}",
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                if (canReview || currentUser.role == UserRole.DISCOVERER || currentUser.role == UserRole.RESEARCHER) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = { onOpenCrossModuleLink?.invoke() },
                                        colors = ButtonDefaults.buttonColors(containerColor = SimoniumTeal),
                                        modifier = Modifier.fillMaxWidth().testTag("tab_open_cross_module_link_btn")
                                    ) {
                                        Icon(imageVector = Icons.Default.Hub, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Create Cross-Module Connection", fontSize = 11.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                        3 -> {
                            // Audit log history
                            Column(modifier = Modifier.fillMaxWidth()) {
                                val entityLogs = auditLogs.filter { it.entityId == submission.id }
                                if (entityLogs.isEmpty()) {
                                    Text(
                                        text = "Initial lifecycle log captured on submission creation.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    entityLogs.forEach { log ->
                                        Row(
                                            verticalAlignment = Alignment.Top,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.History,
                                                contentDescription = null,
                                                tint = SimoniumTeal,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = log.action,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp,
                                                        color = SimoniumTeal
                                                    )
                                                    Text(
                                                        text = " • by ${log.actorName} (${log.actorRole.displayName})",
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Text(
                                                    text = log.details,
                                                    fontSize = 10.sp,
                                                    lineHeight = 14.sp
                                                )
                                                Text(
                                                    text = dateFormat.format(Date(log.timestamp)),
                                                    fontSize = 9.sp,
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

                Spacer(modifier = Modifier.height(12.dp))

                // Reviewer Workflow Actions
                if (canReview) {
                    Text(
                        text = "Authorized Review Actions (${currentUser.role.displayName}):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                onStatusChange(
                                    LifecycleStatus.APPROVED,
                                    "Approved by authorized reviewer ${currentUser.fullName}",
                                    "Methodology reviewed and approved."
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusApproved),
                            modifier = Modifier.weight(1f).testTag("approve_submission_button")
                        ) {
                            Text("Approve", fontSize = 11.sp, color = Color.White)
                        }

                        Button(
                            onClick = {
                                onStatusChange(
                                    LifecycleStatus.VERIFIED,
                                    "Empirically validated and verified by ${currentUser.fullName} (${currentUser.role.displayName})",
                                    "Finding verified against independent criteria and replicate datasets."
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusVerified),
                            modifier = Modifier.weight(1f).testTag("verify_submission_button")
                        ) {
                            Text("Verify", fontSize = 11.sp, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = {
                                onStatusChange(
                                    LifecycleStatus.CORRECTION_REQUESTED,
                                    "Corrections requested by ${currentUser.fullName}",
                                    "Please clarify field sample controls and revise reference citations."
                                )
                            },
                            modifier = Modifier.weight(1f).testTag("request_correction_button")
                        ) {
                            Text("Correction", fontSize = 10.sp)
                        }
                    }
                }

                if (canReview || currentUser.role == UserRole.DISCOVERER || currentUser.role == UserRole.RESEARCHER) {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = { onOpenCrossModuleLink?.invoke() },
                        modifier = Modifier.fillMaxWidth().testTag("open_cross_module_action_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Hub, contentDescription = null, tint = SimoniumTeal, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Connect Across Modules (Classroom / Research / Health)", fontSize = 11.sp, color = SimoniumTeal)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (currentUser.id == submission.authorId || currentUser.role == UserRole.ADMINISTRATOR) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (!isEditing) {
                                TextButton(
                                    onClick = {
                                        selectedTab = 0
                                        isEditing = true
                                    },
                                    modifier = Modifier.testTag("edit_submission_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = SimoniumTeal,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(" Edit", color = SimoniumTeal, fontSize = 11.sp)
                                }
                            }
                            TextButton(
                                onClick = {
                                    onDelete(submission.id)
                                    onDismiss()
                                },
                                modifier = Modifier.testTag("delete_submission_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(" Delete", color = Color(0xFFEF4444), fontSize = 11.sp)
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = SimoniumNavyDark)
                    ) {
                        Text("Close", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
