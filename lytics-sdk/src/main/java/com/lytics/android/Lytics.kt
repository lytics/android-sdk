package com.lytics.android

import android.content.Context
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.lytics.android.events.LyticsConsentEvent
import com.lytics.android.events.LyticsEvent
import com.lytics.android.events.LyticsIdentityEvent
import com.lytics.android.logging.AndroidLogger
import com.lytics.android.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

object Lytics {
    /**
     * a weak reference to the Android application context
     */
    private var contextRef: WeakReference<Context>? = null

    /**
     * Configuration for the Lytics SDK
     */
    private var configuration: LyticsConfiguration? = null

    /**
     * The Lytics SDK logger
     */
    private var logger: Logger = AndroidLogger

    /**
     * Initialize the Lytics SDK with the given configuration
     *
     * @param context the Android applications context
     * @param configuration The Lytics configuration
     */
    fun init(context: Context, configuration: LyticsConfiguration) {
        if (isInitialized) {
            logger.warn("Lytics SDK already initialized")
            return
        }
        contextRef = WeakReference(context)
        this.configuration = configuration
        logger.logLevel = configuration.logLevel
    }

    /**
     * Returns true if this singleton instance has been initialized
     */
    val isInitialized: Boolean
        get() = contextRef != null && configuration != null

    /**
     * Returns the current Lytics user
     */
    fun currentUser(): LyticsUser {
        return LyticsUser()
    }

    /**
     * Updates the user properties and optionally emits an identity event
     */
    fun identify(event: LyticsIdentityEvent) {}

    /**
     * Track a custom event
     */
    fun track(event: LyticsEvent) {}

    /**
     * Emits a special event that represents a screen or page view. Device properties are injected into the payload
     * before emitting
     */
    fun screen(event: LyticsEvent) {}

    /**
     * Updates a user consent properties and optionally emits a special event that represents an app user's explicit
     * consent
     */
    fun consent(event: LyticsConsentEvent) {}

    /**
     * Opts the user into event collection.
     */
    fun optIn() {}

    /**
     * Opt the user out of event collection
     */
    fun optOut() {}

    /**
     * returns true if the user has opted into event collection
     */
    val isOptedIn: Boolean
        get() {
            return true
        }

    /**
     * Enable sending the IDFA, Android Advertising ID, with events. This value could still be disabled by the user in
     * the Android OS privacy settings.
     */
    fun enableIDFA() {}

    /**
     * Disables sending the IDFA, Android Advertising ID, with events.
     */
    fun disableIDFA() {}

    /**
     * Returns if IDFA is enabled
     */
    val isIDFAEnabled: Boolean
        get() {
            return false
        }

    /**
     * Force flush the event queue by sending all events in the queue immediately.
     */
    fun dispatch() {}

    /**
     * Clears all stored user information.
     */
    fun reset() {}
}