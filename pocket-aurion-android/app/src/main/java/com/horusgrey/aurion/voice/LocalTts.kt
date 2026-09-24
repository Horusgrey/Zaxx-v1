package com.horusgrey.aurion.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class LocalTts(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var ready = false

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) return
        val engine = tts ?: return
        val localVoice = engine.voices?.firstOrNull { voice ->
            !voice.isNetworkConnectionRequired && voice.locale.language == Locale.getDefault().language
        } ?: engine.voices?.firstOrNull { !it.isNetworkConnectionRequired }
        if (localVoice != null) {
            engine.voice = localVoice
            ready = true
        }
    }

    fun speak(text: String) {
        if (ready) tts?.speak(text.take(3000), TextToSpeech.QUEUE_FLUSH, null, "aurion")
    }

    fun shutdown() {
        tts?.shutdown()
        tts = null
    }
}
