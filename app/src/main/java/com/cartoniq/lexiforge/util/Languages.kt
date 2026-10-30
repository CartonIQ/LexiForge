package com.cartoniq.lexiforge.util

data class LanguageOption(val label: String, val tag: String)

object Languages {
    val all: List<LanguageOption> = listOf(
        LanguageOption("Device default", ""),
        LanguageOption("English (US)", "en-US"),
        LanguageOption("English (UK)", "en-GB"),
        LanguageOption("Spanish", "es-ES"),
        LanguageOption("French", "fr-FR"),
        LanguageOption("German", "de-DE"),
        LanguageOption("Italian", "it-IT"),
        LanguageOption("Portuguese (Brazil)", "pt-BR"),
        LanguageOption("Dutch", "nl-NL"),
        LanguageOption("Russian", "ru-RU"),
        LanguageOption("Turkish", "tr-TR"),
        LanguageOption("Arabic", "ar"),
        LanguageOption("Hindi", "hi-IN"),
        LanguageOption("Japanese", "ja-JP"),
        LanguageOption("Korean", "ko-KR"),
        LanguageOption("Chinese (Mandarin)", "zh-CN"),
        LanguageOption("Polish", "pl-PL"),
        LanguageOption("Swedish", "sv-SE"),
        LanguageOption("Indonesian", "id-ID")
    )

    fun labelFor(tag: String): String =
        all.firstOrNull { it.tag == tag }?.label ?: "Device default"
}
