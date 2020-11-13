package controllers.photon

import io.ktor.application.*
import io.ktor.request.*
import models.byId
import repository.GameStateStore
import schema.requests.photon.PlayerLeftRequest


class PlayerLeftController(
    private val gameStateStore: GameStateStore
) {

    suspend fun playerLeft(ctx: ApplicationCall) {
        val request = ctx.receive<PlayerLeftRequest>()

        gameStateStore.updateGameState(request.gameId) { state ->
            val gonePlayer = state.players.byId(request.playerId)

            if (!state.isGameStarted && gonePlayer != null) {
                return@updateGameState state.copy(players = state.players - gonePlayer)
            }

            return@updateGameState state
        }
    }
}