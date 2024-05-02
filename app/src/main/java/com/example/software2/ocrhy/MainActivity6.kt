package com.example.software2.ocrhy

import android.content.Intent
import android.os.*
import android.speech.tts.TextToSpeech
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.software2.ocrhy.MainActivity
import java.util.*

class MainActivity6 : AppCompatActivity() {
    private var text: TextView? = null
    private var x1 = 0f
    private var x2 = 0f
    private var y1 = 0f
    private var y2 = 0f
    private var textToSpeech: TextToSpeech? = null

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            startActivity(Intent(applicationContext, MainActivity::class.java))
            Handler(Looper.getMainLooper()).postDelayed({
                textToSpeech?.speak(
                    "You are in the main menu. Just swipe right and say what you want",
                    TextToSpeech.QUEUE_FLUSH,
                    null
                )
            }, 1000)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main6)
        text = findViewById(R.id.text)
        textToSpeech = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech?.language = Locale.getDefault()
                textToSpeech?.setSpeechRate(1f)
                val bm = getSystemService(BATTERY_SERVICE) as BatteryManager
                val percentage = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                text?.text = "Battery Percentage is $percentage %"
                if (percentage < 50) {
                    textToSpeech?.speak(
                        "Battery Percentage is $percentage %. Please charge the phone.",
                        TextToSpeech.QUEUE_FLUSH,
                        null
                    )
                } else {
                    textToSpeech?.speak(
                        "Battery Percentage is $percentage%. Mobile does not require charging.",
                        TextToSpeech.QUEUE_FLUSH,
                        null
                    )
                }
                textToSpeech?.speak(
                    "Swipe left to listen again or swipe right to return to the main menu.",
                    TextToSpeech.QUEUE_ADD,
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
                if (x1 < x2) {
                    val bm = getSystemService(BATTERY_SERVICE) as BatteryManager
                    val percentage = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                    text?.text = "Battery Percentage is $percentage %"
                    textToSpeech?.speak(
                        "Battery Percentage is $percentage%",
                        TextToSpeech.QUEUE_FLUSH,
                        null
                    )
                }
                if (x1 > x2) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        textToSpeech?.speak(
                            "You are in the main menu. Just swipe right and say what you want",
                            TextToSpeech.QUEUE_FLUSH,
                            null
                        )
                    }, 1000)
                    startActivity(Intent(this@MainActivity6, MainActivity::class.java))
                }
            }
        }
        return false
    }

    override fun onDestroy() {
        if (text?.text.toString() == "exit") {
            finish()
        }
        super.onDestroy()
    }

    override fun onPause() {
        textToSpeech?.stop()
        super.onPause()
    }

    companion object {
        private const val REQ_CODE_SPEECH_INPUT = 100
    }
}
