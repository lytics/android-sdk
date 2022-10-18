package com.lytics.android

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.lytics.android.events.LyticsConsentEvent
import com.lytics.android.events.LyticsEvent
import com.lytics.android.events.LyticsIdentityEvent
import com.lytics.android.logging.AndroidLogger
import com.lytics.android.logging.Logger
import org.json.JSONObject
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
     * The current Lytics user
     */
    var currentUser: LyticsUser? = null
        private set

    /**
     * persistent storage for Lytics data
     */
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
        isOptedIn = sharedPreferences.getBoolean(Constants.KEY_IS_OPTED_IN, false)
        isIDFAEnabled = sharedPreferences.getBoolean(Constants.KEY_IS_IDFA_ENABLED, false)
        currentUser = loadCurrentUser()
    }

    /**
     * Load the current user from shared pref JSON string. If no value in shared preferences, or error parsing the JSON,
     * return a new, default Lytics user
     */
    private fun loadCurrentUser(): LyticsUser {
        return kotlin.runCatching {
            val json = sharedPreferences?.getString(Constants.KEY_CURRENT_USER, null)
            if (json.isNullOrBlank()) {
                logger.debug("existing user data not found, creating a new Lytics user")
                createDefaultLyticsUser()
            } else {
                val user = LyticsUser(JSONObject(json))
                logger.debug("found existing Lytics user: $user")
                user
            }
        }.fold(
            onSuccess = { it },
            onFailure = {
                logger.error(it, "Error loading current user, creating a new Lytics user")
                createDefaultLyticsUser()
            }
        )
    }

    /**
     * Creates a Lytics user with only a random UUID set to the anonymous ID key per configuration
     */
    private fun createDefaultLyticsUser(): LyticsUser {
        val user = LyticsUser(identifiers = mapOf(configuration!!.anonymousIdentityKey to Utils.generateUUID()))
        saveCurrentUser(user)
        return user
    }

    /**
     * Save the given user to the userFile for persistence
     */
    private fun saveCurrentUser(user: LyticsUser) {
        sharedPreferences?.edit {
            putString(Constants.KEY_CURRENT_USER, user.serialize().toString())
        }
    }

    /**
     * Returns true if this singleton instance has been initialized
     */
    val isInitialized: Boolean
        get() = contextRef != null && configuration != null


    /**
     * Updates the user properties and optionally emits an identity event
     */
    fun identify(event: LyticsIdentityEvent) {
        logger.info("Identify Event: $event")
        currentUser?.let { user ->
            val existingIdentifiers = user.identifiers ?: emptyMap()
            val existingAttributes = user.attributes ?: emptyMap()
            val updatedIdentifiers = existingIdentifiers.plus(event.identifiers ?: emptyMap())
            val updatedAttributes = existingAttributes.plus(event.attributes ?: emptyMap())
            val updatedUser = user.copy(identifiers = updatedIdentifiers, attributes = updatedAttributes)
            currentUser = updatedUser
            saveCurrentUser(updatedUser)
        }

        // TODO: still send identity event if event.sendEvent is true
    }

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
    fun consent(event: LyticsConsentEvent) {
        logger.info("Consent Event: $event")
        currentUser?.let { user ->
            val existingIdentifiers = user.identifiers ?: emptyMap()
            val existingAttributes = user.attributes ?: emptyMap()
            val existingConsent = user.consent ?: emptyMap()
            val updatedIdentifiers = existingIdentifiers.plus(event.identifiers ?: emptyMap())
            val updatedAttributes = existingAttributes.plus(event.attributes ?: emptyMap())
            val updatedConsent = existingConsent.plus(event.consent ?: emptyMap())
            val updatedUser =
                user.copy(identifiers = updatedIdentifiers, attributes = updatedAttributes, consent = updatedConsent)
            currentUser = updatedUser
            saveCurrentUser(updatedUser)
        }

        // TODO: still send consent event if event.sendEvent is true
    }

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
    fun enableIDFA() {
        logger.info("Enable IDFA")
        isIDFAEnabled = true
        sharedPreferences.edit {
            putBoolean(Constants.KEY_IS_IDFA_ENABLED, true)
        }
    }

    /**
     * Disables sending the IDFA, Android Advertising ID, with events.
     */
    fun disableIDFA() {
        logger.info("Disable IDFA")
        isIDFAEnabled = false
        sharedPreferences.edit {
            putBoolean(Constants.KEY_IS_IDFA_ENABLED, false)
        }
    }

    /**
     * Returns if IDFA is enabled
     */
    var isIDFAEnabled: Boolean = false
        private set

    /**
     * Force flush the event queue by sending all events in the queue immediately.
     */
    fun dispatch() {}

    /**
     * Clears all stored user information.
     */
    fun reset() {
        logger.info("Resetting Lytics user info")
        
        // set opt in to false
        optOut()

        // Create a new Lytics user and persist that user, overwriting any existing user data
        val newUser = createDefaultLyticsUser()
        saveCurrentUser(newUser)
        currentUser = newUser
    }
}