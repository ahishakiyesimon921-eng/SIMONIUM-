package com.example.simonium.data.model

enum class LifecycleStatus(val label: String) {
    DRAFT("Draft"),
    SUBMITTED("Submitted"),
    UNDER_REVIEW("Under Review"),
    CORRECTION_REQUESTED("Correction Requested"),
    APPROVED("Approved"),
    VERIFIED("Verified"),
    REJECTED("Rejected"),
    ARCHIVED("Archived")
}

enum class EvidenceType(val label: String, val badgeColorHex: Long) {
    RAW_OBSERVATION("Raw Observation", 0xFF0284C7),
    HYPOTHESIS("Scientific Hypothesis", 0xFFD97706),
    REVIEWED_EVIDENCE("Reviewed Evidence", 0xFF7C3AED),
    VERIFIED_FINDING("Verified Scientific Finding", 0xFF10B981)
}

enum class SyncState {
    SYNCED,
    PENDING_SYNC,
    LOCAL_DRAFT
}

data class ReviewComment(
    val id: String,
    val submissionId: String,
    val authorId: String,
    val authorName: String,
    val authorRole: UserRole,
    val comment: String,
    val timestamp: Long,
    val isCorrectionRequest: Boolean = false
)

data class AuditLogEntry(
    val id: String,
    val entityId: String,
    val entityType: String, // "Submission", "UserRole", "ResearchProject"
    val actorId: String,
    val actorName: String,
    val actorRole: UserRole,
    val action: String, // "CREATED", "UPDATED", "SUBMITTED", "REVIEWED", "CORRECTION_REQUESTED", "VERIFIED", "REJECTED"
    val details: String,
    val timestamp: Long
)

data class CrossModuleConnection(
    val id: String,
    val sourceSubmissionId: String,
    val targetModule: ModuleType,
    val connectionType: String, // "CLASSROOM_INQUIRY_TASK", "RESEARCH_QUESTION", "SURVEILLANCE_ALERT"
    val targetEntityId: String,
    val targetTitle: String,
    val initiatedBy: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class SubmissionItem(
    val id: String,
    val title: String,
    val description: String,
    val module: ModuleType,
    val evidenceType: EvidenceType,
    val status: LifecycleStatus,
    val authorId: String,
    val authorName: String,
    val authorRole: UserRole,
    val authorInstitution: String? = null,
    val location: String? = null,
    val tags: List<String> = emptyList(),
    val sourceReferences: List<String> = emptyList(),
    val attachments: List<String> = emptyList(),
    val isConfidential: Boolean = false,
    val reviewerId: String? = null,
    val reviewerName: String? = null,
    val verificationNotes: String? = null,
    val syncState: SyncState = SyncState.SYNCED,
    val linkedSourceId: String? = null,
    val linkedSourceTitle: String? = null,
    val linkedSourceModule: ModuleType? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

