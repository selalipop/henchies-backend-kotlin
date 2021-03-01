package controllers.photon

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import repository.GameStateStore
import repository.PlayerSecretsStore
import schema.requests.photon.OkReply
import schema.requests.photon.PlayerJoinedRequest


class PlayerJoinedController(
    private val gameStateStore: GameStateStore,
    private val secretsStore: PlayerSecretsStore
) {

    suspend fun test(ctx: ApplicationCall) {
        ctx.respondText("hello world")
    }

    suspend fun playerJoined(ctx: ApplicationCall) {
        val request = ctx.receive<PlayerJoinedRequest>()
        processPlayerJoined(request.gameId, request.playerId, gameStateStore, secretsStore)
        ctx.respondPhoton(OkReply)
    }

}