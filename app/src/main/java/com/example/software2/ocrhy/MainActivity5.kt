package com.example.software2.ocrhy

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import java.util.*

class MainActivity5 : AppCompatActivity() {
    private var cityInput: EditText? = null
    private val VOICE_CODE = 100
    private var textToSpeech: TextToSpeech? = null
    private var cityBtn: Button? = null
    private var voiceBtn: ImageView? = null
    private var cityTextView: TextView? = null
    private var timeTextView: TextView? = null
    private var dateTextView: TextView? = null
    private var weatherStatusText: TextView? = null
    private var temperatureText: TextView? = null
    private var weatherStatusImg: ImageView? = null
    private var currentTime: String? = null
    private var dateOutput: String? = null
    private var cityEntered: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main5)

        cityTextView = findViewById(R.id.city_text_view)
        timeTextView = findViewById(R.id.time_text_view)
        dateTextView = findViewById(R.id.date_text_view)
        weatherStatusImg = findViewById(R.id.weather_img)
        temperatureText = findViewById(R.id.temperature_text)
        weatherStatusText = findViewById(R.id.weather_status_text)
        cityInput = findViewById(R.id.city_txt_input)
        cityBtn = findViewById(R.id.city_btn)
        voiceBtn = findViewById(R.id.weather_img)

        textToSpeech = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech!!.language = Locale.US
                Toast.makeText(
                    this@MainActivity5,
                    "Tap on the screen and say the name of city",
                    Toast.LENGTH_SHORT
                ).show()
                textToSpeech!!.speak("Say the name of the city", TextToSpeech.QUEUE_FLUSH, null)
                textToSpeech!!.setSpeechRate(1f)
            }
            Handler(Looper.getMainLooper()).postDelayed({ voiceToText() }, 2000)
        }

        voiceBtn!!.setOnClickListener { voiceToText() }
    }

    private fun voiceToText() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(
            RecognizerIntent.EXTRA_PROMPT,
            "Say Something!!"
        )
        try {
            startActivityForResult(intent, VOICE_CODE)
        } catch (e: ActivityNotFoundException) {
            Log.e("VoiceRecognition", "Speech to Text not supported", e)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VOICE_CODE && resultCode == RESULT_OK && data != null) {
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            cityInput?.setText(result?.get(0))
            fetchWeatherData()
        }
    }

    private fun fetchWeatherData() {
        val city = cityInput?.text.toString().trim()
        if (city.isEmpty()) {
            Toast.makeText(this, "Enter city name", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=b761b4cfe64507fdd7579ab7daf29996&units=metric"

        val requestQueue = Volley.newRequestQueue(this)
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                try {
                    val mainObject = response.getJSONObject("main")
                    val weatherArray = response.getJSONArray("weather")
                    val description = weatherArray.getJSONObject(0)
                    val iconId = description.getString("icon")
                    val temp = Math.round(mainObject.getDouble("temp")).toString() + "°C"
                    val desc = description.getString("main")
                    updateUI(temp, desc)
                    setIcon(iconId)
                } catch (e: JSONException) {
                    Log.e("JSONParsing", "Error parsing JSON", e)
                }
            },
            { error ->
                Log.e("APIRequest", "Error fetching weather data", error)
            }
        )
        requestQueue.add(jsonObjectRequest)
    }

    private fun updateUI(temperature: String, description: String) {
        temperatureText?.text = temperature
        weatherStatusText?.text = description
        cityTextView?.text = cityInput?.text.toString()
    }

    private fun setIcon(iconId: String) {
        // Set the appropriate icon based on the weather icon ID
        // (You can implement this according to your icon resources)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            startActivity(Intent(applicationContext, MainActivity::class.java))
            Handler(Looper.getMainLooper()).postDelayed({
                textToSpeech!!.speak(
                    "You are in the main menu. Just swipe right and say what you want",
                    TextToSpeech.QUEUE_FLUSH,
                    null
                )
            }, 1000)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        if (cityInput?.text.toString().equals("exit", ignoreCase = true)) {
            finish()
        }
        super.onDestroy()
    }

    override fun onPause() {
        textToSpeech?.stop()
        super.onPause()
    }
}
