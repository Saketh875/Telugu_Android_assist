package com.example.t2

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.os.Handler
import android.os.Looper

class VoiceAutomationService : AccessibilityService() {

    companion object {
        var instance: VoiceAutomationService? = null
        var pendingQuery: String? = null
        var targetPackage: String? = null // Dynamically set by OverlayService
    }
    override fun onInterrupt() {
        // This is called when the system wants to interrupt the feedback your service is providing.
        // Usually, you can leave this empty.
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val currentPackage = event.packageName?.toString() ?: ""

        // Only trigger if the current package matches the one we just launched
        if (pendingQuery != null && currentPackage == targetPackage) {
            val query = pendingQuery!!
            pendingQuery = null // Clear to prevent loops
            targetPackage = null

            // Universal Automation Attempt:
            // This tries to find ANY search bar or text input in the new app
            Handler(Looper.getMainLooper()).postDelayed({
                performGenericAutomation(query)
            }, 2500)
        }
    }

    private fun performGenericAutomation(query: String) {
        val root = rootInActiveWindow ?: return

        // Logic to find ANY clickable search icon or focused EditText
        // This is better than hardcoding "com.whatsapp:id/search"
        val nodes = root.findAccessibilityNodeInfosByText("Search")
        if (nodes.isNotEmpty()) {
            nodes[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
            // ... type logic ...
        }
    }
}