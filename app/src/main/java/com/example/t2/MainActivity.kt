package com.example.t2

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val voiceButton = findViewById<ImageButton>(R.id.voiceButton)
        val aiGlow = findViewById<View>(R.id.aiGlow)

        voiceButton.setOnClickListener {
            // 1. Check for Overlay Permission first
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivity(intent)
                return@setOnClickListener
            }

            // 2. Play the "Listening" Animation
            playAiPulseAnimation(voiceButton, aiGlow)

            // 3. Start the Overlay Service
            val serviceIntent = Intent(this, OverlayService::class.java)
            startService(serviceIntent)

            // 4. Minimize app to Home Screen after 1 second
            Handler(Looper.getMainLooper()).postDelayed({
                val startMain = Intent(Intent.ACTION_MAIN)
                startMain.addCategory(Intent.CATEGORY_HOME)
                startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(startMain)
            }, 1000)
        }
    }

    private fun playAiPulseAnimation(button: View, glow: View) {
        // Scale button up and down
        val scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 1.2f, 1f)
        val scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 1.2f, 1f)

        // Make the glow view fade in and out
        val glowFade = ObjectAnimator.ofFloat(glow, "alpha", 0f, 0.6f, 0f)

        scaleX.repeatCount = 2
        scaleY.repeatCount = 2
        glowFade.repeatCount = 2

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, glowFade)
            duration = 800
            start()
        }
    }
}