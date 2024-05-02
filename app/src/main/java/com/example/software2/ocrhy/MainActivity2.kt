package com.example.software2.ocrhy

import android.Manifest.permission
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.software2.ocrhy.MainActivity
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Detector.Detections
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import java.io.IOException
import java.util.*

class MainActivity2 : AppCompatActivity() {
    private var buttonCamera: Button? = null
    private var mVoiceInputTv: TextView? = null
    private var textView: TextView? = null
    private var surfaceView: SurfaceView? = null
    private var cameraSource: CameraSource? = null
    private var textRecognizer: TextRecognizer? = null
    private var stringResult: String? = null

    var x1 = 0f
    var x2 = 0f
    var y1 = 0f
    var y2 = 0f

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        mVoiceInputTv = findViewById<View>(R.id.textView) as TextView
        window.decorView.setBackgroundColor(Color.WHITE)
        ActivityCompat.requestPermissions(
            this,
            arrayOf(permission.CAMERA),
            PackageManager.PERMISSION_GRANTED
        )

        textToSpeech = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech?.language = Locale.CANADA
                textToSpeech?.setSpeechRate(1f)
                Toast.makeText(
                    this@MainActivity2,
                    "swipe right and say yes to read and say no to return back to main menu",
                    Toast.LENGTH_SHORT
                ).show()
                textToSpeech?.speak(
                    "swipe right and say yes to read and say no to return back to main menu",
                    TextToSpeech.QUEUE_ADD,
                    null
                )
            }
        }

        mVoiceInputTv = findViewById<View>(R.id.textView) as TextView
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
                    textToSpeech?.speak(stringResult, TextToSpeech.QUEUE_FLUSH, null)
                    textToSpeech?.speak(
                        "Swipe left to listen again. or swipe right and say what you want",
                        TextToSpeech.QUEUE_ADD,
                        null
                    )
                } else if (x1 > x2) {
                    startVoiceInput()
                }
            }
        }
        return false
    }

    private fun textRecognizer() {
        Toast.makeText(this@MainActivity2, "Tap on the screen and listen ", Toast.LENGTH_SHORT)
            .show()
        textToSpeech?.speak(
            " Tap on the screen take a picture of any text with your device and listen",
            TextToSpeech.QUEUE_FLUSH,
            null
        )
        textRecognizer = TextRecognizer.Builder(applicationContext).build()
        cameraSource = CameraSource.Builder(applicationContext, textRecognizer)
            .setRequestedPreviewSize(1280, 1024)
            .setAutoFocusEnabled(true)
            .build()
        surfaceView = findViewById(R.id.surfaceView)
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            textToSpeech?.speak(
                "Image is clearly visible tap on the screen",
                TextToSpeech.QUEUE_FLUSH,
                null
            )
        }, 5000)
        val context: Context = this
        surfaceView?.holder?.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            permission.CAMERA
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    cameraSource?.start(surfaceView?.holder)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource?.stop()
            }
        })
    }

    private fun capture() {
        textRecognizer?.setProcessor(object : Detector.Processor<TextBlock?> {
            override fun release() {}
            override fun receiveDetections(detections: Detections<TextBlock?>) {
                val sparseArray = detections.detectedItems
                val stringBuilder = StringBuilder()
                for (i in 0 until sparseArray.size()) {
                    val textBlock = sparseArray.valueAt(i)
                    if (textBlock != null && textBlock.value != null) {
                        stringBuilder.append(textBlock.value + " ")
                    }
                }
                val stringText = stringBuilder.toString()
                val handler = Handler(Looper.getMainLooper())
                handler.post {
                    stringResult = stringText
                    resultObtained()
                }
            }
        })
    }

    private fun resultObtained() {
        setContentView(R.layout.activity_main2)
        textView = findViewById(R.id.textView)
        textView?.text = stringResult
        textToSpeech?.speak(stringResult, TextToSpeech.QUEUE_FLUSH, null, null)
        textToSpeech?.speak(
            "Swipe left to listen again. or swipe right and say what you want",
            TextToSpeech.QUEUE_ADD,
            null
        )
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
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_CODE_SPEECH_INPUT -> {
                if (resultCode == RESULT_OK && null != data) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    mVoiceInputTv?.text = result?.get(0)
                }
                // Handle different voice input cases here
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            textToSpeech?.speak(
                "You are in main menu. just swipe right and say what you want",
                TextToSpeech.QUEUE_FLUSH,
                null
            )
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                textToSpeech?.speak(
                    "you are in main menu. just swipe right and say what you want",
                    TextToSpeech.QUEUE_FLUSH,
                    null
                )
            }, 1000)
        }
        return true
    }

    public override fun onPause() {
        if (textToSpeech != null) {
            textToSpeech?.stop()
        }
        super.onPause()
    }

    companion object {
        private const val REQ_CODE_SPEECH_INPUT = 100
        private var textToSpeech: TextToSpeech? = null
    }
}
