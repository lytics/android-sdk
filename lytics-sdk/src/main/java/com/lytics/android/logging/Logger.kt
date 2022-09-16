package com.lytics.android.logging

/**
 * abstract logger interface for handling logging
 */
abstract class Logger {

    /**
     * The level at which to log
     */
    var logLevel = LogLevel.NONE

    /**
     * Log a message at the given log level
     *
     * @param logLevel the log level
     * @param message the message
     */
    abstract fun log(logLevel: LogLevel, message: String)

    /**
     * Log a throwable and message at the given log level
     *
     * @param logLevel the log level
     * @param throwable the throwable to log
     * @param message the message to log
     */
    abstract fun log(logLevel: LogLevel, throwable: Throwable?, message: String)

    /**
     * Log a VERBOSE message
     *
     * @param message message to log
     */
    fun verbose(message: String) {
        log(LogLevel.VERBOSE, message)
    }

    /**
     * Log a DEBUG message
     *
     * @param message message to log
     */
    fun debug(message: String) {
        log(LogLevel.DEBUG, message)
    }

    /**
     * Log an INFO message
     *
     * @param message message to log
     */
    fun info(message: String) {
        log(LogLevel.INFO, message)
    }

    /**
     * Log a WARN message
     *
     * @param message message to log
     */
    fun warn(message: String) {
        log(LogLevel.WARN, message)
    }

    /**
     * Log a WARN message
     *
     * @param throwable the throwable to log
     * @param message message to log
     */
    fun warn(throwable: Throwable?, message: String) {
        log(LogLevel.WARN, throwable, message)
    }

    /**
     * Log an ERROR message
     *
     * @param message message to log
     */
    fun error(message: String) {
        log(LogLevel.ERROR, message)
    }

    /**
     * Log an ERROR message
     *
     * @param throwable the throwable to log
     * @param message message to log
     */
    fun error(throwable: Throwable?, message: String) {
        log(LogLevel.ERROR, throwable, message)
    }
}