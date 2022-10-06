package com.lytics.android

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.lytics.android.events.LyticsConsentEvent
import com.lytics.android.events.LyticsEvent
import com.lytics.android.events.LyticsIdentityEvent
import com.lytics.android.logging.AndroidLogger
import com.lytics.android.logging.Logger
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

    private var sharedPreferences: SharedPreferences? = null

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

        sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        isOptedIn = sharedPreferences?.getBoolean(Constants.KEY_IS_OPTED_IN, false) ?: false
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
    fun optIn() {
        logger.info("Opt in!")
        isOptedIn = true
        sharedPreferences?.edit {
            putBoolean(Constants.KEY_IS_OPTED_IN, true)
        }
    }

    /**
     * Opt the user out of event collection
     */
    fun optOut() {
        logger.info("Opt out!")
        isOptedIn = false
        sharedPreferences?.edit {
            putBoolean(Constants.KEY_IS_OPTED_IN, false)
        }
    }

    /**
     * returns true if the user has opted into event collection
     */
    var isOptedIn: Boolean = false
        private set

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