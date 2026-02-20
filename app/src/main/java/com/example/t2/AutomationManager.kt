package com.example.t2

import android.os.Handler
import android.os.Looper
import android.util.Log

class AutomationManager {

    fun openSwiggyAndSearch(query: String) {

        val handler = Handler(Looper.getMainLooper())

        // Wait 3 seconds for app to open
        handler.postDelayed({

            Log.d("AUTO", "Trying to click search")

            AccessibilityAgentService.instance?.findAndClick("Search")

            handler.postDelayed({

                Log.d("AUTO", "Typing query")

                AccessibilityAgentService.instance?.typeText(query)

            }, 1500)

        }, 3000)
    }
}
