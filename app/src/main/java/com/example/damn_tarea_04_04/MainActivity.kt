package com.example.damn_tarea_04_04

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        progressBar = findViewById(R.id.progressBar)
        webView = findViewById(R.id.webView)

        setupWebView()

        findViewById<Button>(R.id.btnOpenMaps).setOnClickListener {
            checkLocationPermissionAndOpenMaps()
        }

        findViewById<Button>(R.id.btnShowInApp).setOnClickListener {
            checkLocationPermissionAndLoadMap()
        }

        findViewById<Button>(R.id.btnRefresh).setOnClickListener {
            checkLocationPermissionAndLoadMap()
        }
    }

    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            setGeolocationEnabled(true)
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                hideProgressBar()
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onGeolocationPermissionsShowPrompt(
                origin: String,
                callback: GeolocationPermissions.Callback
            ) {
                if (checkLocationPermission()) {
                    callback.invoke(origin, true, false)
                } else {
                    requestLocationPermission()
                }
            }
        }
    }

    private fun checkLocationPermissionAndOpenMaps() {
        if (checkLocationPermission()) {
            openGoogleMapsExternal()
        } else {
            requestLocationPermission()
        }
    }

    private fun checkLocationPermissionAndLoadMap() {
        if (checkLocationPermission()) {
            showProgressBar()
            loadMapWithCurrentLocation()
        } else {
            requestLocationPermission()
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    @SuppressLint("MissingPermission")
    private fun loadMapWithCurrentLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                loadGoogleMapsInWebView(location.latitude, location.longitude)
            } else {
                loadDefaultMap()
            }
        }.addOnFailureListener {
            loadDefaultMap()
        }
    }

    private fun loadGoogleMapsInWebView(latitude: Double, longitude: Double) {
        val url = "https://maps.google.com/maps?q=$latitude,$longitude&z=15"
        webView.loadUrl(url)
    }

    private fun loadDefaultMap() {
        webView.loadUrl("https://maps.google.com/maps?q=19.4326,-99.1332")
    }

    private fun openGoogleMapsExternal() {
        val gmmIntentUri = Uri.parse("geo:0,0?q=my+location")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            setPackage("com.google.android.apps.maps")
        }
        startActivity(mapIntent)
    }

    private fun showProgressBar() {
        progressBar.visibility = ProgressBar.VISIBLE
    }

    private fun hideProgressBar() {
        progressBar.visibility = ProgressBar.GONE
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationPermissionAndLoadMap()
            } else {
                Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }
}