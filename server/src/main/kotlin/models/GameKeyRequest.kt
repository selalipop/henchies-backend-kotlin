package models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import models.id.GameId

class GameKeyRequest(
    val playerId: String,
    val gameId: GameId
)