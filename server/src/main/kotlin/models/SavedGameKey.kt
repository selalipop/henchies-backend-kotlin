package models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class SavedGameKey(
    @SerialName("key")
    val rawKey: String,
    @SerialName("ownerIp")
    val ownerIp: String
) {
    constructor(uuid: UUID, ownerIp: String) : this(uuid.toString(), ownerIp)
    val key by lazy { GameKey(rawKey) }
}