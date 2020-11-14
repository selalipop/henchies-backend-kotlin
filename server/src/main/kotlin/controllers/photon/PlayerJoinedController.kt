package controllers.photon

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import repository.GameStateStore
import schema.requests.photon.PlayerJoinedRequest


class PlayerJoinedController(
    private val gameStateStore: GameStateStore
) {

    suspend fun test(ctx: ApplicationCall) {
        ctx.respondText("hello world")
    }

    suspend fun playerJoined(ctx: ApplicationCall) {
        val request = ctx.receive<PlayerJoinedRequest>()
        processPlayerJoined(request.gameId, request.playerId, gameStateStore)
        ctx.respondText("Ok.")
    }

}