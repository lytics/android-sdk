package com.lytics.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lytics.android.events.LyticsEvent
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JsonExtTest {

    @Test
    fun toMapWithNullArrayObject() {
        val json = JSONObject(mapOf("test" to 1, "none" to null, "map" to mapOf("recursive" to "map")))
        val jsonArray = JSONArray(listOf(1.2, "b", false))
        json.put("array", jsonArray)

        val map = json.toMap()
        assert(map.size == 4)
        assert(map["test"] == 1)
        assert(map["none"] == null)
        assert(map["map"] == mapOf("recursive" to "map"))
        assert(map["array"] is List<*>)

        val array = map["array"] as List<*>
        assert(array[0] == 1.2)
        assert(array[1] == "b")
        assert(array[2] == false)
    }

    @Test
    fun testNotJsonType() {
        val json = JSONObject(mapOf("event" to LyticsEvent(properties = mapOf("a" to "z"))))

        val map = json.toMap()
        assert(map == mapOf("event" to null))
    }
}