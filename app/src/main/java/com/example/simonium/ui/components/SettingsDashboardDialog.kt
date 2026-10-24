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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.simonium.data.model.ModuleType
import com.example.simonium.data.model.UserProfile
import com.example.simonium.data.model.UserRole
import com.example.simonium.ui.viewmodel.SimoniumViewModel
import com.example.ui.theme.*

@Composable
fun SettingsDashboardDialog(
    viewModel: SimoniumViewModel,
    currentUser: UserProfile,
    onDismiss: () -> Unit,
    onOpenRoleSwitcher: () -> Unit,
    onOpenHelpSupport: () -> Unit,
    onOpenRegisterWizard: () -> Unit,
    onOpenOfflineQueue: (() -> Unit)? = null
) {
    var selectedCategory by remember { mutableStateOf(0) } // 0: Account & Security, 1: Verification & Modules, 2: Preferences & Data
    var isPushNotifEnabled by remember { mutableStateOf(true) }
    var isEmailNotifEnabled by remember { mutableStateOf(true) }
    var isConfidentialDefault by remember { mutableStateOf(false) }
    var isHighContrast by remember { mutableStateOf(false) }
    var activeLanguage by remember { mutableStateOf("EN") }
    var backupStatusMessage by remember { mutableStateOf<String?>(null) }
    var restoreJsonInput by remember { mutableStateOf("") }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("settings_dashboard_dialog")
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
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = SimoniumTeal
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Settings Dashboard",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "Account security, verification requirements, preferences & system administration",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Navigation Tabs
                TabRow(
                    selectedTabIndex = selectedCategory,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    contentColor = SimoniumTeal
                ) {
                    Tab(
                        selected = selectedCategory == 0,
                        onClick = { selectedCategory = 0 },
                        text = { Text("Account", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedCategory == 1,
                        onClick = { selectedCategory = 1 },
                        text = { Text("Clearance", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedCategory == 2,
                        onClick = { selectedCategory = 2 },
                        text = { Text("Preferences", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Body
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (selectedCategory) {
                        0 -> {
                            // 1. Account Information
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Account Information", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SimoniumTeal)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Full Name: ${currentUser.fullName}", fontSize = 12.sp)
                                    Text("Email: ${currentUser.email}", fontSize = 12.sp)
                                    Text("Phone: ${currentUser.phone}", fontSize = 12.sp)
                                    Text("Role: ${currentUser.role.displayName}", fontSize = 12.sp)
                                    Text("Institution: ${currentUser.institutionName ?: "Independent Researcher"}", fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { showEditProfileDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = SimoniumTeal),
                                            modifier = Modifier.weight(1f).testTag("settings_edit_profile_btn")
                                        ) {
                                            Text("Edit Profile", fontSize = 11.sp, color = Color.White)
                                        }
                                        OutlinedButton(
                                            onClick = onOpenRoleSwitcher,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Switch Role", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }

                            // 2. Password and Security
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Password & Security", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SimoniumTeal)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Standard: Min 6 characters, mixed alphanumerics required.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { showChangePasswordDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = SimoniumNavyDark),
                                        modifier = Modifier.fillMaxWidth().testTag("settings_change_password_btn")
                                    ) {
                                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Change Password", fontSize = 11.sp, color = Color.White)
                                    }
                                }
                            }

                            // 3. Email & Phone Verification Status
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Identifier Verification", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SimoniumTeal)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Email (${currentUser.email})", fontSize = 11.sp)
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = StatusApproved.copy(alpha = 0.15f)
                                        ) {
                                            Text("Verified", color = StatusApproved, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Phone (${currentUser.phone})", fontSize = 11.sp)
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = StatusApproved.copy(alpha = 0.15f)
                                        ) {
                                            Text("SMS Verified", color = StatusApproved, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                            }

                            // 4. Active Sessions
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Active Sessions", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SimoniumTeal)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Current Device: Android Client (Authenticated Session)", fontSize = 11.sp)
                                    Text("Status: Secure TLS 1.3 encrypted local session", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedButton(
                                        onClick = { backupStatusMessage = "All other remote tokens revoked successfully." },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Revoke All Other Sessions", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                        1 -> {
                            // 5. Identity Verification Status
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Identity Verification Status", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SimoniumTeal)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = StatusApproved.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = currentUser.identityVerificationStatus.label,
                                                color = StatusApproved,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "Privilege Tier: ${currentUser.approvalStatus.name}", fontSize = 11.sp)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Registered credentials: Institutional authorization, identity cards, or professional licences.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = onOpenRegisterWizard,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Submit Additional Evidence / Upgrade Role", fontSize = 11.sp)
                                    }
                                }
                            }

                            // 6. Module Access Clearance Matrix
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Module Access Clearance", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SimoniumTeal)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    listOf(ModuleType.HEALTH, ModuleType.EDUCATION, ModuleType.RESEARCH, ModuleType.COMMUNITY).forEach { mod ->
                                        val req = viewModel.checkModuleAccess(mod)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(mod.title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                                Text(req.requirementDescription, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
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
                        2 -> {
                            // 7. Language and Localization
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Platform Language", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SimoniumTeal)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        FilterChip(
                                            selected = activeLanguage == "EN",
                                            onClick = { activeLanguage = "EN"; viewModel.setAiLanguage("EN") },
                                            label = { Text("🇬🇧 EN", fontSize = 11.sp) }
                                        )
                                        FilterChip(
                                            selected = activeLanguage == "RW",
                                            onClick = { activeLanguage = "RW"; viewModel.setAiLanguage("RW") },
                                            label = { Text("🇷🇼 RW", fontSize = 11.sp) }
                                        )
                                        FilterChip(
                                            selected = activeLanguage == "FR",
                                            onClick = { activeLanguage = "FR"; viewModel.setAiLanguage("FR") },
                                            label = { Text("🇫🇷 FR", fontSize = 11.sp) }
                                        )
                                        FilterChip(
                                            selected = activeLanguage == "SW",
                                            onClick = { activeLanguage = "SW"; viewModel.setAiLanguage("SW") },
                                            label = { Text("🇹🇿 SW", fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }

                            // 8. Notifications Preferences
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Notifications & Alerts", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SimoniumTeal)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Push Verification Notifications", fontSize = 11.sp)
                                        Switch(checked = isPushNotifEnabled, onCheckedChange = { isPushNotifEnabled = it })
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Email Peer Review Alerts", fontSize = 11.sp)
                                        Switch(checked = isEmailNotifEnabled, onCheckedChange = { isEmailNotifEnabled = it })
                                    }
                                }
                            }

                            // 9. Privacy & Accessibility
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Privacy & Accessibility", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SimoniumTeal)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("High-Contrast Mode", fontSize = 11.sp)
                                        Switch(checked = isHighContrast, onCheckedChange = { isHighContrast = it })
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Default to Confidential Submissions", fontSize = 11.sp)
                                        Switch(checked = isConfidentialDefault, onCheckedChange = { isConfidentialDefault = it })
                                    }
                                }
                            }

                            // 10. Data Export & Restore
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Data Export & Snapshot Restore", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SimoniumTeal)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Export encrypted local research repository or import a verified JSON archive.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                val json = viewModel.exportBackupJson()
                                                backupStatusMessage = "Snapshot compiled (${json.length} bytes)."
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SimoniumTeal),
                                            modifier = Modifier.weight(1f).testTag("settings_export_backup_btn")
                                        ) {
                                            Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Export JSON", fontSize = 11.sp, color = Color.White)
                                        }
                                        OutlinedButton(
                                            onClick = { showRestoreDialog = true },
                                            modifier = Modifier.weight(1f).testTag("settings_open_restore_btn")
                                        ) {
                                            Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Restore", fontSize = 11.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = { onOpenOfflineQueue?.invoke() },
                                        modifier = Modifier.fillMaxWidth().testTag("settings_open_offline_queue_btn")
                                    ) {
                                        Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Inspect Offline SQLite Queue & Sync State", fontSize = 11.sp)
                                    }
                                    if (backupStatusMessage != null) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(backupStatusMessage!!, fontSize = 11.sp, color = SimoniumTeal, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }

                            // 11. Help, Terms & Legal
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Support & Governance", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SimoniumTeal)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = onOpenHelpSupport,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Help & FAQ", fontSize = 11.sp)
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                backupStatusMessage = "Ethical scientific charter: Submissions conform to research safety standard ISO-27001."
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Terms & Privacy", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }

                            // 12. Sign Out
                            Button(
                                onClick = {
                                    viewModel.logout()
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                modifier = Modifier.fillMaxWidth().testTag("settings_sign_out_btn")
                            ) {
                                Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sign Out of Session", color = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = SimoniumNavyDark),
                    modifier = Modifier.fillMaxWidth().testTag("close_settings_btn")
                ) {
                    Text("Close", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }

    // Change Password sub-dialog
    if (showChangePasswordDialog) {
        var oldPass by remember { mutableStateOf("") }
        var newPass by remember { mutableStateOf("") }
        var passError by remember { mutableStateOf<String?>(null) }
        var passSuccess by remember { mutableStateOf<String?>(null) }

        Dialog(onDismissRequest = { showChangePasswordDialog = false }) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Change Password", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = oldPass,
                        onValueChange = { oldPass = it; passError = null },
                        label = { Text("Current Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it; passError = null },
                        label = { Text("New Password (min 6 chars)") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (passError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(passError!!, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                    }
                    if (passSuccess != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(passSuccess!!, color = StatusApproved, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showChangePasswordDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (newPass.length < 6) {
                                    passError = "Password must be at least 6 characters."
                                    return@Button
                                }
                                val res = viewModel.changePassword(oldPass, newPass)
                                if (res.isSuccess) {
                                    passSuccess = "Password updated successfully."
                                    showChangePasswordDialog = false
                                } else {
                                    passError = res.exceptionOrNull()?.message ?: "Current password incorrect."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SimoniumTeal),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Update", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Restore Data Dialog
    if (showRestoreDialog) {
        Dialog(onDismissRequest = { showRestoreDialog = false }) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Restore JSON Snapshot", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Paste SIMONIUM encrypted JSON backup snapshot to restore repository records.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = restoreJsonInput,
                        onValueChange = { restoreJsonInput = it },
                        placeholder = { Text("{\"format\": \"SIMONIUM_SECURE_BACKUP\", ...}") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        maxLines = 6
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showRestoreDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val jsonToUse = if (restoreJsonInput.isNotBlank()) restoreJsonInput else viewModel.exportBackupJson()
                                val result = viewModel.restoreBackupJson(jsonToUse)
                                if (result.isSuccess) {
                                    backupStatusMessage = "Successfully restored ${result.getOrNull()} items into repository."
                                    showRestoreDialog = false
                                } else {
                                    backupStatusMessage = "Restore failed: ${result.exceptionOrNull()?.message}"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SimoniumTeal),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Restore Now", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(currentUser.fullName) }
        var editEmail by remember { mutableStateOf(currentUser.email) }
        var editInst by remember { mutableStateOf(currentUser.institutionName ?: "") }

        Dialog(onDismissRequest = { showEditProfileDialog = false }) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Edit Profile Information", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(10.dp))
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
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editInst,
                        onValueChange = { editInst = it },
                        label = { Text("Institution") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showEditProfileDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                viewModel.updateProfile(editName, editEmail, currentUser.specialty, editInst.ifBlank { null })
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
}
