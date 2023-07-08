package com.erooja.ghostrun_android.state

sealed class LocationPermissionState {
    object ObtainLocationPermission: LocationPermissionState()

    sealed class Error : LocationPermissionState() {
        object PermissionFail : Error()
    }
}
