package com.example.nowcast

import MySharedPreferences
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.*
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.sql.Timestamp

class MainActivity : AppCompatActivity() {
    private lateinit var startBtn: Button
    private lateinit var nameEditText: TextInputEditText
    private lateinit var idNameText: TextView
    private lateinit var idOrderText: TextView

    private var locationUpdatesEnabled = false

//    private lateinit var databaseReference: DatabaseReference

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            if (locationResult != null) {
                val location: Location = locationResult.lastLocation
                // Handle the location update
                val latitude = location.latitude
                val longitude = location.longitude
                val name = nameEditText.text.toString()
                val timestamp = System.currentTimeMillis()
                // Do something with the coordinates

                // For demonstration purposes, you can log the coordinates
                Log.d("Location: ", "$latitude , $longitude")
                Log.d("ID: ", "$name")

                val locationData = mapOf(
                    "latitude" to latitude,
                    "longitude" to longitude,
                    "timestamp" to timestamp,
                    "name" to name
                )
                postHTTPRequest(latitude, longitude, name)
//                databaseReference.child("$idText").child("$timestamp").setValue(locationData)
            }
        }
    }

    private var BASE_URL = "https://raincheck-drivers.onrender.com/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        databaseReference = FirebaseDatabase.getInstance().getReference("users")
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationUpdatesEnabled = false

        startBtn = findViewById(R.id.btn_start)
        nameEditText = findViewById(R.id.editTextName)
        idNameText = findViewById(R.id.nameTextId)
        idOrderText = findViewById(R.id.orderTextId)

        startBtn.isEnabled = false
        idNameText.text = "Hello, Juan!"
        idOrderText.text = "Input the driver name in the text box."
        nameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun afterTextChanged(editable: Editable?) {
                // Enable or disable the button based on whether the EditText is empty or not
                startBtn.isEnabled = !editable.isNullOrBlank()
            }
        })
        nameEditText.setText(MySharedPreferences.loadData(this, "username"))
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

    @SuppressLint("MissingPermission")
    private fun getLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 30000 // Update location every 30 seconds
        }
        MySharedPreferences.saveData(this, "username", nameEditText.text.toString())
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun startLocationUpdates() {
        locationUpdatesEnabled = true
        startBtn.text = "Disable Tracking"
        getLocationUpdates()
        nameEditText.isEnabled = false
        idNameText.text = "Tracking Enabled!"
        idOrderText.text = "Click Disable Tracking once order is completed."
    }

    private fun stopLocationUpdates() {
        locationUpdatesEnabled = false
        startBtn.text = "Enable Tracking"
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        nameEditText.isEnabled = true
        idNameText.text = "Hello, Juan!"
        idOrderText.text = "Input the driver name in the text box."
    }

    private fun postHTTPRequest(lat: Double, lon: Double, driver_name: String){
        val volleyQueue = Volley.newRequestQueue(this)
        var url = BASE_URL+"enqueue?lat=$lat&lon=$lon&driver_name=$driver_name"
        Log.d("URL: ", url)
        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, url,null,
            { response ->
                Log.d("MainActivity", "Request Sent: ${response.toString()}")
            },
            { error ->
                // make a Toast telling the user
                // that something went wrong
                Toast.makeText(this, "Some error occurred! Cannot send gps location", Toast.LENGTH_LONG).show()
                // log the error message in the error stream
                Log.e("MainActivity", "postHTTPRequest error: ${error.localizedMessage}")
            }
        )
        volleyQueue.add(jsonObjectRequest)
    }
}

