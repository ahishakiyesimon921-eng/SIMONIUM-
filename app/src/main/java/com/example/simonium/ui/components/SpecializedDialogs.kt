package com.example.simonium.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.SimoniumTeal

@Composable
fun NewSurveillanceDialog(
    onDismiss: () -> Unit,
    onSubmit: (
        conditionName: String,
        syndromicCategory: String,
        region: String,
        caseCount: Int,
        severityLevel: String,
        clinicalNotes: String
    ) -> Unit
) {
    var conditionName by remember { mutableStateOf("") }
    var syndromicCategory by remember { mutableStateOf("Waterborne / Enteric") }
    var region by remember { mutableStateOf("Musanze District") }
    var caseCountStr by remember { mutableStateOf("12") }
    var severityLevel by remember { mutableStateOf("MODERATE") }
    var clinicalNotes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.padding(6.dp)
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
                        Icon(imageVector = Icons.Default.LocalHospital, contentDescription = null, tint = Color(0xFF0284C7))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Disease Surveillance Report", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0284C7).copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Confidentiality Protected: Do NOT enter patient names, exact birthdates, or contact details. All health surveillance reports must remain de-identified.",
                        fontSize = 11.sp,
                        color = Color(0xFF0284C7),
                        modifier = Modifier.padding(8.dp)
                    )
                }

                OutlinedTextField(
                    value = conditionName,
                    onValueChange = { conditionName = it },
                    label = { Text("Condition / Syndrome Name *") },
                    placeholder = { Text("e.g., Acute Watery Diarrhea Cluster") },
                    modifier = Modifier.fillMaxWidth().testTag("surveillance_condition_input")
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = syndromicCategory,
                    onValueChange = { syndromicCategory = it },
                    label = { Text("Syndromic Category") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = region,
                        onValueChange = { region = it },
                        label = { Text("District / Region") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = caseCountStr,
                        onValueChange = { caseCountStr = it },
                        label = { Text("Case Count") },
                        modifier = Modifier.weight(0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = clinicalNotes,
                    onValueChange = { clinicalNotes = it },
                    label = { Text("Clinical Presentation & Epidemiology *") },
                    placeholder = { Text("Presenting symptoms, duration, age grouping, suspect water/food source...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        val count = caseCountStr.toIntOrNull() ?: 1
                        if (conditionName.isNotBlank() && clinicalNotes.isNotBlank()) {
                            onSubmit(conditionName, syndromicCategory, region, count, severityLevel, clinicalNotes)
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    modifier = Modifier.fillMaxWidth().testTag("submit_surveillance_button")
                ) {
                    Text("Submit De-Identified Report", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun NewEducationalTaskDialog(
    onDismiss: () -> Unit,
    onSubmit: (
        title: String,
        subject: String,
        description: String,
        researchConnection: String,
        learningObjectives: List<String>,
        practicalPrompt: String,
        dueDate: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Environmental Biology") }
    var description by remember { mutableStateOf("") }
    var researchConnection by remember { mutableStateOf("Connects to Water Sanitation Project #proj-02") }
    var objectivesText by remember { mutableStateOf("Master standardized water sampling\nFormat observations for scientific peer review") }
    var practicalPrompt by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("Nov 30, 2026") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.padding(6.dp)
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
                        Icon(imageVector = Icons.Default.School, contentDescription = null, tint = Color(0xFF7C3AED))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create Research Learning Task", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    placeholder = { Text("e.g., Stream Turbidity & Bio-Indicator Assay") },
                    modifier = Modifier.fillMaxWidth().testTag("educational_task_title_input")
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Due Date") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = researchConnection,
                    onValueChange = { researchConnection = it },
                    label = { Text("Real-World Research Connection") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = practicalPrompt,
                    onValueChange = { practicalPrompt = it },
                    label = { Text("Practical Activity & Fieldwork Prompt *") },
                    placeholder = { Text("What practical steps or samples must students collect?") },
                    modifier = Modifier.fillMaxWidth().height(90.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Task Background & Instructions") },
                    modifier = Modifier.fillMaxWidth().height(80.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        if (title.isNotBlank() && practicalPrompt.isNotBlank()) {
                            onSubmit(
                                title,
                                subject,
                                description.ifBlank { "Educational inquiry task" },
                                researchConnection,
                                objectivesText.lines().filter { it.isNotBlank() },
                                practicalPrompt,
                                dueDate
                            )
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                    modifier = Modifier.fillMaxWidth().testTag("submit_educational_task_button")
                ) {
                    Text("Publish Educational Task", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun NewResearchQuestionDialog(
    onDismiss: () -> Unit,
    onSubmit: (title: String, description: String, discipline: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var discipline by remember { mutableStateOf("Medical Entomology & Ecology") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.padding(6.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.QuestionAnswer, contentDescription = null, tint = SimoniumTeal)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Formulate Research Question", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Research Question *") },
                    placeholder = { Text("What environmental or biological problem are you investigating?") },
                    modifier = Modifier.fillMaxWidth().testTag("research_question_title_input")
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = discipline,
                    onValueChange = { discipline = it },
                    label = { Text("Discipline / Field") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Context & Scientific Justification *") },
                    placeholder = { Text("Explain known data and hypothesis justification...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        if (title.isNotBlank() && description.isNotBlank()) {
                            onSubmit(title, description, discipline)
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SimoniumTeal),
                    modifier = Modifier.fillMaxWidth().testTag("submit_research_question_button")
                ) {
                    Text("Propose Research Question", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun NewCommunityObservationDialog(
    onDismiss: () -> Unit,
    onSubmit: (title: String, description: String, category: String, communityName: String, district: String, isPrivate: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Water & Sanitation") }
    var communityName by remember { mutableStateOf("Kinigi Cooperative") }
    var district by remember { mutableStateOf("Musanze") }
    var isPrivate by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.padding(6.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.People, contentDescription = null, tint = Color(0xFF16A34A))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Record Community Observation", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Observation Title *") },
                    placeholder = { Text("e.g., Abnormal silt in irrigation canal") },
                    modifier = Modifier.fillMaxWidth().testTag("community_observation_title_input")
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = district,
                        onValueChange = { district = it },
                        label = { Text("District") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = communityName,
                    onValueChange = { communityName = it },
                    label = { Text("Community / Village Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Details & Observation *") },
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        if (title.isNotBlank() && description.isNotBlank()) {
                            onSubmit(title, description, category, communityName, district, isPrivate)
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    modifier = Modifier.fillMaxWidth().testTag("submit_community_observation_button")
                ) {
                    Text("Submit Observation", color = Color.White)
                }
            }
        }
    }
}
