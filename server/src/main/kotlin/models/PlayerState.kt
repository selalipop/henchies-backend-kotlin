package models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import models.id.PlayerId

fun playerState(id: PlayerId, color: PlayerColor) = PlayerState(id.value, color)
@Serializable
data class PlayerState(
    @SerialName("id")
    val rawId: String,
    @SerialName("color")
    val color: PlayerColor
) {

    val id by lazy { PlayerId(rawId) }
}


fun List<PlayerState>.byId(id: PlayerId): PlayerState? {
    return firstOrNull { it.id == id }
}