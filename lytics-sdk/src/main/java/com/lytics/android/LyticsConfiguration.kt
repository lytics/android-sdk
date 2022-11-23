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
     * automatically send a screen event when an activity resumes
     */
    val autoTrackActivityScreens: Boolean = false,

    /**
     * automatically send a screen event when a fragment resumes
     */
    val autoTrackFragmentScreens: Boolean = false,

    /**
     * automatically track when the application is opened
     */
    val autoTrackAppOpens: Boolean = false,

    /**
     * The max size of the event queue before forcing an upload of the event queue to the Lytics API.
     * Set to 0 to disable. Defaults to 10.
     */
    val maxQueueSize: Int = 10,

    /**
     * The max number of times to try and resend an event on failure
     */
    val maxRetryCount: Int = 3,

    /**
     * The interval in milliseconds at which the event queue is uploaded to the Lytics API. Set to 0 to disable.
     * Defaults to 1 second.
     */
    val uploadInterval: Long = TimeUnit.SECONDS.toMillis(10),

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

    /**
     * The bulk endpoint URLs to send events. The stream name will be appended to the endpoint URLs.
     *
     * As an example, for the default endpoint of `https://api.lytics.io/collect/json/` events for the stream `crm` will
     * be sent to `https://api.lytics.io/collect/json/crm`
     */
    val collectionEndpoint: String = DEFAULT_COLLECTION_ENDPOINT,

    /**
     * Network request connect and read timeout in milliseconds. Defaults to 30 seconds.
     */
    val networkRequestTimeout: Int = TimeUnit.SECONDS.toMillis(30).toInt(),
) {
    companion object {
        val DEFAULT_COLLECTION_ENDPOINT = "https://api.lytics.io/collect/json/"
    }
}
