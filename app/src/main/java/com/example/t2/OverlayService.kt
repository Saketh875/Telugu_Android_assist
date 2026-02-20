package com.example.t2

import android.content.*
import android.graphics.PixelFormat
import android.os.*
import android.speech.*
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.*
import android.widget.*
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.*

class OverlayService : LifecycleService(), TextToSpeech.OnInitListener {
    private lateinit var windowManager: WindowManager
    private var rootLayout: FrameLayout? = null
    private lateinit var tts: TextToSpeech
    private lateinit var captionText: TextView
    private lateinit var speechRecognizer: SpeechRecognizer
    private val openAIManager = OpenAIManager()

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        tts = TextToSpeech(this, this)

        try {
            setupOverlay()
            setupSpeechRecognizer()
        } catch (e: Exception) {
            Log.e("OVERLAY_ERROR", "Error in onCreate: ${e.message}")
        }
    }

    private fun setupOverlay() {
        if (rootLayout != null) return // Prevent adding twice

        rootLayout = FrameLayout(this)
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(0xDD000000.toInt())
            setPadding(30, 20, 30, 20)
            gravity = Gravity.CENTER_VERTICAL
        }

        captionText = TextView(this).apply {
            text = "AI Ready..."
            setTextColor(0xFFFFFFFF.toInt())
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
        }

        val mic = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_btn_speak_now)
            background = null
            layoutParams = LinearLayout.LayoutParams(150, 150)
            setOnClickListener { startListening() }
        }

        bar.addView(captionText)
        bar.addView(mic)
        rootLayout?.addView(bar)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            200,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.BOTTOM }

        try {
            windowManager.addView(rootLayout, params)
        } catch (e: Exception) {
            Log.e("OVERLAY_ERROR", "Could not add window: ${e.message}")
        }
    }

    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val userText = matches[0]
                    updateCaption(userText)

                    // Inside OverlayService.kt -> onResults
                    // Inside OverlayService.kt onResults
                    // Inside OverlayService.kt
                    // Inside OverlayService.kt
                    lifecycleScope.launch {
                        updateCaption("Thinking...")
                        val response = openAIManager.askOpenAI(userText)

                        if (response.contains("[ACTION:")) {
                            val actionData = response.substringAfter("[ACTION:").substringBefore("]")
                            val parts = actionData.split(":")

                            if (parts.size >= 2) {
                                val appToFind = parts[0].trim()
                                val query = if (parts.size > 2) parts[2] else ""

                                val launcher = AppLauncher(this@OverlayService)
                                // Use the dynamic launcher to find the real package name
                                val actualPackage = launcher.openAppFromVoice(appToFind)

                                if (actualPackage != null) {
                                    VoiceAutomationService.targetPackage = actualPackage
                                    VoiceAutomationService.pendingQuery = query

                                    // Speak the response AFTER we know we found the app
                                    val cleanMsg = response.replace(Regex("\\[ACTION:.*?\\]"), "").trim()
                                    updateCaption(cleanMsg)
                                    speak(cleanMsg)
                                } else {
                                    updateCaption("App not found: $appToFind")
                                    speak("క్షమించండి, ఆ యాప్ దొరకలేదు.") // "Sorry, app not found"
                                }
                            }
                        } else {
                            // Just a normal conversation turn
                            updateCaption(response)
                            speak(response)
                        }
                    }
                }
            }
            override fun onReadyForSpeech(p0: Bundle?) { updateCaption("Listening...") }
            override fun onError(p0: Int) { updateCaption("Mic Error: $p0") }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(p0: Float) {}
            override fun onBufferReceived(p0: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(p0: Bundle?) {}
            override fun onEvent(p0: Int, p1: Bundle?) {}
        })
    }

    private fun startListening() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "te-IN") // Force Telugu listening
            }
            speechRecognizer.startListening(intent)
        } catch (e: Exception) {
            updateCaption("Mic Busy")
        }
    }

    private fun updateCaption(text: String) {
        captionText.post { captionText.text = text }
    }

    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "id1")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) tts.language = Locale("te", "IN")
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (rootLayout != null) windowManager.removeView(rootLayout)
        } catch (e: Exception) { /* Ignore */ }
        speechRecognizer.destroy()
        tts.shutdown()
    }
}