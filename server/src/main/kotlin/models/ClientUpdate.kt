package models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

val PingUpdate = ClientUpdate(null, null, true)

@JsonClass(generateAdapter = true)
data class ClientUpdate(
    @Json(name = "gameState")
    val gameState: GameState?,
    @Json(name = "playerSecrets")
    val playerSecrets: PlayerSecrets?,
    @Json(name = "isPing")
    val isPing: Boolean
)

interface ClientUpdatePackable {
    fun toUpdate(): ClientUpdate
}