package com.horusgrey.aurion.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AurionDb(context: Context) : SQLiteOpenHelper(context, "aurion_pocket.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE messages(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                role TEXT NOT NULL,
                content TEXT NOT NULL,
                created_at INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE memories(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                text TEXT NOT NULL,
                kind TEXT NOT NULL DEFAULT 'observation',
                confidence REAL NOT NULL DEFAULT 1.0,
                provenance TEXT NOT NULL,
                sensitivity TEXT NOT NULL DEFAULT 'normal',
                created_at INTEGER NOT NULL,
                last_verified_at INTEGER
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE stances(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                topic TEXT NOT NULL,
                position TEXT NOT NULL,
                confidence REAL NOT NULL,
                reasons TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'active',
                created_at INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE projects(
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                state TEXT NOT NULL,
                canon TEXT NOT NULL DEFAULT '',
                updated_at INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE lineage(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                concept TEXT NOT NULL,
                relation TEXT NOT NULL,
                target TEXT NOT NULL,
                evidence TEXT NOT NULL,
                created_at INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE settings(
                key TEXT PRIMARY KEY,
                value TEXT NOT NULL
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    fun addMessage(role: String, content: String) {
        writableDatabase.insert("messages", null, ContentValues().apply {
            put("role", role)
            put("content", content)
            put("created_at", System.currentTimeMillis())
        })
    }

    fun recentMessages(limit: Int = 12): List<Pair<String, String>> {
        val rows = mutableListOf<Pair<String, String>>()
        readableDatabase.rawQuery(
            "SELECT role, content FROM messages ORDER BY id DESC LIMIT ?",
            arrayOf(limit.toString())
        ).use { c ->
            while (c.moveToNext()) rows += c.getString(0) to c.getString(1)
        }
        return rows.reversed()
    }

    fun relevantMemories(query: String, limit: Int = 8): List<String> {
        val tokens = query.lowercase().split(Regex("\\W+")).filter { it.length >= 4 }.distinct().take(6)
        if (tokens.isEmpty()) return emptyList()
        val scored = mutableListOf<Pair<Int, String>>()
        readableDatabase.rawQuery(
            "SELECT text, sensitivity FROM memories WHERE sensitivity = 'normal' ORDER BY id DESC LIMIT 250",
            null
        ).use { c ->
            while (c.moveToNext()) {
                val text = c.getString(0)
                val lower = text.lowercase()
                val score = tokens.count { lower.contains(it) }
                if (score > 0) scored += score to text
            }
        }
        return scored.sortedByDescending { it.first }.take(limit).map { it.second }
    }

    fun activeStances(limit: Int = 6): List<String> {
        val rows = mutableListOf<String>()
        readableDatabase.rawQuery(
            "SELECT topic, position, confidence, reasons FROM stances WHERE status='active' ORDER BY id DESC LIMIT ?",
            arrayOf(limit.toString())
        ).use { c ->
            while (c.moveToNext()) {
                rows += "${c.getString(0)}: ${c.getString(1)} (confidence ${c.getDouble(2)}). ${c.getString(3)}"
            }
        }
        return rows
    }

    fun putSetting(key: String, value: String) {
        writableDatabase.insertWithOnConflict(
            "settings",
            null,
            ContentValues().apply { put("key", key); put("value", value) },
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun getSetting(key: String): String? {
        readableDatabase.rawQuery("SELECT value FROM settings WHERE key=?", arrayOf(key)).use { c ->
            return if (c.moveToFirst()) c.getString(0) else null
        }
    }
}
