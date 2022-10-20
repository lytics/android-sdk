package com.lytics.android

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
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
}
