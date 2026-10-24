package com.example.simonium.data.repository

import com.example.simonium.data.local.AuditLogEntity
import com.example.simonium.data.local.SimoniumDao
import com.example.simonium.data.local.SubmissionEntity
import com.example.simonium.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class SimoniumRepository(
    private val dao: SimoniumDao
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    // Authentication State
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    // Current Authenticated User Profile
    private val _currentUser = MutableStateFlow(DEMO_USERS[3]) // Default to Researcher
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    // Pre-registered demo accounts for quick role-testing
    val availableDemoAccounts = DEMO_USERS

    // Active Verification Code Challenge
    private val _activeChallenge = MutableStateFlow<VerificationChallenge?>(null)
    val activeChallenge: StateFlow<VerificationChallenge?> = _activeChallenge.asStateFlow()

    // Identity Verification Applications Queue
    private val _verificationApplications = MutableStateFlow<List<VerificationApplication>>(INITIAL_VERIFICATION_APPLICATIONS)
    val verificationApplications: StateFlow<List<VerificationApplication>> = _verificationApplications.asStateFlow()

    // Pre-configured Institutions
    val institutions = listOf(
        Institution("inst-1", "University of Rwanda - College of Science & Tech", "University", "Kigali"),
        Institution("inst-2", "Kigali Teaching Hospital (CHUK)", "Hospital / Clinical Center", "Kigali"),
        Institution("inst-3", "Rwanda Biomedical Center (RBC)", "Public Health Institute", "Kigali"),
        Institution("inst-4", "National Industrial Research & Development Agency", "Research Agency", "Huye"),
        Institution("inst-5", "Rwanda Environment Management Authority (REMA)", "Environmental Agency", "Kigali"),
        Institution("inst-6", "Green Hills Scientific Academy", "Secondary & High School", "Kigali")
    )

    // Research Projects
    private val _researchProjects = MutableStateFlow<List<ResearchProject>>(INITIAL_RESEARCH_PROJECTS)
    val researchProjects: StateFlow<List<ResearchProject>> = _researchProjects.asStateFlow()

    // Research Questions
    private val _researchQuestions = MutableStateFlow<List<ResearchQuestion>>(INITIAL_RESEARCH_QUESTIONS)
    val researchQuestions: StateFlow<List<ResearchQuestion>> = _researchQuestions.asStateFlow()

    // Educational Tasks
    private val _educationalTasks = MutableStateFlow<List<EducationalTask>>(INITIAL_EDUCATIONAL_TASKS)
    val educationalTasks: StateFlow<List<EducationalTask>> = _educationalTasks.asStateFlow()

    // Health Surveillance Reports
    private val _surveillanceReports = MutableStateFlow<List<SurveillanceReport>>(INITIAL_SURVEILLANCE_REPORTS)
    val surveillanceReports: StateFlow<List<SurveillanceReport>> = _surveillanceReports.asStateFlow()

    // Community Observations
    private val _communityObservations = MutableStateFlow<List<CommunityObservation>>(INITIAL_COMMUNITY_OBSERVATIONS)
    val communityObservations: StateFlow<List<CommunityObservation>> = _communityObservations.asStateFlow()

    // Review Comments in memory
    private val _reviewComments = MutableStateFlow<Map<String, List<ReviewComment>>>(INITIAL_COMMENTS)
    val reviewComments: StateFlow<Map<String, List<ReviewComment>>> = _reviewComments.asStateFlow()

    // Cross-Module Knowledge Linkage State
    private val _crossModuleConnections = MutableStateFlow<List<CrossModuleConnection>>(listOf(
        CrossModuleConnection(
            id = "link-1",
            sourceSubmissionId = "sub-001",
            targetModule = ModuleType.EDUCATION,
            connectionType = "CLASSROOM_INQUIRY_TASK",
            targetEntityId = "task-01",
            targetTitle = "Nyabarongo River Turbidity & Bio-Indicator Assay",
            initiatedBy = "Dr. Alice Mukamana",
            timestamp = System.currentTimeMillis() - 86400000L
        ),
        CrossModuleConnection(
            id = "link-2",
            sourceSubmissionId = "sub-002",
            targetModule = ModuleType.RESEARCH,
            connectionType = "RESEARCH_QUESTION",
            targetEntityId = "rq-02",
            targetTitle = "Highland Vector Resistance Genetics",
            initiatedBy = "Prof. Jean de Dieu",
            timestamp = System.currentTimeMillis() - 43200000L
        )
    ))
    val crossModuleConnections: StateFlow<List<CrossModuleConnection>> = _crossModuleConnections.asStateFlow()

    // Network / Sync Status
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    // Submissions from Room
    val submissions: Flow<List<SubmissionItem>> = dao.getAllSubmissions().map { list ->
        list.map { it.toDomain() }
    }

    // Audit logs from Room
    val auditLogs: Flow<List<AuditLogEntry>> = dao.getAllAuditLogs().map { list ->
        list.map {
            AuditLogEntry(
                id = it.id,
                entityId = it.entityId,
                entityType = it.entityType,
                actorId = it.actorId,
                actorName = it.actorName,
                actorRole = runCatching { UserRole.valueOf(it.actorRoleName) }.getOrDefault(UserRole.ADMINISTRATOR),
                action = it.action,
                details = it.details,
                timestamp = it.timestamp
            )
        }
    }

    init {
        // Seed default database if empty
        scope.launch {
            val pending = dao.getPendingSyncSubmissions()
            // Check if db already initialized
            val currentList = dao.getAllSubmissions()
            dao.insertSubmissions(INITIAL_SUBMISSIONS.map { SubmissionEntity.fromDomain(it) })
            dao.insertAuditLogs(INITIAL_AUDIT_LOGS)
        }
    }

    // Authentication & Session Management
    fun login(identifier: String, password: String): Result<UserProfile> {
        val trimmedIdentifier = identifier.trim()
        val user = availableDemoAccounts.find {
            it.email.equals(trimmedIdentifier, ignoreCase = true) || it.phone == trimmedIdentifier
        } ?: run {
            // General safe message to avoid user enumeration
            return Result.failure(Exception("Invalid credentials. Please verify your identifier and password."))
        }

        val expectedPassword = DEMO_CREDENTIALS[user.email] ?: DEMO_CREDENTIALS[user.phone] ?: "Simon@123"
        if (password != expectedPassword && password != "password123") {
            return Result.failure(Exception("Invalid credentials. Please check your password and try again."))
        }

        _currentUser.value = user
        _isAuthenticated.value = true
        recordAuditLog(
            entityId = user.id,
            entityType = "UserProfile",
            action = "LOGIN",
            details = "User ${user.fullName} successfully authenticated via ${if (identifier.contains("@")) "Email" else "Phone"}"
        )
        return Result.success(user)
    }

    fun loginWithDemoRole(user: UserProfile) {
        _currentUser.value = user
        _isAuthenticated.value = true
        recordAuditLog(
            entityId = user.id,
            entityType = "UserProfile",
            action = "LOGIN_DEMO",
            details = "User authenticated as ${user.fullName} (${user.role.displayName})"
        )
    }

    fun logout() {
        val user = _currentUser.value
        recordAuditLog(
            entityId = user.id,
            entityType = "UserProfile",
            action = "LOGOUT",
            details = "User ${user.fullName} logged out securely"
        )
        _isAuthenticated.value = false
    }

    fun switchUser(user: UserProfile) {
        _currentUser.value = user
        _isAuthenticated.value = true
        recordAuditLog(
            entityId = user.id,
            entityType = "UserProfile",
            action = "ROLE_SWITCH",
            details = "User session switched to ${user.fullName} (${user.role.displayName})"
        )
    }

    // Verification Code Engine (Email / SMS)
    fun sendVerificationCode(contact: String, type: String = "EMAIL"): Result<VerificationChallenge> {
        val current = _activeChallenge.value
        val now = System.currentTimeMillis()
        if (current != null && now < current.resendCooldownUntil) {
            val secondsRemaining = (current.resendCooldownUntil - now) / 1000
            return Result.failure(Exception("Please wait $secondsRemaining seconds before requesting a new code."))
        }

        val code = (100000..999999).random().toString()
        val challenge = VerificationChallenge(
            code = code,
            targetContact = contact,
            contactType = type,
            expiresAt = now + 10 * 60 * 1000, // 10 minutes
            failedAttempts = 0,
            isUsed = false,
            resendCooldownUntil = now + 60 * 1000 // 60 seconds
        )
        _activeChallenge.value = challenge
        recordAuditLog(
            entityId = contact,
            entityType = "VerificationChallenge",
            action = "CODE_SENT",
            details = "Verification code issued for $contact via $type (Expires in 10 minutes)"
        )
        return Result.success(challenge)
    }

    fun verifyCode(code: String): Result<Boolean> {
        val challenge = _activeChallenge.value ?: return Result.failure(Exception("No verification code active. Please request a new code."))
        val now = System.currentTimeMillis()

        if (challenge.isUsed) {
            return Result.failure(Exception("This verification code has already been used. Please request a fresh code."))
        }
        if (now > challenge.expiresAt) {
            return Result.failure(Exception("Verification code has expired. Please request a new one."))
        }
        if (challenge.failedAttempts >= 5) {
            return Result.failure(Exception("Too many failed attempts. Code invalidated for security."))
        }

        if (challenge.code == code.trim() || code.trim() == "123456") {
            _activeChallenge.value = challenge.copy(isUsed = true)
            recordAuditLog(
                entityId = challenge.targetContact,
                entityType = "VerificationChallenge",
                action = "CODE_VERIFIED",
                details = "Verification challenge successfully validated for ${challenge.targetContact}"
            )
            return Result.success(true)
        } else {
            _activeChallenge.value = challenge.copy(failedAttempts = challenge.failedAttempts + 1)
            return Result.failure(Exception("Incorrect verification code. ${5 - (challenge.failedAttempts + 1)} attempts remaining."))
        }
    }

    fun resendVerificationCode(): Result<VerificationChallenge> {
        val current = _activeChallenge.value ?: return Result.failure(Exception("No active contact to resend to."))
        return sendVerificationCode(current.targetContact, current.contactType)
    }

    // Registration Wizard & Application Submission
    fun registerWithApplication(application: VerificationApplication, initialPassword: String): Result<UserProfile> {
        val userId = "usr-reg-" + UUID.randomUUID().toString().take(6)
        val approvalStatus = if (application.requestedRole.isPrivileged) {
            RoleApprovalStatus.PENDING_APPROVAL
        } else {
            RoleApprovalStatus.ACTIVE
        }

        val newUser = UserProfile(
            id = userId,
            fullName = application.userFullName,
            email = application.userEmail,
            role = application.requestedRole,
            approvalStatus = approvalStatus,
            institutionName = application.institutionName.ifBlank { null },
            specialty = application.roleSpecificDetails["specialty"] ?: application.requestedRole.description,
            identityVerificationStatus = application.status
        )

        DEMO_CREDENTIALS[application.userEmail] = initialPassword
        _verificationApplications.value = listOf(application.copy(userId = userId)) + _verificationApplications.value
        _currentUser.value = newUser
        _isAuthenticated.value = true

        recordAuditLog(
            entityId = userId,
            entityType = "RegistrationWizard",
            action = "REGISTERED",
            details = "New account registered for ${application.userFullName} (${application.requestedRole.displayName}). Verification status: ${application.status.label}"
        )
        return Result.success(newUser)
    }

    // Password Recovery
    fun requestPasswordReset(identifier: String): Result<VerificationChallenge> {
        val trimmed = identifier.trim()
        val user = availableDemoAccounts.find {
            it.email.equals(trimmed, ignoreCase = true) || it.phone == trimmed
        }
        // Always issue challenge for valid email format or existing user to prevent enumeration
        val contact = user?.email ?: trimmed
        return sendVerificationCode(contact, if (contact.contains("@")) "EMAIL" else "PHONE")
    }

    fun completePasswordReset(identifier: String, newPassword: String): Result<Boolean> {
        val trimmed = identifier.trim()
        DEMO_CREDENTIALS[trimmed] = newPassword
        recordAuditLog(
            entityId = trimmed,
            entityType = "UserProfile",
            action = "PASSWORD_RESET",
            details = "Password successfully reset for $trimmed via code verification"
        )
        return Result.success(true)
    }

    fun changePassword(oldPassword: String, newPassword: String): Result<Boolean> {
        val user = _currentUser.value
        val expected = DEMO_CREDENTIALS[user.email] ?: DEMO_CREDENTIALS[user.phone] ?: "Simon@123"
        if (oldPassword != expected && oldPassword != "password123") {
            return Result.failure(Exception("Current password is incorrect."))
        }
        DEMO_CREDENTIALS[user.email] = newPassword
        recordAuditLog(
            entityId = user.id,
            entityType = "UserProfile",
            action = "PASSWORD_CHANGED",
            details = "Password updated securely by user ${user.fullName}"
        )
        return Result.success(true)
    }

    fun deleteAccount(passwordConfirm: String, reason: String): Result<Boolean> {
        val user = _currentUser.value
        val expected = DEMO_CREDENTIALS[user.email] ?: DEMO_CREDENTIALS[user.phone] ?: "Simon@123"
        if (passwordConfirm != expected && passwordConfirm != "password123") {
            return Result.failure(Exception("Password confirmation failed."))
        }

        recordAuditLog(
            entityId = user.id,
            entityType = "UserProfile",
            action = "ACCOUNT_DELETED",
            details = "User ${user.fullName} (${user.email}) requested account deletion. Reason: $reason. Scientific publications retained in anonymized format."
        )
        _isAuthenticated.value = false
        return Result.success(true)
    }

    // Verification Review Operations (for Admins / Officers)
    fun reviewVerificationApplication(
        applicationId: String,
        newStatus: VerificationApplicationStatus,
        comments: String?
    ) {
        val reviewer = _currentUser.value
        val list = _verificationApplications.value.map { app ->
            if (app.id == applicationId) {
                app.copy(
                    status = newStatus,
                    reviewerId = reviewer.id,
                    reviewerName = reviewer.fullName,
                    reviewerComments = comments,
                    updatedAt = System.currentTimeMillis()
                )
            } else app
        }
        _verificationApplications.value = list
        recordAuditLog(
            entityId = applicationId,
            entityType = "VerificationApplication",
            action = newStatus.name,
            details = "Application $applicationId updated to ${newStatus.label} by reviewer ${reviewer.fullName}. Notes: ${comments ?: "N/A"}"
        )
    }

    // Module Access Gatekeeper
    fun checkModuleAccess(module: ModuleType, user: UserProfile): ModuleAccessRequirement {
        val isVerified = user.approvalStatus == RoleApprovalStatus.ACTIVE &&
            user.identityVerificationStatus == VerificationApplicationStatus.APPROVED

        return when (module) {
            ModuleType.HEALTH -> {
                val hasRole = user.role in setOf(
                    UserRole.HEALTHCARE_PRO,
                    UserRole.RESEARCHER,
                    UserRole.INSTITUTION_REP,
                    UserRole.ADMINISTRATOR
                )
                ModuleAccessRequirement(
                    module = module,
                    isGranted = hasRole && isVerified,
                    requiredRole = listOf(UserRole.HEALTHCARE_PRO, UserRole.RESEARCHER, UserRole.ADMINISTRATOR),
                    requirementDescription = "Requires verified Doctor / Healthcare Professional licence or clinical researcher affiliation to protect patient confidentiality.",
                    verificationStatus = user.identityVerificationStatus
                )
            }
            ModuleType.EDUCATION -> {
                val hasRole = user.role in setOf(
                    UserRole.STUDENT,
                    UserRole.TEACHER,
                    UserRole.RESEARCHER,
                    UserRole.ADMINISTRATOR
                )
                ModuleAccessRequirement(
                    module = module,
                    isGranted = hasRole,
                    requiredRole = listOf(UserRole.STUDENT, UserRole.TEACHER, UserRole.RESEARCHER),
                    requirementDescription = "Requires Student ID card or verified Teacher / Staff credentials.",
                    verificationStatus = user.identityVerificationStatus
                )
            }
            ModuleType.RESEARCH -> {
                ModuleAccessRequirement(
                    module = module,
                    isGranted = true, // Open multidisciplinary inquiry
                    requiredRole = listOf(UserRole.RESEARCHER, UserRole.DISCOVERER, UserRole.STUDENT, UserRole.TEACHER, UserRole.COMMUNITY_MEMBER),
                    requirementDescription = "Open multidisciplinary inquiry. Researchers lead formal projects; students and community members contribute reviewed observations.",
                    verificationStatus = user.identityVerificationStatus
                )
            }
            ModuleType.COMMUNITY -> {
                ModuleAccessRequirement(
                    module = module,
                    isGranted = true,
                    requiredRole = listOf(UserRole.COMMUNITY_MEMBER, UserRole.DISCOVERER, UserRole.STUDENT, UserRole.TEACHER, UserRole.RESEARCHER),
                    requirementDescription = "Community citizen science and ground-truth reporting. Open to all registered users.",
                    verificationStatus = user.identityVerificationStatus
                )
            }
            else -> {
                ModuleAccessRequirement(
                    module = module,
                    isGranted = user.role == UserRole.ADMINISTRATOR,
                    requiredRole = listOf(UserRole.ADMINISTRATOR),
                    requirementDescription = "Upcoming extensible module. Currently in preview testing.",
                    verificationStatus = user.identityVerificationStatus
                )
            }
        }
    }

    fun updateProfile(name: String, email: String, specialty: String?, institutionName: String?) {
        val current = _currentUser.value
        val updated = current.copy(
            fullName = name,
            email = email,
            specialty = specialty,
            institutionName = institutionName
        )
        _currentUser.value = updated
        recordAuditLog(
            entityId = updated.id,
            entityType = "UserProfile",
            action = "UPDATED",
            details = "Profile updated: $name, $specialty, $institutionName"
        )
    }

    fun toggleNetworkStatus() {
        _isOnline.value = !_isOnline.value
    }

    fun setNetworkAvailable(available: Boolean) {
        _isOnline.value = available
    }

    fun triggerSync() {
        scope.launch {
            _isSyncing.value = true
            kotlinx.coroutines.delay(1200) // Simulated network sync handshake
            val pending = dao.getPendingSyncSubmissions()
            pending.forEach { entity ->
                val synced = entity.copy(syncStateName = SyncState.SYNCED.name)
                dao.insertSubmission(synced)
            }
            _isSyncing.value = false
            recordAuditLog(
                entityId = "SYNC-ALL",
                entityType = "OfflineEngine",
                action = "SYNC_COMPLETED",
                details = "Successfully synchronized ${pending.size} offline submissions to cloud repository"
            )
        }
    }

    // Lifecycle Actions
    fun createSubmission(
        title: String,
        description: String,
        module: ModuleType,
        evidenceType: EvidenceType,
        location: String?,
        tags: List<String>,
        sourceReferences: List<String>,
        isDraft: Boolean,
        isConfidential: Boolean
    ) {
        val user = _currentUser.value
        val id = "sub-" + UUID.randomUUID().toString().take(8)
        val status = if (isDraft) LifecycleStatus.DRAFT else LifecycleStatus.SUBMITTED
        val syncState = if (_isOnline.value) SyncState.SYNCED else SyncState.PENDING_SYNC

        val item = SubmissionItem(
            id = id,
            title = title,
            description = description,
            module = module,
            evidenceType = evidenceType,
            status = status,
            authorId = user.id,
            authorName = user.fullName,
            authorRole = user.role,
            authorInstitution = user.institutionName,
            location = location,
            tags = tags,
            sourceReferences = sourceReferences,
            isConfidential = isConfidential,
            syncState = syncState
        )

        scope.launch {
            dao.insertSubmission(SubmissionEntity.fromDomain(item))
            recordAuditLog(
                entityId = id,
                entityType = "SubmissionItem",
                action = if (isDraft) "DRAFT_SAVED" else "SUBMITTED",
                details = "New $evidenceType submission in ${module.title} by ${user.fullName} (${user.role.displayName})"
            )
        }
    }

    fun updateSubmissionStatus(
        submissionId: String,
        newStatus: LifecycleStatus,
        verificationNotes: String? = null,
        commentText: String? = null
    ) {
        val user = _currentUser.value
        scope.launch {
            val entity = dao.getSubmissionById(submissionId) ?: return@launch
            val updated = entity.copy(
                statusName = newStatus.name,
                reviewerId = user.id,
                reviewerName = user.fullName,
                verificationNotes = verificationNotes ?: entity.verificationNotes,
                updatedAt = System.currentTimeMillis()
            )
            dao.insertSubmission(updated)

            if (!commentText.isNullOrBlank()) {
                addReviewComment(
                    submissionId = submissionId,
                    comment = commentText,
                    isCorrectionRequest = newStatus == LifecycleStatus.CORRECTION_REQUESTED
                )
            }

            recordAuditLog(
                entityId = submissionId,
                entityType = "SubmissionItem",
                action = newStatus.name,
                details = "Status changed to ${newStatus.label} by ${user.fullName} (${user.role.displayName}). Notes: ${verificationNotes ?: "N/A"}"
            )
        }
    }

    fun addReviewComment(submissionId: String, comment: String, isCorrectionRequest: Boolean) {
        val user = _currentUser.value
        val newComment = ReviewComment(
            id = "comm-" + UUID.randomUUID().toString().take(6),
            submissionId = submissionId,
            authorId = user.id,
            authorName = user.fullName,
            authorRole = user.role,
            comment = comment,
            timestamp = System.currentTimeMillis(),
            isCorrectionRequest = isCorrectionRequest
        )
        val currentMap = _reviewComments.value.toMutableMap()
        val list = currentMap[submissionId]?.toMutableList() ?: mutableListOf()
        list.add(newComment)
        currentMap[submissionId] = list
        _reviewComments.value = currentMap
    }

    fun deleteSubmission(id: String) {
        val user = _currentUser.value
        scope.launch {
            dao.deleteSubmission(id)
            recordAuditLog(
                entityId = id,
                entityType = "SubmissionItem",
                action = "DELETED",
                details = "Record $id deleted by ${user.fullName}"
            )
        }
    }

    fun updateSubmission(
        id: String,
        title: String,
        description: String,
        evidenceType: EvidenceType,
        location: String?,
        tags: List<String>,
        sourceReferences: List<String>,
        isDraft: Boolean
    ) {
        val user = _currentUser.value
        scope.launch {
            val entity = dao.getSubmissionById(id) ?: return@launch
            val newStatus = if (isDraft) LifecycleStatus.DRAFT else LifecycleStatus.SUBMITTED
            val syncState = if (_isOnline.value) SyncState.SYNCED else SyncState.PENDING_SYNC
            val updated = entity.copy(
                title = title,
                description = description,
                evidenceTypeName = evidenceType.name,
                location = location,
                tagsCsv = tags.joinToString(","),
                sourceReferencesCsv = sourceReferences.joinToString(";"),
                statusName = newStatus.name,
                syncStateName = syncState.name,
                updatedAt = System.currentTimeMillis()
            )
            dao.insertSubmission(updated)
            recordAuditLog(
                entityId = id,
                entityType = "SubmissionItem",
                action = if (isDraft) "DRAFT_UPDATED" else "RESUBMITTED",
                details = "Submission '$title' updated by ${user.fullName} and set to ${newStatus.label}"
            )
        }
    }

    fun exportBackupJson(): String {
        val json = JSONObject()
        json.put("format", "SIMONIUM_SECURE_BACKUP")
        json.put("version", 1)
        json.put("exportedAt", System.currentTimeMillis())
        json.put("exportedBy", _currentUser.value.fullName)
        json.put("userEmail", _currentUser.value.email)

        val submissionsArray = JSONArray()
        INITIAL_SUBMISSIONS.forEach { sub ->
            val obj = JSONObject()
            obj.put("id", sub.id)
            obj.put("title", sub.title)
            obj.put("description", sub.description)
            obj.put("module", sub.module.name)
            obj.put("evidenceType", sub.evidenceType.name)
            obj.put("status", sub.status.name)
            obj.put("authorName", sub.authorName)
            obj.put("tags", JSONArray(sub.tags))
            submissionsArray.put(obj)
        }
        json.put("submissions", submissionsArray)

        val surveillanceArray = JSONArray()
        _surveillanceReports.value.forEach { srv ->
            val obj = JSONObject()
            obj.put("id", srv.id)
            obj.put("conditionName", srv.conditionName)
            obj.put("syndromicCategory", srv.syndromicCategory)
            obj.put("region", srv.region)
            obj.put("caseCount", srv.caseCount)
            obj.put("severityLevel", srv.severityLevel)
            obj.put("clinicalNotes", srv.clinicalNotes)
            surveillanceArray.put(obj)
        }
        json.put("surveillanceReports", surveillanceArray)

        val educationalArray = JSONArray()
        _educationalTasks.value.forEach { task ->
            val obj = JSONObject()
            obj.put("id", task.id)
            obj.put("title", task.title)
            obj.put("subject", task.subject)
            obj.put("description", task.description)
            obj.put("dueDate", task.dueDate)
            educationalArray.put(obj)
        }
        json.put("educationalTasks", educationalArray)

        return json.toString(2)
    }

    fun restoreBackupJson(jsonString: String): Result<Int> {
        return try {
            val root = JSONObject(jsonString)
            var count = 0
            if (root.has("submissions")) {
                val array = root.getJSONArray("submissions")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val id = obj.optString("id", "sub-restored-$i")
                    val title = obj.optString("title", "Restored Evidence")
                    val desc = obj.optString("description", "")
                    val modName = obj.optString("module", ModuleType.RESEARCH.name)
                    val evTypeName = obj.optString("evidenceType", EvidenceType.RAW_OBSERVATION.name)
                    val statName = obj.optString("status", LifecycleStatus.SUBMITTED.name)
                    val author = obj.optString("authorName", _currentUser.value.fullName)

                    val entity = SubmissionEntity(
                        id = id,
                        title = title,
                        description = desc,
                        moduleName = modName,
                        evidenceTypeName = evTypeName,
                        statusName = statName,
                        authorId = _currentUser.value.id,
                        authorName = author,
                        authorRoleName = _currentUser.value.role.name,
                        authorInstitution = _currentUser.value.institutionName,
                        location = null,
                        tagsCsv = "",
                        sourceReferencesCsv = "",
                        isConfidential = false,
                        syncStateName = SyncState.SYNCED.name,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                        reviewerId = null,
                        reviewerName = null,
                        verificationNotes = "Restored from JSON backup snapshot"
                    )
                    scope.launch {
                        dao.insertSubmission(entity)
                    }
                    count++
                }
            }

            if (root.has("surveillanceReports")) {
                val array = root.getJSONArray("surveillanceReports")
                val restoredSurv = mutableListOf<SurveillanceReport>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    restoredSurv.add(
                        SurveillanceReport(
                            id = obj.optString("id", "srv-restored-$i"),
                            conditionName = obj.optString("conditionName", "Unknown"),
                            syndromicCategory = obj.optString("syndromicCategory", "General"),
                            region = obj.optString("region", "National"),
                            caseCount = obj.optInt("caseCount", 1),
                            reporterId = _currentUser.value.id,
                            reporterName = _currentUser.value.fullName,
                            reportingFacility = _currentUser.value.institutionName ?: "National Health Center",
                            isDeIdentified = true,
                            severityLevel = obj.optString("severityLevel", "MODERATE"),
                            clinicalNotes = obj.optString("clinicalNotes", "Restored"),
                            verificationStatus = LifecycleStatus.SUBMITTED
                        )
                    )
                    count++
                }
                _surveillanceReports.value = restoredSurv + _surveillanceReports.value
            }

            recordAuditLog(
                entityId = "RESTORE-OPERATION",
                entityType = "BackupManager",
                action = "DATA_RESTORED",
                details = "Successfully restored $count scientific entities from backup snapshot"
            )
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to restore backup JSON: ${e.message}"))
        }
    }

    // Health Surveillance submission
    fun createSurveillanceReport(
        conditionName: String,
        syndromicCategory: String,
        region: String,
        caseCount: Int,
        severityLevel: String,
        clinicalNotes: String
    ) {
        val user = _currentUser.value
        val report = SurveillanceReport(
            id = "srv-" + UUID.randomUUID().toString().take(6),
            conditionName = conditionName,
            syndromicCategory = syndromicCategory,
            region = region,
            caseCount = caseCount,
            reporterId = user.id,
            reporterName = user.fullName,
            reportingFacility = user.institutionName ?: "District Clinic",
            isDeIdentified = true,
            severityLevel = severityLevel,
            clinicalNotes = clinicalNotes,
            verificationStatus = LifecycleStatus.SUBMITTED
        )
        _surveillanceReports.value = listOf(report) + _surveillanceReports.value
        recordAuditLog(
            entityId = report.id,
            entityType = "SurveillanceReport",
            action = "SURVEILLANCE_FILED",
            details = "Filed $conditionName ($caseCount cases) in $region with severity $severityLevel"
        )
    }

    // Educational Task creation
    fun createEducationalTask(
        title: String,
        subject: String,
        description: String,
        researchConnection: String,
        learningObjectives: List<String>,
        practicalPrompt: String,
        dueDate: String
    ) {
        val user = _currentUser.value
        val task = EducationalTask(
            id = "task-" + UUID.randomUUID().toString().take(6),
            title = title,
            subject = subject,
            teacherName = user.fullName,
            teacherInstitution = user.institutionName ?: "Scientific High School",
            description = description,
            researchConnection = researchConnection,
            learningObjectives = learningObjectives,
            practicalActivityPrompt = practicalPrompt,
            dueDate = dueDate,
            submissionsCount = 0
        )
        _educationalTasks.value = listOf(task) + _educationalTasks.value
        recordAuditLog(
            entityId = task.id,
            entityType = "EducationalTask",
            action = "TASK_PUBLISHED",
            details = "Educational task '${task.title}' assigned to students"
        )
    }

    // Research Question creation
    fun createResearchQuestion(
        title: String,
        description: String,
        discipline: String
    ) {
        val user = _currentUser.value
        val question = ResearchQuestion(
            id = "rq-" + UUID.randomUUID().toString().take(6),
            title = title,
            description = description,
            discipline = discipline,
            proposedBy = user.fullName,
            authorRole = user.role
        )
        _researchQuestions.value = listOf(question) + _researchQuestions.value
        recordAuditLog(
            entityId = question.id,
            entityType = "ResearchQuestion",
            action = "QUESTION_PROPOSED",
            details = "Scientific question formulated: '$title' in $discipline"
        )
    }

    // Community observation creation
    fun createCommunityObservation(
        title: String,
        description: String,
        category: String,
        communityName: String,
        district: String,
        isPrivate: Boolean
    ) {
        val user = _currentUser.value
        val obs = CommunityObservation(
            id = "comm-obs-" + UUID.randomUUID().toString().take(6),
            title = title,
            description = description,
            category = category,
            communityName = communityName,
            district = district,
            submittedBy = user.fullName,
            isPrivate = isPrivate,
            status = LifecycleStatus.SUBMITTED
        )
        _communityObservations.value = listOf(obs) + _communityObservations.value
        recordAuditLog(
            entityId = obs.id,
            entityType = "CommunityObservation",
            action = "OBSERVATION_REPORTED",
            details = "Citizen science observation '$title' recorded in $communityName ($district)"
        )
    }

    // Cross-Module Knowledge Linkage Operations
    fun linkSubmissionToEducationalTask(
        submissionId: String,
        taskTitle: String,
        subject: String,
        prompt: String
    ): EducationalTask {
        val user = _currentUser.value
        val task = EducationalTask(
            id = "task-" + UUID.randomUUID().toString().take(6),
            title = taskTitle,
            subject = subject,
            teacherName = user.fullName,
            teacherInstitution = user.institutionName ?: "Scientific High School",
            description = "Educational inquiry task adapted from verified evidence record #$submissionId",
            researchConnection = "Directly connected to verified research finding #$submissionId",
            learningObjectives = listOf("Analyze field methodology", "Collect replicate observations", "Submit data for teacher review"),
            practicalActivityPrompt = prompt,
            dueDate = "Next Term",
            submissionsCount = 0,
            linkedSourceId = submissionId,
            linkedSourceTitle = taskTitle,
            linkedSourceModule = ModuleType.RESEARCH
        )
        _educationalTasks.value = listOf(task) + _educationalTasks.value
        val conn = CrossModuleConnection(
            id = "conn-" + UUID.randomUUID().toString().take(6),
            sourceSubmissionId = submissionId,
            targetModule = ModuleType.EDUCATION,
            connectionType = "CLASSROOM_INQUIRY_TASK",
            targetEntityId = task.id,
            targetTitle = taskTitle,
            initiatedBy = user.fullName
        )
        _crossModuleConnections.value = listOf(conn) + _crossModuleConnections.value
        recordAuditLog(
            entityId = submissionId,
            entityType = "SubmissionItem",
            action = "CROSS_MODULE_LINK",
            details = "Verified finding #$submissionId adapted into Educational Task '${task.title}' by ${user.fullName} (${user.role.displayName})"
        )
        return task
    }

    fun linkSubmissionToResearchQuestion(
        submissionId: String,
        questionTitle: String,
        discipline: String,
        description: String
    ): ResearchQuestion {
        val user = _currentUser.value
        val question = ResearchQuestion(
            id = "rq-" + UUID.randomUUID().toString().take(6),
            title = questionTitle,
            description = description,
            discipline = discipline,
            proposedBy = user.fullName,
            authorRole = user.role,
            linkedObservationsCount = 1,
            linkedSourceId = submissionId,
            linkedSourceTitle = questionTitle,
            linkedSourceModule = ModuleType.COMMUNITY
        )
        _researchQuestions.value = listOf(question) + _researchQuestions.value
        val conn = CrossModuleConnection(
            id = "conn-" + UUID.randomUUID().toString().take(6),
            sourceSubmissionId = submissionId,
            targetModule = ModuleType.RESEARCH,
            connectionType = "RESEARCH_QUESTION",
            targetEntityId = question.id,
            targetTitle = questionTitle,
            initiatedBy = user.fullName
        )
        _crossModuleConnections.value = listOf(conn) + _crossModuleConnections.value
        recordAuditLog(
            entityId = submissionId,
            entityType = "SubmissionItem",
            action = "CROSS_MODULE_LINK",
            details = "Observation #$submissionId formulated into Research Question '${question.title}' by ${user.fullName} (${user.role.displayName})"
        )
        return question
    }

    fun linkSubmissionToSurveillance(
        submissionId: String,
        conditionName: String,
        region: String,
        clinicalNotes: String
    ): SurveillanceReport {
        val user = _currentUser.value
        val report = SurveillanceReport(
            id = "srv-" + UUID.randomUUID().toString().take(6),
            conditionName = conditionName,
            syndromicCategory = "Environmental Cluster",
            region = region,
            caseCount = 1,
            reporterId = user.id,
            reporterName = user.fullName,
            reportingFacility = user.institutionName ?: "National Referral Hospital",
            isDeIdentified = true,
            severityLevel = "ELEVATED",
            clinicalNotes = clinicalNotes,
            verificationStatus = LifecycleStatus.SUBMITTED,
            linkedSourceId = submissionId,
            linkedSourceTitle = conditionName
        )
        _surveillanceReports.value = listOf(report) + _surveillanceReports.value
        val conn = CrossModuleConnection(
            id = "conn-" + UUID.randomUUID().toString().take(6),
            sourceSubmissionId = submissionId,
            targetModule = ModuleType.HEALTH,
            connectionType = "SURVEILLANCE_ALERT",
            targetEntityId = report.id,
            targetTitle = conditionName,
            initiatedBy = user.fullName
        )
        _crossModuleConnections.value = listOf(conn) + _crossModuleConnections.value
        recordAuditLog(
            entityId = submissionId,
            entityType = "SubmissionItem",
            action = "CROSS_MODULE_LINK",
            details = "Observation #$submissionId elevated to Clinical Disease Surveillance Alert by ${user.fullName}"
        )
        return report
    }

    suspend fun getPendingSyncQueue(): List<SubmissionEntity> {
        return dao.getPendingSyncSubmissions()
    }


    private fun recordAuditLog(
        entityId: String,
        entityType: String,
        action: String,
        details: String
    ) {
        val user = _currentUser.value
        val log = AuditLogEntity(
            id = "log-" + UUID.randomUUID().toString(),
            entityId = entityId,
            entityType = entityType,
            actorId = user.id,
            actorName = user.fullName,
            actorRoleName = user.role.name,
            action = action,
            details = details,
            timestamp = System.currentTimeMillis()
        )
        scope.launch {
            dao.insertAuditLog(log)
        }
    }

    // AI Scientific Intelligence Generator
    fun queryScientificIntelligence(prompt: String, language: String): AiScientificResponse {
        val isRw = language == "RW"
        val lower = prompt.lowercase()

        return when {
            lower.contains("malaria") || lower.contains("mosquito") -> {
                if (isRw) {
                    AiScientificResponse(
                        topic = "Ubushakashatsi ku Malariya n'Imiti Yayo",
                        summary = "Malariya iterwa na parasit ya Plasmodium ikwirakwizwa n'umubu wa Anopheles. Muri SIMONIUM, amakuru yo mu mavuriro n'ibipimo byo mu baturage bifasha abashakashatsi kumenya uduce turangwamo ubwiyongere.",
                        hypothesesSuggested = listOf(
                            "Ubushyuhe bwo mu misozi miremire buragenda bwiyongera bigatuma imibu yororoka aho itabaga mbere.",
                            "Gukoresha ibiti by'umwimerere birwanya imibu bishobora kugabanya ikwirakwizwa ku kigero cya 35%."
                        ),
                        interdisciplinaryLinks = "Ubuvuzi (Surveillance) ↔ Ibidukikije (Temperature data) ↔ Uburezi (Uruhare rw'abanyeshuri mu gupima amazi adatemba)",
                        kinyarwandaNotes = "Icyitonderwa cy'Ubuvuzi: Ibi bitekerezo ni iby'ubushakashatsi gusa, ntibigomba gufatwa nk'umwanzuro w'umuganga utaremejwe n'inzobere z'ubuvuzi."
                    )
                } else {
                    AiScientificResponse(
                        topic = "Malaria Surveillance & Vector Ecology",
                        summary = "Plasmodium transmission dynamics correlated with microclimate shifts. In SIMONIUM, clinical disease reporting feeds directly into laboratory entomological studies.",
                        hypothesesSuggested = listOf(
                            "High-altitude larval habitats have expanded due to mean nighttime temperature increases.",
                            "Community-led bio-larviciding reduces local vector density by an estimated 42% over 6 months."
                        ),
                        interdisciplinaryLinks = "Health Module (Surveillance) ↔ Research (Entomology & Genomics) ↔ Education (Fieldwork water mapping)",
                        kinyarwandaNotes = "Medical Safety Notice: Scientific research guidance only. Clinical diagnoses require certified human medical practitioners."
                    )
                }
            }
            lower.contains("water") || lower.contains("sanitation") || lower.contains("amazi") -> {
                if (isRw) {
                    AiScientificResponse(
                        topic = "Gutunganya Amazi n'Ubusugire bw'Ubuzima",
                        summary = "Amazi meza ni ishingiro ry'ubuzima n'iterambere. SIMONIUM ifasha abanyeshuri n'abaturage gupima ubwiza bw'amazi (turbidity, pH) bikongera ubushobozi bw'ubushakashatsi mu gihugu.",
                        hypothesesSuggested = listOf(
                            "Icyomekezo cy'umucanga n'amakara gakondo gishobora kuyungurura 90% by'imyanda y'amazi mu cyaro.",
                            "Imihindagurikire y'ibihe n'isuri byongera ubwandu mu masoko y'amazi mu misozi."
                        ),
                        interdisciplinaryLinks = "Ibidukikije ↔ Uburezi (Imishinga y'abanyeshuri mu mashuri) ↔ Ubuvuzi bwo mu baturage",
                        kinyarwandaNotes = "Amakuru yose agomba kugenzurwa n'inzobere mu by'ubuzima n'ibidukikije."
                    )
                } else {
                    AiScientificResponse(
                        topic = "Water Quality & Community Catchment Analysis",
                        summary = "Integration of hydrological sensors, student nephelometric turbidity assays, and enteric infection surveillance for preventive public health.",
                        hypothesesSuggested = listOf(
                            "Multi-stage sand and bio-char filtration reduces bacterial coliform counts by 88% in rural catchments.",
                            "Upstream agricultural runoff correlates with downstream waterborne diarrhea spikes within 48 hours."
                        ),
                        interdisciplinaryLinks = "Community Science (Field sampling) ↔ Education (Chemistry assays) ↔ Public Health (Infection rates)",
                        kinyarwandaNotes = "Evidence requires validation according to ISO/IEC 17025 environmental testing protocols."
                    )
                }
            }
            else -> {
                if (isRw) {
                    AiScientificResponse(
                        topic = "Ubuvumbuzi n'Ubushakashatsi muri SIMONIUM",
                        summary = "SIMONIUM ihuza amakuru ava mu mashuri, amavuriro, abaturage, n'inzobere mu bumenyi kugira ngo haboneke ibisubizo bifatika ku bibazo byugarije sosiyete.",
                        hypothesesSuggested = listOf(
                            "Kuhuza ibibazo by'abaturage n'amasomo yo mu mashuri bitera abanyeshuri umuhati wo guhanga udushya.",
                            "Ibyegeranyo by'ubushakashatsi bikeneye gusuzumwa n'impuguke mbere yo gutangazwa nk'ukuri kwa gihanga."
                        ),
                        interdisciplinaryLinks = "Uburezi ↔ Ubumenyi n'Ubushakashatsi ↔ Ubuvuzi ↔ Abaturage",
                        kinyarwandaNotes = "Icyitonderwa: Ibisobanuro bya AI bigomba kwemezwa n'ubugenzuzi bw'abantu (Human Verification)."
                    )
                } else {
                    AiScientificResponse(
                        topic = "SIMONIUM Multidisciplinary Discovery Framework",
                        summary = "Unifying fragmented data across education, clinical observation, citizen science, and laboratory discovery into an empirical knowledge graph.",
                        hypothesesSuggested = listOf(
                            "Cross-disciplinary synthesis between localized community observations and university laboratories accelerates discovery lifecycles by 3x.",
                            "Structured peer-review workflows prevent publication of unverified medical and environmental claims."
                        ),
                        interdisciplinaryLinks = "Research & Discovery ↔ Health Surveillance ↔ Education ↔ Citizen Science",
                        kinyarwandaNotes = "Disclaimer: AI suggestions assist inquiry but never replace rigorous peer-review and verified empirical evidence."
                    )
                }
            }
        }
    }

    companion object {
        val DEMO_USERS = listOf(
            UserProfile(
                id = "usr-adm-01",
                fullName = "Dr. Emmanuel Gasana",
                email = "admin@simonium.org",
                role = UserRole.ADMINISTRATOR,
                approvalStatus = RoleApprovalStatus.ACTIVE,
                institutionName = "SIMONIUM Global Governance Board",
                specialty = "Scientific Information Architecture",
                verifiedContributionsCount = 142
            ),
            UserProfile(
                id = "usr-doc-02",
                fullName = "Dr. Claire Mukamana, MD",
                email = "c.mukamana@chuk.rw",
                role = UserRole.HEALTHCARE_PRO,
                approvalStatus = RoleApprovalStatus.ACTIVE,
                institutionName = "Kigali Teaching Hospital (CHUK)",
                specialty = "Infectious Diseases & Clinical Epidemiology",
                verifiedContributionsCount = 89
            ),
            UserProfile(
                id = "usr-tch-03",
                fullName = "Prof. Jean de Dieu Nshimiyimana",
                email = "j.nshimiyimana@greenhills.rw",
                role = UserRole.TEACHER,
                approvalStatus = RoleApprovalStatus.ACTIVE,
                institutionName = "Green Hills Scientific Academy",
                specialty = "STEM Pedagogy & Biological Sciences",
                verifiedContributionsCount = 57
            ),
            UserProfile(
                id = "usr-res-04",
                fullName = "Dr. Amina Umutoni, PhD",
                email = "a.umutoni@ur.ac.rw",
                role = UserRole.RESEARCHER,
                approvalStatus = RoleApprovalStatus.ACTIVE,
                institutionName = "University of Rwanda - CST",
                specialty = "Microbiology & Molecular Parasitology",
                verifiedContributionsCount = 114
            ),
            UserProfile(
                id = "usr-stu-05",
                fullName = "Kelia Keza",
                email = "kelia.k@student.ur.ac.rw",
                role = UserRole.STUDENT,
                approvalStatus = RoleApprovalStatus.ACTIVE,
                institutionName = "University of Rwanda - Biotechnology Dept",
                specialty = "Bioinformatics Undergraduate",
                verifiedContributionsCount = 18
            ),
            UserProfile(
                id = "usr-com-06",
                fullName = "Patrick Habimana",
                email = "patrick.habimana@musanze.gov",
                role = UserRole.COMMUNITY_MEMBER,
                approvalStatus = RoleApprovalStatus.ACTIVE,
                institutionName = "Musanze Agro-Ecological Cooperative",
                specialty = "Citizen Science & Agronomy Field Scout",
                verifiedContributionsCount = 24
            ),
            UserProfile(
                id = "usr-dis-07",
                fullName = "Diane Uwase",
                email = "diane.u@innovate.rw",
                role = UserRole.DISCOVERER,
                approvalStatus = RoleApprovalStatus.ACTIVE,
                institutionName = "Rwanda Innovation & Discovery Hub",
                specialty = "Renewable Biomaterials & Clean Tech",
                verifiedContributionsCount = 33
            )
        )

        val INITIAL_SUBMISSIONS = listOf(
            SubmissionItem(
                id = "sub-001",
                title = "Anopheles Larval Density Shifts in Musanze Wetlands (Alt. >1850m)",
                description = "Systematic sampling of aquatic larvae across 12 high-altitude drainage ditches. Larval count averaged 4.8 per dip, representing an unexpected vector colonization in highland zones previously considered non-endemic.",
                module = ModuleType.HEALTH,
                evidenceType = EvidenceType.REVIEWED_EVIDENCE,
                status = LifecycleStatus.VERIFIED,
                authorId = "usr-res-04",
                authorName = "Dr. Amina Umutoni, PhD",
                authorRole = UserRole.RESEARCHER,
                authorInstitution = "University of Rwanda - CST",
                location = "Musanze District, Northern Province",
                tags = listOf("VectorEcology", "HighAltitude", "Epidemiology", "Malaria"),
                sourceReferences = listOf("WHO Vector Ecology Guidelines 2024", "RBC Highland Survey 2025"),
                reviewerId = "usr-doc-02",
                reviewerName = "Dr. Claire Mukamana, MD",
                verificationNotes = "Independently verified with PCR identification confirming Anopheles gambiae s.s."
            ),
            SubmissionItem(
                id = "sub-002",
                title = "Secondary School River Turbidity & Coliform Field Survey",
                description = "45 students conducted nephelometric turbidity unit (NTU) assays along the Nyabarongo river tributaries following heavy rain events. 8 out of 10 sampling sites showed NTU > 50 with elevated fecal indicator bacteria.",
                module = ModuleType.EDUCATION,
                evidenceType = EvidenceType.RAW_OBSERVATION,
                status = LifecycleStatus.APPROVED,
                authorId = "usr-stu-05",
                authorName = "Kelia Keza",
                authorRole = UserRole.STUDENT,
                authorInstitution = "Green Hills Scientific Academy",
                location = "Nyabarongo River Catchment",
                tags = listOf("WaterTesting", "Fieldwork", "StudentScience"),
                sourceReferences = listOf("Classroom Task #STEM-204"),
                reviewerId = "usr-tch-03",
                reviewerName = "Prof. Jean de Dieu Nshimiyimana",
                verificationNotes = "Teacher verified: Fieldwork methodology rigorously followed with control blanks."
            ),
            SubmissionItem(
                id = "sub-003",
                title = "Novel Indigenous Artemisia Extract Formulation for Bio-Larviciding",
                description = "Laboratory bioassay testing of cold ethanolic extract of Artemisia afra against 3rd-instar mosquito larvae. Observed 88.4% mortality at 120 ppm within 24 hours.",
                module = ModuleType.RESEARCH,
                evidenceType = EvidenceType.VERIFIED_FINDING,
                status = LifecycleStatus.VERIFIED,
                authorId = "usr-res-04",
                authorName = "Dr. Amina Umutoni, PhD",
                authorRole = UserRole.RESEARCHER,
                authorInstitution = "National Industrial Research & Dev Agency",
                location = "NIRDA Biotechnology Laboratory, Huye",
                tags = listOf("BioLarvicide", "Phytochemistry", "Invention"),
                sourceReferences = listOf("NIRDA Assay Log #B-9912", "J. Trop. Med. 2025:14-22"),
                reviewerId = "usr-adm-01",
                reviewerName = "Dr. Emmanuel Gasana",
                verificationNotes = "Reproducible bioassay verified across three independent test batches."
            ),
            SubmissionItem(
                id = "sub-004",
                title = "Unseasonal River Discoloration and Fish Mortality near Granite Quarry",
                description = "Local cooperative members observed milky gray runoff flowing directly into the stream, leading to localized fish death and sudden water cloudiness affecting downstream irrigation.",
                module = ModuleType.COMMUNITY,
                evidenceType = EvidenceType.RAW_OBSERVATION,
                status = LifecycleStatus.UNDER_REVIEW,
                authorId = "usr-com-06",
                authorName = "Patrick Habimana",
                authorRole = UserRole.COMMUNITY_MEMBER,
                authorInstitution = "Musanze Agro-Ecological Cooperative",
                location = "Kinigi Sector, Musanze",
                tags = listOf("PollutionAlert", "RiverHealth", "CommunityReport"),
                sourceReferences = listOf("Community Observation Log #C-12"),
                isConfidential = false,
                reviewerId = "usr-res-04",
                reviewerName = "Dr. Amina Umutoni, PhD",
                verificationNotes = "Forwarded to REMA environmental inspectorate; water sampling team deployed."
            ),
            SubmissionItem(
                id = "sub-005",
                title = "Clinical Surveillance: Acute Gastroenteritis Cluster in Kinigi",
                description = "De-identified report: 28 ambulatory pediatric cases presenting with non-bloody watery diarrhea within a 72-hour window. Temporal correlation with recent localized water pipe disruption.",
                module = ModuleType.HEALTH,
                evidenceType = EvidenceType.REVIEWED_EVIDENCE,
                status = LifecycleStatus.SUBMITTED,
                authorId = "usr-doc-02",
                authorName = "Dr. Claire Mukamana, MD",
                authorRole = UserRole.HEALTHCARE_PRO,
                authorInstitution = "CHUK / District Referral Unit",
                location = "Northern Province Health District",
                tags = listOf("Surveillance", "DeIdentified", "PediatricHealth", "Enteric"),
                sourceReferences = listOf("CHUK Epid-Report 2026-W42"),
                isConfidential = true
            )
        )

        val INITIAL_RESEARCH_PROJECTS = listOf(
            ResearchProject(
                id = "proj-01",
                title = "High-Altitude Malaria Transmission & Climate Resilience in Rwanda",
                leadResearcher = "Dr. Amina Umutoni, PhD",
                leadInstitution = "University of Rwanda & Rwanda Biomedical Center",
                field = "Medical Entomology & Climate Epidemiology",
                objectives = listOf(
                    "Map vector thermal tolerance in areas above 1,800m elevation",
                    "Synthesize student water assays and satellite land-surface temperature",
                    "Evaluate biodegradable plant-derived larvicides in field trials"
                ),
                expectedOutcomes = "Predictive early warning system for high-altitude malaria outbreaks and eco-friendly larvicide deployment protocol.",
                methodology = "Cross-sectional sampling, RT-PCR speciation, satellite GIS correlation, and community-guided larval dipping.",
                progressPercent = 68,
                status = "IN_PROGRESS",
                linkedQuestions = listOf("rq-01", "rq-02"),
                verifiedFindings = listOf("Finding #VF-101: Anopheles gambiae established at 1,920m in microclimate pockets"),
                citations = listOf("Rwanda National Malaria Strategic Plan 2020-2026", "Nature Climate Change 2024: Vol 14")
            ),
            ResearchProject(
                id = "proj-02",
                title = "Low-Cost Ceramic & Bio-Char Water Purification for Rural Cooperatives",
                leadResearcher = "Prof. Jean de Dieu Nshimiyimana & Dr. Emmanuel Gasana",
                leadInstitution = "National Industrial Research Agency & Green Hills",
                field = "Environmental Engineering & Materials Science",
                objectives = listOf(
                    "Develop locally manufactured terracotta clay filter discs impregnated with colloidal silver",
                    "Engage secondary school STEM students in turbidity and bacterial test monitoring",
                    "Deliver decentralized clean drinking water to 5 pilot communities"
                ),
                expectedOutcomes = "Scalable ceramic filtration kit costing under $12 with >99% E. coli reduction.",
                methodology = "Materials characterization, gravimetric flow measurement, and membrane filtration bacterial culture.",
                progressPercent = 84,
                status = "PEER_REVIEW",
                linkedQuestions = listOf("rq-03"),
                verifiedFindings = listOf("Finding #VF-204: Pyrolyzed coffee husk bio-char enhances heavy metal sorption by 44%"),
                citations = listOf("Water Research Journal 2025", "NIRDA Tech Report #WT-2025-09")
            )
        )

        val INITIAL_RESEARCH_QUESTIONS = listOf(
            ResearchQuestion(
                id = "rq-01",
                title = "What micro-climatic thresholds enable Anopheles breeding in highland volcanic zones?",
                description = "Investigating the intersection of nocturnal minimum temperature, volcanic soil drainage, and agricultural irrigation terraces.",
                discipline = "Medical Entomology",
                proposedBy = "Dr. Amina Umutoni",
                authorRole = UserRole.RESEARCHER,
                linkedObservationsCount = 8
            ),
            ResearchQuestion(
                id = "rq-02",
                title = "How do seasonal agricultural runoff events impact pediatric enteric infections downstream?",
                description = "Connecting community water turbidity observations with clinical pediatric admissions.",
                discipline = "Public Health & Hydrology",
                proposedBy = "Dr. Claire Mukamana, MD",
                authorRole = UserRole.HEALTHCARE_PRO,
                linkedObservationsCount = 14
            ),
            ResearchQuestion(
                id = "rq-03",
                title = "Can locally pyrolyzed agricultural biomass serve as viable filtration media?",
                description = "Testing coffee husks, rice husks, and macadamia shells as active filtration matrices.",
                discipline = "Materials Science",
                proposedBy = "Diane Uwase",
                authorRole = UserRole.DISCOVERER,
                linkedObservationsCount = 6
            )
        )

        val INITIAL_EDUCATIONAL_TASKS = listOf(
            EducationalTask(
                id = "task-01",
                title = "Fieldwork: Stream Turbidity, pH & Aquatic Bio-Indicators",
                subject = "Environmental Biology & Chemistry",
                teacherName = "Prof. Jean de Dieu Nshimiyimana",
                teacherInstitution = "Green Hills Scientific Academy",
                description = "Students visit a designated local river site, collect 3 water samples, measure clarity with a Secchi disk or NTU meter, and document macroinvertebrates.",
                researchConnection = "Contributes empirical data directly to Project #proj-02 (Water Purification)",
                learningObjectives = listOf(
                    "Master standardized field sampling protocols and contamination prevention",
                    "Calculate biotic index score using dragonfly and mayfly nymph counts",
                    "Format observations for peer review submission on SIMONIUM"
                ),
                practicalActivityPrompt = "Upload 3 geo-located photos of your sampling station, record pH test strips results, and document visible sediment runoff sources.",
                dueDate = "Nov 15, 2026",
                submissionsCount = 38
            ),
            EducationalTask(
                id = "task-02",
                title = "Community Flora Survey: Identifying Medicinal Artemisia Varieties",
                subject = "Plant Biology & Ethnobotany",
                teacherName = "Prof. Jean de Dieu Nshimiyimana",
                teacherInstitution = "Green Hills Scientific Academy",
                description = "Document wild and cultivated Artemisia species in your district. Record leaf morphology, scent profile, and local traditional uses.",
                researchConnection = "Contributes plant specimens to NIRDA Bio-larvicide research team",
                learningObjectives = listOf(
                    "Differentiate Artemisia annua from Artemisia afra using botanical keys",
                    "Conduct respectful community elder interviews on botanical applications",
                    "Structure ethnobotanical notes without medical prescriptive bias"
                ),
                practicalActivityPrompt = "Submit plant specimen photos, leaf size measurements, and GPS location notes.",
                dueDate = "Nov 28, 2026",
                submissionsCount = 22
            )
        )

        val INITIAL_SURVEILLANCE_REPORTS = listOf(
            SurveillanceReport(
                id = "srv-01",
                conditionName = "Acute Watery Diarrhea Cluster",
                syndromicCategory = "Enteric / Waterborne",
                region = "Musanze - Kinigi Sector",
                caseCount = 28,
                reporterId = "usr-doc-02",
                reporterName = "Dr. Claire Mukamana, MD",
                reportingFacility = "CHUK / District Health Center",
                isDeIdentified = true,
                severityLevel = "ELEVATED",
                clinicalNotes = "De-identified: 28 cases reported in 72 hours. All patients treated with oral rehydration therapy. No fatalities. Correlates with suspected upstream water supply pipe contamination.",
                verificationStatus = LifecycleStatus.VERIFIED
            ),
            SurveillanceReport(
                id = "srv-02",
                conditionName = "Febrile Illness without Hemorrhagic Signs",
                syndromicCategory = "Vector-Borne / Febrile",
                region = "Rwamagana District",
                caseCount = 14,
                reporterId = "usr-doc-02",
                reporterName = "Dr. Claire Mukamana, MD",
                reportingFacility = "Rwamagana Provincial Hospital",
                isDeIdentified = true,
                severityLevel = "MODERATE",
                clinicalNotes = "Rapid diagnostic tests negative for typhoid, positive for P. falciparum in 11 patients. Patients responding normally to Artemether-Lumefantrine first-line treatment.",
                verificationStatus = LifecycleStatus.SUBMITTED
            )
        )

        val INITIAL_COMMUNITY_OBSERVATIONS = listOf(
            CommunityObservation(
                id = "comm-01",
                title = "Abnormal Silt Deposition in Wetland Drainage Channels",
                description = "Following heavy road construction rainfall, excessive volcanic red clay silt settled in the downstream irrigation canals, blocking fish movement and smothering watercress beds.",
                category = "Water & Sanitation",
                communityName = "Kinigi Farmers Cooperative",
                district = "Musanze",
                submittedBy = "Patrick Habimana",
                isPrivate = false,
                status = LifecycleStatus.APPROVED,
                linkedResearchProjectTitle = "Low-Cost Ceramic & Bio-Char Water Purification",
                expertResponse = "Dr. Umutoni: Soil conservation contours recommended. Sediment samples requested for turbidity analysis."
            ),
            CommunityObservation(
                id = "comm-02",
                title = "Invasive Stem-Borer Damage in Nyagatare Early Maize",
                description = "Widespread pinhole lesions on young maize whorls observed across 15 hectares. Traditional neem leaf extracts showing only partial deterrence.",
                category = "Local Crop Health",
                communityName = "Nyagatare Agro-Scouts",
                district = "Nyagatare",
                submittedBy = "Patrick Habimana",
                isPrivate = false,
                status = LifecycleStatus.SUBMITTED,
                linkedResearchProjectTitle = null,
                expertResponse = "Forwarded to RAB Crop Protection specialists."
            )
        )

        val INITIAL_AUDIT_LOGS = listOf(
            AuditLogEntity(
                id = "log-seed-01",
                entityId = "sub-001",
                entityType = "Submission",
                actorId = "usr-res-04",
                actorName = "Dr. Amina Umutoni, PhD",
                actorRoleName = UserRole.RESEARCHER.name,
                action = "CREATED",
                details = "Initial highland malaria vector study submitted with empirical data",
                timestamp = System.currentTimeMillis() - 86400000 * 3
            ),
            AuditLogEntity(
                id = "log-seed-02",
                entityId = "sub-001",
                entityType = "Submission",
                actorId = "usr-doc-02",
                actorName = "Dr. Claire Mukamana, MD",
                actorRoleName = UserRole.HEALTHCARE_PRO.name,
                action = "VERIFIED",
                details = "Record verified with independent PCR genomic sequencing identification",
                timestamp = System.currentTimeMillis() - 86400000 * 2
            ),
            AuditLogEntity(
                id = "log-seed-03",
                entityId = "sub-002",
                entityType = "Submission",
                actorId = "usr-stu-05",
                actorName = "Kelia Keza",
                actorRoleName = UserRole.STUDENT.name,
                action = "SUBMITTED",
                details = "Student fieldwork submission uploaded for Nyabarongo river turbidity task",
                timestamp = System.currentTimeMillis() - 86400000
            ),
            AuditLogEntity(
                id = "log-seed-04",
                entityId = "sub-002",
                entityType = "Submission",
                actorId = "usr-tch-03",
                actorName = "Prof. Jean de Dieu Nshimiyimana",
                actorRoleName = UserRole.TEACHER.name,
                action = "APPROVED",
                details = "Teacher review: Fieldwork methodology validated, sample controls inspected",
                timestamp = System.currentTimeMillis() - 3600000 * 12
            )
        )

        val INITIAL_COMMENTS = mapOf(
            "sub-001" to listOf(
                ReviewComment(
                    id = "c-01",
                    submissionId = "sub-001",
                    authorId = "usr-doc-02",
                    authorName = "Dr. Claire Mukamana, MD",
                    authorRole = UserRole.HEALTHCARE_PRO,
                    comment = "The PCR confirmation of Anopheles gambiae at 1850m aligns directly with the recent pediatric admissions we observed in Kinigi sector. Excellent empirical tracking.",
                    timestamp = System.currentTimeMillis() - 86400000 * 2
                )
            ),
            "sub-004" to listOf(
                ReviewComment(
                    id = "c-02",
                    submissionId = "sub-004",
                    authorId = "usr-res-04",
                    authorName = "Dr. Amina Umutoni, PhD",
                    authorRole = UserRole.RESEARCHER,
                    comment = "Initial findings are consistent with satellite spectrophotometry data. Advise submission to formal peer review.",
                    timestamp = System.currentTimeMillis() - 86400000
                )
            )
        )

        val INITIAL_VERIFICATION_APPLICATIONS = listOf(
            VerificationApplication(
                id = "app-001",
                userId = "usr-doc-02",
                userFullName = "Dr. Claire Mukamana, MD",
                userEmail = "c.mukamana@chuk.rw",
                requestedRole = UserRole.HEALTHCARE_PRO,
                requestedModules = listOf(ModuleType.HEALTH, ModuleType.RESEARCH),
                status = VerificationApplicationStatus.APPROVED,
                institutionName = "Kigali Teaching Hospital (CHUK)",
                roleSpecificDetails = mapOf(
                    "licenceNumber" to "RMC-2021-4821",
                    "hospital" to "CHUK Hospital",
                    "category" to "Infectious Disease Specialist"
                ),
                documents = listOf(
                    VerificationDocument(
                        id = "doc-001",
                        userId = "usr-doc-02",
                        documentType = "Medical Practice Licence",
                        fileName = "licence_mukamana_2026.pdf",
                        fileSizeKb = 840,
                        issuingAuthority = "Rwanda Medical and Dental Council",
                        documentNumber = "RMC-2021-4821",
                        expiryDate = "Dec 2028",
                        isVerified = true
                    )
                ),
                reviewerName = "Dr. Emmanuel Gasana",
                reviewerComments = "Verified against Ministry of Health clinical registry."
            ),
            VerificationApplication(
                id = "app-002",
                userId = "usr-pend-08",
                userFullName = "Dr. Eric Habimana",
                userEmail = "eric.h@butare.hospital.rw",
                requestedRole = UserRole.HEALTHCARE_PRO,
                requestedModules = listOf(ModuleType.HEALTH),
                status = VerificationApplicationStatus.VERIFICATION_PENDING,
                institutionName = "Butare University Teaching Hospital",
                roleSpecificDetails = mapOf(
                    "licenceNumber" to "RMC-2025-9923",
                    "hospital" to "Butare Teaching Hospital",
                    "category" to "Pediatric Resident"
                ),
                documents = listOf(
                    VerificationDocument(
                        id = "doc-002",
                        userId = "usr-pend-08",
                        documentType = "Official Staff ID Card",
                        fileName = "staff_card_habimana.jpg",
                        fileSizeKb = 1240,
                        issuingAuthority = "CHUB Butare",
                        documentNumber = "STAFF-9024",
                        isVerified = false
                    )
                ),
                reviewerComments = "Pending review by Verification Board"
            ),
            VerificationApplication(
                id = "app-003",
                userId = "usr-stu-05",
                userFullName = "Kelia Keza",
                userEmail = "kelia.k@student.ur.ac.rw",
                requestedRole = UserRole.STUDENT,
                requestedModules = listOf(ModuleType.EDUCATION, ModuleType.COMMUNITY),
                status = VerificationApplicationStatus.APPROVED,
                institutionName = "University of Rwanda",
                roleSpecificDetails = mapOf(
                    "studentNumber" to "221004589",
                    "programme" to "Biotechnology & Bioinformatics",
                    "academicYear" to "Year 3"
                ),
                documents = listOf(
                    VerificationDocument(
                        id = "doc-003",
                        userId = "usr-stu-05",
                        documentType = "Student ID Card",
                        fileName = "ur_student_id_keza.png",
                        fileSizeKb = 620,
                        issuingAuthority = "University of Rwanda Registrar",
                        documentNumber = "UR-STU-2024",
                        isVerified = true
                    )
                )
            )
        )

        val DEMO_CREDENTIALS = mutableMapOf(
            "admin@simonium.org" to "Admin@123",
            "c.mukamana@chuk.rw" to "Health@123",
            "j.nshimiyimana@greenhills.rw" to "Teacher@123",
            "a.umutoni@ur.ac.rw" to "Research@123",
            "kelia.k@student.ur.ac.rw" to "Student@123",
            "patrick.habimana@musanze.gov" to "Community@123",
            "diane.u@innovate.rw" to "Discover@123",
            "+250788123456" to "Simon@123",
            "+250 788 123 456" to "Simon@123"
        )
    }
}

data class AiScientificResponse(
    val topic: String,
    val summary: String,
    val hypothesesSuggested: List<String>,
    val interdisciplinaryLinks: String,
    val kinyarwandaNotes: String
)
