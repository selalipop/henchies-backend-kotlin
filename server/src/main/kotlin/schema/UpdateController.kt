package schema

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import javalin.sendError
import models.GameKey
import models.id.GameId
import models.id.PlayerId

@JsonClass(generateAdapter = true)
data class GetUpdateRequest(
    @Json(name = "gameId")
    val gameId: GameId,
    @Json(name = "playerId")
    val playerId: PlayerId,
    @Json(name = "gameKey")
    val gameKey: GameKey
)