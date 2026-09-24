package com.horusgrey.aurion

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.horusgrey.aurion.core.DeviceReport
import com.horusgrey.aurion.voice.OnDeviceSpeechInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : AppCompatActivity() {
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private val aurion get() = (application as PocketAurionApp).aurion
    private lateinit var status: TextView
    private lateinit var transcript: TextView
    private lateinit var input: EditText
    private lateinit var transcriptScroll: ScrollView
    private lateinit var speech: OnDeviceSpeechInput

    private val roleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        refreshStatus()
    }

    private val modelPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) importModel(uri)
    }

    private val micPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startSpeech() else append("SYSTEM", "Microphone permission denied.")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        status = findViewById(R.id.status)
        transcript = findViewById(R.id.transcript)
        input = findViewById(R.id.input)
        transcriptScroll = findViewById(R.id.transcriptScroll)
        speech = OnDeviceSpeechInput(this)

        findViewById<Button>(R.id.setAssistantButton).setOnClickListener { requestAssistantRole() }
        findViewById<Button>(R.id.importModelButton).setOnClickListener {
            modelPicker.launch(arrayOf("application/octet-stream", "*/*"))
        }
        findViewById<Button>(R.id.sendButton).setOnClickListener { send() }
        findViewById<Button>(R.id.speakButton).setOnClickListener { ensureMicAndSpeak() }
        findViewById<Button>(R.id.benchmarkButton).setOnClickListener { benchmark() }

        append("SYSTEM", "Pocket Aurion 0.1.0. Core app has no INTERNET permission.")
        append("DEVICE", DeviceReport.create(this).toString(2))
        refreshStatus()

        aurion.db.getSetting("model_path")?.let { saved ->
            val f = File(saved)
            if (f.exists()) scope.launch {
                runCatching { aurion.loadModel(f) }
                    .onSuccess { append("SYSTEM", "Restored local model: ${f.name}"); refreshStatus() }
                    .onFailure { append("ERROR", it.message ?: "Model restore failed") }
            }
        }
    }

    private fun requestAssistantRole() {
        val rm = getSystemService(RoleManager::class.java)
        if (!rm.isRoleAvailable(RoleManager.ROLE_ASSISTANT)) {
            append("SYSTEM", "Android Assistant role is not available on this firmware.")
            return
        }
        if (rm.isRoleHeld(RoleManager.ROLE_ASSISTANT)) {
            append("SYSTEM", "Aurion is already the Android assistant role holder.")
            return
        }
        roleLauncher.launch(rm.createRequestRoleIntent(RoleManager.ROLE_ASSISTANT))
    }

    private fun refreshStatus() {
        val rm = getSystemService(RoleManager::class.java)
        val held = rm.isRoleAvailable(RoleManager.ROLE_ASSISTANT) && rm.isRoleHeld(RoleManager.ROLE_ASSISTANT)
        status.text = "Assistant=${if (held) "YES" else "NO"} • Runtime=${aurion.runtime.name} • Model=${if (aurion.runtime.isModelLoaded) "READY" else "NOT LOADED"}"
    }

    private fun importModel(uri: Uri) {
        scope.launch {
            status.text = "Copying model into Aurion private storage…"
            val copied = withContext(Dispatchers.IO) {
                val models = File(filesDir, "models").apply { mkdirs() }
                val name = contentResolver.query(uri, arrayOf("_display_name"), null, null, null)?.use { c ->
                    if (c.moveToFirst()) c.getString(0) else null
                } ?: "aurion-model.gguf"
                require(name.lowercase().endsWith(".gguf")) { "Select a GGUF model file." }
                val dest = File(models, name.replace(Regex("[^A-Za-z0-9._-]"), "_"))
                contentResolver.openInputStream(uri)!!.use { input ->
                    dest.outputStream().use { out -> input.copyTo(out, 1024 * 1024) }
                }
                dest
            }
            runCatching { aurion.loadModel(copied) }
                .onSuccess { append("SYSTEM", "Model loaded: ${copied.name}") }
                .onFailure { append("ERROR", it.message ?: "Failed to load model") }
            refreshStatus()
        }
    }

    private fun send() {
        val text = input.text.toString().trim()
        if (text.isEmpty()) return
        input.setText("")
        append("YOU", text)
        val buffer = StringBuilder()
        append("AURION", "")
        scope.launch {
            runCatching {
                aurion.answer(text, null) { token ->
                    runOnUiThread {
                        buffer.append(token)
                        replaceLastAurion(buffer.toString())
                    }
                }
            }.onFailure { append("ERROR", it.message ?: "Generation failed") }
        }
    }

    private fun benchmark() {
        scope.launch {
            append("SYSTEM", "Running local model benchmark…")
            runCatching { aurion.benchmark() }
                .onSuccess { append("BENCH", it) }
                .onFailure { append("ERROR", it.message ?: "Benchmark failed") }
        }
    }

    private fun ensureMicAndSpeak() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startSpeech()
        } else micPermission.launch(Manifest.permission.RECORD_AUDIO)
    }

    private fun startSpeech() {
        speech.listen(
            onText = { text -> runOnUiThread { input.setText(text); send() } },
            onError = { message -> runOnUiThread { append("VOICE", message) } }
        )
    }

    private fun append(label: String, text: String) {
        transcript.append(if (transcript.text.isEmpty()) "$label: $text" else "\n\n$label: $text")
        transcriptScroll.post { transcriptScroll.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    private fun replaceLastAurion(text: String) {
        val all = transcript.text.toString()
        val idx = all.lastIndexOf("AURION:")
        if (idx >= 0) transcript.text = all.substring(0, idx) + "AURION: " + text
        transcriptScroll.post { transcriptScroll.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    override fun onDestroy() {
        speech.destroy()
        super.onDestroy()
    }
}
