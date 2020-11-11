package schema

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import models.GameKey
import models.id.GameId
import models.id.PlayerId

@JsonClass(generateAdapter = true)
data class GetGameKeyRequest(
    @Json(name = "gameId")
    val gameId: GameId,
    @Json(name = "playerId")
    val playerId: PlayerId,
)
@JsonClass(generateAdapter = true)
data class GetGameKeyResponse(
    @Json(name = "gameKey")
    val gameKey: GameKey
)