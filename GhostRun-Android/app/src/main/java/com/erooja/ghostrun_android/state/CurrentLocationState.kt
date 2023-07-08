package com.erooja.ghostrun_android.state

sealed class CurrentLocationState {
    object UnInitialized: CurrentLocationState()

    data class Success(
        val boundBottomLeftCoordinate: Coordinate = Coordinate.Default,
        val boundTopRightCoordinate: Coordinate = Coordinate.Default,
        val centerCoordinate: Coordinate = Coordinate.Default,
    ) : CurrentLocationState()

    object Error : CurrentLocationState()
}
