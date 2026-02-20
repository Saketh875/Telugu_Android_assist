package com.example.t2

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log

class AccessibilityAgentService : AccessibilityService() {
    companion object {
        var instance: AccessibilityAgentService? = null
    }
    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onInterrupt() {
        instance = null
    }
    private var alreadyExecuted = false

    private fun performSwiggySearch(query: String) {

        if (alreadyExecuted) return
        alreadyExecuted = true

        val rootNode = rootInActiveWindow ?: return

        val searchNodes = rootNode.findAccessibilityNodeInfosByText("Search")

        for (node in searchNodes) {
            if (node.isClickable) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)

                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({

                    val focusNode = rootInActiveWindow?.findFocus(
                        AccessibilityNodeInfo.FOCUS_INPUT
                    )

                    focusNode?.let {
                        val arguments = android.os.Bundle()
                        arguments.putCharSequence(
                            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                            query
                        )
                        it.performAction(
                            AccessibilityNodeInfo.ACTION_SET_TEXT,
                            arguments
                        )
                    }

                }, 1500)

                break
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        if (event == null) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {

            val packageName = event.packageName?.toString() ?: return

            if (packageName.contains("swiggy")) {

                android.util.Log.d("ACCESS", "Swiggy screen detected")

                performSwiggySearch("biryani")
            }
        }
    }


//    override fun onInterrupt() {
//        Log.d("ACCESSIBILITY", "Service Interrupted")
//    }

    fun findAndClick(text: String) {
        val rootNode = rootInActiveWindow ?: return
        val nodes = rootNode.findAccessibilityNodeInfosByText(text)

        for (node in nodes) {
            if (node.isClickable) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                break
            }
        }
    }

    fun typeText(text: String) {
        val rootNode = rootInActiveWindow ?: return
        val focusNode = rootNode.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)

        focusNode?.let {
            val arguments = android.os.Bundle()
            arguments.putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                text
            )
            it.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        }
    }
}
