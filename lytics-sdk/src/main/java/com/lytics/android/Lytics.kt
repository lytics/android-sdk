package com.lytics.android

import android.content.Context
import android.content.SharedPreferences
import android.os.Looper
import androidx.core.content.edit
import com.lytics.android.database.DatabaseHelper
import com.lytics.android.database.EventsService
import com.lytics.android.events.LyticsConsentEvent
import com.lytics.android.events.LyticsEvent
import com.lytics.android.events.LyticsIdentityEvent
import com.lytics.android.events.Payload
import com.lytics.android.logging.AndroidLogger
import com.lytics.android.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.lang.ref.WeakReference

object Lytics {
    /**
     * a weak reference to the Android application context
     */
    private lateinit var contextRef: WeakReference<Context>

    /**
     * Configuration for the Lytics SDK
     */
    internal lateinit var configuration: LyticsConfiguration

    /**
     * The Lytics SDK logger
     */
    internal var logger: Logger = AndroidLogger

    /**
     * Returns true if this singleton instance has been initialized
     */
    private var isInitialized: Boolean = false

    /**
     * The current Lytics user
     */
    var currentUser: LyticsUser? = null
        private set

    /**
     * persistent storage for Lytics data
     */
    private lateinit var sharedPreferences: SharedPreferences

    /**
     * The coroutine scope to execute background work with
     */
    private lateinit var scope: CoroutineScope

    /**
     * Helper to access the event queue database
     */
    private lateinit var databaseHelper: DatabaseHelper

    /**
     * A handler to handle upload events messages on a delayed timer
     */
    private lateinit var uploadTimerHandler: UploadTimerHandler

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

        // create the coroutine scope on the IO dispatcher using supervisor job so if one background child job fails,
        // the remaining jobs continue to execute
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        databaseHelper = DatabaseHelper(context)

        uploadTimerHandler = UploadTimerHandler(Looper.getMainLooper())

        sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        isOptedIn = sharedPreferences.getBoolean(Constants.KEY_IS_OPTED_IN, false)
        isIDFAEnabled = sharedPreferences.getBoolean(Constants.KEY_IS_IDFA_ENABLED, false)
        currentUser = loadCurrentUser()

        isInitialized = true
    }

    /**
     * Load the current user from shared pref JSON string. If no value in shared preferences, or error parsing the JSON,
     * return a new, default Lytics user
     */
    private fun loadCurrentUser(): LyticsUser {
        return kotlin.runCatching {
            val json = sharedPreferences.getString(Constants.KEY_CURRENT_USER, null)
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
        val user = LyticsUser(identifiers = mapOf(configuration.anonymousIdentityKey to Utils.generateUUID()))
        saveCurrentUser(user)
        return user
    }

    /**
     * Save the given user to the userFile for persistence
     */
    private fun saveCurrentUser(user: LyticsUser) {
        sharedPreferences.edit {
            putString(Constants.KEY_CURRENT_USER, user.serialize().toString())
        }
    }

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

        if (event.sendEvent) {
            val payload = Payload(event)
            submitPayload(payload)
        }
    }

    /**
     * Track a custom event
     */
    fun track(event: LyticsEvent) {
        logger.info("Track Event: $event")

        val payload = Payload(event)
        // inject current user identifiers into payload
        currentUser?.let { user ->
            user.identifiers?.let {
                payload.identifiers = (payload.identifiers ?: emptyMap()).plus(it)
            }
        }

        submitPayload(payload)
    }

    /**
     * Emits a special event that represents a screen or page view.
     */
    fun screen(event: LyticsEvent) {
        logger.info("Screen Event: $event")

        val payload = Payload(event)

        // inject custom event type of "sc"
        payload.data = (payload.data ?: emptyMap())
            .plus(mapOf(Constants.KEY_EVENT_TYPE to Constants.KEY_SCREEN_EVENT_TYPE))

        // inject current user identifiers into payload
        currentUser?.let { user ->
            user.identifiers?.let {
                payload.identifiers = (payload.identifiers ?: emptyMap()).plus(it)
            }
        }

        submitPayload(payload)
    }

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

        if (event.sendEvent) {
            val payload = Payload(event)
            submitPayload(payload)
        }
    }

    private fun submitPayload(payload: Payload) {
        // inject current unix timestamp with all events
        payload.data = (payload.data ?: emptyMap()).plus(mapOf(Constants.KEY_TIMESTAMP to System.currentTimeMillis()))

        logger.debug("Adding payload to queue: $payload")

        // launch background coroutine to insert payload into database queue
        scope.launch {
            val db = databaseHelper.writableDatabase
            EventsService.insertPayload(db, payload)
            val queueSize = EventsService.getPendingPayloadCount(db)
            logger.debug("Payload queue size: $queueSize")

            // if the queue has reached max configured size, dispatch the queue to the API
            if (queueSize >= configuration.maxQueueSize) {
                logger.debug("Payload queue size exceeds max queue size ${configuration.maxQueueSize}. Dispatching!")
                uploadTimerHandler.sendEmptyMessage(UploadTimerHandler.DISPATCH_QUEUE)
            } else {
                // if there is not already a dispatch queue message
                if (!uploadTimerHandler.hasMessages(UploadTimerHandler.DISPATCH_QUEUE)) {
                    logger.debug("sending delayed message to upload timer handler")
                    // send a delayed message to dispatch the queue at the upload interval
                    uploadTimerHandler.sendEmptyMessageDelayed(
                        UploadTimerHandler.DISPATCH_QUEUE,
                        configuration.uploadInterval
                    )
                }
            }
        }
    }

    /**
     * Opts the user into event collection.
     */
    fun optIn() {
        logger.info("Opt in!")
        isOptedIn = true
        sharedPreferences.edit {
            putBoolean(Constants.KEY_IS_OPTED_IN, true)
        }
    }

    /**
     * Opt the user out of event collection
     */
    fun optOut() {
        logger.info("Opt out!")
        isOptedIn = false
        sharedPreferences.edit {
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
    fun dispatch() {
        scope.launch {
            val db = databaseHelper.writableDatabase
            val pendingPayloads = EventsService.getPendingPayloads(db)
            if (pendingPayloads.isEmpty()) {
                logger.info("Payload queue is empty, no dispatch necessary")
                return@launch
            }

            logger.info("Dispatching payload queue size: ${pendingPayloads.size}")

            val payloadSender = PayloadSender(pendingPayloads)
            val results = payloadSender.send()
            EventsService.failedPayloads(db, results.failed.mapNotNull { it.id })
            EventsService.processedPayloads(db, results.success.mapNotNull { it.id })
        }
    }

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
