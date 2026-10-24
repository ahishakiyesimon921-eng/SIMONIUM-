-- =========================================================================
-- SIMONIUM PLATFORM — FULL SUPABASE & POSTGRESQL PRODUCTION SCHEMA
-- Scientific Information Management, Organization, Network, Intelligence
-- and Unified Multidisciplinary Platform
-- Slogan: Connecting Knowledge. Enabling Discovery.
-- =========================================================================

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 1. INSTITUTIONS TABLE
CREATE TABLE IF NOT EXISTS public.institutions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    type TEXT NOT NULL, -- 'University', 'Hospital', 'Research_Institute', 'School', 'Ministry', 'NGO'
    location TEXT NOT NULL,
    country TEXT NOT NULL DEFAULT 'Rwanda',
    is_verified BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 2. USER ROLES ENUM
CREATE TYPE public.simonium_role AS ENUM (
    'COMMUNITY_MEMBER',
    'STUDENT',
    'TEACHER',
    'RESEARCHER',
    'DISCOVERER',
    'HEALTHCARE_PRO',
    'INSTITUTION_REP',
    'ADMINISTRATOR'
);

CREATE TYPE public.role_approval_status AS ENUM (
    'ACTIVE',
    'PENDING_APPROVAL',
    'REJECTED',
    'SUSPENDED'
);

-- 3. PROFILES TABLE (Linked with Supabase Auth users)
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    full_name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    role public.simonium_role NOT NULL DEFAULT 'COMMUNITY_MEMBER',
    approval_status public.role_approval_status NOT NULL DEFAULT 'ACTIVE',
    institution_id UUID REFERENCES public.institutions(id) ON DELETE SET NULL,
    institution_name TEXT,
    specialty TEXT,
    preferred_language TEXT NOT NULL DEFAULT 'EN',
    verified_contributions_count INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 4. PLATFORM MODULES REGISTRY
