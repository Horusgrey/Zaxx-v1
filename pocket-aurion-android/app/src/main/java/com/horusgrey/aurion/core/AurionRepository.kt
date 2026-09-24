package com.horusgrey.aurion.core

import android.content.Context
import com.horusgrey.aurion.data.AurionDb
import com.horusgrey.aurion.runtime.LocalModelRuntime
import com.horusgrey.aurion.runtime.RuntimeFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AurionRepository(private val context: Context) {
    val db = AurionDb(context)
    val runtime: LocalModelRuntime = RuntimeFactory.create(context)
    private val compiler = ContextCompiler(db)
    val kernel: String = AssetText.read(context, "aurion/AURION_KERNEL.md")
    private val soul: String = AssetText.read(context, "aurion/aurion_soul.json")
    private val ego: String = AssetText.read(context, "aurion/aurion_ego.json")
    private val operator: String = AssetText.read(context, "aurion/operator_model.json")

    @Volatile private var systemPromptApplied = false

    suspend fun loadModel(modelFile: File) = withContext(Dispatchers.IO) {
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
        db.addMessage("user", input)
        val prompt = compiler.compile(input, screenContext)
        val answer = runtime.generate(prompt, 768, onToken)
        db.addMessage("assistant", answer)
        answer
    }

    suspend fun benchmark(): String = withContext(Dispatchers.IO) { runtime.benchmark() }
}
