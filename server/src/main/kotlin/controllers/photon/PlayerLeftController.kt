package controllers.photon

import io.ktor.application.*
import io.ktor.request.*
import models.byId
import repository.GameStateStore
import schema.requests.photon.OkReply
import schema.requests.photon.PlayerLeftRequest
import util.logger


class PlayerLeftController(
    private val gameStateStore: GameStateStore
) {

    suspend fun playerLeft(ctx: ApplicationCall) {
        val request = ctx.receive<PlayerLeftRequest>()
        logger.info { "Processing player ${request.playerId} leaving game ${request.gameId} " }
        gameStateStore.updateGameState(request.gameId) { state ->
            val gonePlayer = state.players.byId(request.playerId)

            if (!state.isGameStarted && gonePlayer != null) {
                return@updateGameState state.copy(players = state.players - gonePlayer)
            }

            logger.info {
                "Ignoring player ${request.playerId} leaving game ${request.gameId}." +
                        " Game is started? ${state.isGameStarted} Player is missing? ${gonePlayer == null} Players Left ${state.players.joinToString { "," }}"
            }

            return@updateGameState state
        }
        ctx.respondPhoton(OkReply)
    }
}