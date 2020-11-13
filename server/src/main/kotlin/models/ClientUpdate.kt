package models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

val PingUpdate = ClientUpdate(null, null, true)

@Serializable
data class ClientUpdate(
    @SerialName("gameState")
    val gameState: GameState?,
    @SerialName("playerSecrets")
    val playerSecrets: PlayerSecrets?,
    @SerialName("isPing")
    val isPing: Boolean
)

interface ClientUpdatePackable {
    fun toUpdate(): ClientUpdate
}