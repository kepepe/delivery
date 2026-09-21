package com.example.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.model.DeliveryOrder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LocationTrackingManager(private val context: Context) {
  private val fusedLocationClient: FusedLocationProviderClient =
    LocationServices.getFusedLocationProviderClient(context)

  private var locationCallback: LocationCallback? = null
  private var simulationJob: Job? = null
  private val scope = CoroutineScope(Dispatchers.Default)

  var isTracking: Boolean = false
    private set

  fun hasLocationPermission(): Boolean {
    val fine = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return fine || coarse
  }

  @SuppressLint("MissingPermission")
  fun startTracking(
    order: DeliveryOrder,
    onLocationUpdate: (lat: Double, lng: Double, heading: Float) -> Unit
  ) {
    stopTracking()
    isTracking = true

    if (hasLocationPermission()) {
      try {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
          .setMinUpdateIntervalMillis(2000L)
          .setMinUpdateDistanceMeters(2f)
          .build()

        locationCallback = object : LocationCallback() {
          override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            onLocationUpdate(location.latitude, location.longitude, location.bearing)
          }
        }

        fusedLocationClient.requestLocationUpdates(
          locationRequest,
          locationCallback!!,
          Looper.getMainLooper()
        )
      } catch (e: Exception) {
        Log.e("LocationTracking", "Error starting GPS updates: ${e.message}")
        startRouteSimulation(order, onLocationUpdate)
      }
    } else {
      // Graceful fallback for emulator or when permission is not yet granted
      startRouteSimulation(order, onLocationUpdate)
    }
  }

  private fun startRouteSimulation(
    order: DeliveryOrder,
    onLocationUpdate: (lat: Double, lng: Double, heading: Float) -> Unit
  ) {
    simulationJob?.cancel()
    simulationJob = scope.launch {
      var progress = 0.05f
      val startLat = order.startLat.toDouble()
      val startLng = order.startLng.toDouble()
      val endLat = order.endLat.toDouble()
      val endLng = order.endLng.toDouble()

      while (isActive && isTracking) {
        val curLat = startLat + (endLat - startLat) * progress
        val curLng = startLng + (endLng - startLng) * progress
        val heading = 45f

        onLocationUpdate(curLat, curLng, heading)

        delay(3000)
        progress += 0.03f
        if (progress > 0.95f) {
          progress = 0.95f
        }
      }
    }
  }

  fun stopTracking() {
    isTracking = false
    locationCallback?.let {
      fusedLocationClient.removeLocationUpdates(it)
      locationCallback = null
    }
    simulationJob?.cancel()
    simulationJob = null
  }
}
