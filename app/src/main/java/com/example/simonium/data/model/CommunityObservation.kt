package com.example.simonium.data.model

data class CommunityObservation(
    val id: String,
    val title: String,
    val description: String,
    val category: String, // "Water & Sanitation", "Local Crop Health", "Air Quality", "Educational Access", "Infrastructure"
    val communityName: String,
    val district: String,
    val submittedBy: String,
    val isAnonymous: Boolean = false,
    val isPrivate: Boolean = false,
    val status: LifecycleStatus,
    val linkedResearchProjectTitle: String? = null,
    val expertResponse: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
