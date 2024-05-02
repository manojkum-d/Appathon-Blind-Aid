package com.example.software2.ocrhy

import android.app.IntentService
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.ResultReceiver
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

class GetAllData : IntentService(IDENTIFIER) {
    private var addressResultReceiver: ResultReceiver? = null
    private var textToSpeech: TextToSpeech? = null

    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) {
            Log.e("GetAddressIntentService", "Intent is null, cannot proceed.")
            return
        }

        addressResultReceiver = intent.getParcelableExtra("add_receiver")
        if (addressResultReceiver == null) {
            Log.e("GetAddressIntentService", "No receiver, not processing the request further")
            return
        }

        val location = intent.getParcelableExtra<Location>("add_location")
        if (location == null) {
            val msg = "No location, can't go further without location"
            textToSpeech?.speak(msg, TextToSpeech.QUEUE_FLUSH, null)
            sendResultsToReceiver(0, msg)
            return
        }

        val geocoder = Geocoder(this, Locale.getDefault())
        var addresses: List<Address>? = null
        try {
            addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        } catch (ioException: Exception) {
            Log.e("GetAddressIntentService", "Error in getting address for the location", ioException)
        }
        if (addresses == null || addresses.isEmpty()) {
            val msg = "No address found for the location"
            sendResultsToReceiver(1, msg)
        } else {
            val address = addresses[0]
            val addressDetails = """
                ${address.featureName}.
                Locality is, ${address.locality}.
                City is ,${address.subAdminArea}.
                State is, ${address.adminArea}.
                Country is, ${address.countryName}.
                
                """.trimIndent()
            sendResultsToReceiver(2, addressDetails)
        }
    }

    private fun sendResultsToReceiver(resultCode: Int, message: String) {
        val bundle = Bundle()
        bundle.putString("address_result", message)
        addressResultReceiver?.send(resultCode, bundle)
    }

    companion object {
        private const val IDENTIFIER = "GetAddressIntentService"
    }
}
