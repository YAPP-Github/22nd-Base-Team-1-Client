package com.erooja.ghostrun_android.state

data class Coordinate(
    val x: Double,
    val y: Double
) {
    companion object {
        val Default = Coordinate(
            x = Double.NaN,
            y = Double.NaN
        )
    }
}
