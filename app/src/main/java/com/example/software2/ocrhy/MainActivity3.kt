package com.example.software2.ocrhy

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.software2.ocrhy.MainActivity
import net.objecthunter.exp4j.Expression
import net.objecthunter.exp4j.ExpressionBuilder
import java.util.*

class MainActivity3 : AppCompatActivity() {
    private val button: Button? = null
    var txtScreen: TextView? = null
    var button2: Button? = null
    var textToSpeech: TextToSpeech? = null
    var txtInput: TextView? = null
    private var lastNumeric = false

    // Represent that current state is in error or not
    private var stateError = false
    private val REQ_CODE_SPEECH_INPUT = 100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)
        setNumericOnClickListener()
        setOperatorOnClickListener()
        txtScreen = findViewById(R.id.txtScreen)
        txtInput = findViewById(R.id.txtInput)
        val button2 = findViewById<ImageButton>(R.id.btnSpeak)
        textToSpeech = TextToSpeech(applicationContext) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech!!.language = Locale.US
                textToSpeech!!.setSpeechRate(1f)
                Toast.makeText(
                    this@MainActivity3,
                    "Opening the calculator......  just tap on the screen and say what you want to calculate. And Press the volume up button to return the main menu",
                    Toast.LENGTH_SHORT
                ).show()
                textToSpeech!!.speak(
                    "Opening the calculator......  just tap on the screen and say what you want to calculate or say what you want ",
                    TextToSpeech.QUEUE_FLUSH,
                    null
                )
            }
        }
    }

    private fun setNumericOnClickListener() {
        // Create a common OnClickListener
        View.OnClickListener { v -> // Just append/set the text of clicked button
            val button = v as Button
            if (stateError) {
                // If current state is Error, replace the error message
                txtScreen!!.text = button.text
                stateError = false
            } else {
                // If not, already there is a valid expression so append to it
                txtScreen!!.append(button.text)
            }

            // Set the flag
            lastNumeric = true
        }
    }

    private fun setOperatorOnClickListener() {
        // Create a common OnClickListener for operators
        View.OnClickListener { v -> // If the current state is Error do not append the operator
            // If the last input is number only, append the operator
            if (lastNumeric && !stateError) {
                val button = v as Button
                txtScreen!!.append(button.text)
                lastNumeric = false
            }
        }

        // Clear button
        findViewById<View>(R.id.btnClear).setOnClickListener {
            txtScreen!!.text = "" // Clear the screen
            txtInput?.text = "" // Clear the input with safe call operator
            // Reset all the states and flags
            lastNumeric = false
            stateError = false
        }
        findViewById<View>(R.id.btnSpeak).setOnClickListener {
            if (stateError) {
                // If current state is Error, replace the error message
                txtScreen!!.text = "Try Again"
                stateError = false
            } else {
                // If not, already there is a valid expression so append to it
                promptSpeechInput()
            }
            // Set the flag
            lastNumeric = true
        }
    }

    private fun promptSpeechInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(
            RecognizerIntent.EXTRA_PROMPT,
            getString(R.string.speech_prompt)
        )
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT)
        } catch (a: ActivityNotFoundException) {
            Toast.makeText(
                applicationContext,
                getString(R.string.speech_not_supported),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun onEqual() {
        // If the current state is error, nothing to do.
        // If the last input is a number only, solution can be found.
        if (lastNumeric && !stateError) {
            // Read the expression
            val inputNumber = txtInput?.text.toString() // Safe call operator
            txtScreen!!.text = inputNumber
            // Create an Expression (A class from exp4j library)
            var expression: Expression? = null
            try {
                expression = null
                try {
                    expression = ExpressionBuilder(inputNumber).build()
                    val result = expression.evaluate()
                    txtScreen!!.text =
                        java.lang.Double.toString(result).replace("\\.0*$".toRegex(), "")
                    Toast.makeText(this@MainActivity3, "Answer is", Toast.LENGTH_SHORT).show()
                    textToSpeech!!.speak(
                        "Answer is " + txtScreen!!.text.toString(),
                        TextToSpeech.QUEUE_FLUSH,
                        null
                    )
                    textToSpeech!!.speak(
                        "tap on the screen and say what you want",
                        TextToSpeech.QUEUE_ADD,
                        null
                    )
                    textToSpeech!!.setSpeechRate(1f)
                } catch (e: Exception) {
                    txtScreen!!.text = "Error, tap on the screen and say again"
                    textToSpeech!!.speak(
                        "Error, tap on the screen and say again",
                        TextToSpeech.QUEUE_FLUSH,
                        null
                    )
                    onPause()
                }
            } catch (ex: ArithmeticException) {
                // Display an error message
                txtScreen!!.text = "Error"
                textToSpeech!!.speak(
                    "Error, tap on the screen and say again",
                    TextToSpeech.QUEUE_FLUSH,
                    null
                )
                stateError = true
                lastNumeric = true
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_CODE_SPEECH_INPUT -> {
                if (resultCode == RESULT_OK && null != data) {
                    val result = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    var change = result.toString()
                    txtInput?.text = result!![0] // Safe call operator
                    if (txtInput?.text.toString() == "read") { // Safe call operator
                        val intent = Intent(applicationContext, MainActivity2::class.java)
                        startActivity(intent)
                    }
                    if (txtInput?.text.toString() == "weather") { // Safe call operator
                        val intent = Intent(applicationContext, MainActivity5::class.java)
                        startActivity(intent)
                        txtInput?.text = null // Safe call operator
                    } else {
                        textToSpeech!!.speak(
                            "Do not understand just tap on the screen Say again",
                            TextToSpeech.QUEUE_FLUSH,
                            null
                        )
                    }
                    if (txtInput?.text.toString() == "time and date") { // Safe call operator
                        val intent = Intent(applicationContext, MainActivity4::class.java)
                        startActivity(intent)
                    }
                    if (txtInput?.text.toString() == "location") { // Safe call operator
                        val intent = Intent(applicationContext, MainActivity8::class.java)
                        startActivity(intent)
                        txtInput?.text = null // Safe call operator
                    }
                    if (txtInput?.text.toString() == "battery") { // Safe call operator
                        val intent = Intent(applicationContext, MainActivity6::class.java)
                        startActivity(intent)
                        txtInput?.text = null // Safe call operator
                    } else if (txtInput?.text.toString() == "exit") { // Safe call operator
                        finishAffinity()
                        super.onPause()
                    } else {
                        textToSpeech!!.speak(
                            "Do not understand tap on the screen Say again",
                            TextToSpeech.QUEUE_FLUSH,
                            null
                        )
                    }


                    // english-lang
                    change = change.replace("x", "*")
                    change = change.replace("X", "*")
                    change = change.replace("add", "+")
                    change = change.replace("sub", "-")
                    change = change.replace("to", "2")
                    change = change.replace(" plus ", "+")
                    change = change.replace("two", "2")
                    change = change.replace(" minus ", "-")
                    change = change.replace(" times ", "*")
                    change = change.replace(" into ", "*")
                    change = change.replace(" in2 ", "*")
                    change = change.replace(" multiply by ", "*")
                    change = change.replace(" divide by ", "/")
                    change = change.replace("divide", "/")
                    change = change.replace("equal", "=")
                    change = change.replace("equals", "=")
                    if (change.contains("=")) {
                        change = change.replace("=", "")
                        txtInput?.text = change // Safe call operator
                        onEqual()
                    } else {
                        txtInput?.text = change // Safe call operator
                        onEqual()
                    }
                }
            }
        }
    }

    override fun onTouchEvent(touchEvent: MotionEvent): Boolean {
        when (touchEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                touchEvent.x
                val y1 = touchEvent.y
            }
            MotionEvent.ACTION_UP -> {
                val x2 = touchEvent.x
                val x1 = touchEvent.x
                val y2 = touchEvent.y
                if (x1 < x2) {
                    val i = Intent(this@MainActivity3, MainActivity::class.java)
                    startActivity(i)
                } else {
                    if (x1 > x2) {
                        val i = Intent(this@MainActivity3, MainActivity::class.java)
                        startActivity(i)
                    }
                }
            }
        }
        return false
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            textToSpeech!!.speak(
                "You are in main menu. just swipe right and say what you want",
                TextToSpeech.QUEUE_FLUSH,
                null
            )
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                textToSpeech!!.speak(
                    "you are in main menu. just swipe right and say what you want",
                    TextToSpeech.QUEUE_FLUSH,
                    null
                )
            }, 1000)
        }
        return true
    }

    public override fun onDestroy() {
        if (txtInput?.text.toString() == "exit") { // Safe call operator
            finish()
        }
        super.onDestroy()
    }

    public override fun onPause() {
        if (textToSpeech != null) {
            textToSpeech!!.stop()
        }
        super.onPause()
    }
}
