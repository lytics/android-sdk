package com.lytics.android.demo.data

data class Event(
    val id: Long,
    val artist: String,
    val date: String,
    val location: String,
    val details: String,
    val image: Int,
    val thumbnailImage: Int
)
