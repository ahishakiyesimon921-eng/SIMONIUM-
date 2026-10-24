package com.example.simonium.data.util

data class CountryInfo(
    val isoCode: String,
    val name: String,
    val callingCode: String,
    val flagEmoji: String,
    val phonePatternRegex: String,
    val sampleFormat: String
)

object CountryRegistry {
    val countries: List<CountryInfo> = listOf(
        CountryInfo("RW", "Rwanda", "+250", "🇷🇼", "^\\+?250\\s?7[2389]\\d{7}$", "+250 788 123 456"),
        CountryInfo("KE", "Kenya", "+254", "🇰🇪", "^\\+?254\\s?7\\d{8}$", "+254 712 345 678"),
        CountryInfo("UG", "Uganda", "+256", "🇺🇬", "^\\+?256\\s?7\\d{8}$", "+256 772 123 456"),
        CountryInfo("TZ", "Tanzania", "+255", "🇹🇿", "^\\+?255\\s?7\\d{8}$", "+255 754 123 456"),
        CountryInfo("CD", "DR Congo", "+243", "🇨🇩", "^\\+?243\\s?[89]\\d{8}$", "+243 812 345 678"),
        CountryInfo("BI", "Burundi", "+257", "🇧🇮", "^\\+?257\\s?[267]\\d{7}$", "+257 791 234 56"),
        CountryInfo("US", "United States", "+1", "🇺🇸", "^\\+?1\\s?\\d{10}$", "+1 202 555 0143"),
        CountryInfo("CA", "Canada", "+1", "🇨🇦", "^\\+?1\\s?\\d{10}$", "+1 416 555 0192"),
        CountryInfo("GB", "United Kingdom", "+44", "🇬🇧", "^\\+?44\\s?7\\d{9}$", "+44 7911 123456"),
        CountryInfo("FR", "France", "+33", "🇫🇷", "^\\+?33\\s?[67]\\d{8}$", "+33 6 12 34 56 78"),
        CountryInfo("DE", "Germany", "+49", "🇩🇪", "^\\+?49\\s?1[567]\\d{8,9}$", "+49 151 23456789"),
        CountryInfo("ZA", "South Africa", "+27", "🇿🇦", "^\\+?27\\s?[678]\\d{8}$", "+27 82 123 4567"),
        CountryInfo("NG", "Nigeria", "+234", "🇳🇬", "^\\+?234\\s?[789][01]\\d{8}$", "+234 802 123 4567"),
        CountryInfo("GH", "Ghana", "+233", "🇬🇭", "^\\+?233\\s?[25]\\d{8}$", "+233 24 123 4567"),
        CountryInfo("ET", "Ethiopia", "+251", "🇪🇹", "^\\+?251\\s?9\\d{8}$", "+251 91 123 4567"),
        CountryInfo("IN", "India", "+91", "🇮🇳", "^\\+?91\\s?[6-9]\\d{9}$", "+91 98765 43210"),
        CountryInfo("CN", "China", "+86", "🇨🇳", "^\\+?86\\s?1[3-9]\\d{9}$", "+86 138 0013 8000"),
        CountryInfo("JP", "Japan", "+81", "🇯🇵", "^\\+?81\\s?[789]0\\d{8}$", "+81 90 1234 5678")
    )

    fun findByCallingCode(code: String): CountryInfo? {
        val sanitized = if (code.startsWith("+")) code else "+$code"
        return countries.find { it.callingCode == sanitized }
    }

    fun findByIsoCode(iso: String): CountryInfo? {
        return countries.find { it.isoCode.equals(iso, ignoreCase = true) }
    }

    fun validatePhone(countryIso: String, phoneNumber: String): Boolean {
        val country = findByIsoCode(countryIso) ?: return phoneNumber.length >= 7
        val digitsOnly = phoneNumber.replace(Regex("[^0-9+]"), "")
        return runCatching {
            Regex(country.phonePatternRegex).matches(digitsOnly)
        }.getOrDefault(digitsOnly.length in 8..15)
    }
}

object LocalizationManager {
    val supportedLanguages = listOf(
        LanguageOption("EN", "English", "English", "🇬🇧"),
        LanguageOption("RW", "Kinyarwanda", "Ikinyarwanda", "🇷🇼"),
        LanguageOption("FR", "Français", "French", "🇫🇷"),
        LanguageOption("SW", "Kiswahili", "Swahili", "🇹🇿")
    )

