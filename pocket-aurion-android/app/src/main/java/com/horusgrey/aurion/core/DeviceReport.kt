package com.horusgrey.aurion.core

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.StatFs
import org.json.JSONObject

object DeviceReport {
    fun create(context: Context): JSONObject {
        val am = context.getSystemService(ActivityManager::class.java)
        val mi = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
        val stat = StatFs(context.filesDir.absolutePath)
        return JSONObject().apply {
            put("manufacturer", Build.MANUFACTURER)
            put("model", Build.MODEL)
            put("device", Build.DEVICE)
            put("sdk", Build.VERSION.SDK_INT)
            put("release", Build.VERSION.RELEASE)
            put("soc_manufacturer", if (Build.VERSION.SDK_INT >= 31) Build.SOC_MANUFACTURER else "unknown")
            put("soc_model", if (Build.VERSION.SDK_INT >= 31) Build.SOC_MODEL else "unknown")
            put("abis", Build.SUPPORTED_ABIS.joinToString(","))
            put("ram_total_bytes", mi.totalMem)
            put("ram_available_bytes", mi.availMem)
            put("low_memory", mi.lowMemory)
            put("storage_total_bytes", stat.totalBytes)
            put("storage_available_bytes", stat.availableBytes)
            put("memory_class_mb", am.memoryClass)
            put("large_memory_class_mb", am.largeMemoryClass)
        }
    }
}
