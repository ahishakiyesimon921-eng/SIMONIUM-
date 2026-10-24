package com.example.simonium.data.model

data class ResearchQuestion(
    val id: String,
    val title: String,
    val description: String,
    val discipline: String,
    val proposedBy: String,
    val authorRole: UserRole,
    val linkedObservationsCount: Int = 0,
    val status: String = "ACTIVE", // ACTIVE, RESOLVED, IN_REVIEW
    val linkedSourceId: String? = null,
    val linkedSourceTitle: String? = null,
    val linkedSourceModule: ModuleType? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class ResearchProject(
    val id: String,
    val title: String,
    val leadResearcher: String,
    val leadInstitution: String,
    val field: String,
    val objectives: List<String>,
    val expectedOutcomes: String,
    val methodology: String,
    val progressPercent: Int,
    val status: String, // "PROPOSED", "IN_PROGRESS", "PEER_REVIEW", "COMPLETED"
    val linkedQuestions: List<String> = emptyList(),
    val verifiedFindings: List<String> = emptyList(),
    val citations: List<String> = emptyList(),
    val isDeIdentified: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class VerifiedFinding(
    val id: String,
    val projectId: String,
    val title: String,
    val summary: String,
    val primaryEvidenceId: String,
    val verifiedBy: String,
    val verifiedAt: Long,
    val peerReviewConfidence: String = "High",
    val replicationStatus: String = "Reproducible"
)
