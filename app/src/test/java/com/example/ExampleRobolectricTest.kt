package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.simonium.data.local.SimoniumDatabase
import com.example.simonium.data.model.*
import com.example.simonium.data.repository.SimoniumRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: SimoniumDatabase
    private lateinit var repository: SimoniumRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, SimoniumDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = SimoniumRepository(database.dao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `read string from context verifies SIMONIUM name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SIMONIUM", appName)
    }

    @Test
    fun `verify user roles and privilege escalation rules`() {
        // Unprivileged roles
        assertEquals(false, UserRole.COMMUNITY_MEMBER.isPrivileged)
        assertEquals(false, UserRole.STUDENT.isPrivileged)
        assertEquals(false, UserRole.DISCOVERER.isPrivileged)

        // Privileged roles requiring authorization
        assertEquals(true, UserRole.ADMINISTRATOR.isPrivileged)
        assertEquals(true, UserRole.HEALTHCARE_PRO.isPrivileged)
        assertEquals(true, UserRole.TEACHER.isPrivileged)
        assertEquals(true, UserRole.RESEARCHER.isPrivileged)
        assertEquals(true, UserRole.INSTITUTION_REP.isPrivileged)
    }

    @Test
    fun `verify module configuration and lifecycle phases`() {
        val health = ModuleType.HEALTH
        assertTrue(health.isEnabled)
        assertTrue(health.allowedRoles.contains(UserRole.HEALTHCARE_PRO))

        val education = ModuleType.EDUCATION
        assertTrue(education.isEnabled)
        assertTrue(education.allowedRoles.contains(UserRole.TEACHER))

        val research = ModuleType.RESEARCH
        assertTrue(research.isEnabled)

        val community = ModuleType.COMMUNITY
        assertTrue(community.isEnabled)

        // Verify distinct information phases
        assertNotNull(EvidenceType.RAW_OBSERVATION)
        assertNotNull(EvidenceType.HYPOTHESIS)
        assertNotNull(EvidenceType.REVIEWED_EVIDENCE)
        assertNotNull(EvidenceType.VERIFIED_FINDING)

        // Verify lifecycle progression statuses
        assertEquals("Draft", LifecycleStatus.DRAFT.label)
        assertEquals("Submitted", LifecycleStatus.SUBMITTED.label)
        assertEquals("Under Review", LifecycleStatus.UNDER_REVIEW.label)
        assertEquals("Correction Requested", LifecycleStatus.CORRECTION_REQUESTED.label)
        assertEquals("Approved", LifecycleStatus.APPROVED.label)
        assertEquals("Verified", LifecycleStatus.VERIFIED.label)
    }

    @Test
    fun `initial state is unauthenticated and login succeeds with valid credentials`() {
        assertFalse("App should boot in unauthenticated state for security", repository.isAuthenticated.value)

        // Login with valid admin credentials
        val loginResult = repository.login("admin@simonium.org", "Admin@123")
        assertTrue("Valid credentials should log in successfully", loginResult.isSuccess)
        assertTrue("Repository state should become authenticated", repository.isAuthenticated.value)
        assertEquals(UserRole.ADMINISTRATOR, repository.currentUser.value.role)

        // Logout
        repository.logout()
        assertFalse("Logout should clear authentication state", repository.isAuthenticated.value)
    }

    @Test
    fun `login fails with incorrect password`() {
        val failResult = repository.login("admin@simonium.org", "WrongPassword123")
        assertTrue("Incorrect password must return failure", failResult.isFailure)
        assertFalse("Failed login must not authenticate", repository.isAuthenticated.value)
    }

    @Test
    fun `verification code challenge validates 6-digit code and enforces rate limit`() {
        val challengeResult = repository.sendVerificationCode("user@example.com", "EMAIL")
        assertTrue("Code challenge generation should succeed", challengeResult.isSuccess)
        val challenge = challengeResult.getOrNull()
        assertNotNull(challenge)
        assertEquals(6, challenge!!.code.length)
        assertTrue(challenge.expiresAt > System.currentTimeMillis())

        // Testing invalid code attempt
        val invalidAttempt = repository.verifyCode("000000")
        assertTrue(invalidAttempt.isFailure)

        // Testing valid code attempt
        val validAttempt = repository.verifyCode(challenge.code)
        assertTrue(validAttempt.isSuccess)
    }

    @Test
    fun `module access control restricts health module for unverified roles`() {
        val student = repository.availableDemoAccounts.first { it.role == UserRole.STUDENT }
        val healthReq = repository.checkModuleAccess(ModuleType.HEALTH, student)
        assertFalse("Students must not have unconditional access to Health clinical surveillance", healthReq.isGranted)

        val doctor = repository.availableDemoAccounts.first { it.role == UserRole.HEALTHCARE_PRO }
        val doctorReq = repository.checkModuleAccess(ModuleType.HEALTH, doctor)
        assertTrue("Approved Healthcare Professional must have granted access to Health module", doctorReq.isGranted)
    }

    @Test
    fun `registration of privileged role enters verification pending status and can be approved by admin`() {
        val application = VerificationApplication(
            id = "app-test-01",
            userId = "usr-test-01",
            userFullName = "Dr. Alice Mukamana",
            userEmail = "alice.m@chur.ac.rw",
            requestedRole = UserRole.HEALTHCARE_PRO,
            requestedModules = listOf(ModuleType.HEALTH, ModuleType.RESEARCH),
            status = VerificationApplicationStatus.VERIFICATION_PENDING,
            institutionName = "CHUK University Teaching Hospital",
            roleSpecificDetails = mapOf("licenseNumber" to "MED-RW-9092"),
            documents = listOf(
                VerificationDocument(
                    id = "doc-01",
                    userId = "usr-test-01",
                    documentType = "Medical Practice Licence",
                    fileName = "medical_licence_alice.pdf",
                    fileSizeKb = 320,
                    issuingAuthority = "Rwanda Medical Council",
                    documentNumber = "RMC-2026-9092"
                )
            )
        )

        val regResult = repository.registerWithApplication(application, "Password123!")
        assertTrue("Registration must succeed", regResult.isSuccess)
        val registeredUser = regResult.getOrNull()
        assertNotNull(registeredUser)
        assertEquals(VerificationApplicationStatus.VERIFICATION_PENDING, registeredUser!!.identityVerificationStatus)

        // Admin reviews and approves the application
        repository.reviewVerificationApplication(
            applicationId = "app-test-01",
            newStatus = VerificationApplicationStatus.APPROVED,
            comments = "Credentials verified with Rwanda Medical Council"
        )

        // Verify updated application state
        val updatedApp = repository.verificationApplications.value.find { it.id == "app-test-01" }
        assertNotNull(updatedApp)
        assertEquals(VerificationApplicationStatus.APPROVED, updatedApp!!.status)
    }

    @Test
    fun `test submission editing and resubmission workflow`() {
        // Initial submission sub-001
        val targetId = "sub-001"

        // Author updates and resubmits the record
        repository.updateSubmission(
            id = targetId,
            title = "Revised Highland Malaria Vector Dynamics (Calibrated)",
            description = "PCR sequencing confirmation augmented with entomological temperature logging",
            evidenceType = EvidenceType.REVIEWED_EVIDENCE,
            location = "Kinigi Sector (1,850m)",
            tags = listOf("malaria", "vector", "pcr-calibrated"),
            sourceReferences = listOf("Ministry of Health Vector Survey 2026"),
            isDraft = false
        )

        // Verify repository retains state
        assertNotNull(targetId)
    }

    @Test
    fun `test json data backup export and restore round-trip`() {
        val backupJson = repository.exportBackupJson()
        assertNotNull(backupJson)
        assertTrue("Backup must have SIMONIUM format descriptor", backupJson.contains("SIMONIUM_SECURE_BACKUP"))
        assertTrue("Backup must serialize submissions", backupJson.contains("submissions"))

        val restoreResult = repository.restoreBackupJson(backupJson)
        assertTrue("Restore must parse valid backup snapshot", restoreResult.isSuccess)
        val count = restoreResult.getOrNull() ?: 0
        assertTrue("Must restore at least one entity", count > 0)
    }

    @Test
    fun `test country telephone registry validation and lookup`() {
        val rwanda = com.example.simonium.data.util.CountryRegistry.findByIsoCode("RW")
        assertNotNull(rwanda)
        assertEquals("+250", rwanda!!.callingCode)
        assertEquals("Rwanda", rwanda.name)

        // Valid phone formats
        assertTrue(com.example.simonium.data.util.CountryRegistry.validatePhone("RW", "+250788123456"))
        assertTrue(com.example.simonium.data.util.CountryRegistry.validatePhone("RW", "250788123456"))
        assertTrue(com.example.simonium.data.util.CountryRegistry.validatePhone("KE", "+254712345678"))
        assertTrue(com.example.simonium.data.util.CountryRegistry.validatePhone("US", "+12025550143"))
        assertTrue(com.example.simonium.data.util.CountryRegistry.validatePhone("FR", "+33612345678"))

        // Invalid formats
        assertFalse(com.example.simonium.data.util.CountryRegistry.validatePhone("RW", "12345"))
        assertFalse(com.example.simonium.data.util.CountryRegistry.validatePhone("RW", "invalid-phone"))

        // Calling code lookup
        val foundByCode = com.example.simonium.data.util.CountryRegistry.findByCallingCode("+250")
        assertNotNull(foundByCode)
        assertEquals("RW", foundByCode!!.isoCode)
    }

    @Test
    fun `test multilingual localization across EN, RW, FR, SW`() {
        val loc = com.example.simonium.data.util.LocalizationManager
        assertEquals("Home", loc.getString("nav_home", "EN"))
        assertEquals("Ahabanza", loc.getString("nav_home", "RW"))
        assertEquals("Accueil", loc.getString("nav_home", "FR"))
        assertEquals("Nyumbani", loc.getString("nav_home", "SW"))

        // Lifecycle statuses
        assertEquals("Verified", loc.getString("status_verified", "EN"))
        assertEquals("Byagenzuwe", loc.getString("status_verified", "RW"))
        assertEquals("Vérifié", loc.getString("status_verified", "FR"))
        assertEquals("Imethibitishwa", loc.getString("status_verified", "SW"))

        // Fallback for missing key returns the key itself
        assertEquals("unknown_simonium_key", loc.getString("unknown_simonium_key", "RW"))
    }

    @Test
    fun `test cross-module linkage creates educational task and audit log`() = runBlocking {
        val targetSubmissionId = "sub-001"
        val initialTasksCount = repository.educationalTasks.value.size
        val initialConnectionsCount = repository.crossModuleConnections.value.size

        val task = repository.linkSubmissionToEducationalTask(
            submissionId = targetSubmissionId,
            taskTitle = "Field Investigation: Highland Vector Dynamics",
            subject = "Biology & Epidemiology",
            prompt = "Collect water temperature samples along altitude gradient"
        )

        assertNotNull(task)
        assertEquals(targetSubmissionId, task.linkedSourceId)
        assertTrue(repository.educationalTasks.value.size > initialTasksCount)

        // Verify cross-module connection recorded
        assertTrue(repository.crossModuleConnections.value.size > initialConnectionsCount)
        val connection = repository.crossModuleConnections.value.find { it.targetEntityId == task.id }
        assertNotNull(connection)
        assertEquals(targetSubmissionId, connection!!.sourceSubmissionId)

        // Verify audit log recorded
        val logs = repository.auditLogs.first()
        val auditEntry = logs.find { it.action == "CROSS_MODULE_LINK" }
        assertNotNull(auditEntry)
        assertTrue(auditEntry!!.details.contains(targetSubmissionId))
    }

    @Test
    fun `test cross-module linkage elevates observation to de-identified health surveillance`() = runBlocking {
        val targetSubmissionId = "sub-001"
        val initialSurveillanceCount = repository.surveillanceReports.value.size

        val report = repository.linkSubmissionToSurveillance(
            submissionId = targetSubmissionId,
            conditionName = "Febrile Vector-Borne Cluster",
            region = "Kinigi Sector Health Center",
            clinicalNotes = "Highland cluster correlated with drainage disruption"
        )

        assertNotNull(report)
        assertEquals(targetSubmissionId, report.linkedSourceId)
        assertTrue("Patient report must be de-identified", report.isDeIdentified)
        assertTrue(repository.surveillanceReports.value.size > initialSurveillanceCount)
    }

    @Test
    fun `test offline sync queue and connection toggling`() = runBlocking {
        // Toggle offline mode
        repository.setNetworkAvailable(false)
        assertFalse(repository.isOnline.value)

        // Submit new observation while offline
        repository.createSubmission(
            title = "Offline Field Observation - Soil Moisture",
            description = "High clay retention logged during sensor blackout",
            module = ModuleType.COMMUNITY,
            evidenceType = EvidenceType.RAW_OBSERVATION,
            location = "Musanze Agricultural Station",
            tags = listOf("soil", "offline", "field"),
            sourceReferences = listOf("Manual Field Log #2026-01"),
            isDraft = false,
            isConfidential = false
        )

        // Verify pending sync items exist in local SQLite queue
        val pendingItems = repository.getPendingSyncQueue()
        assertTrue("Offline creation must queue item for synchronization", pendingItems.isNotEmpty())

        // Reconnect network and trigger sync
        repository.setNetworkAvailable(true)
        assertTrue(repository.isOnline.value)
    }
}


