package com.lytics.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lytics.android.events.LyticsEvent
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LyticsUserTest {
    @Test
    fun testPartialJsonSerializationAndDeserialization() {
        val uuid = Utils.generateUUID()
        val lyticsUser = LyticsUser(
            identifiers = mapOf("_uid" to uuid, "userId" to 123, "admin" to true),
            attributes = mapOf("name" to "Jason", "email" to "jason@mobelux.com")
        )
        val json = lyticsUser.serialize()
        assert(json.has(Constants.KEY_IDENTIFIERS))
        val jsonIdentifiers = json.getJSONObject(Constants.KEY_IDENTIFIERS)
        Assert.assertEquals(uuid, jsonIdentifiers.get("_uid"))
        Assert.assertEquals(123, jsonIdentifiers.get("userId"))
        Assert.assertEquals(true, jsonIdentifiers.get("admin"))

        assert(json.has(Constants.KEY_ATTRIBUTES))
        val jsonAttributes = json.getJSONObject(Constants.KEY_ATTRIBUTES)
        Assert.assertEquals("Jason", jsonAttributes.get("name"))
        Assert.assertEquals("jason@mobelux.com", jsonAttributes.get("email"))

        assert(!json.has(Constants.KEY_CONSENT))
        assert(!json.has(Constants.KEY_PROFILE))

        val jsonLyticsUser = LyticsUser(json)
        Assert.assertEquals(lyticsUser, jsonLyticsUser)
    }

    @Test
    fun testFullJsonSerializationAndDeserialization() {
        val uuid = Utils.generateUUID()
        val lyticsUser = LyticsUser(
            identifiers = mapOf("_uid" to uuid, "userId" to 123, "admin" to true),
            attributes = mapOf("name" to "Jason", "email" to "jason@mobelux.com"),
            consent = mapOf("consent" to true),
            profile = mapOf("profile_field_1" to "profile_value_1"),
        )
        val json = lyticsUser.serialize()
        val jsonLyticsUser = LyticsUser(json)
        Assert.assertEquals(lyticsUser, jsonLyticsUser)
    }
}