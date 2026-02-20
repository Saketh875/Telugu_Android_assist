package com.example.t2

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import android.os.Handler
import android.os.Looper
class AppLauncher(private val context: Context) {

    fun openAppFromVoice(appNameFromAI: String): String? {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolvedInfos = pm.queryIntentActivities(mainIntent, 0)

        // Show the count to the user
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, "${resolvedInfos.size} apps scanned. Looking for: $appNameFromAI", Toast.LENGTH_SHORT).show()
        }

        for (info in resolvedInfos) {
            val label = info.loadLabel(pm).toString().lowercase()
            if (label.contains(appNameFromAI.lowercase())) {
                val pkg = info.activityInfo.packageName
                val launchIntent = pm.getLaunchIntentForPackage(pkg)
                context.startActivity(launchIntent)
                return pkg
            }
        }
        return null
    }
}