package models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import util.getUnixTime

@Serializable
data class PlayerSecrets(
    @SerialName("isImposter")
    val isImposter: Boolean,
    @SerialName("gameKey")
    val gameKey: SavedGameKey,
    @SerialName("createdAt")
    val createdAt: Long = getUnixTime()
) : ClientUpdatePackable {


    override fun toUpdate() = ClientUpdate(null, this, false)
    fun withUpdatedTime() = this.copy(createdAt = getUnixTime())
}