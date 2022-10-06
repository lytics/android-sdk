package com.lytics.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LyticsUserTest {
    @Test
    fun testJsonSerializationAndDeserialization() {
        val uuid = Utils.generateUUID()
        val lyticsUser = LyticsUser(
            identifiers = mapOf("_uid" to uuid),
            attributes = mapOf("name" to "Jason", "email" to "jason@mobelux.com")
        )
        val json = lyticsUser.serialize()

        assert(json.has(LyticsUser.KEY_IDENTIFIERS))
        val jsonIdentifiers = json.getJSONObject(LyticsUser.KEY_IDENTIFIERS)
        Assert.assertEquals(uuid, jsonIdentifiers.get("_uid"))

        assert(json.has(LyticsUser.KEY_ATTRIBUTES))
        val jsonAttributes = json.getJSONObject(LyticsUser.KEY_ATTRIBUTES)
        Assert.assertEquals("Jason", jsonAttributes.get("name"))
        Assert.assertEquals("jason@mobelux.com", jsonAttributes.get("email"))

        assert(!json.has(LyticsUser.KEY_CONSENT))

        val jsonLyticsUser = LyticsUser(json)
        Assert.assertEquals(lyticsUser, jsonLyticsUser)
    }
}