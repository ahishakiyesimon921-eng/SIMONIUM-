package com.example.simonium.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simonium.data.model.EvidenceType
import com.example.simonium.data.model.LifecycleStatus
import com.example.simonium.data.model.UserRole
import com.example.simonium.ui.components.EvidenceTypeBadge
import com.example.simonium.ui.components.LifecycleBadge
import com.example.ui.theme.*
import com.example.simonium.ui.viewmodel.SimoniumViewModel

@Composable
fun ResearchScreen(
    viewModel: SimoniumViewModel,
    onOpenResearchQuestionDialog: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val researchProjects by viewModel.researchProjects.collectAsState()
    val researchQuestions by viewModel.researchQuestions.collectAsState()
    val submissions by viewModel.submissions.collectAsState()

    var activeResearchTab by remember { mutableStateOf(0) } // 0: Scientific Pipeline, 1: Projects, 2: Research Questions, 3: Verified Discoveries

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("research_screen")
    ) {
        TabRow(
            selectedTabIndex = activeResearchTab,
            containerColor = SimoniumNavyDark,
            contentColor = SimoniumCyanLight
        ) {
            Tab(
                selected = activeResearchTab == 0,
                onClick = { activeResearchTab = 0 },
                text = { Text("Workflow", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeResearchTab == 1,
                onClick = { activeResearchTab = 1 },
                text = { Text("Projects (${researchProjects.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeResearchTab == 2,
                onClick = { activeResearchTab = 2 },
                text = { Text("Questions (${researchQuestions.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeResearchTab == 3,
                onClick = { activeResearchTab = 3 },
                text = { Text("Verified Findings", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (activeResearchTab) {
                0 -> {
                    // Scientific Workflow Engine Graphic / Explainer
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SimoniumNavy),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "SIMONIUM Scientific Discovery Pipeline",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "An unbroken chain connecting real problems to verified findings:",
                                    fontSize = 11.sp,
                                    color = SimoniumCyanLight
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                WorkflowStepItem(
                                    stepNum = "1",
                                    title = "Formulate Questions & Hypotheses",
                                    desc = "Researchers & institutions identify real problems (e.g. malaria shifts, water purity).",
                                    icon = Icons.Default.HelpOutline,
                                    color = Color(0xFFD97706)
                                )
                                WorkflowStepItem(
                                    stepNum = "2",
                                    title = "Curate Inquiry & Educational Tasks",
                                    desc = "Teachers adapt questions into classroom activities and student fieldwork.",
                                    icon = Icons.Default.School,
                                    color = Color(0xFF7C3AED)
                                )
                                WorkflowStepItem(
                                    stepNum = "3",
                                    title = "Collect Observations & Citizen Data",
                                    desc = "Students & communities record raw environmental & health observations.",
                                    icon = Icons.Default.Sensors,
                                    color = Color(0xFF0284C7)
                                )
                                WorkflowStepItem(
                                    stepNum = "4",
                                    title = "Peer Review & Verification Decision",
                                    desc = "Authorized experts review data controls, calibrate methods, and verify findings.",
                                    icon = Icons.Default.CheckCircle,
                                    color = Color(0xFF10B981)
                                )
                                WorkflowStepItem(
                                    stepNum = "5",
                                    title = "Source-Linked Verified Discovery",
                                    desc = "Approved findings are registered in knowledge database, linked to raw evidence.",
                                    icon = Icons.Default.Lightbulb,
                                    color = SimoniumCyanLight
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Distinction of Information Phases",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            PhaseCard(
                                type = EvidenceType.RAW_OBSERVATION,
                                title = "1. Raw Observation",
                                description = "Unverified field measurements, community notices, sensor feeds. Subject to error."
                            )
                            PhaseCard(
                                type = EvidenceType.HYPOTHESIS,
                                title = "2. Scientific Hypothesis",
                                description = "Proposed causal explanation awaiting systematic empirical testing and replication."
                            )
                            PhaseCard(
                                type = EvidenceType.REVIEWED_EVIDENCE,
                                title = "3. Reviewed Evidence",
                                description = "Observations examined by teachers, clinicians, or peers with verified controls."
                            )
                            PhaseCard(
                                type = EvidenceType.VERIFIED_FINDING,
                                title = "4. Verified Scientific Finding",
                                description = "Rigorously validated conclusions linked to verified sources and reproducible data."
                            )
                        }
                    }
                }
                1 -> {
                    // Research Projects
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Active Research Projects",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (currentUser.role == UserRole.RESEARCHER || currentUser.role == UserRole.ADMINISTRATOR) {
                                Button(
                                    onClick = onOpenResearchQuestionDialog,
                                    colors = ButtonDefaults.buttonColors(containerColor = SimoniumTeal)
                                ) {
                                    Text("+ New Question", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }

                    items(researchProjects) { project ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = SimoniumTeal.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = project.field,
                                            color = SimoniumTeal,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Text(
                                        text = project.status,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0284C7)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = project.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Lead: ${project.leadResearcher} • ${project.leadInstitution}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Methodology: ${project.methodology}",
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Objectives:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                project.objectives.forEach { obj ->
                                    Text(
                                        text = "• $obj",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    LinearProgressIndicator(
                                        progress = { project.progressPercent / 100f },
                                        color = SimoniumTeal,
                                        modifier = Modifier.weight(1f).height(6.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${project.progressPercent}%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SimoniumTeal
                                    )
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Research Questions
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Formulated Research Questions",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = onOpenResearchQuestionDialog,
                                colors = ButtonDefaults.buttonColors(containerColor = SimoniumTeal)
                            ) {
                                Text("+ Propose Question", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }

                    items(researchQuestions) { q ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFD97706).copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = q.discipline,
                                            color = Color(0xFFD97706),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = "${q.linkedObservationsCount} linked observations",
                                        fontSize = 11.sp,
                                        color = SimoniumTeal,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = q.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = q.description,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Proposed by: ${q.proposedBy} (${q.authorRole.displayName})",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                3 -> {
                    // Verified Findings
                    val verifiedItems = submissions.filter { it.status == LifecycleStatus.VERIFIED }
                    item {
                        Text(
                            text = "Verified Scientific Findings Repository (${verifiedItems.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Discoveries confirmed through independent scientific verification and empirical replication:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    items(verifiedItems) { item ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = StatusVerified.copy(alpha = 0.08f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectSubmission(item) }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = null,
                                            tint = StatusVerified,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Verified Discovery",
                                            color = StatusVerified,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                    LifecycleBadge(status = item.status)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = item.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.description,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                                if (item.verificationNotes != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Verification Note: ${item.verificationNotes}",
                                        fontSize = 11.sp,
                                        color = StatusVerified,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WorkflowStepItem(
    stepNum: String,
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(vertical = 6.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = color.copy(alpha = 0.2f),
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = stepNum, fontWeight = FontWeight.Black, color = color, fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
            Text(text = desc, fontSize = 11.sp, color = Color(0xFFCBD5E1), lineHeight = 15.sp)
        }
    }
}

@Composable
fun PhaseCard(
    type: EvidenceType,
    title: String,
    description: String
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(type.badgeColorHex).copy(alpha = 0.08f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(type.badgeColorHex)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 15.sp
            )
        }
    }
}
