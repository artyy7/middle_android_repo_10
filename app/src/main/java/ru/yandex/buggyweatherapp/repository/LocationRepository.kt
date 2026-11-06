package ru.yandex.buggyweatherapp.repository

import android.content.Context
import android.location.Geocoder
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.yandex.buggyweatherapp.model.Location
import ru.yandex.buggyweatherapp.utils.LocationTracker
import java.util.Locale

class LocationRepository(
    
    private val context: Context
) {
    
    private val fusedLocationClient: FusedLocationProviderClient = 
        LocationServices.getFusedLocationProviderClient(context)
    
    
    private var currentLocation: Location? = null
    
    
    private var locationCallback: ((Location?) -> Unit)? = null
    
    
    fun getCurrentLocation(callback: (Location?) -> Unit) {
        try {
            locationCallback = callback
            
            
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val userLocation = Location(
                            latitude = location.latitude,
                            longitude = location.longitude
                        )
                        currentLocation = userLocation
                        callback(userLocation)
                    } else {
                        
                        requestLocationUpdates(callback)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("LocationRepository", "Error getting location", e)
                    callback(null)
                }
        } catch (e: SecurityException) {
            Log.e("LocationRepository", "Location permission not granted", e)
            callback(null)
        }
    }
    
    
    private fun requestLocationUpdates(callback: (Location?) -> Unit) {
        try {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(5000)
                .build()
            
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { location ->
                        val userLocation = Location(
                            latitude = location.latitude,
                            longitude = location.longitude
                        )
                        currentLocation = userLocation

                        fusedLocationClient.removeLocationUpdates(this)

                        callback(userLocation)
                        
                        
                    }
                }
            }
            
            
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e("LocationRepository", "Location permission not granted", e)
            callback(null)
        }
    }

    suspend fun getCityNameFromLocation(location: Location): String? = withContext(Dispatchers.IO) {
        try {
            
            val geocoder = Geocoder(context, Locale.getDefault())
            
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                if (address.locality != null) {
                    address.locality
                } else if (address.subAdminArea != null) {
                    address.subAdminArea
                } else {
                    address.adminArea
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("LocationRepository", "Error getting city name", e)
            null
        }
    }
    
    
    fun startLocationTracking() {
        LocationTracker.getInstance(context).startTracking()
    }
    
    
}