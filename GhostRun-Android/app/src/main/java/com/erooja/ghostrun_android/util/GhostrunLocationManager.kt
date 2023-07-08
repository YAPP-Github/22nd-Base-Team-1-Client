package com.erooja.ghostrun_android.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.erooja.ghostrun_android.permission.LocationPermissionUtil
import com.erooja.ghostrun_android.state.Coordinate
import com.erooja.ghostrun_android.state.CurrentLocationState
import dagger.hilt.android.qualifiers.ActivityContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("MissingPermission")
class GhostrunLocationManager @Inject constructor(
    @ActivityContext private val context: Context,
) {
    private val lifecycleScope: CoroutineScope
        get() = (context as ComponentActivity).lifecycleScope

    private lateinit var locationManager: LocationManager

    val trackingLocationFlow: MutableStateFlow<CurrentLocationState> = MutableStateFlow(
        CurrentLocationState.UnInitialized)


    init {
        lifecycleScope.launch {
            (context as ComponentActivity).lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                trackingLocationFlow
                    .filter { LocationPermissionUtil.isPermissionGranted(context) }
                    .filter { it == CurrentLocationState.Prepared }
                    .onEach {
                        locationManager =
                            context.getSystemService(ComponentActivity.LOCATION_SERVICE) as LocationManager
                        locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            1000,
                            0f,
                            locationListener
                        )
                    }
                    .collect()
            }
        }
    }


    private val locationListener = object : LocationListener {
        override fun onLocationChanged(locations: MutableList<Location>) {
            super.onLocationChanged(locations)
            Log.e("onLocationChanged", locations.toString())
        }

        override fun onLocationChanged(location: Location) {
            val longitude = location.longitude
            val latitude = location.latitude

            emitFlow(CurrentLocationState.Success(
                Coordinate(
                    longitude,
                    latitude
                )
            ))

            Log.e("Location", "Latitude : $latitude, Longitude : $longitude")
        }

        override fun onProviderEnabled(provider: String) {
            super.onProviderEnabled(provider)
            // provider가 사용 가능한 생태가 되는 순간 호출
        }

        override fun onProviderDisabled(provider: String) {
            super.onProviderDisabled(provider)
            // provider가 사용 불가능 상황이 되는 순간 호출
        }
    }

    fun getLastKnownLocation(): Coordinate? {
        return if (LocationPermissionUtil.isPermissionGranted(context)) {
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            location?.let { Coordinate(location.longitude, location.latitude) }
        } else {
            null
        }
    }

    private fun emitFlow(state: CurrentLocationState) = lifecycleScope.launch {
        trackingLocationFlow.emit(state)
    }

    fun init() {
        emitFlow(CurrentLocationState.Prepared)
    }
}
