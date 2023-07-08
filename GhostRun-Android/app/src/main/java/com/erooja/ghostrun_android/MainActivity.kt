package com.erooja.ghostrun_android

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.erooja.ghostrun_android.permission.LocationPermissionRequester
import com.erooja.ghostrun_android.permission.LocationPermissionUtil
import com.erooja.ghostrun_android.state.CurrentLocationState
import com.erooja.ghostrun_android.state.LocationPermissionState
import com.erooja.ghostrun_android.ui.theme.GhostRun_AndroidTheme
import com.erooja.ghostrun_android.util.GhostrunLocationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var permissionRequester: LocationPermissionRequester

    @Inject
    lateinit var locationManager: GhostrunLocationManager

    private var uiState: MainUiState by mutableStateOf(MainUiState.Loading)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                permissionRequester
                    .permissionFlow
                    .collect { permissionState ->
                        Log.e("permissonState", permissionState.toString())
                        when (permissionState) {
                            is LocationPermissionState.ObtainLocationPermission -> {
                                locationManager.init()
                            }
                            is LocationPermissionState.Error -> Unit
                        }
                    }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                locationManager
                    .trackingLocationFlow
                    .collect { locationState ->
                        Log.e("locationState", locationState.toString())
                        when (locationState) {
                            is CurrentLocationState.Success -> {
                                if (LocationPermissionUtil.isPermissionGranted(this@MainActivity)) {
                                    uiState = MainUiState.Success(locationState.coordinate)
                                }
                            }
                            else -> Unit
                        }
                    }
            }
        }

        setContent {
            permissionRequester.requestPermission()

            GhostRun_AndroidTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CoordinateText(uiState)
                }
            }
        }
    }
}

@Composable
fun CoordinateText(uiState: MainUiState) {

    if (uiState is MainUiState.Success)
        Column {
            Text(
                text = "longitude is  ${uiState.coordinate.x}!",
            )
            Text(
                text = "latitude is  ${uiState.coordinate.y}!",
            )
        }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
}
