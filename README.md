# SIMONIUM Platform

> **Scientific Information Management, Organization, Network, Intelligence and Unified Multidisciplinary Platform**
> *Slogan: Connecting Knowledge. Enabling Discovery.*

---

## 1. Vision & Purpose

**SIMONIUM** connects communities, students, educators, researchers, healthcare professionals, discoverers, and institutions through a unified multidisciplinary scientific platform. 

Instead of fragmented silos across schools, hospitals, social media, and static document folders, SIMONIUM provides an empirical, structured environment where:
- Real-world problems and community observations become research questions.
- Teachers translate scientific inquiries into practical student fieldwork.
- Students and citizens collect observations with calibrated controls.
- Peer reviewers, educators, and clinicians verify evidence through an audit-backed lifecycle.
- Researchers publish source-linked, reproducible scientific discoveries.
- Patient confidentiality and sensitive data are strictly protected.

---

## 2. Core Modules Architecture

### A. Health & Surveillance
- **Disease Surveillance**: Real-time syndromic reporting (e.g. acute diarrhea clusters, malaria shifts) from district clinics and health centers.
- **Patient Confidentiality**: Strict de-identification policy enforced at the database (RLS) and UI levels. No PII is exposed.
- **Medical Safety Notice**: AI and research suggestions are explicitly identified as scientific hypotheses and never replace certified medical practitioner diagnoses.

### B. Education & Academia
- **Research-Linked Fieldwork**: Practical STEM tasks adapted from real university and institutional research projects.
- **Inquiry Activities**: Field testing (e.g., river water turbidity assays, botanical identification of *Artemisia* species).
- **Teacher Review Workflow**: Dedicated evaluation queue, rubric scoring, correction requests, and progress tracking.

### C. Research & Discovery
- **Central Scientific Collaboration Hub**: Formulate questions, define methodologies, track project milestones, and record verified discoveries.
- **Unbroken Scientific Pipeline**:
  `1. Question Formulated` ➔ `2. Inquiry Activity Curated` ➔ `3. Field Observation Collected` ➔ `4. Peer Review & Validation` ➔ `5. Verified Scientific Discovery`
- **Distinction of Evidence Levels**:
  - `Raw Observation`: Unverified sensor/field readings.
  - `Scientific Hypothesis`: Theoretical causal proposition.
  - `Reviewed Evidence`: Calibrated data examined by teachers/peers.
  - `Verified Finding`: Independently validated, reproducible conclusion.

### D. Community Knowledge & Citizen Science
- **Local Ground-Truth**: Citizen reports on water siltation, crop pest outbreaks, and environmental hazards.
- **Privacy Controls**: Public vs. Confidential submission settings.
- **Expert Response**: Direct communication channels between university researchers and local cooperatives.

### E. Extensible Future Modules
- Environmental Science & Catchment Resilience
- Agro-Sciences & Soil Biology
- Biotechnology & Molecular Assays

---

## 3. User Roles & Server-Side Permissions

SIMONIUM enforces role-based access control (RBAC). In real-world operation, privileged roles require server-side institutional verification:

| Role | Responsibilities | Privileged Status |
|---|---|---|
| **Community Member** | File local observations, community alerts | Unprivileged |
| **Student** | Access learning tasks, submit fieldwork data | Unprivileged |
| **Teacher / Educator** | Create tasks, review student submissions, request corrections | Privileged (Verified) |
| **Scientist / Researcher** | Formulate hypotheses, verify evidence, publish findings | Privileged (Verified) |
| **Discoverer / Innovator** | Cross-disciplinary linkages, breakthrough proposals | Unprivileged |
| **Doctor / Healthcare Pro** | Disease surveillance reporting, clinical validation | Privileged (Verified) |
| **Institution Representative** | Organizational governance, resource management | Privileged (Verified) |
| **Platform Administrator** | System governance, RLS audit oversight, role approval | Privileged (Verified) |

---

## 4. Information Submission & Verification Lifecycle

Every record follows an immutable, auditable state machine:
```
Draft ➔ Submitted ➔ Under Review ➔ [Correction Requested OR Approved] ➔ [Verified OR Rejected] ➔ Archived
```
- **Auditing**: Every transition records `Actor ID`, `Actor Name`, `Actor Role`, `Action`, `Details`, and `Timestamp`.
- **Reviewer Rules**: Only authorized roles (`Teacher`, `Healthcare Pro`, `Researcher`, `Administrator`) can execute approvals or independent scientific verifications.

---

## 5. Database & Supabase Backend Setup

The platform is designed for **Supabase with PostgreSQL** and Row-Level Security (RLS). The complete schema is packaged at:
`app/src/main/assets/supabase_schema.sql`

### How to Provision Supabase:
1. Create a new project in your [Supabase Dashboard](https://supabase.com).
2. Open the **SQL Editor** tab.
3. Paste the contents of `app/src/main/assets/supabase_schema.sql` and click **Run**.
4. All 11 tables, enum types, indexes, and RLS policies (`profiles`, `submissions`, `health_surveillance_reports`, `audit_logs`) will be automatically created with zero-trust security.

---

## 6. Offline-First Architecture

- **Local Persistence**: Powered by Android Jetpack Room (`SimoniumDatabase` & `SimoniumDao`).
- **Sync State Tracking**: Submissions created offline receive `PENDING_SYNC` status.
- **Automatic / Manual Sync**: When internet connectivity returns, the repository queues and synchronizes local records with the cloud backend.
- **Backup & Restore**: Export encrypted JSON snapshots of the local database or verify schema integrity directly from the Profile screen.

---

## 7. AI Scientific Intelligence

- Dual-language support (**English** & **Kinyarwanda**).
- Explains complex scientific phenomena (epidemiological shifts, hydrological assays, biomaterials).
- Recommends testable hypotheses and interdisciplinary linkages between community observations and laboratory research.
- Prominent safety disclaimer: AI suggestions assist inquiry and must be verified by human scientists.

---

## 8. Android Build & Release Instructions

### Build Prerequisites
- JDK 17 or higher
- Android SDK 36 (minSdk 24)
- Gradle 9.1.1 with Kotlin 2.2.10

### Command Line Build

1. **Compile & Run Unit / Robolectric Tests**:
   ```bash
   gradle test
   ```

2. **Assemble Debug APK**:
   ```bash
   gradle assembleDebug
   ```
   *Output APK location*: `app/build/outputs/apk/debug/app-debug.apk`

3. **Assemble Release Android App Bundle (AAB)**:
   ```bash
   gradle bundleRelease
   ```
   *Output AAB location*: `app/build/outputs/bundle/release/app-release.aab`
