package models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import models.id.PlayerId

@JsonClass(generateAdapter = true)
data class PlayerState(
    @Json(name = "id")
    val id: PlayerId,
    @Json(name = "color")
    val color: PlayerColor,
    @Json(name = "isImposter")
    val isImposter: Boolean,
){

}


fun List<PlayerState>.byId(id: PlayerId): PlayerState? {
    return firstOrNull{ it.id == id}
}