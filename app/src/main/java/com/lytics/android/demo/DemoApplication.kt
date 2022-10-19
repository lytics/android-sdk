package com.lytics.android.demo

import android.app.Application
import com.lytics.android.LyticsConfiguration
import com.lytics.android.Lytics
import com.lytics.android.events.LyticsConsentEvent
import com.lytics.android.events.LyticsEvent
import com.lytics.android.events.LyticsIdentityEvent
import com.lytics.android.logging.LogLevel

class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = LyticsConfiguration(apiKey = "API KEY", defaultStream = "defaultStream", logLevel = LogLevel.DEBUG)
        Lytics.init(applicationContext, config)

        Lytics.optIn()

        val idEvent = LyticsIdentityEvent().apply {
            stream = "androidIdentify"
            identifiers = mapOf("userId" to "this-users-known-id-or-something", "email" to "someemail@lytics.com")
            attributes = mapOf("firstName" to "Mark", "lastName" to "Hayden", "title" to "VP Product")
        }
        Lytics.identify(idEvent)

        val consentEvent = LyticsConsentEvent().apply {
            stream = "androidConsent"
            identifiers = mapOf("userId" to "this-users-known-id-or-something", "email" to "someemail@lytics.com")
            attributes = mapOf("firstName" to "Mark", "lastName" to "Hayden", "title" to "VP Product")
            consent = mapOf(
                "document" to "gdpr_collection_agreement_v1",
                "timestamp" to "46236424246",
                "consented" to "true",
            )
        }
        Lytics.consent(consentEvent)

        val event = LyticsEvent().apply {
            stream = "ios"
            name = "cart_add"
            identifiers = mapOf("userId" to "this-users-known-id-or-something")
            properties = mapOf("orderId" to "some-order-id", "total" to "19.95")
        }
        Lytics.track(event)

        val screenEvent = LyticsEvent().copy(name="cart_view")
        Lytics.screen(screenEvent)

        Lytics.disableIDFA()

        Lytics.dispatch()
    }
}
