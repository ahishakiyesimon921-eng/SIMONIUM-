package com.example.simonium.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Warning
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
import com.example.simonium.data.model.VerificationApplication
import com.example.simonium.data.model.VerificationApplicationStatus
import com.example.ui.theme.SimoniumNavyDark
import com.example.ui.theme.SimoniumTeal
import com.example.ui.theme.StatusApproved
import com.example.ui.theme.StatusCorrection
import com.example.ui.theme.StatusRejected

@Composable
fun VerificationQueueDialog(
    applications: List<VerificationApplication>,
    onDismiss: () -> Unit,
    onReviewDecision: (appId: String, newStatus: VerificationApplicationStatus, comments: String?) -> Unit
) {
    var selectedApp by remember { mutableStateOf<VerificationApplication?>(null) }
    var reviewCommentText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(6.dp)
                .testTag("verification_queue_dialog")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Rule, contentDescription = null, tint = SimoniumTeal)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Identity Verification Queue",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "Review submitted student IDs, teacher credentials, and medical licences before granting privileged module access:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (selectedApp == null) {
                    // List of applications
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(applications) { app ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (app.status == VerificationApplicationStatus.VERIFICATION_PENDING)
                                        Color(0xFFD97706).copy(alpha = 0.08f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("queue_item_${app.id}")
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = app.userFullName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = when (app.status) {
                                                VerificationApplicationStatus.APPROVED -> StatusApproved.copy(alpha = 0.2f)
                                                VerificationApplicationStatus.VERIFICATION_PENDING -> Color(0xFFD97706).copy(alpha = 0.2f)
                                                else -> Color.Gray.copy(alpha = 0.2f)
                                            }
                                        ) {
                                            Text(
                                                text = app.status.label,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (app.status) {
                                                    VerificationApplicationStatus.APPROVED -> StatusApproved
                                                    VerificationApplicationStatus.VERIFICATION_PENDING -> Color(0xFFD97706)
                                                    else -> Color.Gray
                                                },
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "Requested: ${app.requestedRole.displayName} • ${app.institutionName}",
                                        fontSize = 11.sp,
                                        color = SimoniumTeal
                                    )
                                    Text(
                                        text = "Contact: ${app.userEmail}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (app.documents.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        val doc = app.documents.first()
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Default.Folder, contentDescription = null, tint = SimoniumTeal, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${doc.documentType} (${doc.documentNumber}) - ${doc.fileSizeKb} KB",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                        Button(
                                            onClick = { selectedApp = app },
                                            colors = ButtonDefaults.buttonColors(containerColor = SimoniumTeal),
                                            modifier = Modifier.testTag("inspect_app_${app.id}")
                                        ) {
                                            Text("Review & Decide", fontSize = 11.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Application Detail Inspection
                    val app = selectedApp!!
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "Applicant: ${app.userFullName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "Email: ${app.userEmail}", fontSize = 12.sp)
                        Text(text = "Requested Role: ${app.requestedRole.displayName}", fontSize = 12.sp, color = SimoniumTeal, fontWeight = FontWeight.Bold)
                        Text(text = "Institution: ${app.institutionName}", fontSize = 12.sp)

                        if (app.documents.isNotEmpty()) {
                            val doc = app.documents.first()
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = "Attached Credential Evidence:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text(text = "Type: ${doc.documentType}", fontSize = 11.sp)
                                    Text(text = "Number: ${doc.documentNumber}", fontSize = 11.sp)
                                    Text(text = "Issuing Authority: ${doc.issuingAuthority}", fontSize = 11.sp)
                                    Text(text = "File: ${doc.fileName} (${doc.fileSizeKb} KB)", fontSize = 10.sp, color = SimoniumTeal)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = reviewCommentText,
                            onValueChange = { reviewCommentText = it },
                            label = { Text("Reviewer Notes / Verification Decision *") },
                            placeholder = { Text("e.g. Verified against faculty registry, approved.") },
                            modifier = Modifier.fillMaxWidth().height(90.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    onReviewDecision(app.id, VerificationApplicationStatus.APPROVED, reviewCommentText.ifBlank { "Verified and approved by administrator." })
                                    selectedApp = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusApproved),
                                modifier = Modifier.weight(1f).testTag("approve_app_btn")
                            ) {
                                Text("Approve", fontSize = 11.sp, color = Color.White)
                            }

                            Button(
                                onClick = {
                                    onReviewDecision(app.id, VerificationApplicationStatus.ADDITIONAL_INFO_REQUIRED, reviewCommentText.ifBlank { "Please upload higher resolution scan of official seal." })
                                    selectedApp = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusCorrection),
                                modifier = Modifier.weight(1f).testTag("request_info_btn")
                            ) {
                                Text("Request Info", fontSize = 11.sp, color = Color.White)
                            }

                            Button(
                                onClick = {
                                    onReviewDecision(app.id, VerificationApplicationStatus.REJECTED, reviewCommentText.ifBlank { "Credentials do not match verified institutional registry." })
                                    selectedApp = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusRejected),
                                modifier = Modifier.weight(1f).testTag("reject_app_btn")
                            ) {
                                Text("Reject", fontSize = 11.sp, color = Color.White)
                            }
                        }

                        TextButton(onClick = { selectedApp = null }) {
                            Text("← Back to List", fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = SimoniumNavyDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = Color.White)
                }
            }
        }
    }
}
