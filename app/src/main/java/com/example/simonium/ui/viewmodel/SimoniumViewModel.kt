package com.example.simonium.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simonium.data.model.*
import com.example.simonium.data.repository.AiScientificResponse
import com.example.simonium.data.repository.SimoniumRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class NavigationDestination(val label: String) {
    HOME("Home"),
    MODULES("Modules"),
    RESEARCH("Research"),
    SUBMISSIONS("Lifecycle"),
    INTELLIGENCE("AI Assistant"),
    PROFILE("Profile & Security")
}

data class UiNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

class SimoniumViewModel(
    private val repository: SimoniumRepository
) : ViewModel() {

    val isAuthenticated = repository.isAuthenticated
    val currentUser = repository.currentUser
    val demoAccounts = repository.availableDemoAccounts
    val institutions = repository.institutions
    val activeChallenge = repository.activeChallenge
    val verificationApplications = repository.verificationApplications

    val isOnline = repository.isOnline
    val isSyncing = repository.isSyncing

    val researchProjects = repository.researchProjects
    val researchQuestions = repository.researchQuestions
    val educationalTasks = repository.educationalTasks
    val surveillanceReports = repository.surveillanceReports
    val communityObservations = repository.communityObservations
    val reviewComments = repository.reviewComments
    val auditLogs = repository.auditLogs
    val crossModuleConnections = repository.crossModuleConnections


    private val _currentDestination = MutableStateFlow(NavigationDestination.HOME)
    val currentDestination: StateFlow<NavigationDestination> = _currentDestination.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedModuleFilter = MutableStateFlow<ModuleType?>(null)
    val selectedModuleFilter: StateFlow<ModuleType?> = _selectedModuleFilter.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow<LifecycleStatus?>(null)
    val selectedStatusFilter: StateFlow<LifecycleStatus?> = _selectedStatusFilter.asStateFlow()

    // Submissions and filtering
    val submissions: StateFlow<List<SubmissionItem>> = repository.submissions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredSubmissions: StateFlow<List<SubmissionItem>> = combine(
        submissions,
        searchQuery,
        selectedModuleFilter,
        selectedStatusFilter
    ) { items, query, module, status ->
        items.filter { item ->
            val matchesQuery = query.isBlank() ||
                item.title.contains(query, ignoreCase = true) ||
                item.description.contains(query, ignoreCase = true) ||
                item.authorName.contains(query, ignoreCase = true) ||
                item.tags.any { it.contains(query, ignoreCase = true) }
            val matchesModule = module == null || item.module == module
            val matchesStatus = status == null || item.status == status
            matchesQuery && matchesModule && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Submission for Review / Detail Modal
    private val _selectedSubmission = MutableStateFlow<SubmissionItem?>(null)
    val selectedSubmission: StateFlow<SubmissionItem?> = _selectedSubmission.asStateFlow()

    // AI Scientific Intelligence State
    private val _aiQuery = MutableStateFlow("High-altitude malaria transmission dynamics")
    val aiQuery: StateFlow<String> = _aiQuery.asStateFlow()

    private val _aiLanguage = MutableStateFlow("EN") // "EN" or "RW"
    val aiLanguage: StateFlow<String> = _aiLanguage.asStateFlow()

    private val _aiResponse = MutableStateFlow<AiScientificResponse?>(null)
    val aiResponse: StateFlow<AiScientificResponse?> = _aiResponse.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Notification Alerts
    private val _notifications = MutableStateFlow<List<UiNotification>>(
        listOf(
            UiNotification(
                id = "n-1",
                title = "Verification Approved",
                message = "PCR identification data verified for Highland Vector Study (#sub-001)"
            ),
            UiNotification(
                id = "n-2",
                title = "New Fieldwork Submission",
                message = "Secondary school Nyabarongo river turbidity assay submitted by student"
            )
        )
    )
    val notifications: StateFlow<List<UiNotification>> = _notifications.asStateFlow()

    init {
        // Run initial AI query so the screen has immediate valuable data
        askAiIntelligence(_aiQuery.value)
    }

    fun navigateTo(destination: NavigationDestination) {
        _currentDestination.value = destination
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setModuleFilter(module: ModuleType?) {
        _selectedModuleFilter.value = module
    }

    fun setStatusFilter(status: LifecycleStatus?) {
        _selectedStatusFilter.value = status
    }

    fun selectSubmission(item: SubmissionItem?) {
        _selectedSubmission.value = item
    }

    fun switchUser(user: UserProfile) {
        repository.switchUser(user)
    }

    fun login(identifier: String, pass: String): Result<UserProfile> {
        return repository.login(identifier, pass)
    }

    fun loginWithDemoRole(user: UserProfile) {
        repository.loginWithDemoRole(user)
    }

    fun logout() {
        repository.logout()
        _currentDestination.value = NavigationDestination.HOME
    }

    fun sendVerificationCode(contact: String, type: String = "EMAIL"): Result<VerificationChallenge> {
        return repository.sendVerificationCode(contact, type)
    }

    fun verifyCode(code: String): Result<Boolean> {
        return repository.verifyCode(code)
    }

    fun resendVerificationCode(): Result<VerificationChallenge> {
        return repository.resendVerificationCode()
    }

    fun registerWithApplication(application: VerificationApplication, initialPassword: String): Result<UserProfile> {
        return repository.registerWithApplication(application, initialPassword)
    }

    fun requestPasswordReset(identifier: String): Result<VerificationChallenge> {
        return repository.requestPasswordReset(identifier)
    }

    fun completePasswordReset(identifier: String, newPassword: String): Result<Boolean> {
        return repository.completePasswordReset(identifier, newPassword)
    }

    fun changePassword(oldPassword: String, newPassword: String): Result<Boolean> {
        return repository.changePassword(oldPassword, newPassword)
    }

    fun deleteAccount(passwordConfirm: String, reason: String): Result<Boolean> {
        return repository.deleteAccount(passwordConfirm, reason)
    }

    fun reviewVerificationApplication(appId: String, newStatus: VerificationApplicationStatus, comments: String?) {
        repository.reviewVerificationApplication(appId, newStatus, comments)
    }

    fun checkModuleAccess(module: ModuleType): ModuleAccessRequirement {
        return repository.checkModuleAccess(module, currentUser.value)
    }

    fun updateProfile(name: String, email: String, specialty: String?, institutionName: String?) {
        repository.updateProfile(name, email, specialty, institutionName)
    }

    fun toggleNetworkStatus() {
        repository.toggleNetworkStatus()
    }

    fun syncNow() {
        repository.triggerSync()
    }

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
        repository.createSubmission(
            title = title,
            description = description,
            module = module,
            evidenceType = evidenceType,
            location = location,
            tags = tags,
            sourceReferences = sourceReferences,
            isDraft = isDraft,
            isConfidential = isConfidential
        )
    }

    fun updateSubmissionStatus(
        submissionId: String,
        newStatus: LifecycleStatus,
        notes: String? = null,
        commentText: String? = null
    ) {
        repository.updateSubmissionStatus(submissionId, newStatus, notes, commentText)
        // Refresh currently selected item if open
        val current = _selectedSubmission.value
        if (current?.id == submissionId) {
            _selectedSubmission.value = current.copy(
                status = newStatus,
                verificationNotes = notes ?: current.verificationNotes,
                reviewerName = currentUser.value.fullName
            )
        }
    }

    fun addReviewComment(submissionId: String, comment: String, isCorrection: Boolean) {
        repository.addReviewComment(submissionId, comment, isCorrection)
    }

    fun deleteSubmission(id: String) {
        repository.deleteSubmission(id)
        if (_selectedSubmission.value?.id == id) {
            _selectedSubmission.value = null
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
        repository.updateSubmission(
            id = id,
            title = title,
            description = description,
            evidenceType = evidenceType,
            location = location,
            tags = tags,
            sourceReferences = sourceReferences,
            isDraft = isDraft
        )
        val current = _selectedSubmission.value
        if (current?.id == id) {
            _selectedSubmission.value = current.copy(
                title = title,
                description = description,
                evidenceType = evidenceType,
                location = location,
                tags = tags,
                sourceReferences = sourceReferences,
                status = if (isDraft) LifecycleStatus.DRAFT else LifecycleStatus.SUBMITTED
            )
        }
    }

    fun exportBackupJson(): String {
        return repository.exportBackupJson()
    }

    fun restoreBackupJson(json: String): Result<Int> {
        return repository.restoreBackupJson(json)
    }

    fun dismissNotification(id: String) {
        _notifications.value = _notifications.value.filter { it.id != id }
    }

    fun clearAllNotifications() {
        _notifications.value = emptyList()
    }

    fun createSurveillanceReport(
        conditionName: String,
        syndromicCategory: String,
        region: String,
        caseCount: Int,
        severityLevel: String,
        clinicalNotes: String
    ) {
        repository.createSurveillanceReport(
            conditionName,
            syndromicCategory,
            region,
            caseCount,
            severityLevel,
            clinicalNotes
        )
    }

    fun createEducationalTask(
        title: String,
        subject: String,
        description: String,
        researchConnection: String,
        learningObjectives: List<String>,
        practicalPrompt: String,
        dueDate: String
    ) {
        repository.createEducationalTask(
            title,
            subject,
            description,
            researchConnection,
            learningObjectives,
            practicalPrompt,
            dueDate
        )
    }

    fun createResearchQuestion(
        title: String,
        description: String,
        discipline: String
    ) {
        repository.createResearchQuestion(title, description, discipline)
    }

    fun createCommunityObservation(
        title: String,
        description: String,
        category: String,
        communityName: String,
        district: String,
        isPrivate: Boolean
    ) {
        repository.createCommunityObservation(
            title,
            description,
            category,
            communityName,
            district,
            isPrivate
        )
    }

    fun linkSubmissionToEducationalTask(
        submissionId: String,
        taskTitle: String,
        subject: String,
        prompt: String
    ): EducationalTask {
        return repository.linkSubmissionToEducationalTask(submissionId, taskTitle, subject, prompt)
    }

    fun linkSubmissionToResearchQuestion(
        submissionId: String,
        questionTitle: String,
        discipline: String,
        description: String
    ): ResearchQuestion {
        return repository.linkSubmissionToResearchQuestion(submissionId, questionTitle, discipline, description)
    }

    fun linkSubmissionToSurveillance(
        submissionId: String,
        conditionName: String,
        region: String,
        clinicalNotes: String
    ): SurveillanceReport {
        return repository.linkSubmissionToSurveillance(submissionId, conditionName, region, clinicalNotes)
    }

    suspend fun getPendingSyncQueue(): List<com.example.simonium.data.local.SubmissionEntity> {
        return repository.getPendingSyncQueue()
    }

    fun setAiQuery(query: String) {
        _aiQuery.value = query
    }

    fun setAiLanguage(lang: String) {
        _aiLanguage.value = lang
        askAiIntelligence(_aiQuery.value)
    }

    fun askAiIntelligence(query: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            kotlinx.coroutines.delay(600) // Realistic analysis delay
            val response = repository.queryScientificIntelligence(query, _aiLanguage.value)
            _aiResponse.value = response
            _isAiLoading.value = false
        }
    }
}
