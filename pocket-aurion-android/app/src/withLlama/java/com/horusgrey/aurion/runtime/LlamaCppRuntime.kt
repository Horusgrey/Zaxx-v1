package com.horusgrey.aurion.runtime

import android.content.Context
import com.arm.aichat.AiChat
import com.arm.aichat.InferenceEngine
import com.arm.aichat.InferenceEngine.State
import com.arm.aichat.isModelLoaded
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout

class LlamaCppRuntime(context: Context) : LocalModelRuntime {
    private val engine: InferenceEngine = AiChat.getInferenceEngine(context.applicationContext)

    override val name: String = "llama.cpp Android binding"
    override val isModelLoaded: Boolean
        get() = engine.state.value.isModelLoaded

    override suspend fun loadModel(path: String) {
        // The engine loads its native library asynchronously after construction and rejects
        // loadModel() until that finishes. A failed native load never leaves Initializing, hence the timeout.
        val state = withTimeout(NATIVE_INIT_TIMEOUT_MS) {
            engine.state.first { it !is State.Uninitialized && it !is State.Initializing }
        }
        // The engine only accepts a model from Initialized: unload the previous model or clear a prior error first.
        if (state is State.ModelReady || state is State.Error) engine.cleanUp()
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

    private companion object {
        const val NATIVE_INIT_TIMEOUT_MS = 30_000L
    }
}
