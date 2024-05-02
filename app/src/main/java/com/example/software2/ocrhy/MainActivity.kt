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

class MainActivity : AppCompatActivity() {
    private var mVoiceInputTv: TextView? = null
    private var textToSpeech: TextToSpeech? = null

    var x1 = 0f
    var x2 = 0f
    var y1 = 0f
    var y2 = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textToSpeech = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech!!.language = Locale.US
                textToSpeech!!.setSpeechRate(1f)
                if (firstTime == 0) textToSpeech!!.speak(
                    "Welcome to Blind App. Swipe left to listen the features of the app and swipe right and say what you want",
                    TextToSpeech.QUEUE_FLUSH,
                    null
                )
            }
        }

        mVoiceInputTv = findViewById<View>(R.id.voiceInput) as TextView
    }

    override fun onTouchEvent(touchEvent: MotionEvent): Boolean {
        firstTime = 1
        when (touchEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                x1 = touchEvent.x
                y1 = touchEvent.y
            }
            MotionEvent.ACTION_UP -> {
                x2 = touchEvent.x
                y2 = touchEvent.y
                if (x1 < x2) {
                    firstTime = 1
                    val intent = Intent(this@MainActivity, MainActivity7::class.java)
                    startActivity(intent)
                } else if (x1 > x2) {
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
            mVoiceInputTv!!.text = result!![0]

            when (mVoiceInputTv!!.text.toString()) {
                "exit" -> {
                    finishAffinity()
                    System.exit(0)
                }
                "read" -> {
                    val intent = Intent(applicationContext, MainActivity2::class.java)
                    startActivity(intent)
                    mVoiceInputTv!!.text = null
                }
                "calculator" -> {
                    val intent = Intent(applicationContext, MainActivity3::class.java)
                    startActivity(intent)
                    mVoiceInputTv!!.text = null
                }
                "time and date" -> {
                    val intent = Intent(applicationContext, MainActivity4::class.java)
                    startActivity(intent)
                    mVoiceInputTv!!.text = null
                }
                "weather" -> {
                    val intent = Intent(applicationContext, MainActivity5::class.java)
                    startActivity(intent)
                    mVoiceInputTv!!.text = null
                }
                "battery" -> {
                    val intent = Intent(applicationContext, MainActivity6::class.java)
                    startActivity(intent)
                    mVoiceInputTv!!.text = null
                }
                "yes" -> {
                    textToSpeech!!.speak(
                        "  Say Read for reading,  calculator for calculator,  time and date,  weather for weather,  battery for battery. Do you want to listen again",
                        TextToSpeech.QUEUE_FLUSH,
                        null
                    )
                    mVoiceInputTv!!.text = null
                }
                "no" -> {
                    textToSpeech!!.speak(
                        "then Swipe right and say what you want",
                        TextToSpeech.QUEUE_FLUSH,
                        null
                    )
                }
                "location" -> {
                    val intent = Intent(applicationContext, MainActivity8::class.java)
                    startActivity(intent)
                    mVoiceInputTv!!.text = null
                }
                else -> {
                    textToSpeech!!.speak(
                        "Do not understand just Swipe right and say again",
                        TextToSpeech.QUEUE_FLUSH,
                        null
                    )
                }
            }
        }
    }

    public override fun onPause() {
        if (textToSpeech != null) {
            textToSpeech!!.stop()
        }
        super.onPause()
    }

    companion object {
        private const val REQ_CODE_SPEECH_INPUT = 100
        private var firstTime = 0
    }
}
