package com.example.simonium.data.model

data class EducationalTask(
    val id: String,
    val title: String,
    val subject: String,
    val teacherName: String,
    val teacherInstitution: String,
    val description: String,
    val researchConnection: String, // E.g., "Connects to Project: Water Sanitation in Northern Province"
    val learningObjectives: List<String>,
    val practicalActivityPrompt: String,
    val dueDate: String,
    val submissionsCount: Int = 0,
    val status: String = "ACTIVE",
    val linkedSourceId: String? = null,
    val linkedSourceTitle: String? = null,
    val linkedSourceModule: ModuleType? = null
)

data class StudentSubmissionRecord(
    val id: String,
    val taskId: String,
    val studentId: String,
    val studentName: String,
    val content: String,
    val fieldworkObservations: String,
    val status: LifecycleStatus, // SUBMITTED, UNDER_REVIEW, CORRECTION_REQUESTED, APPROVED
    val teacherFeedback: String? = null,
    val grade: String? = null,
    val submittedAt: Long = System.currentTimeMillis()
)
