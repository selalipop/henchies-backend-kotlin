package controllers.photon

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import kotlinx.coroutines.flow.first
import repository.GameStateStore
import repository.PlayerSecretsStore
import schema.requests.photon.RoomClosedRequest

class RoomClosedController(
    private val gameStateStore: GameStateStore,
    private val playerSecretsStore: PlayerSecretsStore
) {
    suspend fun roomClosed(ctx: ApplicationCall) {
        val request = ctx.receive<RoomClosedRequest>()

        val (stateUpdates, error) = gameStateStore.observeGameState(request.gameId)
        if (error != null || stateUpdates == null) {
            throw Error("Failed to get game state to close room", error)
        }

        stateUpdates.first().players.forEach {
            playerSecretsStore.clearPlayerSecrets(request.gameId, it.id)
        }

        gameStateStore.clearGameState(request.gameId)
        ctx.respondText("Ok.")
    }
}