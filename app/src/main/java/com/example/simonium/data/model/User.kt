package com.example.simonium.data.model

enum class UserRole(val displayName: String, val description: String, val isPrivileged: Boolean) {
    COMMUNITY_MEMBER("Community Member", "Contribute local observations, concerns, and suggestions", false),
    STUDENT("Student", "Access learning resources, participate in fieldwork, submit research tasks", false),
    TEACHER("Teacher / Educator", "Design assignments, guide fieldwork, review and evaluate student submissions", true),
    RESEARCHER("Scientist / Researcher", "Initiate research projects, formulate hypotheses, analyze evidence, verify findings", true),
    DISCOVERER("Discoverer / Innovator", "Explore interdisciplinary links, propose breakthrough questions and solutions", false),
    HEALTHCARE_PRO("Doctor / Health Professional", "Submit surveillance reports, verify medical research questions, review clinical observations", true),
    INSTITUTION_REP("Institution Representative", "Coordinate organizational participation, grant approvals, oversee departmental projects", true),
    ADMINISTRATOR("Platform Administrator", "System governance, manage user roles, audit logs, and module configurations", true)
}

enum class RoleApprovalStatus {
    ACTIVE,
    PENDING_APPROVAL,
    REJECTED,
    SUSPENDED
}

enum class VerificationApplicationStatus(val label: String) {
    DRAFT("Draft"),
    SUBMITTED("Submitted"),
    VERIFICATION_PENDING("Verification Pending"),
    ADDITIONAL_INFO_REQUIRED("Additional Information Required"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    SUSPENDED("Suspended")
}

data class Institution(
    val id: String,
    val name: String,
    val type: String, // University, Hospital, Research Institute, Ministry, Community Org
    val location: String,
    val isVerified: Boolean = true
)

data class UserProfile(
    val id: String,
    val fullName: String,
    val email: String,
    val role: UserRole,
    val approvalStatus: RoleApprovalStatus = RoleApprovalStatus.ACTIVE,
    val institutionId: String? = null,
    val institutionName: String? = null,
    val specialty: String? = null, // e.g., "Epidemiology", "Biology Education", "Environmental Ecology"
    val avatarUrl: String? = null,
    val preferredLanguage: String = "EN", // "EN" or "RW" (Kinyarwanda)
    val verifiedContributionsCount: Int = 0,
    val token: String = "sim_token_auth_valid",
    val phone: String = "+250 788 123 456",
    val isEmailVerified: Boolean = true,
    val isPhoneVerified: Boolean = true,
    val identityVerificationStatus: VerificationApplicationStatus = VerificationApplicationStatus.APPROVED
)
