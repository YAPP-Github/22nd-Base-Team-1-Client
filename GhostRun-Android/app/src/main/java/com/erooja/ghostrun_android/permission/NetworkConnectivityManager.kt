package com.erooja.ghostrun_android.permission

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class NetworkConnectivityManager @Inject constructor(
    @ActivityContext context: Context
) {
    private val connectivityManager: ConnectivityManager by lazy {
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
    }

    private val networkRequest: NetworkRequest by lazy {
        NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build()
    }

    private var onNetworkConnectionSucceed: (() -> Unit)? = null
    private var onNetworkConnectionFailed: (() -> Unit)? = null

    private val networkConnectivityCallback by lazy {
        object: ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                onNetworkConnectionSucceed?.invoke()
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                onNetworkConnectionFailed?.invoke()
            }
        }
    }

    init {
        connectivityManager.registerNetworkCallback(networkRequest, networkConnectivityCallback)
    }

    fun isNotAvailableNetwork(): Boolean {
        val networkCapabilities = connectivityManager.activeNetwork ?: return true
        val activateTransport =
            connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return true

        return !(activateTransport.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                || activateTransport.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || activateTransport.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
    }

    fun setNetworkConnectionSucceed(onNetworkConnectionSucceed: () -> Unit): NetworkConnectivityManager {
        this.onNetworkConnectionSucceed = onNetworkConnectionSucceed
        return this
    }

    fun setNetworkConnectionFailed(onNetworkConnectionFailed: () -> Unit): NetworkConnectivityManager {
        this.onNetworkConnectionFailed = onNetworkConnectionFailed
        return this
    }
}
