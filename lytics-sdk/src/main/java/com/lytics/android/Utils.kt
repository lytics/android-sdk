package com.lytics.android

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Process
import java.util.*

internal object Utils {
    /**
     * Generates a random UUID string
     *
     * @return a random UUID string. ex "25eebcba-5ec9-43fe-9179-dafd8d8dd157"
     */
    fun generateUUID(): String {
        return UUID.randomUUID().toString()
    }

    /**
     * Check to see if a given permission has been granted
     */
    fun hasPermission(context: Context, permission: String): Boolean {
        return (context.checkPermission(
            permission,
            Process.myPid(),
            Process.myUid()
        ) == PackageManager.PERMISSION_GRANTED)
    }

    /**
     * Get the current, active networks connection status.  If we cannot access network state due to lacking permissions
     * or no connectivity manager, return null indicating unknown connection status. Otherwise get the active network
     * and return it's connection status.
     *
     * @return true if connected, false if not connected, null if unknown or cannot check
     */
    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    fun getConnectionStatus(context: Context?): Boolean? {
        if (context == null) {
            Lytics.logger.debug("No context to check network status.")
            return null
        }
        // check if we can event check network connection status
        if (!hasPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
            Lytics.logger.debug("No ACCESS_NETWORK_STATE permission to check network status.")
            return null
        }
        val connectivityManager: ConnectivityManager? =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (connectivityManager == null) {
            Lytics.logger.debug("ConnectivityManager is null. Cannot check network status.");
            return null
        }

        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        if (activeNetworkInfo == null) {
            Lytics.logger.debug("No active network, no connection")
            return false
        }
        val isConnected = activeNetworkInfo.isConnected
        Lytics.logger.debug("Active network is connected: $isConnected")
        return isConnected
    }

    /**
     * Attempt to get the Google Android Advertising Id if the library is installed
     *
     * @return the Android advertising id or null if not installed or empty string if limit ad tracking is enabled
     */
    fun getAdvertisingId(context: Context): String? {
        try {
            val advertisingIdClientClass = Class.forName("com.google.android.gms.ads.identifier.AdvertisingIdClient")
            val getAdvertisingIdInfoMethod = advertisingIdClientClass.getMethod("getAdvertisingIdInfo", Context::class.java)
            val advertisingInfo = getAdvertisingIdInfoMethod.invoke(null, context)

            val isLimitAdTrackingEnabledMethod = advertisingInfo.javaClass.getMethod("isLimitAdTrackingEnabled")
            val limitAdTrackingEnabled = isLimitAdTrackingEnabledMethod.invoke(advertisingInfo) as Boolean
            if (limitAdTrackingEnabled) {
                return ""
            }

            val getIdMethod = advertisingInfo.javaClass.getMethod("getId")
            val advertisingId = getIdMethod.invoke(advertisingInfo) as String
            return advertisingId
        } catch (cnfe: Exception) {
            Lytics.logger.debug("Error getting ad id: $cnfe")
        }
        return null
    }

    /**
     * Returns a cleaned stream value. if null or blank, use default config stream. Replace spaces with underscores.
     */
    fun streamify(stream: String?): String {
        if (stream.isNullOrBlank()) {
            return Lytics.configuration.defaultStream
        }
        return stream.trim().replace("""\s+""".toRegex(), "_")
    }
}
