package com.example.simonium.data.model

data class VerificationDocument(
    val id: String,
    val userId: String,
    val documentType: String, // e.g., "Student ID Card", "Teacher Staff ID", "Medical Practice Licence", "Institutional Authorization Letter"
    val fileName: String,
    val fileSizeKb: Int,
    val issuingAuthority: String,
    val documentNumber: String,
    val expiryDate: String? = null,
    val uploadTimestamp: Long = System.currentTimeMillis(),
    val isVerified: Boolean = false,
    val reviewerNotes: String? = null
)

data class VerificationApplication(
    val id: String,
    val userId: String,
    val userFullName: String,
    val userEmail: String,
    val requestedRole: UserRole,
    val requestedModules: List<ModuleType>,
    val status: VerificationApplicationStatus,
    val institutionName: String,
    val roleSpecificDetails: Map<String, String>, // e.g. "studentNumber" -> "STU-2026-904"
    val documents: List<VerificationDocument>,
    val reviewerId: String? = null,
    val reviewerName: String? = null,
    val reviewerComments: String? = null,
    val submittedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class VerificationChallenge(
    val code: String,
    val targetContact: String,
    val contactType: String, // "EMAIL" or "PHONE"
    val expiresAt: Long,
    val failedAttempts: Int = 0,
    val isUsed: Boolean = false,
    val resendCooldownUntil: Long = 0
)

data class ModuleAccessRequirement(
    val module: ModuleType,
    val isGranted: Boolean,
    val requiredRole: List<UserRole>,
    val requirementDescription: String,
    val verificationStatus: VerificationApplicationStatus
)
