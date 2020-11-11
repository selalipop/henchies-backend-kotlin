package models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import getUnixTime

@JsonClass(generateAdapter = true)
data class GameState(
    @Json(name = "maxPlayers")
    val maxPlayers: Int,
    @Json(name = "imposterCount")
    val imposterCount: Int,
    @Json(name = "phase")
    val phase: GamePhase,
    @Json(name = "players")
    val players: List<PlayerState>,
    @Json(name = "createdAt")
    val createdAt: Long = getUnixTime()
) : ClientUpdatePackable {
    override fun toUpdate() = ClientUpdate(this, null, false)

    fun withUpdatedTime(): GameState = this.copy(createdAt = getUnixTime())
}
