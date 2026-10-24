package com.example.simonium.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
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
import com.example.ui.theme.SimoniumNavyDark
import com.example.ui.theme.SimoniumTeal

@Composable
fun HelpSupportDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .testTag("help_support_dialog")
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
                        Icon(imageVector = Icons.Default.HelpOutline, contentDescription = null, tint = SimoniumTeal)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Help & Scientific Support", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Frequently Asked Questions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = SimoniumTeal
                )
                Spacer(modifier = Modifier.height(6.dp))

                FaqItem(
                    q = "Why is module access restricted?",
                    a = "To protect patient confidentiality, preserve laboratory empirical integrity, and ensure academic accountability, access to Health, Education, and Research modules requires verified institutional credentials."
                )

                FaqItem(
                    q = "How long does verification take?",
                    a = "Institutional verification takes between 12 to 48 hours. You may participate in public community observations and access verified scientific findings while your privileged role application is under review."
                )

                FaqItem(
                    q = "What if I do not have a digital ID?",
                    a = "SIMONIUM supports alternative manual verification. During registration, check the 'Manual Verification' option to submit an institutional letter or request verification from a registered school or clinic."
                )

                FaqItem(
                    q = "Can I switch roles during testing?",
                    a = "Yes! Use the 'Quick Demo Sign-In' on the login dashboard or the Role Switcher in Profile to test Doctor, Teacher, Student, Researcher, Administrator, and Community Member roles."
                )

                Spacer(modifier = Modifier.height(14.dp))
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

@Composable
private fun FaqItem(q: String, a: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = q, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = a, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 14.sp)
        }
    }
}
