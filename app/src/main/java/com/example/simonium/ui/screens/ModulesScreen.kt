package com.example.simonium.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.simonium.data.model.LifecycleStatus
import com.example.simonium.data.model.ModuleType
import com.example.simonium.data.model.UserRole
import com.example.simonium.ui.components.LifecycleBadge
import com.example.ui.theme.*
import com.example.simonium.ui.viewmodel.SimoniumViewModel

@Composable
fun ModulesScreen(
    viewModel: SimoniumViewModel,
    onOpenSurveillanceDialog: () -> Unit,
    onOpenEducationalTaskDialog: () -> Unit,
    onOpenResearchQuestionDialog: () -> Unit,
    onOpenCommunityDialog: () -> Unit,
    onOpenNewSubmission: (ModuleType) -> Unit,
    onViewRequirements: ((ModuleType) -> Unit)? = null
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val submissions by viewModel.submissions.collectAsState()
    val surveillanceReports by viewModel.surveillanceReports.collectAsState()
    val educationalTasks by viewModel.educationalTasks.collectAsState()
    val researchProjects by viewModel.researchProjects.collectAsState()
    val communityObservations by viewModel.communityObservations.collectAsState()

    var selectedModule by remember { mutableStateOf(ModuleType.HEALTH) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("modules_screen")
    ) {
        // Module Selector Tabs
        ScrollableTabRow(
            selectedTabIndex = ModuleType.values().indexOf(selectedModule).coerceAtLeast(0),
            containerColor = SimoniumNavyDark,
            contentColor = SimoniumCyanLight,
            edgePadding = 12.dp
        ) {
            ModuleType.values().forEach { module ->
                Tab(
                    selected = selectedModule == module,
                    onClick = { selectedModule = module },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = module.title,
                                fontWeight = if (selectedModule == module) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                            if (!module.isEnabled) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF64748B)
                                ) {
                                    Text(
                                        text = "Future",
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Module Header Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (selectedModule) {
                            ModuleType.HEALTH -> ModuleHealth.copy(alpha = 0.1f)
                            ModuleType.EDUCATION -> ModuleEducation.copy(alpha = 0.1f)
                            ModuleType.RESEARCH -> ModuleResearch.copy(alpha = 0.1f)
                            ModuleType.COMMUNITY -> ModuleCommunity.copy(alpha = 0.1f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = selectedModule.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (selectedModule.isEnabled) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SimoniumTeal
                                ) {
                                    Text(
                                        text = "Active Module",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedModule.subtitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SimoniumTeal
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = selectedModule.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 17.sp
                        )

                        if (selectedModule.isEnabled) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onOpenNewSubmission(selectedModule) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SimoniumTeal),
                                    modifier = Modifier.testTag("module_add_submission_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Text(" Submit to Module", fontSize = 11.sp, color = Color.White)
                                }

                                when (selectedModule) {
                                    ModuleType.HEALTH -> {
                                        OutlinedButton(onClick = onOpenSurveillanceDialog) {
                                            Text("File Surveillance", fontSize = 11.sp)
                                        }
                                    }
                                    ModuleType.EDUCATION -> {
                                        OutlinedButton(onClick = onOpenEducationalTaskDialog) {
                                            Text("Assign Task", fontSize = 11.sp)
                                        }
                                    }
                                    ModuleType.RESEARCH -> {
                                        OutlinedButton(onClick = onOpenResearchQuestionDialog) {
                                            Text("Propose Question", fontSize = 11.sp)
                                        }
                                    }
                                    ModuleType.COMMUNITY -> {
                                        OutlinedButton(onClick = onOpenCommunityDialog) {
                                            Text("Local Observation", fontSize = 11.sp)
                                        }
                                    }
                                    else -> {}
                                }

                                OutlinedButton(
                                    onClick = { onViewRequirements?.invoke(selectedModule) },
                                    modifier = Modifier.testTag("view_module_requirements_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.Security, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Access Rules", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Health Specific: Strict Patient Confidentiality Banner
            if (selectedModule == ModuleType.HEALTH) {
                item {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0284C7).copy(alpha = 0.12f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Patient Confidentiality & Medical Safety Notice",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0284C7)
                                )
                                Text(
                                    text = "Patient records are completely de-identified. AI suggestions are research hypotheses and never replace certified medical practitioner diagnoses.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Disease Surveillance Reports",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(surveillanceReports) { report ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = report.conditionName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (report.severityLevel == "ELEVATED") Color(0xFFEA580C).copy(alpha = 0.15f)
                                    else Color(0xFF0284C7).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${report.caseCount} Cases • ${report.severityLevel}",
                                        color = if (report.severityLevel == "ELEVATED") Color(0xFFEA580C) else Color(0xFF0284C7),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${report.reportingFacility} (${report.region})",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = report.clinicalNotes,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Education Specific: Educational Tasks & Practical Prompts
            if (selectedModule == ModuleType.EDUCATION) {
                item {
                    Text(
                        text = "Active Inquiry Tasks & Fieldwork",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(educationalTasks) { task ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = task.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF7C3AED).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = task.subject,
                                        color = Color(0xFF7C3AED),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Teacher: ${task.teacherName} • ${task.teacherInstitution}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = task.description,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = SimoniumTeal.copy(alpha = 0.08f)
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = SimoniumTeal, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = task.researchConnection,
                                        fontSize = 11.sp,
                                        color = SimoniumTeal,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Research Specific: Active Projects & Verified Findings
            if (selectedModule == ModuleType.RESEARCH) {
                item {
                    Text(
                        text = "Current Research Projects",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(researchProjects) { project ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = project.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Lead: ${project.leadResearcher} • ${project.leadInstitution}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = project.expectedOutcomes,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                LinearProgressIndicator(
                                    progress = { project.progressPercent / 100f },
                                    modifier = Modifier.weight(1f).height(6.dp),
                                    color = SimoniumTeal
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "${project.progressPercent}% (${project.status})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SimoniumTeal
                                )
                            }
                        }
                    }
                }
            }

            // Community Specific: Citizen Science Reports
            if (selectedModule == ModuleType.COMMUNITY) {
                item {
                    Text(
                        text = "Local Community Observations & Alerts",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(communityObservations) { obs ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = obs.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF16A34A).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = obs.category,
                                        color = Color(0xFF16A34A),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "By ${obs.submittedBy} • ${obs.communityName} (${obs.district})",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = obs.description,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                            if (obs.expertResponse != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Expert Response: ${obs.expertResponse}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = SimoniumTeal,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // General Module Submissions
            val moduleSubmissions = submissions.filter { it.module == selectedModule }
            if (moduleSubmissions.isNotEmpty()) {
                item {
                    Text(
                        text = "Submissions in ${selectedModule.title} (${moduleSubmissions.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(moduleSubmissions) { item ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectSubmission(item) }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = item.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                LifecycleBadge(status = item.status)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.description,
                                fontSize = 11.sp,
                                maxLines = 2,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
