package com.horusgrey.aurion.assistant

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.horusgrey.aurion.PocketAurionApp
import com.horusgrey.aurion.R
import com.horusgrey.aurion.voice.OnDeviceSpeechInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AurionVoiceInteractionSession(context: Context) : VoiceInteractionSession(context) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private val aurion get() = (context.applicationContext as PocketAurionApp).aurion
    private lateinit var response: TextView
    private lateinit var input: EditText
    private lateinit var contextView: TextView
    private var screenContext: String = ""
    private val speech = OnDeviceSpeechInput(context)

    override fun onCreateContentView(): View {
        val v = LayoutInflater.from(context).inflate(R.layout.assistant_session, null, false)
        response = v.findViewById(R.id.sessionResponse)
        input = v.findViewById(R.id.sessionInput)
        contextView = v.findViewById(R.id.sessionContext)
        v.findViewById<Button>(R.id.sessionSend).setOnClickListener { send() }
        v.findViewById<Button>(R.id.sessionMic).setOnClickListener {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                response.text = "Open Aurion once and grant microphone permission for local voice input."
            } else {
                speech.listen(
                    onText = { text -> input.setText(text); send() },
                    onError = { message -> response.text = message }
                )
            }
        }
        return v
    }

    override fun onShow(args: Bundle?, showFlags: Int) {
        super.onShow(args, showFlags)
        if (aurion.runtime.isModelLoaded) {
            response.text = "Listening."
            return
        }
        response.text = "Loading local model…"
        scope.launch {
            response.text = runCatching { aurion.restoreSavedModel() }.fold(
                onSuccess = {
                    if (aurion.runtime.isModelLoaded) "Listening."
                    else "Aurion is the assistant, but no local model has been imported yet. Open Aurion and tap Import GGUF."
                },
                onFailure = { "Local model failed to load: ${it.message}" }
            )
        }
    }

    override fun onHandleAssist(state: AssistState) {
        super.onHandleAssist(state)
        if (!state.isFocused) return
        screenContext = AssistContextExtractor.extract(state.assistStructure)
        contextView.text = if (screenContext.isBlank()) "No readable screen context" else "Screen context available locally"
    }

    private fun send() {
        val text = input.text.toString().trim()
        if (text.isBlank()) return
        input.setText("")
        response.text = ""
        val buffer = StringBuilder()
        scope.launch {
            runCatching {
                aurion.answer(text, screenContext) { token ->
                    buffer.append(token)
                    response.post { response.text = buffer.toString() }
                }
            }.onFailure { response.text = it.message ?: "Generation failed" }
        }
    }

    override fun onDestroy() {
        speech.destroy()
        super.onDestroy()
    }
}
