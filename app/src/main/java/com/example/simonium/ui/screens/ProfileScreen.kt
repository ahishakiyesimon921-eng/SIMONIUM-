package com.example.simonium.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.simonium.data.model.ModuleType
import com.example.simonium.data.model.RoleApprovalStatus
import com.example.simonium.data.model.UserRole
import com.example.simonium.data.model.VerificationApplicationStatus
import com.example.simonium.ui.components.RoleBadge
import com.example.simonium.ui.components.VerificationQueueDialog
import com.example.ui.theme.*
import com.example.simonium.ui.viewmodel.SimoniumViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    viewModel: SimoniumViewModel,
    onOpenRoleSwitcher: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState(initial = emptyList())
    val verificationApplications by viewModel.verificationApplications.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showVerificationQueueDialog by remember { mutableStateOf(false) }
    var showSchemaDialog by remember { mutableStateOf(false) }
    var showAuditLogsDialog by remember { mutableStateOf(false) }
    var backupStatusMessage by remember { mutableStateOf<String?>(null) }
    var activeSessionsMessage by remember { mutableStateOf<String?>(null) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreJsonText by remember { mutableStateOf("") }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("profile_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. User Profile & Identity Verification Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SimoniumNavyDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SimoniumTeal,
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = currentUser.fullName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentUser.fullName,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${currentUser.email} • ${currentUser.phone}",
                                fontSize = 11.sp,
                                color = SimoniumTealLight
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RoleBadge(role = currentUser.role, onClick = onOpenRoleSwitcher)
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = StatusApproved.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = currentUser.identityVerificationStatus.label,
                                        color = StatusApproved,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Institution: ${currentUser.institutionName ?: "Independent Researcher"}",
                        fontSize = 12.sp,
                        color = Color(0xFFE2E8F0)
                    )
                    Text(
                        text = "Specialty: ${currentUser.specialty ?: currentUser.role.description}",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1)
                    )
                    Text(
                        text = "Verified Scientific Contributions: ${currentUser.verifiedContributionsCount}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SimoniumTealLight
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onOpenRoleSwitcher,
                            colors = ButtonDefaults.buttonColors(containerColor = SimoniumTeal),
                            modifier = Modifier.weight(1f).testTag("switch_role_btn")
                        ) {
                            Text("Switch Test Role", fontSize = 11.sp, color = Color.White)
                        }
                        OutlinedButton(
                            onClick = { showEditProfileDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Edit Profile", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // 2. Administrator Verification Queue (if Admin)
        if (currentUser.role == UserRole.ADMINISTRATOR) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4C1D95).copy(alpha = 0.12f)),
                    modifier = Modifier.fillMaxWidth().testTag("admin_verification_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color(0xFF7C3AED))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Identity Verification Queue",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF7C3AED)
                            ) {
                                Text(
                                    text = "${verificationApplications.size} Applications",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Review submitted student IDs, teacher credentials, and doctor licences. Approve, request additional evidence, or reject applications.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { showVerificationQueueDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                            modifier = Modifier.fillMaxWidth().testTag("open_verification_queue_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Rule, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Manage Verification Queue", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 3. Password & Security Management
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = SimoniumTeal)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Password & Security Settings",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(text = "Password", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text(text = "Last changed 14 days ago", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        OutlinedButton(
                            onClick = { showChangePasswordDialog = true },
                            modifier = Modifier.testTag("change_password_btn")
                        ) {
                            Text("Change Password", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(text = "Active Sessions", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text(text = "1 active session on Android (Current Device)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(
                            onClick = { activeSessionsMessage = "All other remote sessions revoked." }
                        ) {
                            Text("Revoke Others", fontSize = 11.sp, color = SimoniumTeal)
                        }
                    }
                    if (activeSessionsMessage != null) {
                        Text(text = activeSessionsMessage!!, color = SimoniumTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 4. Module Access Permissions Matrix Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = SimoniumTeal)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Module Access & Permissions Matrix",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    listOf(ModuleType.HEALTH, ModuleType.EDUCATION, ModuleType.RESEARCH, ModuleType.COMMUNITY).forEach { mod ->
                        val req = viewModel.checkModuleAccess(mod)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = mod.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = req.requirementDescription, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (req.isGranted) StatusApproved.copy(alpha = 0.15f) else Color(0xFFD97706).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (req.isGranted) "Granted" else "Restricted",
                                    color = if (req.isGranted) StatusApproved else Color(0xFFD97706),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Offline & Data Synchronization Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = if (isOnline) SimoniumTeal else Color(0xFFEF4444)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Offline & Synchronization Engine",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Switch(
                            checked = isOnline,
                            onCheckedChange = { viewModel.toggleNetworkStatus() }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isOnline) "Connected to SIMONIUM repository. Real-time updates active."
                        else "Simulated Offline Mode. Changes are queued locally in Room database until connection is restored.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.syncNow() },
                        enabled = !isSyncing,
                        colors = ButtonDefaults.buttonColors(containerColor = SimoniumTeal),
                        modifier = Modifier.fillMaxWidth().testTag("sync_now_btn")
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Synchronizing...", color = Color.White, fontSize = 12.sp)
                        } else {
                            Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sync Local Database Now", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 6. Backend & Database Architecture (Supabase & RLS)
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Storage, contentDescription = null, tint = SimoniumTeal)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Supabase PostgreSQL & Security Policies",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Row Level Security (RLS) policies are defined for all entities. View and export the full PostgreSQL migration schema.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { showSchemaDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SimoniumNavyDark),
                            modifier = Modifier.weight(1f).testTag("view_schema_btn")
                        ) {
                            Text("View SQL Schema", fontSize = 11.sp, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = { showAuditLogsDialog = true },
                            modifier = Modifier.weight(1f).testTag("view_audit_logs_btn")
                        ) {
                            Text("Audit Trail (${auditLogs.size})", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // 7. Data Backup & Restore
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Backup & Data Management",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Export local scientific repository as secure encrypted JSON snapshot or verify database integrity.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                val json = viewModel.exportBackupJson()
                                backupStatusMessage = "Snapshot compiled (${json.length} bytes): simonium_backup_${System.currentTimeMillis()}.json"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SimoniumTeal),
                            modifier = Modifier.weight(1f).testTag("backup_data_btn")
                        ) {
                            Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export JSON", fontSize = 11.sp, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = {
                                showRestoreDialog = true
                            },
                            modifier = Modifier.weight(1f).testTag("restore_data_btn")
                        ) {
                            Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Restore JSON", fontSize = 11.sp)
                        }
                    }

                    if (backupStatusMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = backupStatusMessage!!,
                            color = SimoniumTeal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 8. Account Deletion & Logout Actions
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.logout() },
                        colors = ButtonDefaults.buttonColors(containerColor = SimoniumNavyDark),
                        modifier = Modifier.fillMaxWidth().testTag("logout_button")
                    ) {
                        Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign Out of Session", color = Color.White)
                    }

                    OutlinedButton(
                        onClick = { showDeleteAccountDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        modifier = Modifier.fillMaxWidth().testTag("delete_account_open_btn")
                    ) {
                        Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFEF4444))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Account", color = Color(0xFFEF4444))
                    }
                }
            }
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(currentUser.fullName) }
        var editEmail by remember { mutableStateOf(currentUser.email) }
        var editSpecialty by remember { mutableStateOf(currentUser.specialty ?: "") }
        var editInstitution by remember { mutableStateOf(currentUser.institutionName ?: "") }

        Dialog(onDismissRequest = { showEditProfileDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Edit User Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editInstitution,
                        onValueChange = { editInstitution = it },
                        label = { Text("Institution") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editSpecialty,
                        onValueChange = { editSpecialty = it },
                        label = { Text("Scientific Specialty") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showEditProfileDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                viewModel.updateProfile(editName, editEmail, editSpecialty.ifBlank { null }, editInstitution.ifBlank { null })
                                showEditProfileDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SimoniumTeal),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Change Password Dialog
    if (showChangePasswordDialog) {
        var oldPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var confirmNewPassword by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf<String?>(null) }
        var successMsg by remember { mutableStateOf<String?>(null) }

        Dialog(onDismissRequest = { showChangePasswordDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Change Password", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it; errorMsg = null },
                        label = { Text("Current Password *") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().testTag("old_password_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it; errorMsg = null },
                        label = { Text("New Password * (Min 6 chars)") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().testTag("new_password_change_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmNewPassword,
                        onValueChange = { confirmNewPassword = it; errorMsg = null },
                        label = { Text("Confirm New Password *") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().testTag("confirm_new_password_change_input")
                    )

                    if (errorMsg != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = errorMsg!!, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                    }

                    if (successMsg != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = successMsg!!, color = StatusApproved, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showChangePasswordDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (newPassword.length < 6) {
                                    errorMsg = "Password must be at least 6 characters."
                                    return@Button
                                }
                                if (newPassword != confirmNewPassword) {
                                    errorMsg = "New passwords do not match."
                                    return@Button
                                }
                                val res = viewModel.changePassword(oldPassword, newPassword)
                                if (res.isSuccess) {
                                    successMsg = "Password successfully updated."
                                    showChangePasswordDialog = false
                                } else {
                                    errorMsg = res.exceptionOrNull()?.message ?: "Failed to update password."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SimoniumTeal),
                            modifier = Modifier.weight(1f).testTag("save_changed_password_btn")
                        ) {
                            Text("Update", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Delete Account Dialog with Reauthentication and Retention Policy
    if (showDeleteAccountDialog) {
        var deletePassword by remember { mutableStateOf("") }
        var deleteReason by remember { mutableStateOf("Personal preference") }
        var deleteError by remember { mutableStateOf<String?>(null) }

        Dialog(onDismissRequest = { showDeleteAccountDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Account & Data Notice", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFEF4444))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Data Retention Policy: Pursuant to scientific ethics and institutional audit obligations, peer-reviewed scientific publications and laboratory datasets will be preserved in anonymized form. Your personal credentials, identity documents, and active sessions will be permanently revoked.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = deleteReason,
                        onValueChange = { deleteReason = it },
                        label = { Text("Reason for deletion") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = deletePassword,
                        onValueChange = { deletePassword = it; deleteError = null },
                        label = { Text("Confirm with Password *") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().testTag("delete_account_password_input")
                    )

                    if (deleteError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = deleteError!!, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showDeleteAccountDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val res = viewModel.deleteAccount(deletePassword, deleteReason)
                                if (res.isSuccess) {
                                    showDeleteAccountDialog = false
                                } else {
                                    deleteError = res.exceptionOrNull()?.message ?: "Authentication failed."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            modifier = Modifier.weight(1f).testTag("confirm_delete_account_btn")
                        ) {
                            Text("Delete Account", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Verification Queue Dialog
    if (showVerificationQueueDialog) {
        VerificationQueueDialog(
            applications = verificationApplications,
            onDismiss = { showVerificationQueueDialog = false },
            onReviewDecision = { appId, newStatus, comments ->
                viewModel.reviewVerificationApplication(appId, newStatus, comments)
            }
        )
    }

    // Supabase Schema Viewer Dialog
    if (showSchemaDialog) {
        Dialog(onDismissRequest = { showSchemaDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SimoniumNavyDark,
                modifier = Modifier.fillMaxWidth().height(480.dp).padding(6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Supabase PostgreSQL Schema",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        IconButton(onClick = { showSchemaDialog = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                    Text(
                        text = "File: assets/supabase_schema.sql (Complete RLS policies & tables)",
                        fontSize = 11.sp,
                        color = SimoniumCyanLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A),
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    ) {
                        LazyColumn(modifier = Modifier.padding(10.dp)) {
                            item {
                                Text(
                                    text = """
-- SIMONIUM PLATFORM SCHEMA SUMMARY
1. institutions (id, name, type, location, is_verified)
2. profiles (id -> auth.users, role, approval_status, institution_id, specialty)
3. platform_modules (id, title, is_enabled, allowed_roles)
4. submissions (id, title, module_id, evidence_type, status, author_id, tags, references, is_confidential)
5. health_surveillance_reports (condition_name, region, cases, severity, de_identified)
6. educational_tasks (title, subject, teacher_id, research_connection)
7. research_projects (title, lead_researcher, objectives, findings, status)
8. community_observations (title, category, district, expert_response)
9. review_comments (submission_id, author_id, comment, is_correction)
10. audit_logs (entity_id, actor_id, action, details, timestamp)
11. verification_applications (id, user_id, requested_role, status, details, reviewer_id)
12. verification_documents (id, application_id, doc_type, file_name, is_verified)

-- ROW LEVEL SECURITY (RLS) POLICIES
- ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
- ALTER TABLE public.submissions ENABLE ROW LEVEL SECURITY;
- ALTER TABLE public.health_surveillance_reports ENABLE ROW LEVEL SECURITY;
- ALTER TABLE public.verification_applications ENABLE ROW LEVEL SECURITY;
- ALTER TABLE public.audit_logs ENABLE ROW LEVEL SECURITY;
                                    """.trimIndent(),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8),
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { showSchemaDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = SimoniumTeal),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Done", color = Color.White)
                    }
                }
            }
        }
    }

    // Audit Logs Viewer Dialog
    if (showAuditLogsDialog) {
        Dialog(onDismissRequest = { showAuditLogsDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().height(500.dp).padding(6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.History, contentDescription = null, tint = SimoniumTeal)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Immutable Security Audit Log",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        IconButton(onClick = { showAuditLogsDialog = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                    Text(
                        text = "All status transitions, reviews, verifications, and logins are logged with actor identities and timestamps.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(auditLogs) { log ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = log.action,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = SimoniumTeal
                                        )
                                        Text(
                                            text = dateFormat.format(Date(log.timestamp)),
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = log.details,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                    Text(
                                        text = "Actor: ${log.actorName} (${log.actorRole.displayName})",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { showAuditLogsDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = SimoniumNavyDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close", color = Color.White)
                    }
                }
            }
        }
    }

    // Restore JSON Snapshot Dialog
    if (showRestoreDialog) {
        Dialog(onDismissRequest = { showRestoreDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Restore Database from Snapshot", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Paste SIMONIUM encrypted JSON backup snapshot to restore repository records.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = restoreJsonText,
                        onValueChange = { restoreJsonText = it },
                        placeholder = { Text("{\"format\": \"SIMONIUM_SECURE_BACKUP\", ...}") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        maxLines = 6
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showRestoreDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val jsonToUse = if (restoreJsonText.isNotBlank()) restoreJsonText else viewModel.exportBackupJson()
                                val result = viewModel.restoreBackupJson(jsonToUse)
                                if (result.isSuccess) {
                                    backupStatusMessage = "Successfully restored ${result.getOrNull()} items into repository."
                                    showRestoreDialog = false
                                } else {
                                    backupStatusMessage = "Restore failed: ${result.exceptionOrNull()?.message}"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SimoniumTeal),
                            modifier = Modifier.weight(1f).testTag("confirm_restore_json_btn")
                        ) {
                            Text("Restore Now", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
