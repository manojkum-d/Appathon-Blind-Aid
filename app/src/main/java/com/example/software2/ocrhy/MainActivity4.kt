package com.example.software2.ocrhy

import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.software2.ocrhy.MainActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity4 : AppCompatActivity() {
    private var textToSpeech: TextToSpeech? = null
    private var format7: TextView? = null
    var x1 = 0f
    var x2 = 0f
    var y1 = 0f
    var y2 = 0f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main4)
        var dateTime: String? = null
        var calendar: Calendar? = null
        val simpleDateFormat: SimpleDateFormat
        format7 = findViewById<View>(R.id.format7) as TextView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            calendar = Calendar.getInstance()
        }
        simpleDateFormat = SimpleDateFormat("'date is' dd-LLLL-yyyy 'and time is' KK:mm aaa ")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            dateTime = simpleDateFormat.format(calendar!!.time).toString()
        }
        format7!!.text = dateTime
        format7!!.text.toString()
        val finalDateTime = dateTime
        textToSpeech = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech!!.language = Locale.ENGLISH
                textToSpeech!!.setSpeechRate(1f)
                textToSpeech!!.speak(finalDateTime, TextToSpeech.QUEUE_FLUSH, null)
                textToSpeech!!.speak(
                    "swipe left to listen again and swipe right to return back in main menu",
                    TextToSpeech.QUEUE_ADD,
                    null
                )
            }
        }
    }

    //    public boolean onKeyDown(int keyCode, @Nullable KeyEvent event) {
    //        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
    //            textToSpeech.speak("You are in main menu. just swipe right and say what you want", TextToSpeech.QUEUE_FLUSH, null);
    //            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
    //            startActivity(intent);
    //            final Handler handler = new Handler(Looper.getMainLooper());
    //            handler.postDelayed(new Runnable() {
    //                @Override
    //                public void run() {
    //                    textToSpeech.speak("you are in main menu. just swipe right and say what you want", TextToSpeech.QUEUE_FLUSH, null);
    //
    //                }
    //            },1000);
    //
    //        }
    //        return true;
    //    }
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
                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed({
                        textToSpeech!!.speak(
                            "you are in main menu. just swipe right and say what you want",
                            TextToSpeech.QUEUE_FLUSH,
                            null
                        )
                    }, 1000)
                    val intent = Intent(this@MainActivity4, MainActivity::class.java)
                    startActivity(intent)
                }
                if (x1 < x2) {
                    var dateTime: String? = null
                    var calendar: Calendar? = null
                    val simpleDateFormat: SimpleDateFormat
                    format7 = findViewById<View>(R.id.format7) as TextView
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        calendar = Calendar.getInstance()
                    }
                    simpleDateFormat =
                        SimpleDateFormat("'date is' dd-LLLL-yyyy 'and time is' KK:mm aaa ")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        dateTime = simpleDateFormat.format(calendar!!.time).toString()
                    }
                    format7!!.text = dateTime
                    format7!!.text.toString()
                    val finalDateTime = dateTime
                    textToSpeech!!.speak(finalDateTime, TextToSpeech.QUEUE_FLUSH, null)
                    textToSpeech!!.speak(
                        "swipe left to listen again and swipe right to return back in main menu",
                        TextToSpeech.QUEUE_ADD,
                        null
                    )
                }
            }
        }
        return false
    }

    public override fun onPause() {
        if (textToSpeech != null) {
            textToSpeech!!.stop()
        }
        super.onPause()
    }
}