package com.example.simonium.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simonium.data.model.EvidenceType
import com.example.simonium.data.model.LifecycleStatus
import com.example.ui.theme.*

@Composable
fun LifecycleBadge(status: LifecycleStatus, modifier: Modifier = Modifier) {
    val bgColor: Color
    val textColor: Color
    val icon: ImageVector

    when (status) {
        LifecycleStatus.DRAFT -> {
            bgColor = StatusDraft.copy(alpha = 0.15f)
            textColor = StatusDraft
            icon = Icons.Default.Info
        }
        LifecycleStatus.SUBMITTED -> {
            bgColor = StatusSubmitted.copy(alpha = 0.15f)
            textColor = StatusSubmitted
            icon = Icons.Default.Pending
        }
        LifecycleStatus.UNDER_REVIEW -> {
            bgColor = StatusUnderReview.copy(alpha = 0.15f)
            textColor = StatusUnderReview
            icon = Icons.Default.Pending
        }
        LifecycleStatus.CORRECTION_REQUESTED -> {
            bgColor = StatusCorrection.copy(alpha = 0.15f)
            textColor = StatusCorrection
            icon = Icons.Default.Warning
        }
        LifecycleStatus.APPROVED -> {
            bgColor = StatusApproved.copy(alpha = 0.15f)
            textColor = StatusApproved
            icon = Icons.Default.CheckCircle
        }
        LifecycleStatus.VERIFIED -> {
            bgColor = StatusVerified.copy(alpha = 0.20f)
            textColor = StatusVerified
            icon = Icons.Default.CheckCircle
        }
        LifecycleStatus.REJECTED -> {
            bgColor = StatusRejected.copy(alpha = 0.15f)
            textColor = StatusRejected
            icon = Icons.Default.Warning
        }
        LifecycleStatus.ARCHIVED -> {
            bgColor = StatusArchived.copy(alpha = 0.15f)
            textColor = StatusArchived
            icon = Icons.Default.Info
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = status.label,
            tint = textColor,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = " " + status.label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun EvidenceTypeBadge(evidenceType: EvidenceType, modifier: Modifier = Modifier) {
    val color = Color(evidenceType.badgeColorHex)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Science,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = " " + evidenceType.label,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
