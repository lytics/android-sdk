package com.lytics.android.logging

import android.util.Log

internal object AndroidLogger : Logger() {
    private const val TAG = "Lytics-SDK"

    override fun log(logLevel: LogLevel, message: String) {
        log(logLevel, null, message)
    }

    override fun log(logLevel: LogLevel, throwable: Throwable?, message: String) {
        if (logLevel >= this.logLevel) {
            when (logLevel) {
                LogLevel.VERBOSE -> Log.v(TAG, message, throwable)
                LogLevel.DEBUG -> Log.d(TAG, message, throwable)
                LogLevel.INFO -> Log.i(TAG, message, throwable)
                LogLevel.WARN -> Log.w(TAG, message, throwable)
                LogLevel.ERROR -> Log.e(TAG, message, throwable)
                else -> Log.d(TAG, message, throwable)
            }
        }
    }
}