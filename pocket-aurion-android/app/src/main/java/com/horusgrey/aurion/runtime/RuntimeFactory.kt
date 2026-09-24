package com.horusgrey.aurion.runtime

import android.content.Context

object RuntimeFactory {
    fun create(context: Context): LocalModelRuntime {
        return try {
            val clazz = Class.forName("com.horusgrey.aurion.runtime.LlamaCppRuntime")
            clazz.getConstructor(Context::class.java).newInstance(context) as LocalModelRuntime
        } catch (_: Throwable) {
            StubRuntime()
        }
    }
}
