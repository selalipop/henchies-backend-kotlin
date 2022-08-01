package models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import models.game.item.GameItem
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
    @SerialName("spawnedItems")
    val spawnedItems: List<GameItem>,
    @SerialName("createdAt")
    val createdAt: Long = getUnixTime(),
)  {
    val isGameStarted = phase >= GamePhase.Started
    fun withUpdatedTime(): GameState = this.copy(createdAt = getUnixTime())
}
