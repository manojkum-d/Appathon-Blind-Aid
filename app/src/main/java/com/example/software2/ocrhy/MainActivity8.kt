package com.example.software2.ocrhy

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.MotionEvent
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*

import java.util.Locale

class MainActivity8 : AppCompatActivity() {
    private var x1 = 0f
    private var x2 = 0f
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var addressResultReceiver: LocationAddressResultReceiver? = null
    private var currentAddTv: TextView? = null
    private var currentLocation: Location? = null
    private var locationCallback: LocationCallback? = null
    private var textToSpeech: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main8)
        addressResultReceiver = LocationAddressResultReceiver(Handler(Looper.getMainLooper()))
        currentAddTv = findViewById(R.id.textView)
        textToSpeech = TextToSpeech(this@MainActivity8) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech!!.language = Locale.getDefault()
                textToSpeech!!.setSpeechRate(1f)
                textToSpeech!!.speak(
                    "swipe left to get current location and swipe right to return in main menu",
                    TextToSpeech.QUEUE_FLUSH,
                    null
                )
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                currentLocation = locationResult.locations[0]
                getAddress()
            }
        }
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            val locationRequest = LocationRequest()
            locationRequest.interval = 2000
            locationRequest.fastestInterval = 1000
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//            fusedLocationClient!!.requestLocationUpdates(locationRequest, locationCallback!!, null)
        }
    }

    private fun getAddress() {
        if (!Geocoder.isPresent()) {
            Toast.makeText(
                this@MainActivity8, "Can't find current address, ",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val intent = Intent(this, GetAllData::class.java)
        intent.putExtra("add_receiver", addressResultReceiver)
        intent.putExtra("add_location", currentLocation)
        startService(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                Toast.makeText(
                    this,
                    "Location permission not granted, restart the app if you want the feature",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private inner class LocationAddressResultReceiver(handler: Handler?) :
        ResultReceiver(handler ?: Looper.getMainLooper()?.let { Handler(it) }) {
        override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
            if (resultCode == 0) {
                Log.d("Address", "Location null retrying")
                getAddress()
            }
            if (resultCode == 1) {
                Toast.makeText(this@MainActivity8, "Address not found", Toast.LENGTH_SHORT).show()
            }
            val currentAdd = resultData.getString("address_result")
            showResults(currentAdd)
        }
    }

    private fun showResults(currentAdd: String?) {
        currentAddTv!!.text = currentAdd
    }

    override fun onTouchEvent(touchEvent: MotionEvent): Boolean {
        when (touchEvent.action) {
            MotionEvent.ACTION_DOWN -> x1 = touchEvent.x
            MotionEvent.ACTION_UP -> {
                x2 = touchEvent.x
                if (x1 < x2) {
                    val data = currentAddTv!!.text.toString()
                    if (data.isEmpty()) {
                        textToSpeech!!.speak(
                            "Please turn on location",
                            TextToSpeech.QUEUE_FLUSH,
                            null
                        )
                    } else {
                        textToSpeech!!.speak(
                            "Your current location is $data",
                            TextToSpeech.QUEUE_FLUSH,
                            null
                        )
                        textToSpeech!!.speak(
                            "Swipe left to listen again or swipe right to return back to the main menu",
                            TextToSpeech.QUEUE_ADD,
                            null
                        )
                    }
                }
                if (x1 > x2) {
                    val i = Intent(this@MainActivity8, MainActivity::class.java)
                    startActivity(i)
                    val handler = Handler(Looper.getMainLooper())
                    handler.post {
                        textToSpeech!!.speak(
                            "You are in the main menu. Swipe right and say what you want.",
                            TextToSpeech.QUEUE_FLUSH,
                            null
                        )
                    }
                }
            }
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient!!.removeLocationUpdates(locationCallback!!)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 2
    }
}
