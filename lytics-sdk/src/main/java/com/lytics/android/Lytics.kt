package com.lytics.android

import android.app.Application
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
import com.lytics.android.logging.LogLevel
import com.lytics.android.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

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
    internal var logger: Logger? = null

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
     * A lock object for updating the current user
     */
    private val currentUserLock: Any = LyticsUser::class.java

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
     * The time the last activity for the application was paused
     */
    private val lastInteractionTimestamp = AtomicLong(0)

    /**
     * A flag to send session start flag with next event
     */
    private val sessionStart = AtomicBoolean(false)


    /**
     * Initialize the Lytics SDK with the given configuration
     *
     * @param context the Android applications context
     * @param configuration The Lytics configuration
     */
    fun init(context: Context, configuration: LyticsConfiguration) {
        if (isInitialized) {
            logger?.warn("Lytics SDK already initialized")
            return
        }
        contextRef = WeakReference(context)
        this.configuration = configuration
        if (configuration.logLevel != LogLevel.NONE) {
            logger = AndroidLogger
            logger?.logLevel = configuration.logLevel
        }

        if (this.configuration.anonymousIdentityKey.isBlank()) {
            this.configuration =
                this.configuration.copy(anonymousIdentityKey = LyticsConfiguration.DEFAULT_ANONYMOUS_IDENTITY_KEY)
        }

        if (this.configuration.primaryIdentityKey.isBlank()) {
            this.configuration =
                this.configuration.copy(primaryIdentityKey = LyticsConfiguration.DEFAULT_PRIMARY_IDENTITY_KEY)
        }

        if (this.configuration.apiKey.isBlank()) {
            logger.error("Lytics API key is blank.")
            return
        }

        // create the coroutine scope on the IO dispatcher using supervisor job so if one background child job fails,
        // the remaining jobs continue to execute
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        databaseHelper = DatabaseHelper(context)

        uploadTimerHandler = UploadTimerHandler(Looper.getMainLooper())

        sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        isOptedIn = sharedPreferences.getBoolean(Constants.KEY_IS_OPTED_IN, false)
        isIDFAEnabled = sharedPreferences.getBoolean(Constants.KEY_IS_IDFA_ENABLED, false)
        lastInteractionTimestamp.set(sharedPreferences.getLong(Constants.KEY_LAST_INTERACTION_TIME, 0L))
        synchronized(currentUserLock) {
            currentUser = loadCurrentUser()
        }

        (context as? Application)?.registerActivityLifecycleCallbacks(
            ApplicationLifecycleWatcher()
        )

        isInitialized = true
        logger?.debug("Lytics initialized")
    }

    /**
     * Load the current user from shared pref JSON string. If no value in shared preferences, or error parsing the JSON,
     * return a new, default Lytics user
     */
    private fun loadCurrentUser(): LyticsUser {
        return kotlin.runCatching {
            val json = sharedPreferences.getString(Constants.KEY_CURRENT_USER, null)
            if (json.isNullOrBlank()) {
                logger?.debug("existing user data not found, creating a new Lytics user")
                createDefaultLyticsUser()
            } else {
                var user = LyticsUser(JSONObject(json))
                // if the user loaded does not contain anonymous identity key or it is null/blank
                if (user.identifiers?.containsKey(configuration.anonymousIdentityKey) == false ||
                    (user.identifiers?.get(configuration.anonymousIdentityKey) as? String).isNullOrBlank()
                ) {
                    val identifiers = user.identifiers ?: emptyMap()
                    user = user.copy(
                        identifiers = identifiers.plus(mapOf(configuration.anonymousIdentityKey to Utils.generateUUID()))
                    )
                }
                logger?.debug("found existing Lytics user: $user")
                user
            }
        }.fold(
            onSuccess = { it },
            onFailure = {
                logger?.error(it, "Error loading current user, creating a new Lytics user")
                createDefaultLyticsUser()
            }
        )
    }

    /**
     * Creates a Lytics user with only a random UUID set to the anonymous ID key per configuration
     */
    private fun createDefaultLyticsUser(): LyticsUser {
        val user = LyticsUser(identifiers = mapOf(configuration.anonymousIdentityKey to Utils.generateUUID()))
        sharedPreferences.edit { putString(Constants.KEY_CURRENT_USER, user.serialize().toString()) }
        return user
    }

    /**
     * Save the given user to the userFile for persistence
     */
    private fun saveCurrentUser(user: LyticsUser) {
        synchronized(currentUserLock) {
            currentUser = user
            sharedPreferences.edit {
                putString(Constants.KEY_CURRENT_USER, user.serialize().toString())
            }
        }
    }

    /**
     * Updates the user properties and optionally emits an identity event
     */
    fun identify(event: LyticsIdentityEvent) {
        if (!isInitialized) {
            logger?.error("Lytics SDK not initialized.")
            return
        }

        logger?.info("Identify Event: $event")
        currentUser?.let { user ->
            val existingIdentifiers = user.identifiers ?: emptyMap()
            val existingAttributes = user.attributes ?: emptyMap()
            val updatedIdentifiers = existingIdentifiers.plus(event.identifiers ?: emptyMap())
            val updatedAttributes = existingAttributes.plus(event.attributes ?: emptyMap())
            val updatedUser = user.copy(identifiers = updatedIdentifiers, attributes = updatedAttributes)
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
        if (!isInitialized) {
            logger?.error("Lytics SDK not initialized.")
            return
        }

        logger?.info("Track Event: $event")

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
        if (!isInitialized) {
            logger?.error("Lytics SDK not initialized.")
            return
        }

        logger?.info("Screen Event: $event")

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
        if (!isInitialized) {
            logger?.error("Lytics SDK not initialized.")
            return
        }

        logger?.info("Consent Event: $event")
        currentUser?.let { user ->
            val existingIdentifiers = user.identifiers ?: emptyMap()
            val existingAttributes = user.attributes ?: emptyMap()
            val existingConsent = user.consent ?: emptyMap()
            val updatedIdentifiers = existingIdentifiers.plus(event.identifiers ?: emptyMap())
            val updatedAttributes = existingAttributes.plus(event.attributes ?: emptyMap())
            val updatedConsent = existingConsent.plus(event.consent ?: emptyMap())
            val updatedUser =
                user.copy(identifiers = updatedIdentifiers, attributes = updatedAttributes, consent = updatedConsent)
            saveCurrentUser(updatedUser)
        }

        if (event.sendEvent) {
            val payload = Payload(event)
            submitPayload(payload)
        }
    }

    /**
     * Updates the last interaction time for session tracking. Set the session start flag if the last interaction was
     * greater than the configured session timeout.
     */
    internal fun markLastInteractionTime() {
        val currentTime = System.currentTimeMillis()
        logger?.debug("Marking last interaction time: $currentTime")
        val lastInteractionTime = lastInteractionTimestamp.getAndSet(currentTime)
        val timeSinceLastInteraction = currentTime - lastInteractionTime
        val startSession = (lastInteractionTime == 0L || timeSinceLastInteraction > configuration.sessionTimeout)
        logger?.debug("last: $lastInteractionTime  current: $currentTime  diff: $timeSinceLastInteraction ?> ${configuration.sessionTimeout}: $startSession")
        sessionStart.set(startSession)
        sharedPreferences.edit {
            putLong(Constants.KEY_LAST_INTERACTION_TIME, currentTime)
        }
    }

    private fun submitPayload(payload: Payload) {
        // if not opted in, drop payload
        if (!isOptedIn) {
            logger?.debug("Payload dropped. Not opted in.")
            return
        }

        // launch background coroutine to insert payload into database queue
        scope.launch {
            // inject current unix timestamp with all events
            payload.data =
                (payload.data ?: emptyMap()).plus(mapOf(Constants.KEY_TIMESTAMP to System.currentTimeMillis()))

            // inject the session start flag if set, and clear it
            if (sessionStart.getAndSet(false)) {
                payload.data =
                    payload.data?.plus(mapOf(Constants.KEY_SESSION_START to Constants.KEY_SESSION_START_FLAG))
            }

            // if IDFA is enabled, try and get the Android Advertising ID and update the payload identifiers
            if (isIDFAEnabled) {
                contextRef.get()?.let { context ->
                    val id = Utils.getAdvertisingId(context)
                    logger?.debug("Adding IDFA $id to payload")
                    id?.let {
                        payload.identifiers =
                            (payload.identifiers ?: emptyMap()).plus(mapOf(Constants.KEY_ADVERTISING_ID to it))

                        // also update the current user if it has changed
                        currentUser?.let { user ->
                            val existingIdentifiers = user.identifiers ?: emptyMap()
                            val existingId = existingIdentifiers[Constants.KEY_ADVERTISING_ID] as? String
                            if (existingId != it) {
                                val updatedIdentifiers =
                                    existingIdentifiers.plus(mapOf(Constants.KEY_ADVERTISING_ID to it))
                                val updatedUser = user.copy(identifiers = updatedIdentifiers)
                                saveCurrentUser(updatedUser)
                            }
                        }
                    }
                }
            }

            // mark the last interaction timestamp
            markLastInteractionTime()

            logger?.debug("Adding payload to queue: $payload")

            val db = databaseHelper.writableDatabase
            EventsService.insertPayload(db, payload)
            val queueSize = EventsService.getPendingPayloadCount(db)
            logger?.debug("Payload queue size: $queueSize")

            // if the queue has reached max configured size, dispatch the queue to the API
            if (queueSize >= configuration.maxQueueSize) {
                logger?.debug("Payload queue size exceeds max queue size ${configuration.maxQueueSize}. Dispatching!")
                uploadTimerHandler.sendEmptyMessage(UploadTimerHandler.DISPATCH_QUEUE)
            } else {
                // if there is not already a dispatch queue message
                if (!uploadTimerHandler.hasMessages(UploadTimerHandler.DISPATCH_QUEUE)) {
                    logger?.debug("sending delayed message to upload timer handler")
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
        logger?.info("Opt in!")
        isOptedIn = true
        sharedPreferences.edit {
            putBoolean(Constants.KEY_IS_OPTED_IN, true)
        }
    }

    /**
     * Opt the user out of event collection
     */
    fun optOut() {
        logger?.info("Opt out!")
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
     * the Android OS privacy settings in which case an empty string will be sent instead of an ID.
     *
     * The Android Advertisting ID is retrieved on each event sent and will update the current user if new value.
     */
    fun enableIDFA() {
        logger?.info("Enable IDFA")
        isIDFAEnabled = true
        sharedPreferences.edit {
            putBoolean(Constants.KEY_IS_IDFA_ENABLED, true)
        }
    }

    /**
     * Disables sending the IDFA, Android Advertising ID, with events. Removes IDFA value from user identifiers.
     */
    fun disableIDFA() {
        logger?.info("Disable IDFA")
        isIDFAEnabled = false
        sharedPreferences.edit {
            putBoolean(Constants.KEY_IS_IDFA_ENABLED, false)
        }

        // remove advertising id from current user on disable IDFA
        currentUser?.let { user ->
            val identifiers = user.identifiers?.minus(Constants.KEY_ADVERTISING_ID)
            val updatedUser = user.copy(identifiers = identifiers)
            saveCurrentUser(updatedUser)
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
        if (!isInitialized) {
            logger.error("Lytics SDK not initialized.")
            return
        }

        scope.launch {
            // if the connection status is unknown or connected, dispatch events. If known to not be connected, do not.
            if (Utils.getConnectionStatus(contextRef.get()) != false) {
                val db = databaseHelper.writableDatabase
                val pendingPayloads = EventsService.getPendingPayloads(db)
                if (pendingPayloads.isEmpty()) {
                    logger?.debug("Payload queue is empty, no dispatch necessary")
                    return@launch
                }

                logger?.info("Dispatching payload queue size: ${pendingPayloads.size}")

                kotlin.runCatching {
                    val payloadSender = PayloadSender(pendingPayloads)
                    val results = payloadSender.send()
                    EventsService.failedPayloads(db, results.failed.mapNotNull { it.id })
                    EventsService.processedPayloads(db, results.success.mapNotNull { it.id })
                }.onFailure { e ->
                    logger?.error(e, "Error sending payloads.")
                    EventsService.failedPayloads(db, pendingPayloads.mapNotNull { it.id })
                }
            } else {
                logger?.info("No network connection, skipping dispatch.")
            }
        }
    }

    /**
     * Clears all stored user information.
     */
    fun reset() {
        if (!isInitialized) {
            logger?.error("Lytics SDK not initialized.")
            return
        }
        
        logger?.info("Resetting Lytics user info")

        // set opt in to false
        optOut()
        disableIDFA()

        // remove all events in the database queue
        scope.launch {
            EventsService.clearAll(databaseHelper.writableDatabase)
        }

        // Create a new Lytics user and persist that user, overwriting any existing user data
        val newUser = createDefaultLyticsUser()
        saveCurrentUser(newUser)
    }
}