CREATE TABLE IF NOT EXISTS public.platform_modules (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    subtitle TEXT NOT NULL,
    description TEXT NOT NULL,
    is_enabled BOOLEAN DEFAULT TRUE,
    allowed_roles public.simonium_role[] NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Seed initial 4 modules + future expansion slots
INSERT INTO public.platform_modules (id, title, subtitle, description, is_enabled, allowed_roles)
VALUES
    ('health', 'Health & Surveillance', 'Clinical Observations & Disease Surveillance', 'De-identified health reports and pathogen inquiries.', TRUE, ARRAY['HEALTHCARE_PRO', 'RESEARCHER', 'INSTITUTION_REP', 'ADMINISTRATOR', 'COMMUNITY_MEMBER']::public.simonium_role[]),
    ('education', 'Education & Academia', 'Research Learning & Student Fieldwork', 'Classroom tasks connecting students to real research problems.', TRUE, ARRAY['STUDENT', 'TEACHER', 'RESEARCHER', 'ADMINISTRATOR']::public.simonium_role[]),
    ('research', 'Research & Discovery', 'Scientific Questions, Projects & Findings', 'Multi-disciplinary hypotheses and source-verified findings.', TRUE, ARRAY['COMMUNITY_MEMBER', 'STUDENT', 'TEACHER', 'RESEARCHER', 'DISCOVERER', 'HEALTHCARE_PRO', 'INSTITUTION_REP', 'ADMINISTRATOR']::public.simonium_role[]),
    ('community', 'Community Knowledge', 'Local Observations & Citizen Science', 'Ground truth reporting on environment, health, and development.', TRUE, ARRAY['COMMUNITY_MEMBER', 'STUDENT', 'TEACHER', 'RESEARCHER', 'DISCOVERER', 'HEALTHCARE_PRO', 'INSTITUTION_REP', 'ADMINISTRATOR']::public.simonium_role[]),
    ('environment', 'Environmental Science', 'Ecological Monitoring & Water Catchment', 'Water quality, forestry, and climate adaptation metrics.', FALSE, ARRAY['RESEARCHER', 'ADMINISTRATOR']::public.simonium_role[]),
    ('agriculture', 'Agro-Sciences & Food Security', 'Crop Health & Soil Biology', 'Agronomic surveillance and crop pathology.', FALSE, ARRAY['RESEARCHER', 'ADMINISTRATOR']::public.simonium_role[])
ON CONFLICT (id) DO NOTHING;

-- 5. SUBMISSIONS & LIFECYCLE
CREATE TYPE public.lifecycle_status AS ENUM (
    'DRAFT',
    'SUBMITTED',
    'UNDER_REVIEW',
    'CORRECTION_REQUESTED',
    'APPROVED',
    'VERIFIED',
    'REJECTED',
    'ARCHIVED'
);

CREATE TYPE public.evidence_type AS ENUM (
    'RAW_OBSERVATION',
    'HYPOTHESIS',
    'REVIEWED_EVIDENCE',
    'VERIFIED_FINDING'
);

CREATE TABLE IF NOT EXISTS public.submissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    module_id TEXT NOT NULL REFERENCES public.platform_modules(id),
    evidence_type public.evidence_type NOT NULL,
    status public.lifecycle_status NOT NULL DEFAULT 'DRAFT',
    author_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    author_name TEXT NOT NULL,
    author_role public.simonium_role NOT NULL,
    author_institution TEXT,
    location TEXT,
    tags TEXT[] DEFAULT '{}',
    source_references TEXT[] DEFAULT '{}',
    is_confidential BOOLEAN DEFAULT FALSE,
    reviewer_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    reviewer_name TEXT,
    verification_notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 6. RESEARCH PROJECTS & QUESTIONS
CREATE TABLE IF NOT EXISTS public.research_projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    lead_researcher_id UUID REFERENCES public.profiles(id),
    lead_researcher_name TEXT NOT NULL,
    lead_institution TEXT NOT NULL,
    field TEXT NOT NULL,
    objectives TEXT[] NOT NULL DEFAULT '{}',
    expected_outcomes TEXT NOT NULL,
    methodology TEXT NOT NULL,
    progress_percent INT DEFAULT 0 CHECK (progress_percent BETWEEN 0 AND 100),
    status TEXT NOT NULL DEFAULT 'IN_PROGRESS',
    linked_question_ids UUID[] DEFAULT '{}',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.research_questions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    discipline TEXT NOT NULL,
    proposed_by_id UUID REFERENCES public.profiles(id),
    proposed_by_name TEXT NOT NULL,
    author_role public.simonium_role NOT NULL,
    status TEXT NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 7. EDUCATIONAL TASKS & ASSIGNMENTS
CREATE TABLE IF NOT EXISTS public.educational_tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    subject TEXT NOT NULL,
    teacher_id UUID REFERENCES public.profiles(id),
    teacher_name TEXT NOT NULL,
    teacher_institution TEXT NOT NULL,
    description TEXT NOT NULL,
    research_connection TEXT NOT NULL,
    learning_objectives TEXT[] NOT NULL DEFAULT '{}',
    practical_activity_prompt TEXT NOT NULL,
    due_date TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 8. HEALTH SURVEILLANCE REPORTS (Strict Patient De-identification)
CREATE TABLE IF NOT EXISTS public.health_surveillance_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    condition_name TEXT NOT NULL,
    syndromic_category TEXT NOT NULL,
    region TEXT NOT NULL,
    case_count INT NOT NULL DEFAULT 1 CHECK (case_count >= 1),
    reporter_id UUID REFERENCES public.profiles(id),
    reporter_name TEXT NOT NULL,
    reporting_facility TEXT NOT NULL,
    is_de_identified BOOLEAN NOT NULL DEFAULT TRUE CHECK (is_de_identified = TRUE),
    severity_level TEXT NOT NULL DEFAULT 'MODERATE', -- 'MILD', 'MODERATE', 'ELEVATED', 'CRITICAL'
    clinical_notes TEXT NOT NULL,
    verification_status public.lifecycle_status NOT NULL DEFAULT 'SUBMITTED',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 9. COMMUNITY OBSERVATIONS
CREATE TABLE IF NOT EXISTS public.community_observations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    category TEXT NOT NULL,
    community_name TEXT NOT NULL,
    district TEXT NOT NULL,
    submitted_by_id UUID REFERENCES public.profiles(id),
    submitted_by_name TEXT NOT NULL,
    is_private BOOLEAN DEFAULT FALSE,
    status public.lifecycle_status NOT NULL DEFAULT 'SUBMITTED',
    expert_response TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 10. REVIEWS & COMMENTS
CREATE TABLE IF NOT EXISTS public.review_comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    submission_id UUID NOT NULL REFERENCES public.submissions(id) ON DELETE CASCADE,
    author_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    author_name TEXT NOT NULL,
    author_role public.simonium_role NOT NULL,
    comment TEXT NOT NULL,
    is_correction_request BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 11. AUDIT LOGS (Immutable Security Trail)
CREATE TABLE IF NOT EXISTS public.audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_id TEXT NOT NULL,
    entity_type TEXT NOT NULL,
    actor_id TEXT NOT NULL,
    actor_name TEXT NOT NULL,
    actor_role TEXT NOT NULL,
    action TEXT NOT NULL,
    details TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 12. VERIFICATION APPLICATIONS (Identity & Credential Vetting)
CREATE TYPE public.verification_app_status AS ENUM (
    'DRAFT',
    'SUBMITTED',
    'VERIFICATION_PENDING',
    'UNDER_REVIEW',
    'APPROVED',
    'ADDITIONAL_INFO_REQUESTED',
    'REJECTED',
    'REVOKED'
);

CREATE TABLE IF NOT EXISTS public.verification_applications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    user_full_name TEXT NOT NULL,
    user_email TEXT NOT NULL,
    requested_role public.simonium_role NOT NULL,
    requested_modules TEXT[] NOT NULL,
    status public.verification_app_status NOT NULL DEFAULT 'VERIFICATION_PENDING',
    institution_name TEXT NOT NULL,
    details JSONB NOT NULL DEFAULT '{}'::jsonb,
    reviewer_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    reviewer_name TEXT,
    reviewer_comments TEXT,
    submitted_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 13. VERIFICATION DOCUMENTS (Institutional & Professional Credentials)
CREATE TABLE IF NOT EXISTS public.verification_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id UUID NOT NULL REFERENCES public.verification_applications(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    document_type TEXT NOT NULL,
    file_name TEXT NOT NULL,
    file_size_kb INT NOT NULL,
    issuing_authority TEXT NOT NULL,
    document_number TEXT NOT NULL,
    expiry_date DATE,
    upload_timestamp TIMESTAMPTZ DEFAULT NOW(),
    is_verified BOOLEAN DEFAULT FALSE,
    reviewer_notes TEXT
);

-- =========================================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- =========================================================================

ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.submissions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.health_surveillance_reports ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.community_observations ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.audit_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.verification_applications ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.verification_documents ENABLE ROW LEVEL SECURITY;

-- Profiles: Anyone can view basic profile info; only self or admin can update
CREATE POLICY "Public profiles are viewable by authenticated users"
ON public.profiles FOR SELECT TO authenticated USING (true);

CREATE POLICY "Users can update own profile"
ON public.profiles FOR UPDATE TO authenticated USING (auth.uid() = id);

-- Submissions:
-- Authors can manage their own drafts.
-- Non-confidential submitted/approved items are viewable by authenticated users.
-- Confidential items only accessible by authors, reviewers, and admins.
CREATE POLICY "View published submissions"
ON public.submissions FOR SELECT TO authenticated
USING (
    is_confidential = FALSE
    OR author_id = auth.uid()
    OR (SELECT role FROM public.profiles WHERE id = auth.uid()) IN ('RESEARCHER', 'HEALTHCARE_PRO', 'ADMINISTRATOR')
);

CREATE POLICY "Insert own submissions"
ON public.submissions FOR INSERT TO authenticated
WITH CHECK (author_id = auth.uid());

CREATE POLICY "Reviewers and Admins can update submission status"
ON public.submissions FOR UPDATE TO authenticated
USING (
    author_id = auth.uid()
    OR (SELECT role FROM public.profiles WHERE id = auth.uid()) IN ('TEACHER', 'RESEARCHER', 'HEALTHCARE_PRO', 'ADMINISTRATOR')
);

-- Health Surveillance: Only health professionals, researchers and admins can view
CREATE POLICY "Authorized view of health surveillance"
ON public.health_surveillance_reports FOR SELECT TO authenticated
USING (
    (SELECT role FROM public.profiles WHERE id = auth.uid()) IN ('HEALTHCARE_PRO', 'RESEARCHER', 'INSTITUTION_REP', 'ADMINISTRATOR')
);

-- Verification Applications & Documents: User can view/insert their own; Admins can review all
CREATE POLICY "Users can view own verification applications"
ON public.verification_applications FOR SELECT TO authenticated
USING (user_id = auth.uid() OR (SELECT role FROM public.profiles WHERE id = auth.uid()) = 'ADMINISTRATOR');

CREATE POLICY "Users can insert own verification application"
ON public.verification_applications FOR INSERT TO authenticated
WITH CHECK (user_id = auth.uid());

CREATE POLICY "Admins can update verification applications"
ON public.verification_applications FOR UPDATE TO authenticated
USING ((SELECT role FROM public.profiles WHERE id = auth.uid()) = 'ADMINISTRATOR');

CREATE POLICY "Users can view own verification documents"
ON public.verification_documents FOR SELECT TO authenticated
USING (user_id = auth.uid() OR (SELECT role FROM public.profiles WHERE id = auth.uid()) = 'ADMINISTRATOR');

CREATE POLICY "Users can insert own verification documents"
ON public.verification_documents FOR INSERT TO authenticated
WITH CHECK (user_id = auth.uid());

-- Audit logs: Read-only for admins and researchers; system inserts only
CREATE POLICY "Admins can view audit logs"
ON public.audit_logs FOR SELECT TO authenticated
USING (
    (SELECT role FROM public.profiles WHERE id = auth.uid()) = 'ADMINISTRATOR'
);

