package schema.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import models.GameKey

fun getGameKeyResponse(gameKey: GameKey): GetGameKeyResponse = getGameKeyResponse(gameKey)

@Serializable
data class GetGameKeyResponse(
    @SerialName("gameKey")
    val rawGameKey: String
)