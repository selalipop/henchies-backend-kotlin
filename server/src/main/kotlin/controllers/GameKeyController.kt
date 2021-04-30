package controllers

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.response.*
import io.ktor.util.*
import models.id.GameId
import models.id.PlayerId
import repository.GameKeyStore
import schema.responses.getGameKeyResponse

class GameKeyController(
    private val gameKeyStore: GameKeyStore
) {
    suspend fun healthCheck(ctx: ApplicationCall) {

        ctx.respond("I'm good")
    }
    suspend fun getPlayerGameKey(ctx: ApplicationCall) {

        val gameId = GameId(ctx.parameters.getOrFail("gameId"))
        val playerId = PlayerId(ctx.parameters.getOrFail("playerId"))

        val (savedKey, err) = gameKeyStore.createOrRetrieveGameKey(gameId, playerId, ctx.request.origin.host)
        if (savedKey == null || err != null) {
            throw Error("Failed to retrieve game key, game ID: $gameId player ID: $playerId")
        }
        ctx.respond(getGameKeyResponse(savedKey.key))
    }
}

