package com.lytics.android.demo

import android.app.Application
import android.util.Log
import com.lytics.android.LyticsConfiguration
import com.lytics.android.Lytics
import com.lytics.android.events.LyticsConsentEvent
import com.lytics.android.events.LyticsEvent
import com.lytics.android.events.LyticsIdentityEvent
import com.lytics.android.logging.LogLevel
import java.util.concurrent.TimeUnit

class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = LyticsConfiguration(
            apiKey = "YOUR API KEY HERE",
            defaultStream = "default",
            logLevel = LogLevel.DEBUG,
            autoTrackActivityScreens = true,
            autoTrackAppOpens = true,
            autoTrackFragmentScreens = true,
        )
        Lytics.init(applicationContext, config)

        Lytics.optIn()
    }
}
