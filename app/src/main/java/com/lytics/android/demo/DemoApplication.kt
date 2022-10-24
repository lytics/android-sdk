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
    }
}
