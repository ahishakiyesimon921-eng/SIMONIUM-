package com.example.simonium.data.model

data class SurveillanceReport(
    val id: String,
    val conditionName: String,
    val syndromicCategory: String, // E.g., "Acute Respiratory", "Waterborne / Enteric", "Vector-borne"
    val region: String,
    val caseCount: Int,
    val reporterId: String,
    val reporterName: String,
    val reportingFacility: String,
    val isDeIdentified: Boolean = true,
    val severityLevel: String, // "MILD", "MODERATE", "ELEVATED", "CRITICAL"
    val clinicalNotes: String,
    val verificationStatus: LifecycleStatus,
    val linkedSourceId: String? = null,
    val linkedSourceTitle: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class HealthResearchRequest(
    val id: String,
    val topic: String,
    val targetPathogenOrDisease: String,
    val urgencyLevel: String,
    val submittedBy: String,
    val institution: String,
    val clinicalHypothesis: String,
    val status: LifecycleStatus
)
