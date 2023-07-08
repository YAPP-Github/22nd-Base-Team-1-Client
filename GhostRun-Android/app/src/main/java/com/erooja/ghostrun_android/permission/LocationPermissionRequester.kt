package com.erooja.ghostrun_android.permission

import android.Manifest
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.erooja.ghostrun_android.permission.LocationPermissionUtil.permissions
import com.erooja.ghostrun_android.state.LocationPermissionState
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject

@ActivityScoped
class LocationPermissionRequester @Inject constructor(
    @ActivityContext private val context: Context,
) {
    private val lifecycleScope: CoroutineScope
        get() = (context as ComponentActivity).lifecycleScope

    private var resolutionResultLauncher: ActivityResultLauncher<IntentSenderRequest>? = null
    private var requestPermissionLauncher: ActivityResultLauncher<Array<String>>? = null

    val permissionFlow: MutableSharedFlow<LocationPermissionState> = MutableSharedFlow()

    init {
        initializeLauncher()
    }

    private fun initializeLauncher() {
        resolutionResultLauncher = (context as ComponentActivity).activityResultRegistry.register(
            RESOLUTION_RESULT,
            ActivityResultContracts.StartIntentSenderForResult()
        ) {
            if (it.resultCode == ComponentActivity.RESULT_OK) {
                emitFlow(LocationPermissionState.ObtainLocationPermission)
            } else {
                emitFlow(LocationPermissionState.Error.PermissionFail)
            }
        }

        requestPermissionLauncher = context.activityResultRegistry.register(
            REQUEST_LOCATION_PERMISSION, ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    emitFlow(LocationPermissionState.ObtainLocationPermission)
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    emitFlow(LocationPermissionState.Error.PermissionFail)
                }
                else -> {
                    emitFlow(LocationPermissionState.Error.PermissionFail)
                }
            }
        }
    }

    private fun emitFlow(state: LocationPermissionState) = lifecycleScope.launch {
        permissionFlow.emit(state)
    }

    fun requestPermission() {
        requestPermissionLauncher?.launch(permissions)
    }

    fun requestResolution(request: IntentSenderRequest) {
        resolutionResultLauncher?.launch(request)
    }

    companion object {
        private const val RESOLUTION_RESULT = "RESOLUTION_RESULT"
        private const val REQUEST_LOCATION_PERMISSION = "REQUEST_LOCATION_PERMISSION"
    }
}

