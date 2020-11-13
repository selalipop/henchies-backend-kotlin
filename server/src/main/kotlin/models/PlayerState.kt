package models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import models.id.PlayerId

@Serializable
data class PlayerState(
    @SerialName("id")
    val rawId: String,
    @SerialName("color")
    val color: PlayerColor,
    @SerialName("isImposter")
    val isImposter: Boolean,
) {
    constructor(id: PlayerId, color: PlayerColor) : this(id.value, color, false)

    val id by lazy { PlayerId(rawId) }
}


fun List<PlayerState>.byId(id: PlayerId): PlayerState? {
    return firstOrNull { it.id == id }
}