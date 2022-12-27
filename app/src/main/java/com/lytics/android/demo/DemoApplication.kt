package com.lytics.android.demo

import android.app.Application
import com.lytics.android.Lytics
import com.lytics.android.LyticsConfiguration
import com.lytics.android.logging.LogLevel

class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = LyticsConfiguration(
            apiKey = "at.d14ed80c766b500e5b4b1684b02038bd.7ccfb5bfcf0c2a3825776c0120e6de02",
            logLevel = LogLevel.DEBUG,
            autoTrackActivityScreens = true,
            autoTrackAppOpens = true,
            autoTrackFragmentScreens = true,
            defaultStream = "mobile"
        )
        Lytics.init(applicationContext, config)

        Lytics.optIn()
    }
}
