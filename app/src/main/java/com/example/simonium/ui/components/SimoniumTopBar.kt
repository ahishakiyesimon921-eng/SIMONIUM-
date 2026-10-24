package com.example.simonium.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.simonium.data.model.UserProfile
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimoniumTopBar(
    currentUser: UserProfile,
    isOnline: Boolean,
    isSyncing: Boolean,
    activeLanguage: String,
    onRoleClick: () -> Unit,
    onLanguageToggle: () -> Unit,
    onNotificationsClick: () -> Unit,
    notificationsCount: Int = 0,
    onSettingsClick: (() -> Unit)? = null,
    onOfflineQueueClick: (() -> Unit)? = null
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = SimoniumNavyDark,
            titleContentColor = Color.White
        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                // App Logo
                Image(
                    painter = painterResource(id = R.drawable.simonium_icon),
                    contentDescription = "SIMONIUM Emblem",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "SIMONIUM",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Connecting Knowledge. Enabling Discovery.",
                        fontSize = 9.sp,
                        color = SimoniumCyanLight,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        actions = {
            // Language selector button
            TextButton(
                onClick = onLanguageToggle,
                modifier = Modifier.testTag("language_toggle_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Language",
                    tint = SimoniumCyanLight,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = " " + activeLanguage,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Sync / Network state indicator
            IconButton(
                onClick = {
                    onOfflineQueueClick?.invoke()
                },
                modifier = Modifier.testTag("network_sync_button")
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = SimoniumCyanLight
                    )
                } else if (isOnline) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "Online - Synced",
                        tint = SimoniumTealLight,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = "Offline Mode",
                        tint = Color(0xFFF87171),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Notifications
            IconButton(
                onClick = onNotificationsClick,
                modifier = Modifier.testTag("notifications_button")
            ) {
                if (notificationsCount > 0) {
                    BadgedBox(
                        badge = {
                            Badge(containerColor = SimoniumTeal) {
                                Text("$notificationsCount", color = Color.White, fontSize = 9.sp)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications ($notificationsCount)",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Settings
            if (onSettingsClick != null) {
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.testTag("topbar_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))
        }
    )
}
