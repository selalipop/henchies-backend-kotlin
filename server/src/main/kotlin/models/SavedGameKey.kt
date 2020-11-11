package models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SavedGameKey(
    @Json(name = "key")
    val key: GameKey,
    @Json(name = "ownerIp")
    val ownerIp: String
)