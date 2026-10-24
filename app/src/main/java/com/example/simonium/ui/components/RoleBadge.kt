package com.example.simonium.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simonium.data.model.UserRole
import com.example.ui.theme.SimoniumCyanLight
import com.example.ui.theme.SimoniumNavy
import com.example.ui.theme.SimoniumNavyElevated
import com.example.ui.theme.SimoniumTeal

@Composable
fun RoleBadge(
    role: UserRole,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val (icon, bgCol, textCol) = when (role) {
        UserRole.ADMINISTRATOR -> Triple(Icons.Default.AdminPanelSettings, Color(0xFF4C1D95), Color(0xFFDDD6FE))
        UserRole.HEALTHCARE_PRO -> Triple(Icons.Default.LocalHospital, Color(0xFF0369A1), Color(0xFFBAE6FD))
        UserRole.TEACHER -> Triple(Icons.Default.School, Color(0xFF4338CA), Color(0xFFC7D2FE))
        UserRole.RESEARCHER -> Triple(Icons.Default.Science, Color(0xFF0F766E), Color(0xFFCCFBF1))
        UserRole.STUDENT -> Triple(Icons.Default.Person, Color(0xFF334155), Color(0xFFE2E8F0))
        UserRole.COMMUNITY_MEMBER -> Triple(Icons.Default.Person, Color(0xFF15803D), Color(0xFFDCFCE7))
        UserRole.DISCOVERER -> Triple(Icons.Default.Science, Color(0xFFB45309), Color(0xFFFEF3C7))
        UserRole.INSTITUTION_REP -> Triple(Icons.Default.AdminPanelSettings, Color(0xFF1E293B), Color(0xFFF1F5F9))
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(bgCol, RoundedCornerShape(16.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textCol,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = " " + role.displayName,
            color = textCol,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        if (onClick != null) {
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Switch Role",
                tint = textCol,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
