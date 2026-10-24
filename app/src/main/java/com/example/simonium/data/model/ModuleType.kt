package com.example.simonium.data.model

enum class ModuleType(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val isEnabled: Boolean,
    val allowedRoles: Set<UserRole> = UserRole.values().toSet()
) {
    HEALTH(
        id = "health",
        title = "Health & Surveillance",
        subtitle = "Clinical Observations & Disease Surveillance",
        description = "De-identified health observations, public health alerts, clinical research requests, and medicine surveillance.",
        isEnabled = true,
        allowedRoles = setOf(
            UserRole.HEALTHCARE_PRO,
            UserRole.RESEARCHER,
            UserRole.INSTITUTION_REP,
            UserRole.ADMINISTRATOR,
            UserRole.COMMUNITY_MEMBER
        )
    ),
    EDUCATION(
        id = "education",
        title = "Education & Academia",
        subtitle = "Research Learning & Student Fieldwork",
        description = "Curated learning resources, research-guided assignments, student fieldwork, and teacher evaluation workflows.",
        isEnabled = true,
        allowedRoles = setOf(
            UserRole.STUDENT,
            UserRole.TEACHER,
            UserRole.RESEARCHER,
            UserRole.ADMINISTRATOR
        )
    ),
    RESEARCH(
        id = "research",
        title = "Research & Discovery",
        subtitle = "Scientific Questions, Projects & Verified Findings",
        description = "Multi-disciplinary hypothesis formulation, evidence aggregation, peer reviews, and source-linked scientific discoveries.",
        isEnabled = true,
        allowedRoles = UserRole.values().toSet()
    ),
    COMMUNITY(
        id = "community",
        title = "Community Knowledge",
        subtitle = "Local Observations & Citizen Science",
        description = "Community environmental observations, local challenges, citizen reports, and direct links to scientific projects.",
        isEnabled = true,
        allowedRoles = UserRole.values().toSet()
    ),
    // Extensible future modules
    ENVIRONMENTAL_SCIENCE(
        id = "environment",
        title = "Environmental Science",
        subtitle = "Ecological Monitoring & Climate Resilience",
        description = "Water catchment data, soil quality, biodiversity metrics, and climate adaptation research.",
        isEnabled = false
    ),
    AGRICULTURE(
        id = "agriculture",
        title = "Agro-Sciences & Food",
        subtitle = "Crop Health, Soil Biology & Nutrition",
        description = "Agronomic observations, pest surveillance, seed genetics, and harvest sustainability.",
        isEnabled = false
    ),
    BIOTECHNOLOGY(
        id = "biotech",
        title = "Biotechnology & Lab Sciences",
        subtitle = "Genomics, Molecular Analysis & Assays",
        description = "Laboratory protocols, sequencing datasets, assay verification, and biomaterial synthesis.",
        isEnabled = false
    )
}