    private val translations: Map<String, Map<String, String>> = mapOf(
        "nav_home" to mapOf(
            "EN" to "Home",
            "RW" to "Ahabanza",
            "FR" to "Accueil",
            "SW" to "Nyumbani"
        ),
        "nav_modules" to mapOf(
            "EN" to "Modules",
            "RW" to "Imigabane",
            "FR" to "Modules",
            "SW" to "Moduli"
        ),
        "nav_research" to mapOf(
            "EN" to "Research",
            "RW" to "Ubushakashatsi",
            "FR" to "Recherche",
            "SW" to "Utafiti"
        ),
        "nav_lifecycle" to mapOf(
            "EN" to "Lifecycle",
            "RW" to "Inzira y'Ubumenyi",
            "FR" to "Cycle de Vie",
            "SW" to "Mzunguko"
        ),
        "nav_ai" to mapOf(
            "EN" to "AI Assist",
            "RW" to "Ubufasha bwa AI",
            "FR" to "Assistance IA",
            "SW" to "Msaada wa AI"
        ),
        "nav_profile" to mapOf(
            "EN" to "Profile",
            "RW" to "Umwirondoro",
            "FR" to "Profil",
            "SW" to "Wasifu"
        ),
        "status_draft" to mapOf(
            "EN" to "Draft",
            "RW" to "Umushinga",
            "FR" to "Brouillon",
            "SW" to "Rasimu"
        ),
        "status_submitted" to mapOf(
            "EN" to "Submitted",
            "RW" to "Byoherejwe",
            "FR" to "Soumis",
            "SW" to "Imewasilishwa"
        ),
        "status_under_review" to mapOf(
            "EN" to "Under Review",
            "RW" to "Birimo Gusuzumwa",
            "FR" to "En Révision",
            "SW" to "Inakaguliwa"
        ),
        "status_correction_requested" to mapOf(
            "EN" to "Correction Requested",
            "RW" to "Hakenewe Gukosora",
            "FR" to "Correction Requise",
            "SW" to "Marekebisho Yanahitajika"
        ),
        "status_approved" to mapOf(
            "EN" to "Approved",
            "RW" to "Byemejwe",
            "FR" to "Approuvé",
            "SW" to "Imeidhinishwa"
        ),
        "status_verified" to mapOf(
            "EN" to "Verified",
            "RW" to "Byagenzuwe",
            "FR" to "Vérifié",
            "SW" to "Imethibitishwa"
        ),
        "btn_save" to mapOf(
            "EN" to "Save",
            "RW" to "Bika",
            "FR" to "Enregistrer",
            "SW" to "Hifadhi"
        ),
        "btn_cancel" to mapOf(
            "EN" to "Cancel",
            "RW" to "Hagarika",
            "FR" to "Annuler",
            "SW" to "Ghairi"
        ),
        "btn_back" to mapOf(
            "EN" to "Back",
            "RW" to "Subira Inyuma",
            "FR" to "Retour",
            "SW" to "Rudi"
        ),
        "btn_next" to mapOf(
            "EN" to "Next",
            "RW" to "Komeza",
            "FR" to "Suivant",
            "SW" to "Mbele"
        ),
        "btn_verify" to mapOf(
            "EN" to "Verify",
            "RW" to "Genzura",
            "FR" to "Vérifier",
            "SW" to "Thibitisha"
        ),
        "btn_approve" to mapOf(
            "EN" to "Approve",
            "RW" to "Emeza",
            "FR" to "Approuver",
            "SW" to "Idhinisha"
        ),
        "btn_resubmit" to mapOf(
            "EN" to "Resubmit",
            "RW" to "Ongera Wohereze",
            "FR" to "Resoumettre",
            "SW" to "Wasilisha Tena"
        ),
        "btn_delete" to mapOf(
            "EN" to "Delete",
            "RW" to "Siba",
            "FR" to "Supprimer",
            "SW" to "Futa"
        ),
        "btn_sync" to mapOf(
            "EN" to "Sync Now",
            "RW" to "Guhuza Nonaha",
            "FR" to "Synchroniser",
            "SW" to "Sawazisha Sasa"
        ),
        "btn_sign_out" to mapOf(
            "EN" to "Sign Out",
            "RW" to "Sohoka",
            "FR" to "Déconnexion",
            "SW" to "Ondoka"
        ),
        "tagline" to mapOf(
            "EN" to "Connecting Knowledge. Enabling Discovery.",
            "RW" to "Guhuza Ubumenyi. Gufasha Ubuvumbuzi.",
            "FR" to "Connecter le Savoir. Permettre la Découverte.",
            "SW" to "Kuunganisha Maarifa. Kuwezesha Ugunduzi."
        )
    )

    fun getString(key: String, languageCode: String): String {
        val entry = translations[key] ?: return key
        return entry[languageCode] ?: entry["EN"] ?: key
    }
}

data class LanguageOption(
    val code: String,
    val name: String,
    val nativeName: String,
    val flagEmoji: String
)
