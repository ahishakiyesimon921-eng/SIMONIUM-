package com.example.simonium.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.simonium.data.model.ModuleAccessRequirement
import com.example.simonium.data.model.ModuleType
import com.example.simonium.data.model.UserProfile
import com.example.simonium.data.model.VerificationApplicationStatus
import com.example.ui.theme.SimoniumNavyDark
import com.example.ui.theme.SimoniumTeal
import com.example.ui.theme.StatusApproved

@Composable
fun ModuleRequirementsDialog(
    module: ModuleType,
    currentUser: UserProfile?,
    accessRequirement: ModuleAccessRequirement,
    onDismiss: () -> Unit,
    onRequestVerification: () -> Unit,
    onSignIn: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .testTag("module_requirements_dialog")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (accessRequirement.isGranted) Icons.Default.VerifiedUser else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (accessRequirement.isGranted) StatusApproved else Color(0xFFD97706)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = module.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = module.subtitle,
                    fontSize = 11.sp,
                    color = SimoniumTeal,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Current Access Status Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (accessRequirement.isGranted) StatusApproved.copy(alpha = 0.1f)
                        else Color(0xFFD97706).copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (accessRequirement.isGranted) "Access Status: Granted & Verified"
                                else "Access Status: Verification Required",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (accessRequirement.isGranted) StatusApproved else Color(0xFFD97706)
                            )
                        }
                        if (currentUser != null) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Your Current Role: ${currentUser.role.displayName} • ${currentUser.identityVerificationStatus.label}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Module Security Requirements:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = accessRequirement.requirementDescription,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Allowed / Participating Roles:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                accessRequirement.requiredRole.forEach { role ->
                    Text(
                        text = "• ${role.displayName}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (currentUser == null) {
                    Button(
                        onClick = {
                            onDismiss()
                            onSignIn()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SimoniumTeal),
                        modifier = Modifier.fillMaxWidth().testTag("req_dialog_signin_btn")
                    ) {
                        Text("Sign In or Register to Access", color = Color.White, fontSize = 12.sp)
                    }
                } else if (!accessRequirement.isGranted) {
                    Button(
                        onClick = {
                            onDismiss()
                            onRequestVerification()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SimoniumTeal),
                        modifier = Modifier.fillMaxWidth().testTag("req_dialog_verify_btn")
                    ) {
                        Text("Submit Identity Evidence", color = Color.White, fontSize = 12.sp)
                    }
                } else {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = SimoniumNavyDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Open Module Content", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
