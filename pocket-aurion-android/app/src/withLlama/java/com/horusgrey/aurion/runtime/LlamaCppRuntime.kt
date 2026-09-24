package com.horusgrey.aurion.runtime

import android.content.Context
import com.arm.aichat.AiChat
import com.arm.aichat.InferenceEngine
import com.arm.aichat.isModelLoaded
import kotlinx.coroutines.flow.collect

class LlamaCppRuntime(context: Context) : LocalModelRuntime {
    private val engine: InferenceEngine = AiChat.getInferenceEngine(context.applicationContext)

    override val name: String = "llama.cpp Android binding"
    override val isModelLoaded: Boolean
        get() = engine.state.value.isModelLoaded

    override suspend fun loadModel(path: String) {
        engine.loadModel(path)
    }

    override suspend fun setSystemPrompt(prompt: String) {
        engine.setSystemPrompt(prompt)
    }

    override suspend fun generate(
        prompt: String,
        predictLength: Int,
        onToken: (String) -> Unit
    ): String {
        val out = StringBuilder()
        engine.sendUserPrompt(prompt, predictLength).collect { token ->
            out.append(token)
            onToken(token)
        }
        return out.toString()
    }

    override suspend fun benchmark(): String = engine.bench(pp = 256, tg = 96, pl = 1, nr = 1)

    override fun unload() {
        engine.cleanUp()
    }
}
