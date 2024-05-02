package com.example.software2.ocrhy

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity7 : AppCompatActivity() {
    private var mVoiceInputTv: TextView? = null
    private var x1 = 0f
    private var x2 = 0f
    private var y1 = 0f
    private var y2 = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main7)
        mVoiceInputTv = findViewById<View>(R.id.voiceInput) as TextView
        textToSpeech = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech!!.language = Locale.US
                textToSpeech!!.setSpeechRate(1f)
                textToSpeech!!.speak(
                    "Say 'read' for read, 'calculator' for calculator, 'weather' for weather, 'location' for location, 'battery' for battery, 'time and date' for time and date. Say 'exit' for closing the application. Swipe right and say what you want.",
                    TextToSpeech.QUEUE_FLUSH,
                    null
                )
            }
        }
    }

    override fun onTouchEvent(touchEvent: MotionEvent): Boolean {
        when (touchEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                x1 = touchEvent.x
                y1 = touchEvent.y
            }
            MotionEvent.ACTION_UP -> {
                x2 = touchEvent.x
                y2 = touchEvent.y
                if (x1 > x2) {
                    textToSpeech!!.stop()
                    startVoiceInput()
                }
            }
        }
        return false
    }

    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hello, How can I help you?")
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT)
        } catch (a: ActivityNotFoundException) {
            a.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            result?.get(0)?.let { handleVoiceInput(it) }
        }
    }

    private fun handleVoiceInput(input: String) {
        when (input.toLowerCase(Locale.getDefault())) {
            "read" -> startActivity(Intent(applicationContext, MainActivity2::class.java))
            "calculator" -> startActivity(Intent(applicationContext, MainActivity3::class.java))
            "time and date" -> startActivity(Intent(applicationContext, MainActivity4::class.java))
            "weather" -> startActivity(Intent(applicationContext, MainActivity5::class.java))
            "battery" -> startActivity(Intent(applicationContext, MainActivity6::class.java))
            "location" -> startActivity(Intent(applicationContext, MainActivity8::class.java))
            "exit" -> {
                onPause()
                finishAffinity()
            }
        }
    }

    override fun onDestroy() {
        if (mVoiceInputTv!!.text.toString().equals("exit", ignoreCase = true)) {
            finish()
        }
        super.onDestroy()
    }

    companion object {
        private const val REQ_CODE_SPEECH_INPUT = 100
        private var textToSpeech: TextToSpeech? = null
    }
}
