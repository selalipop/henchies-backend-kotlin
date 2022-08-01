package models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import util.getUnixTime

val EmptyPlayerSecrets = PlayerSecrets(false)
@Serializable
data class PlayerSecrets(
    @SerialName("isImposter")
    val isImposter: Boolean,
    @SerialName("createdAt")
    val createdAt: Long = getUnixTime()
) {
    fun withUpdatedTime() = this.copy(createdAt = getUnixTime())
}