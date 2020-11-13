package models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SavedGameKey(
    @SerialName("key")
    val rawKey: String,
    @SerialName("ownerIp")
    val ownerIp: String
) {
    val key by lazy { GameKey(rawKey) }
}