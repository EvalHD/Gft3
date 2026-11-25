package de.lokalki.app

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs

object DeviceInfo {

    fun status(ctx: Context): String {
        val stat = StatFs(Environment.getExternalStorageDirectory().path)
        val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }

        val battery = ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = battery?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = battery?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val pct = if (level >= 0) level * 100 / scale else -1

        return buildString {
            appendLine("Speicher: ${gb(stat.availableBytes)} frei von ${gb(stat.totalBytes)}")
            appendLine("RAM frei: ${gb(mi.availMem)} von ${gb(mi.totalMem)}")
            append("Akku: ${if (pct >= 0) "$pct %" else "unbekannt"}")
        }
    }

    fun apps(ctx: Context): String {
        val pm = ctx.packageManager
        val names = pm.getInstalledApplications(0)
            .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
            .map { pm.getApplicationLabel(it).toString() }
            .sorted()
        return "Du hast ${names.size} Apps installiert:\n" + names.joinToString(", ")
    }

    private fun gb(bytes: Long): String = String.format("%.1f GB", bytes / 1e9)
}
