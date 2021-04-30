package models.game.item

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import models.Vector3
import models.Vector4
import util.getUnixTime
import java.util.*

@Serializable
data class GameItem(
    @SerialName("definition")
    val definition: GameItemDefinition,

    @SerialName("position")
    val position: Vector3,

    @SerialName("rotation")
    val rotation: Vector4,

    @SerialName("createdAt")
    val createdAt: Long = getUnixTime(),

    @SerialName("id")
    val id: String = UUID.randomUUID().toString(),
)

