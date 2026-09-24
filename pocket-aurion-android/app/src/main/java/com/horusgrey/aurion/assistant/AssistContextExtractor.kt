package com.horusgrey.aurion.assistant

import android.app.assist.AssistStructure

object AssistContextExtractor {
    fun extract(structure: AssistStructure?): String {
        if (structure == null) return ""
        val out = StringBuilder()
        for (w in 0 until structure.windowNodeCount) {
            walk(structure.getWindowNodeAt(w).rootViewNode, out, 0)
            if (out.length > 4500) break
        }
        return out.toString().take(5000)
    }

    private fun walk(node: AssistStructure.ViewNode?, out: StringBuilder, depth: Int) {
        if (node == null || depth > 40 || out.length > 5000) return
        val text = node.text?.toString()?.trim()
        val hint = node.hint?.trim()
        if (!text.isNullOrBlank()) out.append(text).append('\n')
        if (!hint.isNullOrBlank() && hint != text) out.append("[hint: ").append(hint).append("]\n")
        for (i in 0 until node.childCount) walk(node.getChildAt(i), out, depth + 1)
    }
}
