package com.erooja.ghostrun_android

import com.erooja.ghostrun_android.state.Coordinate
sealed class MainUiState {
    object Loading : MainUiState()
    data class Success(val coordinate: Coordinate) : MainUiState()
}
