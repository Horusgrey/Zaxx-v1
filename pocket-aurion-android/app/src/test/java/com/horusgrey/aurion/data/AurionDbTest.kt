package com.horusgrey.aurion.data

import android.content.ContentValues
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class AurionDbTest {
    private lateinit var db: AurionDb

    @Before
    fun setUp() {
        db = AurionDb(RuntimeEnvironment.getApplication())
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun settingsRoundTripAndReplace() {
        assertNull(db.getSetting("model_path"))
        db.putSetting("model_path", "/a.gguf")
        db.putSetting("model_path", "/b.gguf")
        assertEquals("/b.gguf", db.getSetting("model_path"))
    }

    @Test
    fun recentMessagesAreChronologicalAndKeepTheNewest() {
        (1..5).forEach { db.addMessage(if (it % 2 == 1) "user" else "assistant", "m$it") }
        assertEquals(listOf("m3", "m4", "m5"), db.recentMessages(limit = 3).map { it.second })
        assertEquals("user", db.recentMessages(limit = 1).single().first)
    }

    @Test
    fun relevantMemoriesRankByOverlapAndExcludeNonNormalSensitivity() {
        memory("Pocket Aurion runs llama inference locally on the phone")
        memory("The operator prefers concise phone answers")
        memory("Private phone vault location", sensitivity = "private")

        val hits = db.relevantMemories("How fast is local llama inference on the phone?")

        assertEquals("Pocket Aurion runs llama inference locally on the phone", hits.first())
        assertTrue(hits.contains("The operator prefers concise phone answers"))
        assertTrue(hits.none { it.contains("vault") })
    }

    @Test
    fun relevantMemoriesIgnoreQueriesWithOnlyShortWords() {
        memory("any memory at all")
        assertEquals(emptyList<String>(), db.relevantMemories("is it on?"))
    }

    @Test
    fun activeStancesExcludeRetiredOnes() {
        stance("runtime", "CPU first", status = "active")
        stance("runtime", "GPU first", status = "superseded")
        val stances = db.activeStances()
        assertEquals(1, stances.size)
        assertTrue(stances.single().startsWith("runtime: CPU first"))
    }

    private fun memory(text: String, sensitivity: String = "normal") {
        db.writableDatabase.insert("memories", null, ContentValues().apply {
            put("text", text)
            put("provenance", "test")
            put("sensitivity", sensitivity)
            put("created_at", 0L)
        })
    }

    private fun stance(topic: String, position: String, status: String) {
        db.writableDatabase.insert("stances", null, ContentValues().apply {
            put("topic", topic)
            put("position", position)
            put("confidence", 0.8)
            put("reasons", "test")
            put("status", status)
            put("created_at", 0L)
        })
    }
}
