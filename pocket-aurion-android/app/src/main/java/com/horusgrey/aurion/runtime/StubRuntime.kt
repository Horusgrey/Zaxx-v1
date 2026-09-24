package com.horusgrey.aurion.runtime

class StubRuntime : LocalModelRuntime {
    override val name: String = "StubRuntime"
    override val isModelLoaded: Boolean = false

    override suspend fun loadModel(path: String) {
        throw IllegalStateException("llama.cpp runtime is not bundled in this build. Rebuild with -PwithLlama=true.")
    }

    override suspend fun setSystemPrompt(prompt: String) = Unit

    override suspend fun generate(prompt: String, predictLength: Int, onToken: (String) -> Unit): String {
        val msg = "Pocket Aurion shell is installed, but no local model runtime is bundled in this APK yet."
        onToken(msg)
        return msg
    }

    override suspend fun benchmark(): String = "No model runtime bundled."
    override fun unload() = Unit
}
