package com.cartoniq.lexiforge.util

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TtsManager(context: Context) : TextToSpeech.OnInitListener {

    @Volatile
    private var ready = false

    private val tts: TextToSpeech = TextToSpeech(context.applicationContext, this)

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
    }

    fun speak(text: String, languageTag: String) {
        if (!ready || text.isBlank()) return
        val locale = if (languageTag.isBlank()) Locale.getDefault() else Locale.forLanguageTag(languageTag)
        tts.setLanguage(locale)
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "lexiforge_utterance")
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}
