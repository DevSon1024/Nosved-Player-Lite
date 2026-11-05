package com.devson.nvplayerlite.model

import java.util.Locale

data class LanguageEntry(
    val code: String,
    val displayName: String,
    val nativeName: String = ""
)

object LanguageList {
    val all: List<LanguageEntry> = listOf(
        LanguageEntry("afr", "Afrikaans", "Afrikaans"),
        LanguageEntry("ak", "Akan", "Akan"),
        LanguageEntry("alb", "Albanian", "Shqip"),
        LanguageEntry("amh", "Amharic", "አማርኛ"),
        LanguageEntry("ara", "Arabic", "العربية"),
        LanguageEntry("ar-SA", "Arabic (Saudi Arabia)", "العربية"),
        LanguageEntry("ar-EG", "Arabic (Egypt)", "العربية"),
        LanguageEntry("arm", "Armenian", "Հայերեն"),
        LanguageEntry("asm", "Assamese", "অসমীয়া"),
        LanguageEntry("aze", "Azerbaijani", "Azərbaycan"),
        LanguageEntry("ba", "Bashkir", "Башҡортса"),
        LanguageEntry("baq", "Basque", "Euskara"),
        LanguageEntry("bel", "Belarusian", "Беларуская"),
        LanguageEntry("ben", "Bengali", "বাংলা"),
        LanguageEntry("bos", "Bosnian", "Bosanski"),
        LanguageEntry("br", "Breton", "Brezhoneg"),
        LanguageEntry("bul", "Bulgarian", "Български"),
        LanguageEntry("bur", "Burmese", "မြန်မာစာ"),
        LanguageEntry("cat", "Catalan", "Català"),
        LanguageEntry("ceb", "Cebuano", "Sinugboanon"),
        LanguageEntry("ny", "Chichewa", "Chichewa"),
        LanguageEntry("chi", "Chinese", "中文"),
        LanguageEntry("zh-CN", "Chinese (Simplified)", "简体中文"),
        LanguageEntry("zh-TW", "Chinese (Traditional)", "繁體中文"),
        LanguageEntry("zh-HK", "Chinese (Hong Kong)", "香港中文"),
        LanguageEntry("zho", "Chinese", "中文"),
        LanguageEntry("co", "Corsican", "Corsu"),
        LanguageEntry("hrv", "Croatian", "Hrvatski"),
        LanguageEntry("cze", "Czech", "Čeština"),
        LanguageEntry("dan", "Danish", "Dansk"),
        LanguageEntry("dut", "Dutch", "Nederlands"),
        LanguageEntry("nl-BE", "Dutch (Belgium)", "Nederlands (België)"),
        LanguageEntry("dz", "Dzongkha", "རྫོང་ཁ"),
        LanguageEntry("eng", "English", "English"),
        LanguageEntry("en-US", "English (United States)", "English (US)"),
        LanguageEntry("en-GB", "English (United Kingdom)", "English (UK)"),
        LanguageEntry("en-AU", "English (Australia)", "English (AU)"),
        LanguageEntry("en-CA", "English (Canada)", "English (CA)"),
        LanguageEntry("en-IN", "English (India)", "English (India)"),
        LanguageEntry("en-NZ", "English (New Zealand)", "English (NZ)"),
        LanguageEntry("eo", "Esperanto", "Esperanto"),
        LanguageEntry("est", "Estonian", "Eesti"),
        LanguageEntry("ee", "Ewe", "Eʋegbe"),
        LanguageEntry("fo", "Faroese", "Føroyskt"),
        LanguageEntry("fj", "Fijian", "Vosa Vakaviti"),
        LanguageEntry("fin", "Finnish", "Suomi"),
        LanguageEntry("fre", "French", "Français"),
        LanguageEntry("fr-FR", "French (France)", "Français"),
        LanguageEntry("fr-CA", "French (Canada)", "Français (Canada)"),
        LanguageEntry("fr-BE", "French (Belgium)", "Français (Belgique)"),
        LanguageEntry("fr-CH", "French (Switzerland)", "Français (Suisse)"),
        LanguageEntry("fy", "Frisian", "Frysk"),
        LanguageEntry("ff", "Fulah", "Fulfulde"),
        LanguageEntry("glg", "Galician", "Galego"),
        LanguageEntry("geo", "Georgian", "ქართული"),
        LanguageEntry("ger", "German", "Deutsch"),
        LanguageEntry("de-DE", "German (Germany)", "Deutsch"),
        LanguageEntry("de-AT", "German (Austria)", "Deutsch (Österreich)"),
        LanguageEntry("de-CH", "German (Switzerland)", "Deutsch (Schweiz)"),
        LanguageEntry("gre", "Greek", "Ελληνικά"),
        LanguageEntry("gn", "Guarani", "Avañe'ẽ"),
        LanguageEntry("guj", "Gujarati", "ગુજરાતી"),
        LanguageEntry("ht", "Haitian Creole", "Kreyòl ayisyen"),
        LanguageEntry("hau", "Hausa", "Hausa"),
        LanguageEntry("heb", "Hebrew", "עברית"),
        LanguageEntry("hin", "Hindi", "हिन्दी"),
        LanguageEntry("hmn", "Hmong", "Hmoob"),
        LanguageEntry("hun", "Hungarian", "Magyar"),
        LanguageEntry("ice", "Icelandic", "Íslenska"),
        LanguageEntry("ig", "Igbo", "Asụsụ Igbo"),
        LanguageEntry("ind", "Indonesian", "Bahasa Indonesia"),
        LanguageEntry("gle", "Irish", "Gaeilge"),
        LanguageEntry("ita", "Italian", "Italiano"),
        LanguageEntry("it-IT", "Italian (Italy)", "Italiano"),
        LanguageEntry("it-CH", "Italian (Switzerland)", "Italiano (Svizzera)"),
        LanguageEntry("jpn", "Japanese", "日本語"),
        LanguageEntry("jv", "Javanese", "Basa Jawa"),
        LanguageEntry("kan", "Kannada", "ಕನ್ನಡ"),
        LanguageEntry("kaz", "Kazakh", "Қазақша"),
        LanguageEntry("khm", "Khmer", "ភាសាខ្មែរ"),
        LanguageEntry("ki", "Kikuyu", "Gĩkũyũ"),
        LanguageEntry("rw", "Kinyarwanda", "Ikinyarwanda"),
        LanguageEntry("kor", "Korean", "한국어"),
        LanguageEntry("kur", "Kurdish", "Kurdî"),
        LanguageEntry("kir", "Kyrgyz", "Кыргызча"),
        LanguageEntry("lao", "Lao", "ລາວ"),
        LanguageEntry("lat", "Latin", "Latina"),
        LanguageEntry("lav", "Latvian", "Latviešu"),
        LanguageEntry("ln", "Lingala", "Lingála"),
        LanguageEntry("lit", "Lithuanian", "Lietuvių"),
        LanguageEntry("lb", "Luxembourgish", "Lëtzebuergesch"),
        LanguageEntry("mac", "Macedonian", "Македонски"),
        LanguageEntry("mg", "Malagasy", "Malagasy"),
        LanguageEntry("may", "Malay", "Bahasa Melayu"),
        LanguageEntry("mal", "Malayalam", "മലയാളം"),
        LanguageEntry("mlt", "Maltese", "Malti"),
        LanguageEntry("gv", "Manx", "Gaelg"),
        LanguageEntry("mao", "Maori", "Māori"),
        LanguageEntry("mar", "Marathi", "मराठी"),
        LanguageEntry("mh", "Marshallese", "Kajin M̧ajeļ"),
        LanguageEntry("mon", "Mongolian", "Монгол"),
        LanguageEntry("na", "Nauru", "Dorerin Naoero"),
        LanguageEntry("nv", "Navajo", "Diné bizaad"),
        LanguageEntry("nd", "Ndebele (North)", "isiNdebele"),
        LanguageEntry("nep", "Nepali", "नेपाली"),
        LanguageEntry("ng", "Ndonga", "Oshindonga"),
        LanguageEntry("nb", "Norwegian Bokmål", "Norsk Bokmål"),
        LanguageEntry("nn", "Norwegian Nynorsk", "Norsk Nynorsk"),
        LanguageEntry("nor", "Norwegian", "Norsk"),
        LanguageEntry("ii", "Nuosu", "ꆈꌠ꒿"),
        LanguageEntry("oc", "Occitan", "Occitan"),
        LanguageEntry("ori", "Odia", "ଓଡ଼ିଆ"),
        LanguageEntry("om", "Oromo", "Afaan Oromoo"),
        LanguageEntry("os", "Ossetic", "Ирон"),
        LanguageEntry("pi", "Pali", "Pāli"),
        LanguageEntry("pan", "Punjabi", "ਪੰਜਾਬੀ"),
        LanguageEntry("pa-IN", "Punjabi (India)", "ਪੰਜਾਬੀ"),
        LanguageEntry("pa-PK", "Punjabi (Pakistan)", "پنجابی"),
        LanguageEntry("pus", "Pashto", "پښتو"),
        LanguageEntry("per", "Persian (Farsi)", "فارسی"),
        LanguageEntry("pol", "Polish", "Polski"),
        LanguageEntry("por", "Portuguese", "Português"),
        LanguageEntry("pt-BR", "Portuguese (Brazil)", "Português (Brasil)"),
        LanguageEntry("pt-PT", "Portuguese (Portugal)", "Português (Portugal)"),
        LanguageEntry("qu", "Quechua", "Runa Simi"),
        LanguageEntry("rum", "Romanian", "Română"),
        LanguageEntry("rm", "Romansh", "Rumantsch"),
        LanguageEntry("rn", "Rundi", "Ikirundi"),
        LanguageEntry("rus", "Russian", "Русский"),
        LanguageEntry("se", "Northern Sami", "Davvisámegiella"),
        LanguageEntry("sm", "Samoan", "Gagana fa'a Sāmoa"),
        LanguageEntry("sg", "Sango", "Sängö"),
        LanguageEntry("san", "Sanskrit", "संस्कृतम्"),
        LanguageEntry("scc", "Serbian", "Српски"),
        LanguageEntry("sn", "Shona", "chiShona"),
        LanguageEntry("sd", "Sindhi", "سنڌي"),
        LanguageEntry("sin", "Sinhala", "සිංහල"),
        LanguageEntry("slo", "Slovak", "Slovenčina"),
        LanguageEntry("slv", "Slovenian", "Slovenščina"),
        LanguageEntry("som", "Somali", "Soomaaliga"),
        LanguageEntry("nr", "South Ndebele", "isiNdebele"),
        LanguageEntry("st", "Southern Sotho", "Sesotho"),
        LanguageEntry("spa", "Spanish", "Español"),
        LanguageEntry("es-ES", "Spanish (Spain)", "Español"),
        LanguageEntry("es-MX", "Spanish (Mexico)", "Español (México)"),
        LanguageEntry("es-AR", "Spanish (Argentina)", "Español (Argentina)"),
        LanguageEntry("es-CO", "Spanish (Colombia)", "Español (Colombia)"),
        LanguageEntry("es-419", "Spanish (Latin America)", "Español"),
        LanguageEntry("su", "Sundanese", "Basa Sunda"),
        LanguageEntry("swa", "Swahili", "Kiswahili"),
        LanguageEntry("ss", "Swati", "SiSwati"),
        LanguageEntry("swe", "Swedish", "Svenska"),
        LanguageEntry("sv-SE", "Swedish (Sweden)", "Svenska"),
        LanguageEntry("tgl", "Tagalog", "Tagalog"),
        LanguageEntry("ty", "Tahitian", "Reo Tahiti"),
        LanguageEntry("tg", "Tajik", "Тоҷикӣ"),
        LanguageEntry("tam", "Tamil", "தமிழ்"),
        LanguageEntry("tt", "Tatar", "Татарча"),
        LanguageEntry("tel", "Telugu", "తెలుగు"),
        LanguageEntry("tha", "Thai", "ไทย"),
        LanguageEntry("tib", "Tibetan", "བོད་སྐད་"),
        LanguageEntry("bo", "Tibetan", "བོད་སྐད་"),
        LanguageEntry("ti", "Tigrinya", "ትግርኛ"),
        LanguageEntry("to", "Tonga", "Lea Faka-Tonga"),
        LanguageEntry("ts", "Tsonga", "Xitsonga"),
        LanguageEntry("tn", "Tswana", "Setswana"),
        LanguageEntry("tur", "Turkish", "Türkçe"),
        LanguageEntry("tk", "Turkmen", "Türkmençe"),
        LanguageEntry("tw", "Twi", "Twi"),
        LanguageEntry("ug", "Uyghur", "ئۇيغۇرچە"),
        LanguageEntry("ukr", "Ukrainian", "Українська"),
        LanguageEntry("urd", "Urdu", "اردو"),
        LanguageEntry("uzb", "Uzbek", "Oʻzbekcha"),
        LanguageEntry("ve", "Venda", "Tshivenḓa"),
        LanguageEntry("vie", "Vietnamese", "Tiếng Việt"),
        LanguageEntry("vo", "Volapük", "Volapük"),
        LanguageEntry("wa", "Walloon", "Walon"),
        LanguageEntry("wel", "Welsh", "Cymraeg"),
        LanguageEntry("wo", "Wolof", "Wolof"),
        LanguageEntry("xho", "Xhosa", "isiXhosa"),
        LanguageEntry("yid", "Yiddish", "ייִדיש"),
        LanguageEntry("yor", "Yoruba", "Yorùbá"),
        LanguageEntry("za", "Zhuang", "Vahcuengh"),
        LanguageEntry("zul", "Zulu", "isiZulu")
    ).sortedWith(compareBy({ it.displayName }, { it.code }))

