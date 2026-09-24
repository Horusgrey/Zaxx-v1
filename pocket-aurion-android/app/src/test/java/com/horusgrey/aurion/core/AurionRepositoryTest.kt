package com.horusgrey.aurion.core

import android.content.Context
import com.horusgrey.aurion.runtime.LocalModelRuntime
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.File

@RunWith(RobolectricTestRunner::class)
class AurionRepositoryTest {
    private lateinit var context: Context
    private lateinit var model: File

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        model = File(context.filesDir, "models/test.gguf").apply { parentFile!!.mkdirs(); writeText("gguf") }
    }

    @Test
    fun systemPromptIsAppliedAfterEveryFreshModelLoad() = runBlocking {
        val runtime = FakeRuntime()
        val repo = AurionRepository(context, runtime)

        repo.loadModel(model)
        repo.loadModel(model)

        assertEquals(2, runtime.systemPrompts.size)
        assertTrue(runtime.systemPrompts.all { it.startsWith(repo.kernel.trimEnd()) && "--- SOUL STATE ---" in it })
    }

    @Test
    fun restoreReloadsTheSavedModelInAFreshProcess() = runBlocking {
        val noModelYet = AurionRepository(context, FakeRuntime())
        assertNull(noModelYet.restoreSavedModel())

        AurionRepository(context, FakeRuntime()).loadModel(model)

        // Simulates the Side button after the process was killed: new repository, empty runtime.
        val runtime = FakeRuntime()
        val restored = AurionRepository(context, runtime).restoreSavedModel()

        assertEquals(model.absolutePath, restored?.absolutePath)
        assertEquals(listOf(model.absolutePath), runtime.loadedPaths)
        assertEquals(1, runtime.systemPrompts.size)
    }

    @Test
    fun restoreDoesNothingWhenAModelIsAlreadyLoaded() = runBlocking {
        val runtime = FakeRuntime()
        val repo = AurionRepository(context, runtime)
        repo.loadModel(model)

        assertNull(repo.restoreSavedModel())
        assertEquals(1, runtime.loadedPaths.size)
    }

    @Test
    fun answerSendsTheCurrentRequestOnceAndStoresBothTurns() = runBlocking {
        val runtime = FakeRuntime()
        val repo = AurionRepository(context, runtime)
        repo.loadModel(model)

        val streamed = StringBuilder()
        val answer = repo.answer("unique-request-marker", null) { streamed.append(it) }

        assertEquals("ok", answer)
        assertEquals("ok", streamed.toString())
        assertEquals(1, Regex("unique-request-marker").findAll(runtime.prompts.single()).count())
        assertEquals(listOf("user" to "unique-request-marker", "assistant" to "ok"), repo.db.recentMessages())
    }

    private class FakeRuntime : LocalModelRuntime {
        val loadedPaths = mutableListOf<String>()
        val systemPrompts = mutableListOf<String>()
        val prompts = mutableListOf<String>()
        override val name = "fake"
        override var isModelLoaded = false
            private set

        override suspend fun loadModel(path: String) {
            loadedPaths += path
            isModelLoaded = true
        }

        override suspend fun setSystemPrompt(prompt: String) {
            systemPrompts += prompt
        }

        override suspend fun generate(prompt: String, predictLength: Int, onToken: (String) -> Unit): String {
            prompts += prompt
            onToken("ok")
            return "ok"
        }

        override suspend fun benchmark() = "fake"
        override fun unload() {
            isModelLoaded = false
        }
    }
}
