package com.example.simonium.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.simonium.data.model.*

@Entity(tableName = "cached_submissions")
data class SubmissionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val moduleName: String,
    val evidenceTypeName: String,
    val statusName: String,
    val authorId: String,
    val authorName: String,
    val authorRoleName: String,
    val authorInstitution: String?,
    val location: String?,
    val tagsCsv: String,
    val sourceReferencesCsv: String,
    val isConfidential: Boolean,
    val reviewerId: String?,
    val reviewerName: String?,
    val verificationNotes: String?,
    val syncStateName: String,
    val linkedSourceId: String? = null,
    val linkedSourceTitle: String? = null,
    val linkedSourceModuleName: String? = null,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(): SubmissionItem {
        return SubmissionItem(
            id = id,
            title = title,
            description = description,
            module = runCatching { ModuleType.valueOf(moduleName) }.getOrDefault(ModuleType.RESEARCH),
            evidenceType = runCatching { EvidenceType.valueOf(evidenceTypeName) }.getOrDefault(EvidenceType.RAW_OBSERVATION),
            status = runCatching { LifecycleStatus.valueOf(statusName) }.getOrDefault(LifecycleStatus.SUBMITTED),
            authorId = authorId,
            authorName = authorName,
            authorRole = runCatching { UserRole.valueOf(authorRoleName) }.getOrDefault(UserRole.COMMUNITY_MEMBER),
            authorInstitution = authorInstitution,
            location = location,
            tags = if (tagsCsv.isBlank()) emptyList() else tagsCsv.split(","),
            sourceReferences = if (sourceReferencesCsv.isBlank()) emptyList() else sourceReferencesCsv.split(";"),
            isConfidential = isConfidential,
            reviewerId = reviewerId,
            reviewerName = reviewerName,
            verificationNotes = verificationNotes,
            syncState = runCatching { SyncState.valueOf(syncStateName) }.getOrDefault(SyncState.SYNCED),
            linkedSourceId = linkedSourceId,
            linkedSourceTitle = linkedSourceTitle,
            linkedSourceModule = linkedSourceModuleName?.let { runCatching { ModuleType.valueOf(it) }.getOrNull() },
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(item: SubmissionItem): SubmissionEntity {
            return SubmissionEntity(
                id = item.id,
                title = item.title,
                description = item.description,
                moduleName = item.module.name,
                evidenceTypeName = item.evidenceType.name,
                statusName = item.status.name,
                authorId = item.authorId,
                authorName = item.authorName,
                authorRoleName = item.authorRole.name,
                authorInstitution = item.authorInstitution,
                location = item.location,
                tagsCsv = item.tags.joinToString(","),
                sourceReferencesCsv = item.sourceReferences.joinToString(";"),
                isConfidential = item.isConfidential,
                reviewerId = item.reviewerId,
                reviewerName = item.reviewerName,
                verificationNotes = item.verificationNotes,
                syncStateName = item.syncState.name,
                linkedSourceId = item.linkedSourceId,
                linkedSourceTitle = item.linkedSourceTitle,
                linkedSourceModuleName = item.linkedSourceModule?.name,
                createdAt = item.createdAt,
                updatedAt = item.updatedAt
            )
        }
    }
}

@Entity(tableName = "audit_log_entries")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val entityId: String,
    val entityType: String,
    val actorId: String,
    val actorName: String,
    val actorRoleName: String,
    val action: String,
    val details: String,
    val timestamp: Long
)

@Entity(tableName = "verification_applications")
data class VerificationApplicationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userFullName: String,
    val userEmail: String,
    val requestedRoleName: String,
    val requestedModulesCsv: String,
    val statusName: String,
    val institutionName: String,
    val detailsJson: String,
    val reviewerId: String?,
    val reviewerName: String?,
    val reviewerComments: String?,
    val submittedAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "verification_documents")
data class VerificationDocumentEntity(
    @PrimaryKey val id: String,
    val applicationId: String,
    val userId: String,
    val documentType: String,
    val fileName: String,
    val fileSizeKb: Int,
    val issuingAuthority: String,
    val documentNumber: String,
    val expiryDate: String?,
    val uploadTimestamp: Long,
    val isVerified: Boolean,
    val reviewerNotes: String?
)
