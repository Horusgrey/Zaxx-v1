package com.horusgrey.aurion.core

import android.content.Context
import com.horusgrey.aurion.data.AurionDb
import com.horusgrey.aurion.runtime.LocalModelRuntime
import com.horusgrey.aurion.runtime.RuntimeFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

class AurionRepository(
    private val context: Context,
    val runtime: LocalModelRuntime = RuntimeFactory.create(context)
) {
    val db = AurionDb(context)
    private val compiler = ContextCompiler(db)
    val kernel: String = AssetText.read(context, "aurion/AURION_KERNEL.md")
    private val soul: String = AssetText.read(context, "aurion/aurion_soul.json")
    private val ego: String = AssetText.read(context, "aurion/aurion_ego.json")
    private val operator: String = AssetText.read(context, "aurion/operator_model.json")

    @Volatile private var systemPromptApplied = false
    private val loadLock = Mutex()

    suspend fun loadModel(modelFile: File) = withContext(Dispatchers.IO) {
        loadLock.withLock { loadLocked(modelFile) }
    }

    /**
     * Reloads the last imported model if none is loaded, so a Side-button session works after the
     * process was killed without the user opening MainActivity first. Returns the file it loaded, or null.
     */
    suspend fun restoreSavedModel(): File? = withContext(Dispatchers.IO) {
        loadLock.withLock {
            if (runtime.isModelLoaded) return@withLock null
            val saved = db.getSetting("model_path")?.let(::File)?.takeIf { it.exists() } ?: return@withLock null
            loadLocked(saved)
            saved
        }
    }

    private suspend fun loadLocked(modelFile: File) {
        // A fresh model load starts with an empty context, so the kernel must be applied again.
        systemPromptApplied = false
        runtime.loadModel(modelFile.absolutePath)
        applySystemPrompt()
        db.putSetting("model_path", modelFile.absolutePath)
    }

    private suspend fun applySystemPrompt() {
        if (!systemPromptApplied) {
            runtime.setSystemPrompt(
                buildString {
                    appendLine(kernel)
                    appendLine("\n--- SOUL STATE ---\n$soul")
                    appendLine("\n--- EGO / EVALUATION POLICY ---\n$ego")
                    appendLine("\n--- USER OPERATOR MODEL ---\n$operator")
                }
            )
            systemPromptApplied = true
        }
    }

    suspend fun answer(
        input: String,
        screenContext: String? = null,
        onToken: (String) -> Unit
    ): String = withContext(Dispatchers.IO) {
        require(input.isNotBlank())
        if (runtime.isModelLoaded) applySystemPrompt()
        // Compile before storing the new turn so the request is not repeated inside RECENT LOCAL CONVERSATION.
        val prompt = compiler.compile(input, screenContext)
        db.addMessage("user", input)
        val answer = runtime.generate(prompt, 768, onToken)
        db.addMessage("assistant", answer)
        answer
    }

    suspend fun benchmark(): String = withContext(Dispatchers.IO) { runtime.benchmark() }
}
