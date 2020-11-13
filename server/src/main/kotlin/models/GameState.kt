package models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import util.getUnixTime

@Serializable
data class GameState(
    @SerialName("maxPlayers")
    val maxPlayers: Int,
    @SerialName("imposterCount")
    val imposterCount: Int,
    @SerialName("phase")
    val phase: GamePhase,
    @SerialName("players")
    val players: List<PlayerState>,
    @SerialName("createdAt")
    val createdAt: Long = getUnixTime()
) : ClientUpdatePackable {

    val isGameStarted = phase >= GamePhase.Started
    override fun toUpdate() = ClientUpdate(this, null, false)

    fun withUpdatedTime(): GameState = this.copy(createdAt = getUnixTime())
}
