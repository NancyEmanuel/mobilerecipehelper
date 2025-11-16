package com.example.recipegroceryhelper

// Imports for permissions, location services, maps, and Google Places API
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.gms.common.api.ApiException

// This activity shows a map and finds nearby grocery stores
class NearbyGroceryStoresMaps : AppCompatActivity(), OnMapReadyCallback {

    // Google Map object
    private lateinit var mMap: GoogleMap

    // Client for the Google Places API
    private lateinit var placesClient: PlacesClient

    // Client for accessing the user’s location (GPS / network)
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Custom request code for location permission
    private val locationPermissionRequestCode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_grocery_stores_maps)

        // Load the API key from secrets.properties
        val apiKey = BuildConfig.GOOGLE_MAPS_API_KEY

        // Safety check in case API key is missing
        if (apiKey.isEmpty() || apiKey == "YOUR_API_KEY_HERE") {
            Toast.makeText(this, "ERROR: Google Maps API Key is missing. Check secrets.properties.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Initialize the Google Places API
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }
        placesClient = Places.createClient(this)

        // Initialize the fused location provider (for GPS)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Load the map fragment from the layout
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        // Tell the map fragment to notify us when the map is ready
        mapFragment.getMapAsync(this)
    }

    // This runs when the map is fully loaded on screen
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        checkLocationPermission() // Next step: make sure location permission is granted
    }

    // Checks if we have permission to access device location
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission already granted → get location
            getUserLocationAndFindStores()
        } else {
            // Ask user for permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionRequestCode
            )
        }
    }

    // Get the user's last known location, then search for stores
    private fun getUserLocationAndFindStores() {
        // Final safety check in case permission was not granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Enable the blue “You are here” dot
        mMap.isMyLocationEnabled = true

        // Request the phone’s last known GPS location
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                // Convert the location to a LatLng object
                val currentLatLng = LatLng(location.latitude, location.longitude)

                // Move the map camera to the user's location
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14f))

                // Now search for grocery stores near the location
                findNearbyGroceryStores()

            } else {
                Toast.makeText(
                    this,
                    "Could not get location. Make sure location is enabled on the device.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Uses Google Places API to find nearby grocery stores
    private fun findNearbyGroceryStores() {

        // Safety check again for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Select what information we want from each place
        val placeFields = listOf(
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.TYPES
        )

        // Creates a request to find places around the user
        val request = FindCurrentPlaceRequest.newInstance(placeFields)

        // Send the request to Google Places
        placesClient.findCurrentPlace(request)
            .addOnSuccessListener { response ->

                // Use green markers for grocery stores
                val greenMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)

                // Clear old markers
                mMap.clear()

                var storesFound = 0

                // Loop through every nearby place Google found
                for (placeLikelihood in response.placeLikelihoods) {

                    val place = placeLikelihood.place

                    // Check if this place is a grocery store or supermarket
                    val isGrocery =
                        place.types?.any {
                            it == Place.Type.GROCERY_OR_SUPERMARKET ||
                            it == Place.Type.SUPERMARKET
                        } == true

                    if (isGrocery) {
                        // Add a green marker on the map
                        mMap.addMarker(
                            MarkerOptions()
                                .position(place.latLng!!)
                                .title(place.name)
                                .icon(greenMarker)
                        )
                        storesFound++
                    }
                }

                if (storesFound == 0) {
                    Toast.makeText(this, "No nearby grocery stores found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                if (exception is ApiException) {
                    Log.e(
                        "MapsActivity",
                        "Place not found: ${exception.statusCode} ${exception.message}"
                    )
                }
            }
    }

    // Called after the user responds to the permission popup
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == locationPermissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission accepted → fetch location
                getUserLocationAndFindStores()
            } else {
                // Permission denied
                Toast.makeText(this, "Location permission is required to show nearby stores.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
