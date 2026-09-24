package com.horusgrey.aurion.core

import android.content.Context

object AssetText {
    fun read(context: Context, path: String): String =
        context.assets.open(path).bufferedReader().use { it.readText() }
}
