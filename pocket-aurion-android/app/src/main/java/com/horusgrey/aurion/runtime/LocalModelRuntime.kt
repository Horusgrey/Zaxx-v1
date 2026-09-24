package com.horusgrey.aurion.runtime

interface LocalModelRuntime {
    val name: String
    val isModelLoaded: Boolean

    suspend fun loadModel(path: String)
    suspend fun setSystemPrompt(prompt: String)
    suspend fun generate(prompt: String, predictLength: Int = 768, onToken: (String) -> Unit): String
    suspend fun benchmark(): String
    fun unload()
}
