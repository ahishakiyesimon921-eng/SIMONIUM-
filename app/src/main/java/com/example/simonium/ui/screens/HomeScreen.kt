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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.simonium.data.model.*
import com.example.simonium.ui.components.EvidenceTypeBadge
import com.example.simonium.ui.components.LifecycleBadge
import com.example.simonium.ui.components.RoleBadge
import com.example.ui.theme.*
import com.example.simonium.ui.viewmodel.NavigationDestination
import com.example.simonium.ui.viewmodel.SimoniumViewModel

@Composable
fun HomeScreen(
    viewModel: SimoniumViewModel,
    onOpenSubmissionDialog: () -> Unit,
    onOpenSurveillanceDialog: () -> Unit,
    onOpenEducationalTaskDialog: () -> Unit,
    onOpenResearchQuestionDialog: () -> Unit,
    onOpenCommunityDialog: () -> Unit,
    onOpenRoleSwitcher: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val submissions by viewModel.submissions.collectAsState()
    val researchProjects by viewModel.researchProjects.collectAsState()
    val educationalTasks by viewModel.educationalTasks.collectAsState()
    val surveillanceReports by viewModel.surveillanceReports.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Banner Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SimoniumNavyDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth().testTag("hero_banner_card")
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.simonium_hero),
                        contentDescription = "SIMONIUM Scientific Network",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        SimoniumNavyDark.copy(alpha = 0.88f),
                                        SimoniumNavyDark
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Connecting Knowledge. Enabling Discovery.",
                            color = SimoniumCyanLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Unified Multidisciplinary Platform",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // 2. Active User Role Bar & Offline Alert
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Welcome back, ${currentUser.fullName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentUser.institutionName ?: "Independent Contributor",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    RoleBadge(
                        role = currentUser.role,
                        onClick = onOpenRoleSwitcher
                    )
                }
            }

            if (!isOnline) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Offline Mode: Submissions will be stored as local drafts and automatically synchronized when connection is restored.",
                            fontSize = 11.sp,
                            color = Color(0xFFEF4444),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // 3. Role-Adaptive Spotlight Dashboard
        item {
            RoleAdaptiveSpotlight(
                currentUser = currentUser,
                surveillanceReports = surveillanceReports,
                educationalTasks = educationalTasks,
                researchProjects = researchProjects,
                submissions = submissions,
                onOpenSurveillanceDialog = onOpenSurveillanceDialog,
                onOpenEducationalTaskDialog = onOpenEducationalTaskDialog,
                onOpenResearchQuestionDialog = onOpenResearchQuestionDialog,
                onOpenCommunityDialog = onOpenCommunityDialog
            )
        }

        // 4. Quick Scientific Action Buttons
        item {
            Column {
                Text(
                    text = "Quick Actions",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        ActionChip(
                            title = "New Evidence",
                            icon = Icons.Default.Add,
                            color = SimoniumTeal,
                            tag = "action_new_evidence",
                            onClick = onOpenSubmissionDialog
                        )
                    }
                    item {
                        ActionChip(
                            title = "Surveillance",
                            icon = Icons.Default.LocalHospital,
                            color = Color(0xFF0284C7),
                            tag = "action_surveillance",
                            onClick = onOpenSurveillanceDialog
                        )
                    }
                    item {
                        ActionChip(
                            title = "Assign Task",
                            icon = Icons.Default.School,
                            color = Color(0xFF7C3AED),
                            tag = "action_assign_task",
                            onClick = onOpenEducationalTaskDialog
                        )
                    }
                    item {
                        ActionChip(
                            title = "Propose Question",
                            icon = Icons.Default.Science,
                            color = Color(0xFFD97706),
                            tag = "action_propose_question",
                            onClick = onOpenResearchQuestionDialog
                        )
                    }
                    item {
                        ActionChip(
                            title = "Community Report",
                            icon = Icons.Default.People,
                            color = Color(0xFF16A34A),
                            tag = "action_community_report",
                            onClick = onOpenCommunityDialog
                        )
                    }
                }
            }
        }

        // 5. Recent Verified Scientific Discoveries & Evidence
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Verified Discoveries & Submissions",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = { viewModel.navigateTo(NavigationDestination.SUBMISSIONS) }
                ) {
                    Text("View All (${submissions.size})", fontSize = 12.sp, color = SimoniumTeal)
                }
            }
        }

        items(submissions.take(5)) { item ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectSubmission(item) }
                    .testTag("submission_card_${item.id}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SimoniumTeal.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = item.module.title,
                                color = SimoniumTeal,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                        LifecycleBadge(status = item.status)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.description,
                        fontSize = 12.sp,
                        maxLines = 2,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        EvidenceTypeBadge(evidenceType = item.evidenceType)
                        Text(
                            text = "by ${item.authorName}",
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
fun RoleAdaptiveSpotlight(
    currentUser: UserProfile,
    surveillanceReports: List<SurveillanceReport>,
    educationalTasks: List<EducationalTask>,
    researchProjects: List<ResearchProject>,
    submissions: List<SubmissionItem>,
    onOpenSurveillanceDialog: () -> Unit,
    onOpenEducationalTaskDialog: () -> Unit,
    onOpenResearchQuestionDialog: () -> Unit,
    onOpenCommunityDialog: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SimoniumNavy),
        modifier = Modifier.fillMaxWidth().testTag("role_spotlight_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            when (currentUser.role) {
                UserRole.HEALTHCARE_PRO -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocalHospital, contentDescription = null, tint = SimoniumCyanLight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Healthcare Surveillance Dashboard",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Confidential Clinical Surveillance: Monitor syndromic clusters and link clinical questions to biomedical researchers.",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Active Surveillance Reports: ${surveillanceReports.size}",
                            color = SimoniumCyanLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(onClick = onOpenSurveillanceDialog) {
                            Text("+ File Surveillance", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
                UserRole.TEACHER -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.School, contentDescription = null, tint = SimoniumCyanLight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Educator Guidance & Fieldwork Center",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Transform scientific research questions into student inquiry tasks. Review student submissions and guide practical skills.",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Assigned Fieldwork Tasks: ${educationalTasks.size}",
                            color = SimoniumCyanLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(onClick = onOpenEducationalTaskDialog) {
                            Text("+ Create Task", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
                UserRole.RESEARCHER -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Science, contentDescription = null, tint = SimoniumCyanLight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Scientific Research & Discovery Hub",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Formulate empirical hypotheses, analyze peer-reviewed evidence, and verify scientific findings.",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Active Projects: ${researchProjects.size} • Pending Reviews: ${submissions.count { it.status == LifecycleStatus.UNDER_REVIEW }}",
                            color = SimoniumCyanLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(onClick = onOpenResearchQuestionDialog) {
                            Text("+ Propose Question", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
                UserRole.STUDENT -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = SimoniumCyanLight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Student Discovery & Fieldwork Tasks",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Participate in real-world research tasks. Collect water quality data, document local biodiversity, and receive teacher feedback.",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Active Tasks Available: ${educationalTasks.size}",
                        color = SimoniumCyanLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                UserRole.COMMUNITY_MEMBER -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.People, contentDescription = null, tint = SimoniumCyanLight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Community Citizen Science Hub",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Report local conditions: water changes, unusual pest activity, or health concerns. Connect with scientific researchers.",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onOpenCommunityDialog) {
                        Text("+ Submit Local Observation", color = SimoniumCyanLight, fontSize = 11.sp)
                    }
                }
                UserRole.ADMINISTRATOR -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = SimoniumCyanLight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Platform Governance & Security Audit",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "System health normal. Database schemas synchronized with Supabase PostgreSQL RLS policies.",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Total Submissions: ${submissions.size} • 4 Active Modules • Immutable Audit Trail Enabled",
                        color = SimoniumCyanLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                else -> {
                    Text(
                        text = "SIMONIUM Multidisciplinary Platform: Explore interdisciplinary research connections.",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ActionChip(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        modifier = Modifier
            .clickable { onClick() }
            .testTag(tag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
