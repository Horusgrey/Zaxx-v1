package com.horusgrey.aurion.voice

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionService
import android.speech.SpeechRecognizer

/**
 * Required recognition component for VoiceInteractionService metadata.
 * Pocket Aurion's user-facing voice button uses Android's explicit on-device recognizer.
 * This service intentionally does not proxy to a network recognizer.
 */
class AurionRecognitionService : RecognitionService() {
    override fun onStartListening(recognizerIntent: Intent?, listener: Callback?) {
        listener?.error(SpeechRecognizer.ERROR_CLIENT)
    }
    override fun onCancel(listener: Callback?) = Unit
    override fun onStopListening(listener: Callback?) = Unit
}
