package com.app.prayer_times.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

@Serializable
data class PTObject (
    val date: String,
    val fajr: String,
    val thur: String,
    val asr: String,
    val maghrib: String,
    val isha: String
)

fun main() {
    val pt = PTObject("today", "fajr time", "thur time", "asr time", "mag", "ish")
    val jsonString = Json.encodeToString(pt)
    println(jsonString)
}