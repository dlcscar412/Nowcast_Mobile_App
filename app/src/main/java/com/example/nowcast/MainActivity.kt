package com.example.nowcast

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.content.SharedPreferences;
import android.text.Editable
import android.text.TextWatcher

class MainActivity : AppCompatActivity() {
    private lateinit var startBtn: Button
    private lateinit var idEditText: EditText
    private var locationUpdatesEnabled = false

    private lateinit var databaseReference: DatabaseReference

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            if (locationResult != null) {
                val location: Location = locationResult.lastLocation
                // Handle the location update
                val latitude = location.latitude
                val longitude = location.longitude
                val idText = idEditText.text.toString()
                val timestamp = System.currentTimeMillis()
                // Do something with the coordinates

                // For demonstration purposes, you can log the coordinates
                Log.d("Location: ", "$latitude , $longitude")
                Log.d("ID: ", "$idText")

                val locationData = mapOf(
                    "latitude" to latitude,
                    "longitude" to longitude,
                    "timestamp" to timestamp,
                    "id" to idText
                )
                databaseReference.child("$idText").child("$timestamp").setValue(locationData)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        databaseReference = FirebaseDatabase.getInstance().getReference("users")
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationUpdatesEnabled = false

        startBtn = findViewById(R.id.btn_start)
        startBtn.isEnabled = false
        idEditText = findViewById(R.id.editTextId)

        idEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun afterTextChanged(editable: Editable?) {
                // Enable or disable the button based on whether the EditText is empty or not
                startBtn.isEnabled = !editable.isNullOrBlank()
            }
        })
        idEditText.setText(MySharedPreferences.loadData(this, "username"))
        startBtn.setOnClickListener {
            if (locationUpdatesEnabled) {
                stopLocationUpdates()
            } else {
                startLocationUpdates()
            }
        }

        Log.d("Username:", MySharedPreferences.loadData(this, "username"))
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permission
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 101)
        }
    }

    private fun getLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 60000 // Update location every 1 minute
        }
        MySharedPreferences.saveData(this, "username", idEditText.text.toString())
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun startLocationUpdates() {
        locationUpdatesEnabled = true
        startBtn.text = "Stop Location Updates"
        getLocationUpdates()
        idEditText.isEnabled = false
    }

    private fun stopLocationUpdates() {
        locationUpdatesEnabled = false
        startBtn.text = "Start Location Updates"
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        idEditText.isEnabled = true
    }
}