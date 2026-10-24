package com.example.simonium.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.simonium.data.model.ModuleType
import com.example.simonium.data.model.UserProfile
import com.example.simonium.data.model.UserRole
import com.example.simonium.ui.components.RoleBadge
import com.example.ui.theme.*
import com.example.simonium.ui.viewmodel.SimoniumViewModel

@Composable
fun LoginDashboardScreen(
    viewModel: SimoniumViewModel,
    onOpenRegisterWizard: () -> Unit,
    onOpenForgotPassword: () -> Unit,
    onSelectModulePreview: (ModuleType) -> Unit,
    onOpenHelpSupport: () -> Unit
) {
    val demoAccounts = viewModel.demoAccounts
    val aiLanguage by viewModel.aiLanguage.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Login & Security, 1: Explore Modules
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showDemoAccounts by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("login_dashboard_screen")
    ) {
        // Platform Welcome Header Card
        Surface(
            color = SimoniumNavyDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Public Language Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(SimoniumNavyElevated, RoundedCornerShape(16.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        TextButton(
                            onClick = { viewModel.setAiLanguage("EN") },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = if (aiLanguage == "EN") SimoniumCyanLight else Color.Gray
                            )
                        ) {
                            Text("EN", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        TextButton(
                            onClick = { viewModel.setAiLanguage("RW") },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = if (aiLanguage == "RW") SimoniumCyanLight else Color.Gray
                            )
                        ) {
                            Text("RW", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Help & Support
                    IconButton(onClick = onOpenHelpSupport) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Help & Support",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Emblem & Title
                Image(
                    painter = painterResource(id = R.drawable.simonium_icon),
                    contentDescription = "SIMONIUM Emblem",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "SIMONIUM",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "Scientific Information Management, Organization, Network, Intelligence and Unified Multidisciplinary Platform",
                    fontSize = 11.sp,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SimoniumTeal.copy(alpha = 0.2f),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "Connecting Knowledge. Enabling Discovery.",
                        color = SimoniumCyanLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Navigation Tabs (Sign In vs Explore Modules)
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = SimoniumNavy,
            contentColor = SimoniumCyanLight
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sign In & Security", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.testTag("tab_sign_in")
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Widgets, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Module Access Requirements", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.testTag("tab_explore_modules")
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (activeTab == 0) {
                // Sign In Form
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth().testTag("login_form_card")
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Account Authentication",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Enter your approved identifier (Email or Phone) and password.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Identifier Input
                            OutlinedTextField(
                                value = identifier,
                                onValueChange = {
                                    identifier = it
                                    errorMessage = null
                                },
                                label = { Text("Email Address or Phone Number *") },
                                placeholder = { Text("e.g. admin@simonium.org or +250 788 123 456") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = SimoniumTeal)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_identifier_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Password Input
                            OutlinedTextField(
                                value = password,
                                onValueChange = {
                                    password = it
                                    errorMessage = null
                                },
                                label = { Text("Password *") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = SimoniumTeal)
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { passwordVisible = !passwordVisible },
                                        modifier = Modifier.testTag("password_visibility_toggle")
                                    ) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                        )
                                    }
                                },
                                singleLine = true,
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_password_input")
                            )

                            // Forgot Password Link
                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                            ) {
                                TextButton(
                                    onClick = onOpenForgotPassword,
                                    modifier = Modifier.testTag("forgot_password_link")
                                ) {
                                    Text("Forgot Password?", fontSize = 11.sp, color = SimoniumTeal)
                                }
                            }

                            if (errorMessage != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ErrorOutline,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = errorMessage!!,
                                            color = MaterialTheme.colorScheme.error,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            // Sign In Button
                            Button(
                                onClick = {
                                    if (identifier.isBlank() || password.isBlank()) {
                                        errorMessage = "Please enter both identifier and password."
                                        return@Button
                                    }
                                    isLoading = true
                                    val result = viewModel.login(identifier, password)
                                    isLoading = false
                                    if (result.isFailure) {
                                        errorMessage = result.exceptionOrNull()?.message ?: "Authentication failed."
                                    }
                                },
                                enabled = !isLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = SimoniumTeal),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("sign_in_button")
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                                } else {
                                    Text("Sign In to SIMONIUM", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Register / Create Account Button
                            OutlinedButton(
                                onClick = onOpenRegisterWizard,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("register_account_button")
                            ) {
                                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Register / Create Verified Account", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // Quick Demo Role Login Section (for evaluation/testing)
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().testTag("demo_roles_card")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDemoAccounts = !showDemoAccounts }
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Badge,
                                        contentDescription = null,
                                        tint = SimoniumTeal,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Quick Demo Sign-In (Pre-Verified Accounts)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "One-tap login to test any role with pre-configured credentials",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = if (showDemoAccounts) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null
                                )
                            }

                            if (showDemoAccounts) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    demoAccounts.forEach { account ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.loginWithDemoRole(account)
                                                }
                                                .testTag("demo_login_${account.role.name}")
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.padding(10.dp)
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = account.fullName,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp
                                                    )
                                                    Text(
                                                        text = "${account.email} • ${account.institutionName ?: "Independent"}",
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                RoleBadge(role = account.role)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Explore Modules & Verification Requirements Section
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SimoniumTeal.copy(alpha = 0.1f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = SimoniumTeal)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Access Policy & Distinction",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SimoniumTeal
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Selecting a module does not grant access or privileges. Each module enforces strict identity and verification requirements to protect clinical records, academic integrity, and scientific accuracy.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "SIMONIUM Protected Modules",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                items(listOf(ModuleType.HEALTH, ModuleType.EDUCATION, ModuleType.RESEARCH, ModuleType.COMMUNITY)) { module ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectModulePreview(module) }
                            .testTag("preview_module_${module.name}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = module.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SimoniumNavy.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = "Verification Required",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SimoniumNavy,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = module.subtitle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = SimoniumTeal
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = module.description,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Tap to view required credentials",
                                    fontSize = 10.sp,
                                    color = SimoniumTeal,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = SimoniumTeal,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
