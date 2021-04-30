package controllers.photon

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import repository.GameStateStore
import repository.PlayerSecretsStore
import schema.requests.photon.AllowCreateReply
import schema.requests.photon.CreateOptions
import schema.requests.photon.RoomCreatedRequest
import util.logger
import kotlin.math.ceil
import kotlin.math.roundToInt

class RoomCreatedController(
    private val gameStateStore: GameStateStore,
    private val secretsStore: PlayerSecretsStore
) {
    suspend fun roomCreated(ctx: ApplicationCall) {
        val request = ctx.receive<RoomCreatedRequest>()
        logger.info { "Room created $request" }

        val createOptions = request.createOptions
        if (createOptions.lobbyId != GameLobbyId) {
            logger.warn { "Ignoring room created outside of Game Lobby ${createOptions.lobbyId}}" }
            return ctx.respondPhoton(AllowCreateReply)
        }


        val imposterCount = getImposterCountForGame(createOptions)

        val (_, error) = gameStateStore.initGameState(request.gameId, getPlayerCountForGame(createOptions), imposterCount)
        if (error != null) {
            throw Error("Failed to initialize game state", error)
        }

        processPlayerJoined(request.gameId, request.playerId, gameStateStore, secretsStore)

        ctx.respondPhoton(AllowCreateReply)
    }
    private fun getPlayerCountForGame(createOptions: CreateOptions): Int {
        return createOptions.maxPlayers ?: 6
    }
    private fun getImposterCountForGame(createOptions: CreateOptions): Int {
        var imposterCount = createOptions.customProps?.imposterCount ?: 1
        if (imposterCount < 1) {

            //TODO: Get real imposter count or return InvalidRoomParametersReply if not possible
            imposterCount = ceil(0.2f * (createOptions.maxPlayers ?: 0)).roundToInt()
        }
        return imposterCount
    }
}