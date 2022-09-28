package com.lytics.android

import com.lytics.android.logging.LogLevel
import java.util.concurrent.TimeUnit

data class LyticsConfiguration(
    /**
     * Lytics account API token
     */
    val apiKey: String,

    /**
     * default stream name to which events will be sent if not explicitly set for an event
     */
    val defaultStream: String,

    /**
     * the key that represents the core identifier to be used in api calls
     */
    val primaryIdentityKey: String = "_uid",

    /**
     * the key which we use to store the anonymous identifier
     */
    val anonymousIdentityKey: String = "_uid",

    /**
     * automatically send application lifecycle events
     */
    val trackApplicationLifecycleEvents: Boolean = true,

    /**
     * automatically send activity/fragment lifecycle events
     */
    val trackScreenViews: Boolean = true,

    /**
     * The max size of the event queue before forcing an upload of the event queue to the Lytics API.
     * Set to 0 to disable. Defaults to 10.
     */
    val maxQueueSize: Int = 10,

    /**
     * The interval in milliseconds at which the event queue is uploaded to the Lytics API. Set to 0 to disable.
     * Defaults to 1 second.
     */
    val uploadInterval: Long = TimeUnit.SECONDS.toMillis(1),

    /**
     * Session timeout in milliseconds. This is the period from when the app enters the background and the session
     * expires, starting a new session.  Defaults to 20 minutes.
     */
    val sessionTimeout: Long = TimeUnit.MINUTES.toMillis(20),

    /**
     * Set the logging level of the SDK. Defaults to no logging output.
     */
    val logLevel: LogLevel = LogLevel.NONE,

    /**
     * Enable sandbox mode which adds a "sandbox" flag to all outbound events. This flag then enables those events to be
     * processed in an alternative way or skipped entirely upon delivery to the Lytics collection APIs.
     *
     * Default is disabled.
     */
    val sandboxMode: Boolean = false,
)
