package models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class ClientUpdate(
    @SerialName("gameState")
    val gameState: GameState? = null,
    @SerialName("playerSecrets")
    val playerSecrets: PlayerSecrets? = null,
    @SerialName("isPing")
    val isPing: Boolean = false
){
    companion object{
       val Ping = ClientUpdate(isPing = true)
    }
}