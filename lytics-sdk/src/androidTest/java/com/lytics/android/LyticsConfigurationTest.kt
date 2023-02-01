package com.lytics.android

import com.lytics.android.logging.LogLevel
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.TimeUnit

class LyticsConfigurationTest {
    @Test
    fun testDefaultConfig() {
        val configuration = LyticsConfiguration("API_KEY")
        Assert.assertEquals("API_KEY", configuration.apiKey)
        Assert.assertEquals(LyticsConfiguration.DEFAULT_STREAM, configuration.defaultStream)
        Assert.assertEquals(LyticsConfiguration.DEFAULT_PRIMARY_IDENTITY_KEY, configuration.primaryIdentityKey)
        Assert.assertEquals(LyticsConfiguration.DEFAULT_ANONYMOUS_IDENTITY_KEY, configuration.anonymousIdentityKey)
        Assert.assertEquals(LyticsConfiguration.DEFAULT_ENTITY_TABLE, configuration.defaultTable)
        Assert.assertFalse(configuration.requireConsent)
        Assert.assertFalse(configuration.autoTrackActivityScreens)
        Assert.assertFalse(configuration.autoTrackFragmentScreens)
        Assert.assertFalse(configuration.autoTrackAppOpens)
        Assert.assertEquals(10, configuration.maxQueueSize)
        Assert.assertEquals(3, configuration.maxUploadRetryAttempts)
        Assert.assertEquals(1, configuration.maxLoadRetryAttempts)
        Assert.assertEquals(TimeUnit.SECONDS.toMillis(10), configuration.uploadInterval)
        Assert.assertEquals(TimeUnit.MINUTES.toMillis(20), configuration.sessionTimeout)
        Assert.assertEquals(LogLevel.NONE, configuration.logLevel)
        Assert.assertFalse(configuration.sandboxMode)
        Assert.assertEquals(LyticsConfiguration.DEFAULT_COLLECTION_ENDPOINT, configuration.collectionEndpoint)
        Assert.assertEquals(LyticsConfiguration.DEFAULT_ENTITY_ENDPOINT, configuration.entityEndpoint)
        Assert.assertEquals(TimeUnit.SECONDS.toMillis(30).toInt(), configuration.networkRequestTimeout)
    }
}