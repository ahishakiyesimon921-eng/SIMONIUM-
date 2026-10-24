package com.example.simonium.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SimoniumDao {
    @Query("SELECT * FROM cached_submissions ORDER BY updatedAt DESC")
    fun getAllSubmissions(): Flow<List<SubmissionEntity>>

    @Query("SELECT * FROM cached_submissions WHERE id = :id")
    suspend fun getSubmissionById(id: String): SubmissionEntity?

    @Query("SELECT * FROM cached_submissions WHERE syncStateName = 'PENDING_SYNC' OR syncStateName = 'LOCAL_DRAFT'")
    suspend fun getPendingSyncSubmissions(): List<SubmissionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmissions(items: List<SubmissionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(item: SubmissionEntity)

    @Query("DELETE FROM cached_submissions WHERE id = :id")
    suspend fun deleteSubmission(id: String)

    @Query("SELECT * FROM audit_log_entries ORDER BY timestamp DESC LIMIT 100")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_log_entries WHERE entityId = :entityId ORDER BY timestamp DESC")
    suspend fun getLogsForEntity(entityId: String): List<AuditLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLogs(logs: List<AuditLogEntity>)

    // Verification Applications
    @Query("SELECT * FROM verification_applications ORDER BY updatedAt DESC")
    fun getAllVerificationApplications(): Flow<List<VerificationApplicationEntity>>

    @Query("SELECT * FROM verification_applications WHERE id = :id")
    suspend fun getVerificationApplicationById(id: String): VerificationApplicationEntity?

    @Query("SELECT * FROM verification_applications WHERE userId = :userId")
    suspend fun getVerificationApplicationsForUser(userId: String): List<VerificationApplicationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerificationApplication(app: VerificationApplicationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerificationApplications(apps: List<VerificationApplicationEntity>)

    @Query("DELETE FROM verification_applications WHERE id = :id")
    suspend fun deleteVerificationApplication(id: String)

    // Verification Documents
    @Query("SELECT * FROM verification_documents WHERE applicationId = :appId")
    suspend fun getDocumentsForApplication(appId: String): List<VerificationDocumentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerificationDocuments(docs: List<VerificationDocumentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerificationDocument(doc: VerificationDocumentEntity)
}
