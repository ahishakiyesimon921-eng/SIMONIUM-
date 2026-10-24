package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simonium.data.local.SimoniumDatabase
import com.example.simonium.data.model.ModuleType
import com.example.simonium.data.model.UserRole
import com.example.simonium.data.repository.SimoniumRepository
import com.example.simonium.ui.components.*
import com.example.simonium.ui.screens.*
import com.example.ui.theme.SimoniumCyanLight
import com.example.ui.theme.SimoniumNavyDark
import com.example.ui.theme.SimoniumTeal
import com.example.ui.theme.SimoniumTheme
import com.example.simonium.ui.viewmodel.NavigationDestination
import com.example.simonium.ui.viewmodel.SimoniumViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SimoniumTheme {
                val context = LocalContext.current
                val database = remember { SimoniumDatabase.getDatabase(context) }
                val repository = remember { SimoniumRepository(database.dao()) }
                val viewModel = remember { SimoniumViewModel(repository) }

                SimoniumApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SimoniumApp(viewModel: SimoniumViewModel) {
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val currentDestination by viewModel.currentDestination.collectAsState()
    val selectedSubmission by viewModel.selectedSubmission.collectAsState()
    val reviewCommentsMap by viewModel.reviewComments.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState(initial = emptyList())
    val crossModuleConnections by viewModel.crossModuleConnections.collectAsState()
    val demoAccounts = viewModel.demoAccounts
    val aiLanguage by viewModel.aiLanguage.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    var showRoleSwitcher by remember { mutableStateOf(false) }
    var showNewSubmissionDialog by remember { mutableStateOf(false) }
    var initialSubmissionModule by remember { mutableStateOf(ModuleType.RESEARCH) }
    var showSurveillanceDialog by remember { mutableStateOf(false) }
    var showEducationalTaskDialog by remember { mutableStateOf(false) }
    var showResearchQuestionDialog by remember { mutableStateOf(false) }
    var showCommunityObservationDialog by remember { mutableStateOf(false) }

    // Authentication & Verification Dialog States
    var showRegisterWizard by remember { mutableStateOf(false) }
    var showForgotPassword by remember { mutableStateOf(false) }
    var showHelpSupport by remember { mutableStateOf(false) }
    var showNotificationsTray by remember { mutableStateOf(false) }
    var showSettingsDashboard by remember { mutableStateOf(false) }
    var showOfflineSyncQueue by remember { mutableStateOf(false) }
    var showCrossModuleLinkDialog by remember { mutableStateOf(false) }
    var selectedModuleForRequirements by remember { mutableStateOf<ModuleType?>(null) }

    // System Back Handler
    BackHandler(enabled = isAuthenticated && (currentDestination != NavigationDestination.HOME || selectedSubmission != null)) {
        if (selectedSubmission != null) {
            viewModel.selectSubmission(null)
        } else if (currentDestination != NavigationDestination.HOME) {
            viewModel.navigateTo(NavigationDestination.HOME)
        }
    }

    if (!isAuthenticated) {
        LoginDashboardScreen(
            viewModel = viewModel,
            onOpenRegisterWizard = { showRegisterWizard = true },
            onOpenForgotPassword = { showForgotPassword = true },
            onSelectModulePreview = { module ->
                selectedModuleForRequirements = module
            },
            onOpenHelpSupport = { showHelpSupport = true }
        )
    } else {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("simonium_main_scaffold"),
            topBar = {
                SimoniumTopBar(
                    currentUser = currentUser,
                    isOnline = isOnline,
                    isSyncing = isSyncing,
                    activeLanguage = aiLanguage,
                    onRoleClick = { showRoleSwitcher = true },
                    onLanguageToggle = {
                        val nextLang = when (aiLanguage) {
                            "EN" -> "RW"
                            "RW" -> "FR"
                            "FR" -> "SW"
                            else -> "EN"
                        }
                        viewModel.setAiLanguage(nextLang)
                    },
                    onNotificationsClick = {
                        showNotificationsTray = true
                    },
                    notificationsCount = notifications.size,
                    onSettingsClick = {
                        showSettingsDashboard = true
                    },
                    onOfflineQueueClick = {
                        showOfflineSyncQueue = true
                    }
                )
            },
        bottomBar = {
            NavigationBar(
                containerColor = SimoniumNavyDark,
                contentColor = Color.White
            ) {
                NavigationBarItem(
                    selected = currentDestination == NavigationDestination.HOME,
                    onClick = { viewModel.navigateTo(NavigationDestination.HOME) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Home", modifier = Modifier.size(20.dp)) },
                    label = { Text("Home", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SimoniumCyanLight,
                        selectedTextColor = SimoniumCyanLight,
                        indicatorColor = SimoniumTeal.copy(alpha = 0.3f),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_item_home")
                )
                NavigationBarItem(
                    selected = currentDestination == NavigationDestination.MODULES,
                    onClick = { viewModel.navigateTo(NavigationDestination.MODULES) },
                    icon = { Icon(Icons.Default.Widgets, contentDescription = "Modules", modifier = Modifier.size(20.dp)) },
                    label = { Text("Modules", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SimoniumCyanLight,
                        selectedTextColor = SimoniumCyanLight,
                        indicatorColor = SimoniumTeal.copy(alpha = 0.3f),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_item_modules")
                )
                NavigationBarItem(
                    selected = currentDestination == NavigationDestination.RESEARCH,
                    onClick = { viewModel.navigateTo(NavigationDestination.RESEARCH) },
                    icon = { Icon(Icons.Default.Science, contentDescription = "Research", modifier = Modifier.size(20.dp)) },
                    label = { Text("Research", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SimoniumCyanLight,
                        selectedTextColor = SimoniumCyanLight,
                        indicatorColor = SimoniumTeal.copy(alpha = 0.3f),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_item_research")
                )
                NavigationBarItem(
                    selected = currentDestination == NavigationDestination.SUBMISSIONS,
                    onClick = { viewModel.navigateTo(NavigationDestination.SUBMISSIONS) },
                    icon = { Icon(Icons.Default.ListAlt, contentDescription = "Lifecycle", modifier = Modifier.size(20.dp)) },
                    label = { Text("Lifecycle", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SimoniumCyanLight,
                        selectedTextColor = SimoniumCyanLight,
                        indicatorColor = SimoniumTeal.copy(alpha = 0.3f),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_item_submissions")
                )
                NavigationBarItem(
                    selected = currentDestination == NavigationDestination.INTELLIGENCE,
                    onClick = { viewModel.navigateTo(NavigationDestination.INTELLIGENCE) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI", modifier = Modifier.size(20.dp)) },
                    label = { Text("AI Assist", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SimoniumCyanLight,
                        selectedTextColor = SimoniumCyanLight,
                        indicatorColor = SimoniumTeal.copy(alpha = 0.3f),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_item_intelligence")
                )
                NavigationBarItem(
                    selected = currentDestination == NavigationDestination.PROFILE,
                    onClick = { viewModel.navigateTo(NavigationDestination.PROFILE) },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile", modifier = Modifier.size(20.dp)) },
                    label = { Text("Profile", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SimoniumCyanLight,
                        selectedTextColor = SimoniumCyanLight,
                        indicatorColor = SimoniumTeal.copy(alpha = 0.3f),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_item_profile")
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (currentDestination) {
                NavigationDestination.HOME -> HomeScreen(
                    viewModel = viewModel,
                    onOpenSubmissionDialog = {
                        initialSubmissionModule = ModuleType.RESEARCH
                        showNewSubmissionDialog = true
                    },
                    onOpenSurveillanceDialog = { showSurveillanceDialog = true },
                    onOpenEducationalTaskDialog = { showEducationalTaskDialog = true },
                    onOpenResearchQuestionDialog = { showResearchQuestionDialog = true },
                    onOpenCommunityDialog = { showCommunityObservationDialog = true },
                    onOpenRoleSwitcher = { showRoleSwitcher = true }
                )
                NavigationDestination.MODULES -> ModulesScreen(
                    viewModel = viewModel,
                    onOpenSurveillanceDialog = { showSurveillanceDialog = true },
                    onOpenEducationalTaskDialog = { showEducationalTaskDialog = true },
                    onOpenResearchQuestionDialog = { showResearchQuestionDialog = true },
                    onOpenCommunityDialog = { showCommunityObservationDialog = true },
                    onOpenNewSubmission = { module ->
                        initialSubmissionModule = module
                        showNewSubmissionDialog = true
                    },
                    onViewRequirements = { module ->
                        selectedModuleForRequirements = module
                    }
                )
                NavigationDestination.RESEARCH -> ResearchScreen(
                    viewModel = viewModel,
                    onOpenResearchQuestionDialog = { showResearchQuestionDialog = true }
                )
                NavigationDestination.SUBMISSIONS -> SubmissionsScreen(
                    viewModel = viewModel,
                    onOpenNewSubmission = {
                        initialSubmissionModule = ModuleType.RESEARCH
                        showNewSubmissionDialog = true
                    }
                )
                NavigationDestination.INTELLIGENCE -> IntelligenceScreen(
                    viewModel = viewModel
                )
                NavigationDestination.PROFILE -> ProfileScreen(
                    viewModel = viewModel,
                    onOpenRoleSwitcher = { showRoleSwitcher = true }
                )
            }
        }
    }
    }

    // Role Switcher Dialog
    if (showRoleSwitcher) {
        RoleSwitcherDialog(
            currentUser = currentUser,
            availableAccounts = demoAccounts,
            onSelectAccount = { user ->
                viewModel.switchUser(user)
            },
            onDismiss = { showRoleSwitcher = false }
        )
    }

    // New Scientific Submission Dialog
    if (showNewSubmissionDialog) {
        NewSubmissionDialog(
            initialModule = initialSubmissionModule,
            onDismiss = { showNewSubmissionDialog = false },
            onSubmit = { title, desc, mod, evType, loc, tags, refs, isDraft, isConf ->
                viewModel.createSubmission(title, desc, mod, evType, loc, tags, refs, isDraft, isConf)
            }
        )
    }

    // Selected Submission Detailed Viewer & Review Workflow Dialog
    if (selectedSubmission != null) {
        val item = selectedSubmission!!
        val comments = reviewCommentsMap[item.id] ?: emptyList()

        SubmissionDetailDialog(
            submission = item,
            currentUser = currentUser,
            comments = comments,
            auditLogs = auditLogs,
            connections = crossModuleConnections,
            onDismiss = { viewModel.selectSubmission(null) },
            onStatusChange = { newStatus, notes, commentText ->
                viewModel.updateSubmissionStatus(item.id, newStatus, notes, commentText)
            },
            onAddComment = { comment, isCorrection ->
                viewModel.addReviewComment(item.id, comment, isCorrection)
            },
            onDelete = { id ->
                viewModel.deleteSubmission(id)
            },
            onUpdateSubmission = { id, title, desc, evType, loc, tags, refs, isDraft ->
                viewModel.updateSubmission(id, title, desc, evType, loc, tags, refs, isDraft)
            },
            onOpenCrossModuleLink = {
                showCrossModuleLinkDialog = true
            }
        )
    }

    // Specialized Health Surveillance Dialog
    if (showSurveillanceDialog) {
        NewSurveillanceDialog(
            onDismiss = { showSurveillanceDialog = false },
            onSubmit = { cond, synd, reg, cnt, sev, notes ->
                viewModel.createSurveillanceReport(cond, synd, reg, cnt, sev, notes)
            }
        )
    }

    // Specialized Educational Task Dialog
    if (showEducationalTaskDialog) {
        NewEducationalTaskDialog(
            onDismiss = { showEducationalTaskDialog = false },
            onSubmit = { title, subj, desc, conn, objs, prompt, due ->
                viewModel.createEducationalTask(title, subj, desc, conn, objs, prompt, due)
            }
        )
    }

    // Specialized Research Question Dialog
    if (showResearchQuestionDialog) {
        NewResearchQuestionDialog(
            onDismiss = { showResearchQuestionDialog = false },
            onSubmit = { title, desc, disc ->
                viewModel.createResearchQuestion(title, desc, disc)
            }
        )
    }

    // Specialized Community Observation Dialog
    if (showCommunityObservationDialog) {
        NewCommunityObservationDialog(
            onDismiss = { showCommunityObservationDialog = false },
            onSubmit = { title, desc, cat, comm, dist, isPriv ->
                viewModel.createCommunityObservation(title, desc, cat, comm, dist, isPriv)
            }
        )
    }

    // 9-Step Registration & Identity Verification Wizard
    if (showRegisterWizard) {
        RegistrationWizardDialog(
            initialRole = UserRole.STUDENT,
            onDismiss = { showRegisterWizard = false },
            onRequestVerificationCode = { contact, type ->
                viewModel.sendVerificationCode(contact, type)
            },
            onVerifyCode = { code ->
                viewModel.verifyCode(code)
            },
            onCompleteRegistration = { application, password ->
                viewModel.registerWithApplication(application, password)
                showRegisterWizard = false
            }
        )
    }

    // 3-Step Password Recovery Dialog
    if (showForgotPassword) {
        ForgotPasswordDialog(
            onDismiss = { showForgotPassword = false },
            onRequestResetCode = { id -> viewModel.requestPasswordReset(id) },
            onVerifyCode = { code -> viewModel.verifyCode(code) },
            onCompleteReset = { id, newPass -> viewModel.completePasswordReset(id, newPass) }
        )
    }

    // Module Security & Clearance Requirements Inspector
    if (selectedModuleForRequirements != null) {
        val mod = selectedModuleForRequirements!!
        ModuleRequirementsDialog(
            module = mod,
            currentUser = if (isAuthenticated) currentUser else null,
            accessRequirement = viewModel.checkModuleAccess(mod),
            onDismiss = { selectedModuleForRequirements = null },
            onRequestVerification = {
                selectedModuleForRequirements = null
                showRegisterWizard = true
            },
            onSignIn = {
                selectedModuleForRequirements = null
            }
        )
    }

    // Public Help & Support Dialog
    if (showHelpSupport) {
        HelpSupportDialog(
            onDismiss = { showHelpSupport = false }
        )
    }

    // Platform Notifications Tray
    if (showNotificationsTray) {
        NotificationsTrayDialog(
            notifications = notifications,
            onDismissNotification = { id -> viewModel.dismissNotification(id) },
            onClearAll = { viewModel.clearAllNotifications() },
            onClose = { showNotificationsTray = false }
        )
    }

    // Settings Dashboard
    if (showSettingsDashboard) {
        SettingsDashboardDialog(
            viewModel = viewModel,
            currentUser = currentUser,
            onDismiss = { showSettingsDashboard = false },
            onOpenRoleSwitcher = { showRoleSwitcher = true },
            onOpenHelpSupport = { showHelpSupport = true },
            onOpenRegisterWizard = { showRegisterWizard = true },
            onOpenOfflineQueue = { showOfflineSyncQueue = true }
        )
    }

    // Offline Sync Queue Inspector Dialog
    if (showOfflineSyncQueue) {
        OfflineSyncQueueDialog(
            viewModel = viewModel,
            onDismiss = { showOfflineSyncQueue = false }
        )
    }

    // Cross-Module Knowledge Linkage Dialog
    if (showCrossModuleLinkDialog && selectedSubmission != null) {
        val item = selectedSubmission!!
        CrossModuleLinkDialog(
            submission = item,
            currentUser = currentUser,
            onDismiss = { showCrossModuleLinkDialog = false },
            onLinkToEducation = { taskTitle, subject, prompt ->
                viewModel.linkSubmissionToEducationalTask(item.id, taskTitle, subject, prompt)
                showCrossModuleLinkDialog = false
            },
            onLinkToResearch = { questionTitle, discipline, desc ->
                viewModel.linkSubmissionToResearchQuestion(item.id, questionTitle, discipline, desc)
                showCrossModuleLinkDialog = false
            },
            onLinkToHealth = { cond, reg, notes ->
                viewModel.linkSubmissionToSurveillance(item.id, cond, reg, notes)
                showCrossModuleLinkDialog = false
            }
        )
    }
}
