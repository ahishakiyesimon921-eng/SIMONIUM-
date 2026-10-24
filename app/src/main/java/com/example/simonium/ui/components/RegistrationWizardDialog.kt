package com.example.simonium.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.simonium.data.model.*
import com.example.simonium.data.util.*
import com.example.ui.theme.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationWizardDialog(
    initialRole: UserRole = UserRole.STUDENT,
    onDismiss: () -> Unit,
    onRequestVerificationCode: (contact: String, type: String) -> Result<VerificationChallenge>,
    onVerifyCode: (code: String) -> Result<Boolean>,
    onCompleteRegistration: (application: VerificationApplication, password: String) -> Unit
) {
    var currentStep by remember { mutableStateOf(1) } // 1 to 9
    var isDraftSaved by remember { mutableStateOf(false) }

    // Step 1: Role
    var selectedRole by remember { mutableStateOf(initialRole) }

    // Step 2: Basic Account Details
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+250 788 ") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Step 3: Modules
    var selectedModules by remember { mutableStateOf(setOf(ModuleType.EDUCATION, ModuleType.COMMUNITY)) }

    // Step 4: Role-specific Info
    var institutionName by remember { mutableStateOf("") }
    var regOrStaffNumber by remember { mutableStateOf("") }
    var departmentOrProgramme by remember { mutableStateOf("") }
    var specialityOrAcademicYear by remember { mutableStateOf("") }
    var communityNameOrFocus by remember { mutableStateOf("") }

    // Step 5: Document Upload
    var documentType by remember { mutableStateOf("Official ID Card") }
    var issuingAuthority by remember { mutableStateOf("Institutional Registrar") }
    var documentNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("2028-12-31") }
    var uploadedFileName by remember { mutableStateOf("credentials_scan.pdf") }
    var uploadedFileSizeKb by remember { mutableStateOf(650) }
    var isManualVerificationRequested by remember { mutableStateOf(false) }

    // Step 6: Code Challenge
    var verificationCodeInput by remember { mutableStateOf("") }
    var isCodeVerified by remember { mutableStateOf(false) }
    var activeChallengeCode by remember { mutableStateOf<String?>(null) }
    var resendCooldownSeconds by remember { mutableStateOf(0) }

    // Step 7: Privacy & Terms
    var termsAccepted by remember { mutableStateOf(false) }
    var confidentialityAccepted by remember { mutableStateOf(false) }

    // Step 8 & 9: Status
    var applicationStatus by remember { mutableStateOf(VerificationApplicationStatus.VERIFICATION_PENDING) }
    var generatedAppId by remember { mutableStateOf("APP-" + UUID.randomUUID().toString().take(8).uppercase()) }

    var stepError by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(4.dp)
                .testTag("registration_wizard_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Wizard Header with Title, Progress, and Cancel
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = SimoniumTeal,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$currentStep",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Registration Wizard",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Step $currentStep of 9: ${getStepTitle(currentStep)}",
                                fontSize = 11.sp,
                                color = SimoniumTeal,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Bar Indicator
                LinearProgressIndicator(
                    progress = { currentStep / 9f },
                    color = SimoniumTeal,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                )

                if (isDraftSaved) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SimoniumTeal.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(6.dp)) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = SimoniumTeal, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Draft saved locally. You can resume at any time.", color = SimoniumTeal, fontSize = 10.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Step Content (Scrollable)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    when (currentStep) {
                        1 -> Step1ChooseRole(
                            selectedRole = selectedRole,
                            onSelectRole = {
                                selectedRole = it
                                stepError = null
                            }
                        )
                        2 -> Step2BasicInfo(
                            fullName = fullName,
                            onFullNameChange = { fullName = it; stepError = null },
                            email = email,
                            onEmailChange = { email = it; stepError = null },
                            phone = phone,
                            onPhoneChange = { phone = it; stepError = null },
                            password = password,
                            onPasswordChange = { password = it; stepError = null },
                            confirmPassword = confirmPassword,
                            onConfirmPasswordChange = { confirmPassword = it; stepError = null },
                            passwordVisible = passwordVisible,
                            onTogglePasswordVisibility = { passwordVisible = !passwordVisible }
                        )
                        3 -> Step3SelectModules(
                            selectedRole = selectedRole,
                            selectedModules = selectedModules,
                            onToggleModule = { mod ->
                                selectedModules = if (selectedModules.contains(mod)) {
                                    if (selectedModules.size > 1) selectedModules - mod else selectedModules
                                } else {
                                    selectedModules + mod
                                }
                            }
                        )
                        4 -> Step4RoleSpecificDetails(
                            role = selectedRole,
                            institutionName = institutionName,
                            onInstitutionChange = { institutionName = it; stepError = null },
                            regOrStaffNumber = regOrStaffNumber,
                            onRegOrStaffChange = { regOrStaffNumber = it; stepError = null },
                            departmentOrProgramme = departmentOrProgramme,
                            onDeptChange = { departmentOrProgramme = it; stepError = null },
                            specialityOrYear = specialityOrAcademicYear,
                            onSpecialityChange = { specialityOrAcademicYear = it; stepError = null },
                            communityName = communityNameOrFocus,
                            onCommunityNameChange = { communityNameOrFocus = it; stepError = null }
                        )
                        5 -> Step5DocumentUpload(
                            role = selectedRole,
                            documentType = documentType,
                            onDocumentTypeChange = { documentType = it },
                            documentNumber = documentNumber,
                            onDocumentNumberChange = { documentNumber = it; stepError = null },
                            issuingAuthority = issuingAuthority,
                            onAuthorityChange = { issuingAuthority = it },
                            expiryDate = expiryDate,
                            onExpiryChange = { expiryDate = it },
                            uploadedFileName = uploadedFileName,
                            onSimulateUpload = { fileName, size ->
                                uploadedFileName = fileName
                                uploadedFileSizeKb = size
                            },
                            isManualVerification = isManualVerificationRequested,
                            onToggleManualVerification = { isManualVerificationRequested = it }
                        )
                        6 -> Step6VerificationCode(
                            targetContact = email.ifBlank { phone },
                            activeCode = activeChallengeCode,
                            codeValue = verificationCodeInput,
                            onCodeValueChange = { verificationCodeInput = it; stepError = null },
                            isVerified = isCodeVerified,
                            onRequestCode = {
                                val contact = email.ifBlank { phone }
                                val res = onRequestVerificationCode(contact, if (contact.contains("@")) "EMAIL" else "PHONE")
                                if (res.isSuccess) {
                                    activeChallengeCode = res.getOrNull()?.code
                                    stepError = null
                                } else {
                                    stepError = res.exceptionOrNull()?.message
                                }
                            },
                            onVerify = {
                                val res = onVerifyCode(verificationCodeInput)
                                if (res.isSuccess && res.getOrDefault(false)) {
                                    isCodeVerified = true
                                    stepError = null
                                } else {
                                    stepError = res.exceptionOrNull()?.message ?: "Invalid code."
                                }
                            }
                        )
                        7 -> Step7PrivacyAndTerms(
                            termsAccepted = termsAccepted,
                            onTermsChange = { termsAccepted = it; stepError = null },
                            confidentialityAccepted = confidentialityAccepted,
                            onConfidentialityChange = { confidentialityAccepted = it; stepError = null }
                        )
                        8 -> Step8ApplicationSummary(
                            fullName = fullName,
                            email = email,
                            phone = phone,
                            role = selectedRole,
                            modules = selectedModules,
                            institution = institutionName,
                            docType = documentType,
                            docNumber = documentNumber,
                            fileName = uploadedFileName
                        )
                        9 -> Step9StatusDisplay(
                            appId = generatedAppId,
                            status = applicationStatus,
                            fullName = fullName,
                            role = selectedRole,
                            onFinish = onDismiss
                        )
                    }
                }

                if (stepError != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stepError!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons: Back, Save Draft, Next, Submit
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (currentStep in 2..8) {
                        OutlinedButton(
                            onClick = {
                                stepError = null
                                currentStep--
                            },
                            modifier = Modifier.testTag("wizard_back_button")
                        ) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Back", fontSize = 11.sp)
                        }
                    } else if (currentStep == 1) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", fontSize = 11.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (currentStep in 1..8) {
                            TextButton(
                                onClick = {
                                    isDraftSaved = true
                                },
                                modifier = Modifier.testTag("wizard_save_draft_button")
                            ) {
                                Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save Draft", fontSize = 11.sp)
                            }
                        }

                        if (currentStep in 1..7) {
                            Button(
                                onClick = {
                                    // Validate Current Step
                                    val error = validateStep(
                                        step = currentStep,
                                        fullName = fullName,
                                        email = email,
                                        password = password,
                                        confirmPassword = confirmPassword,
                                        institution = institutionName,
                                        role = selectedRole,
                                        regOrStaff = regOrStaffNumber,
                                        docNumber = documentNumber,
                                        isManual = isManualVerificationRequested,
                                        isCodeVerified = isCodeVerified,
                                        termsAccepted = termsAccepted,
                                        confidentialityAccepted = confidentialityAccepted
                                    )
                                    if (error != null) {
                                        stepError = error
                                    } else {
                                        stepError = null
                                        if (currentStep == 5 && activeChallengeCode == null) {
                                            // Pre-generate code when moving to Step 6
                                            val contact = email.ifBlank { phone }
                                            val res = onRequestVerificationCode(contact, if (contact.contains("@")) "EMAIL" else "PHONE")
                                            activeChallengeCode = res.getOrNull()?.code
                                        }
                                        currentStep++
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SimoniumTeal),
                                modifier = Modifier.testTag("wizard_next_button")
                            ) {
                                Text("Next", fontSize = 11.sp, color = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                            }
                        } else if (currentStep == 8) {
                            // Final Submit
                            Button(
                                onClick = {
                                    val application = VerificationApplication(
                                        id = generatedAppId,
                                        userId = "",
                                        userFullName = fullName,
                                        userEmail = email,
                                        requestedRole = selectedRole,
                                        requestedModules = selectedModules.toList(),
                                        status = if (selectedRole.isPrivileged) VerificationApplicationStatus.VERIFICATION_PENDING else VerificationApplicationStatus.APPROVED,
                                        institutionName = institutionName,
                                        roleSpecificDetails = mapOf(
                                            "identifierNumber" to regOrStaffNumber,
                                            "department" to departmentOrProgramme,
                                            "specialty" to specialityOrAcademicYear,
                                            "community" to communityNameOrFocus
                                        ),
                                        documents = if (isManualVerificationRequested) emptyList() else listOf(
                                            VerificationDocument(
                                                id = "doc-" + UUID.randomUUID().toString().take(6),
                                                userId = "",
                                                documentType = documentType,
                                                fileName = uploadedFileName,
                                                fileSizeKb = uploadedFileSizeKb,
                                                issuingAuthority = issuingAuthority,
                                                documentNumber = documentNumber,
                                                expiryDate = expiryDate
                                            )
                                        )
                                    )
                                    applicationStatus = application.status
                                    onCompleteRegistration(application, password)
                                    currentStep = 9
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SimoniumTeal),
                                modifier = Modifier.testTag("wizard_submit_button")
                            ) {
                                Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Submit for Verification", fontSize = 11.sp, color = Color.White)
                            }
                        } else {
                            // Step 9: Finished
                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(containerColor = SimoniumNavyDark),
                                modifier = Modifier.testTag("wizard_finish_button")
                            ) {
                                Text("Proceed to Platform", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getStepTitle(step: Int): String {
    return when (step) {
        1 -> "Choose Requested Role"
        2 -> "Account Credentials"
        3 -> "Select Modules"
        4 -> "Role-Specific Details"
        5 -> "Upload Evidence / ID"
        6 -> "Verify Contact Code"
        7 -> "Privacy & Scientific Terms"
        8 -> "Review & Submit"
        9 -> "Application Status"
        else -> ""
    }
}

private fun validateStep(
    step: Int,
    fullName: String,
    email: String,
    password: String,
    confirmPassword: String,
    institution: String,
    role: UserRole,
    regOrStaff: String,
    docNumber: String,
    isManual: Boolean,
    isCodeVerified: Boolean,
    termsAccepted: Boolean,
    confidentialityAccepted: Boolean
): String? {
    return when (step) {
        1 -> null // role selected
        2 -> {
            if (fullName.isBlank()) "Please enter your full name."
            else if (!email.contains("@") || !email.contains(".")) "Please enter a valid email address."
            else if (password.length < 6) "Password must be at least 6 characters long."
            else if (password != confirmPassword) "Passwords do not match."
            else null
        }
        3 -> null // modules checked
        4 -> {
            if (role == UserRole.STUDENT && regOrStaff.isBlank()) "Student registration number is required."
            else if (role == UserRole.TEACHER && regOrStaff.isBlank()) "Staff ID number is required."
            else if (role == UserRole.HEALTHCARE_PRO && regOrStaff.isBlank()) "Medical licence number is required."
            else if (role.isPrivileged && institution.isBlank()) "Institution / Facility name is required."
            else null
        }
        5 -> {
            if (!isManual && docNumber.isBlank()) "Please enter document/licence number or request manual verification."
            else null
        }
        6 -> {
            if (!isCodeVerified) "Please enter and verify the 6-digit confirmation code."
            else null
        }
        7 -> {
            if (!termsAccepted || !confidentialityAccepted) "You must accept the data privacy policy and ethics agreement."
            else null
        }
        else -> null
    }
}

@Composable
private fun Step1ChooseRole(
    selectedRole: UserRole,
    onSelectRole: (UserRole) -> Unit
) {
    Column {
        Text(
            text = "Select your primary role or affiliation in SIMONIUM:",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))

        listOf(
            UserRole.STUDENT,
            UserRole.TEACHER,
            UserRole.RESEARCHER,
            UserRole.HEALTHCARE_PRO,
            UserRole.COMMUNITY_MEMBER,
            UserRole.DISCOVERER,
            UserRole.INSTITUTION_REP
        ).forEach { role ->
            val isSelected = selectedRole == role
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) SimoniumTeal.copy(alpha = 0.12f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onSelectRole(role) }
                    .testTag("role_option_${role.name}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelectRole(role) }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = role.displayName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            if (role.isPrivileged) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFD97706).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Verification Required",
                                        color = Color(0xFFD97706),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = role.description,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Step2BasicInfo(
    fullName: String,
    onFullNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = fullName,
            onValueChange = onFullNameChange,
            label = { Text("Full Name *") },
            placeholder = { Text("e.g. Dr. Keza Claire") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("reg_full_name")
        )

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email Address *") },
            placeholder = { Text("e.g. claire@ur.ac.rw") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth().testTag("reg_email")
        )

        var selectedCountry by remember { mutableStateOf(CountryRegistry.countries.first()) }
        var showCountryMenu by remember { mutableStateOf(false) }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(0.38f)) {
                OutlinedButton(
                    onClick = { showCountryMenu = true },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp).testTag("reg_country_dropdown_btn")
                ) {
                    Text("${selectedCountry.flagEmoji} ${selectedCountry.callingCode}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                DropdownMenu(
                    expanded = showCountryMenu,
                    onDismissRequest = { showCountryMenu = false }
                ) {
                    CountryRegistry.countries.forEach { country ->
                        DropdownMenuItem(
                            text = { Text("${country.flagEmoji} ${country.name} (${country.callingCode})", fontSize = 11.sp) },
                            onClick = {
                                selectedCountry = country
                                showCountryMenu = false
                                if (!phone.startsWith(country.callingCode)) {
                                    onPhoneChange("${country.callingCode} ")
                                }
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = phone,
                onValueChange = onPhoneChange,
                label = { Text("Phone Number *") },
                placeholder = { Text(selectedCountry.sampleFormat) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.weight(0.62f).testTag("reg_phone")
            )
        }

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Password * (Min 6 characters)") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth().testTag("reg_password")
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = { Text("Confirm Password *") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth().testTag("reg_confirm_password")
        )

        // Password strength guidance
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = SimoniumTeal, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (password.length >= 8 && password.any { it.isDigit() }) "Password Strength: Strong (Meets scientific platform standard)"
                    else "Password Guidance: Use 8+ characters including letters and numbers.",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun Step3SelectModules(
    selectedRole: UserRole,
    selectedModules: Set<ModuleType>,
    onToggleModule: (ModuleType) -> Unit
) {
    Column {
        Text(
            text = "Select modules you plan to participate in. Selecting a module registers your request; access will be activated upon role verification:",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        listOf(ModuleType.HEALTH, ModuleType.EDUCATION, ModuleType.RESEARCH, ModuleType.COMMUNITY).forEach { module ->
            val isChecked = selectedModules.contains(module)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isChecked) SimoniumTeal.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(10.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onToggleModule(module) }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { onToggleModule(module) }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(text = module.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = module.subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun Step4RoleSpecificDetails(
    role: UserRole,
    institutionName: String,
    onInstitutionChange: (String) -> Unit,
    regOrStaffNumber: String,
    onRegOrStaffChange: (String) -> Unit,
    departmentOrProgramme: String,
    onDeptChange: (String) -> Unit,
    specialityOrYear: String,
    onSpecialityChange: (String) -> Unit,
    communityName: String,
    onCommunityNameChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = SimoniumTeal.copy(alpha = 0.12f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Credentials required for: ${role.displayName}",
                color = SimoniumTeal,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.padding(8.dp)
            )
        }

        when (role) {
            UserRole.STUDENT -> {
                OutlinedTextField(
                    value = regOrStaffNumber,
                    onValueChange = onRegOrStaffChange,
                    label = { Text("Student Registration Number *") },
                    placeholder = { Text("e.g. 221004589") },
                    modifier = Modifier.fillMaxWidth().testTag("reg_student_id")
                )
                OutlinedTextField(
                    value = institutionName,
                    onValueChange = onInstitutionChange,
                    label = { Text("University / Secondary School Name *") },
                    placeholder = { Text("e.g. University of Rwanda") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = departmentOrProgramme,
                    onValueChange = onDeptChange,
                    label = { Text("Department or Academic Programme") },
                    placeholder = { Text("e.g. Biotechnology & Environmental Biology") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = specialityOrYear,
                    onValueChange = onSpecialityChange,
                    label = { Text("Academic Year / Level") },
                    placeholder = { Text("e.g. Year 3 or Senior 5") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            UserRole.TEACHER -> {
                OutlinedTextField(
                    value = regOrStaffNumber,
                    onValueChange = onRegOrStaffChange,
                    label = { Text("Staff / Teacher ID Number *") },
                    placeholder = { Text("e.g. TCH-88219") },
                    modifier = Modifier.fillMaxWidth().testTag("reg_teacher_id")
                )
                OutlinedTextField(
                    value = institutionName,
                    onValueChange = onInstitutionChange,
                    label = { Text("School / Academic Institution Name *") },
                    placeholder = { Text("e.g. Green Hills Scientific Academy") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = departmentOrProgramme,
                    onValueChange = onDeptChange,
                    label = { Text("Teaching Responsibility / Subject") },
                    placeholder = { Text("e.g. Biology & Chemistry Lead") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            UserRole.HEALTHCARE_PRO -> {
                OutlinedTextField(
                    value = regOrStaffNumber,
                    onValueChange = onRegOrStaffChange,
                    label = { Text("Professional Medical Licence Number *") },
                    placeholder = { Text("e.g. RMC-2022-9912") },
                    modifier = Modifier.fillMaxWidth().testTag("reg_doctor_id")
                )
                OutlinedTextField(
                    value = institutionName,
                    onValueChange = onInstitutionChange,
                    label = { Text("Hospital / Clinical Referral Center *") },
                    placeholder = { Text("e.g. CHUK or District Hospital") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = specialityOrYear,
                    onValueChange = onSpecialityChange,
                    label = { Text("Clinical Category / Specialization") },
                    placeholder = { Text("e.g. Infectious Disease, Epidemiology") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            UserRole.RESEARCHER -> {
                OutlinedTextField(
                    value = regOrStaffNumber,
                    onValueChange = onRegOrStaffChange,
                    label = { Text("ORCID or Researcher Identification") },
                    placeholder = { Text("e.g. 0000-0002-1825-0097") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = institutionName,
                    onValueChange = onInstitutionChange,
                    label = { Text("Research Institute / University Laboratory *") },
                    placeholder = { Text("e.g. NIRDA or RBC") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = specialityOrYear,
                    onValueChange = onSpecialityChange,
                    label = { Text("Research Interests & Discipline") },
                    placeholder = { Text("e.g. Vector Ecology & Phytochemistry") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            UserRole.COMMUNITY_MEMBER -> {
                OutlinedTextField(
                    value = communityName,
                    onValueChange = onCommunityNameChange,
                    label = { Text("Community / Cooperative Name") },
                    placeholder = { Text("e.g. Musanze Agro-Ecological Cooperative") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = specialityOrYear,
                    onValueChange = onSpecialityChange,
                    label = { Text("Local Observation Focus") },
                    placeholder = { Text("e.g. River Water Health & Soil Quality") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            else -> {
                OutlinedTextField(
                    value = institutionName,
                    onValueChange = onInstitutionChange,
                    label = { Text("Affiliated Institution or Organization") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun Step5DocumentUpload(
    role: UserRole,
    documentType: String,
    onDocumentTypeChange: (String) -> Unit,
    documentNumber: String,
    onDocumentNumberChange: (String) -> Unit,
    issuingAuthority: String,
    onAuthorityChange: (String) -> Unit,
    expiryDate: String,
    onExpiryChange: (String) -> Unit,
    uploadedFileName: String,
    onSimulateUpload: (name: String, sizeKb: Int) -> Unit,
    isManualVerification: Boolean,
    onToggleManualVerification: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Upload identity or institutional affiliation evidence. Documents are stored in secure private storage accessible only by authorized verification officers:",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 15.sp
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isManualVerification,
                onCheckedChange = onToggleManualVerification
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = "I do not have a conventional digital ID card",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Request alternative manual verification via community elder or institutional letter",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (!isManualVerification) {
            OutlinedTextField(
                value = documentType,
                onValueChange = onDocumentTypeChange,
                label = { Text("Document Type") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = documentNumber,
                onValueChange = onDocumentNumberChange,
                label = { Text("Document / Serial / Licence Number *") },
                placeholder = { Text("e.g. STU-2024-9024") },
                modifier = Modifier.fillMaxWidth().testTag("reg_doc_number")
            )

            OutlinedTextField(
                value = issuingAuthority,
                onValueChange = onAuthorityChange,
                label = { Text("Issuing Authority") },
                placeholder = { Text("e.g. University Registrar or Medical Council") },
                modifier = Modifier.fillMaxWidth()
            )

            // Document Upload Box
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, tint = SimoniumTeal, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Document File: $uploadedFileName", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(text = "PDF, JPEG, or PNG (Max 5MB) • Validated", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            onSimulateUpload("id_credential_${System.currentTimeMillis().toString().takeLast(4)}.pdf", 720)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SimoniumTeal)
                    ) {
                        Text("Select Document File", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun Step6VerificationCode(
    targetContact: String,
    activeCode: String?,
    codeValue: String,
    onCodeValueChange: (String) -> Unit,
    isVerified: Boolean,
    onRequestCode: () -> Unit,
    onVerify: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "To prevent unauthorized accounts, verify your contact identifier ($targetContact).",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Sandbox Test Code Display Banner
        if (activeCode != null) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SimoniumTeal.copy(alpha = 0.15f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(10.dp)) {
                    Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = SimoniumTeal, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "Test Sandbox Delivery Code: $activeCode", fontWeight = FontWeight.Black, color = SimoniumTeal, fontSize = 13.sp)
                        Text(text = "In production, delivered via configured SMS / SMTP gateway.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        OutlinedTextField(
            value = codeValue,
            onValueChange = onCodeValueChange,
            label = { Text("6-Digit Verification Code *") },
            placeholder = { Text("Enter 6-digit code") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().testTag("reg_code_input")
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onRequestCode,
                modifier = Modifier.weight(1f)
            ) {
                Text("Send Code", fontSize = 11.sp)
            }

            Button(
                onClick = onVerify,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isVerified) StatusApproved else SimoniumTeal
                ),
                modifier = Modifier.weight(1f).testTag("reg_verify_code_btn")
            ) {
                if (isVerified) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Verified", color = Color.White, fontSize = 11.sp)
                } else {
                    Text("Verify Code", color = Color.White, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun Step7PrivacyAndTerms(
    termsAccepted: Boolean,
    onTermsChange: (Boolean) -> Unit,
    confidentialityAccepted: Boolean,
    onConfidentialityChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "SIMONIUM Scientific Ethics & Data Privacy Agreement",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = """
1. Patient Confidentiality: I will not upload or disclose identifying personal health data.
2. Scientific Accuracy: Observations and research must reflect empirical measurements with authentic sources.
3. Verification Integrity: Privileged medical and educational decisions require independent human expert review.
4. Security & Audit: All activities are immutably logged for audit oversight.
                    """.trimIndent(),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = termsAccepted,
                onCheckedChange = onTermsChange,
                modifier = Modifier.testTag("reg_terms_checkbox")
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "I accept the Terms of Service & Data Policy *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = confidentialityAccepted,
                onCheckedChange = onConfidentialityChange,
                modifier = Modifier.testTag("reg_confidentiality_checkbox")
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "I agree to uphold scientific ethics and confidentiality *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun Step8ApplicationSummary(
    fullName: String,
    email: String,
    phone: String,
    role: UserRole,
    modules: Set<ModuleType>,
    institution: String,
    docType: String,
    docNumber: String,
    fileName: String
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = "Application Summary Review", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            HorizontalDivider()
            Text(text = "Applicant: $fullName", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(text = "Contact: $email • $phone", fontSize = 11.sp)
            Text(text = "Requested Role: ${role.displayName}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SimoniumTeal)
            Text(text = "Affiliated Institution: ${institution.ifBlank { "Independent" }}", fontSize = 11.sp)
            Text(text = "Selected Modules: ${modules.joinToString { it.title }}", fontSize = 11.sp)
            Text(text = "Uploaded Document: $docType ($docNumber)", fontSize = 11.sp)
            Text(text = "File: $fileName", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Note: Privileged roles will enter 'Verification Pending' status until reviewed by an institutional verification officer.",
                fontSize = 10.sp,
                color = Color(0xFFD97706),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun Step9StatusDisplay(
    appId: String,
    status: VerificationApplicationStatus,
    fullName: String,
    role: UserRole,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = StatusApproved,
            modifier = Modifier.size(56.dp)
        )

        Text(
            text = "Application Submitted Successfully",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFD97706).copy(alpha = 0.15f)
        ) {
            Text(
                text = "Status: ${status.label}",
                color = Color(0xFFD97706),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(text = "Tracking Application ID: $appId", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Welcome to SIMONIUM, $fullName. Your account has been registered for role '${role.displayName}'. An institutional verification officer will review your submitted credentials. You can access public resources immediately.",
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
