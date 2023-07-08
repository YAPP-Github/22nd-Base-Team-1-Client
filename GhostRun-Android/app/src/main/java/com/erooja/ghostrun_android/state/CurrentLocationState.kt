package com.erooja.ghostrun_android.state

sealed class CurrentLocationState {
    object UnInitialized: CurrentLocationState()

    object Prepared: CurrentLocationState()

    data class Success(
        val coordinate: Coordinate = Coordinate.Default
    ) : CurrentLocationState()

    object Error : CurrentLocationState()
}
