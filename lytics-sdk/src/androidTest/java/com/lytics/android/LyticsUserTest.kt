package com.lytics.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.json.JSONObject
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LyticsUserTest {

    @Test
    fun testGettersSetters() {
        val user = LyticsUser()
        val profile = mapOf("profile" to true)
        user.profile = profile
        Assert.assertEquals(profile, user.profile)

        val identifiers = mapOf("identifiers" to 1)
        user.identifiers = identifiers
        Assert.assertEquals(identifiers, user.identifiers)

        val consent = mapOf("consent" to false)
        user.consent = consent
        Assert.assertEquals(consent, user.consent)

        val attributes = mapOf("attributes" to "blue")
        user.attributes = attributes
        Assert.assertEquals(attributes, user.attributes)
    }

    @Test
    fun testJsonConstructor() {
        val jsonString = """{"identifiers": {"identifiers": 1}, "attributes": {"attributes": "blue"}, "consent": {"consent": false}}"""
        val user = LyticsUser(JSONObject(jsonString))

        Assert.assertEquals(mapOf("identifiers" to 1), user.identifiers)
        Assert.assertEquals(mapOf("consent" to false), user.consent)
        Assert.assertEquals(mapOf("attributes" to "blue"), user.attributes)

        val emptyUser = LyticsUser(JSONObject())
        Assert.assertNull(emptyUser.identifiers)
        Assert.assertNull(emptyUser.consent)
        Assert.assertNull(emptyUser.attributes)
    }

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

        val serializedLyticsUser = LyticsUser(
            identifiers = mapOf("_uid" to uuid, "userId" to 123, "admin" to true),
            attributes = mapOf("name" to "Jason", "email" to "jason@mobelux.com"),
            consent = mapOf("consent" to true),
        )
        Assert.assertEquals(serializedLyticsUser, jsonLyticsUser)
    }

    @Test
    fun testEmptyUserSerialize() {
        val emptyUser = LyticsUser()
        val jsonString = emptyUser.serialize().toString()
        Assert.assertEquals("{}", jsonString)
    }
}