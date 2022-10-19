package com.lytics.android

import org.json.JSONObject

interface JsonSerializable {
    fun serialize(): JSONObject
}
