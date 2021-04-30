package schema.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import models.GameKey

fun getGameKeyResponse(gameKey: GameKey): GetGameKeyResponse = GetGameKeyResponse(gameKey.value)

@Serializable
data class GetGameKeyResponse(
    @SerialName("gameKey")
    val rawGameKey: String
)