    private fun createLocale(tag: String): Locale {
        return try {
            val parsed = Locale.forLanguageTag(tag)
            if (parsed.language.isNotBlank()) parsed else Locale.Builder().setLanguage(tag).build()
        } catch (_: Exception) {
            Locale.forLanguageTag(tag)
        }
    }

    fun getDisplayName(code: String?): String {
        if (code.isNullOrBlank() || code.equals("und", ignoreCase = true)) return "Default"
        val clean = code.trim().lowercase()
        val found = all.find { it.code.equals(clean, ignoreCase = true) }
        if (found != null) return found.displayName
        return try {
            val loc = createLocale(clean)
            val disp = loc.getDisplayLanguage(Locale.ENGLISH)
            if (disp.isNotBlank() && !disp.equals(clean, ignoreCase = true)) {
                disp.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() }
            } else clean.uppercase()
        } catch (_: Exception) {
            clean.uppercase()
        }
    }

    fun getNativeName(code: String?): String? {
        if (code.isNullOrBlank() || code.equals("und", ignoreCase = true)) return null
        val clean = code.trim().lowercase()
        val found = all.find { it.code.equals(clean, ignoreCase = true) }
        if (found != null && found.nativeName.isNotBlank() && !found.nativeName.equals(found.displayName, ignoreCase = true)) {
            return found.nativeName
        }
        return try {
            val loc = createLocale(clean)
            val native = loc.getDisplayLanguage(loc)
            if (native.isNotBlank() && !native.equals(clean, ignoreCase = true) && !native.equals(getDisplayName(clean), ignoreCase = true)) {
                native.replaceFirstChar { if (it.isLowerCase()) it.titlecase(loc) else it.toString() }
            } else null
        } catch (_: Exception) {
            null
        }
    }

    fun getEquivalents(code: String?): List<String> {
        if (code.isNullOrBlank()) return emptyList()
        val results = mutableSetOf<String>()
        val cleanCode = code.trim().lowercase()
        results.add(cleanCode)

        try {
            val loc = createLocale(cleanCode)
            if (loc.language.isNotBlank()) results.add(loc.language.lowercase())
            try {
                if (loc.isO3Language.isNotBlank()) results.add(loc.isO3Language.lowercase())
            } catch (_: Exception) {}
        } catch (_: Exception) {}

        // Match with known aliases
        val entry = all.find { it.code.equals(cleanCode, ignoreCase = true) }
        if (entry != null) {
            results.add(entry.code.lowercase())
            val baseCode = entry.code.substringBefore("-").lowercase()
            results.add(baseCode)
        }

        // Add common 2-letter / 3-letter bridges
        when (cleanCode) {
            "hin", "hi" -> { results.add("hin"); results.add("hi") }
            "eng", "en" -> { results.add("eng"); results.add("en") }
            "tam", "ta" -> { results.add("tam"); results.add("ta") }
            "tel", "te" -> { results.add("tel"); results.add("te") }
            "mal", "ml" -> { results.add("mal"); results.add("ml") }
            "kan", "kn" -> { results.add("kan"); results.add("kn") }
            "ben", "bn" -> { results.add("ben"); results.add("bn") }
            "mar", "mr" -> { results.add("mar"); results.add("mr") }
            "guj", "gu" -> { results.add("guj"); results.add("gu") }
            "pan", "pa" -> { results.add("pan"); results.add("pa") }
            "urd", "ur" -> { results.add("urd"); results.add("ur") }
            "spa", "es" -> { results.add("spa"); results.add("es") }
            "fre", "fra", "fr" -> { results.add("fre"); results.add("fra"); results.add("fr") }
            "ger", "deu", "de" -> { results.add("ger"); results.add("deu"); results.add("de") }
            "jpn", "ja" -> { results.add("jpn"); results.add("ja") }
            "kor", "ko" -> { results.add("kor"); results.add("ko") }
            "chi", "zho", "zh" -> { results.add("chi"); results.add("zho"); results.add("zh") }
            "rus", "ru" -> { results.add("rus"); results.add("ru") }
            "ara", "ar" -> { results.add("ara"); results.add("ar") }
            "por", "pt" -> { results.add("por"); results.add("pt") }
            "ita", "it" -> { results.add("ita"); results.add("it") }
        }

        return results.toList()
    }

    fun isMatch(trackLang: String?, trackLabel: String?, targetCode: String?): Boolean {
        if (targetCode.isNullOrBlank()) return false
        val target = targetCode.trim().lowercase()
        val equivalents = getEquivalents(target)

        if (!trackLang.isNullOrBlank() && !trackLang.equals("und", ignoreCase = true)) {
            val tLang = trackLang.trim().lowercase()
            if (equivalents.contains(tLang)) return true
            val trackEquivs = getEquivalents(tLang)
            if (trackEquivs.any { equivalents.contains(it) }) return true
        }

        val targetName = getDisplayName(target).lowercase()
        if (targetName.isNotBlank() && targetName != "default") {
            if (!trackLabel.isNullOrBlank()) {
                val labelLower = trackLabel.lowercase()
                if (labelLower.contains(targetName) || equivalents.any { labelLower.contains(it) }) {
                    return true
                }
            }
        }

        val nativeName = getNativeName(target)?.lowercase()
        if (!nativeName.isNullOrBlank()) {
            if (!trackLabel.isNullOrBlank() && trackLabel.lowercase().contains(nativeName)) {
                return true
            }
        }

        return false
    }
}
