package com.example.simonium.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import com.example.simonium.data.model.VerificationChallenge
import com.example.ui.theme.SimoniumNavyDark
import com.example.ui.theme.SimoniumTeal
import com.example.ui.theme.StatusApproved

@Composable
fun ForgotPasswordDialog(
    onDismiss: () -> Unit,
    onRequestResetCode: (identifier: String) -> Result<VerificationChallenge>,
    onVerifyCode: (code: String) -> Result<Boolean>,
    onCompleteReset: (identifier: String, newPassword: String) -> Result<Boolean>
) {
    var recoveryStep by remember { mutableStateOf(1) } // 1: Enter ID, 2: Enter Code, 3: Set New Password, 4: Done
    var identifier by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var activeTestCode by remember { mutableStateOf<String?>(null) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .testTag("forgot_password_dialog")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LockReset, contentDescription = null, tint = SimoniumTeal)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Account Recovery",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                when (recoveryStep) {
                    1 -> {
                        Text(
                            text = "Enter your registered email address or phone number. If associated with an account, a secure verification challenge will be dispatched.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = identifier,
                            onValueChange = { identifier = it; errorMessage = null },
                            label = { Text("Email or Phone Number *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("forgot_identifier_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                if (identifier.isBlank()) {
                                    errorMessage = "Please enter your identifier."
                                    return@Button
                                }
                                val res = onRequestResetCode(identifier)
                                if (res.isSuccess) {
                                    activeTestCode = res.getOrNull()?.code
                                    recoveryStep = 2
                                    errorMessage = null
                                } else {
                                    errorMessage = res.exceptionOrNull()?.message
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SimoniumTeal),
                            modifier = Modifier.fillMaxWidth().testTag("send_recovery_code_btn")
                        ) {
                            Text("Send Recovery Code", color = Color.White)
                        }
                    }
                    2 -> {
                        Text(
                            text = "Enter the 6-digit recovery code sent to $identifier.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (activeTestCode != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SimoniumTeal.copy(alpha = 0.15f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                                    Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = SimoniumTeal, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Test Sandbox Code: $activeTestCode",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = SimoniumTeal
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = verificationCode,
                            onValueChange = { verificationCode = it; errorMessage = null },
                            label = { Text("6-Digit Code *") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("recovery_code_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { recoveryStep = 1 },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Change ID")
                            }
                            Button(
                                onClick = {
                                    val res = onVerifyCode(verificationCode)
                                    if (res.isSuccess && res.getOrDefault(false)) {
                                        recoveryStep = 3
                                        errorMessage = null
                                    } else {
                                        errorMessage = res.exceptionOrNull()?.message ?: "Invalid code."
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SimoniumTeal),
                                modifier = Modifier.weight(1f).testTag("verify_recovery_code_btn")
                            ) {
                                Text("Verify Code", color = Color.White)
                            }
                        }
                    }
                    3 -> {
                        Text(
                            text = "Create a new strong password for your SIMONIUM account (min 6 characters).",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it; errorMessage = null },
                            label = { Text("New Password *") },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("new_password_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it; errorMessage = null },
                            label = { Text("Confirm New Password *") },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().testTag("confirm_new_password_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                if (newPassword.length < 6) {
                                    errorMessage = "Password must be at least 6 characters."
                                    return@Button
                                }
                                if (newPassword != confirmPassword) {
                                    errorMessage = "Passwords do not match."
                                    return@Button
                                }
                                val res = onCompleteReset(identifier, newPassword)
                                if (res.isSuccess) {
                                    recoveryStep = 4
                                    errorMessage = null
                                } else {
                                    errorMessage = res.exceptionOrNull()?.message
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SimoniumTeal),
                            modifier = Modifier.fillMaxWidth().testTag("save_reset_password_btn")
                        ) {
                            Text("Reset & Save Password", color = Color.White)
                        }
                    }
                    4 -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.LockReset, contentDescription = null, tint = StatusApproved, modifier = Modifier.size(48.dp))
                            Text(text = "Password Reset Successful", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(text = "You can now log in using your updated credentials.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(containerColor = SimoniumNavyDark),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Back to Sign In", color = Color.White)
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
