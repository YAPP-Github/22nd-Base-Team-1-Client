package com.erooja.ghostrun_android.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.erooja.ghostrun_android.state.CurrentLocationState
import kotlinx.coroutines.flow.MutableStateFlow

object LocationPermissionUtil {
    val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    fun needPermissionRequest(context: Context): Boolean {
        return !isPermissionGranted(context)
    }

    fun isAlreadyPermissionDenied(context: Context): Boolean {
        return permissions.any {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
    }

    fun isPermissionGranted(context: Context): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}
