package controller.updates.photon

import com.squareup.moshi.Moshi
import io.javalin.http.Context
import io.javalin.http.Handler
import javalin.bindQueryParams
import javalin.scope
import javalin.sendError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import logger
import models.id.GameId
import models.id.PlayerId
import org.eclipse.jetty.http.HttpStatus
import org.koin.ext.scope
import repository.GameKeyStore
import repository.GameStateStore
import schema.CreateOptions
import schema.GetGameKeyResponse
import schema.RoomCreatedRequest
import kotlin.math.ceil
import kotlin.math.roundToInt

class RoomCreatedController(val moshi: Moshi, val gameStateStore: GameStateStore) {
    companion object {
        const val GameLobbyId = "GameLobby"
    }

    fun roomCreated(ctx: Context){
        val (request, errors) = ctx.bindQueryParams<RoomCreatedRequest>(moshi)
        if (request == null || errors != null) {
            ctx.sendError("Invalid parameters: ${errors.toString()}")
            return
        }
        logger.info { "Room created $request" }

        val createOptions = request.createOptions
        if (createOptions.lobbyId != GameLobbyId) {
            logger.warn { "Ignoring room created outside of Game Lobby ${createOptions.lobbyId}}" }
            return
        }

        val imposterCount = getImposterCountForGame(createOptions)

        val (_, error) = gameStateStore.initGameState(request.gameId, createOptions.maxPlayers, imposterCount)
        GlobalScope.launch(Dispatchers.IO) {
            processPlayerJoined(request.gameId, request.playerId, gameStateStore)
        }

        if (error != null) {
            return ctx.sendError("Failed to initialize game state", error)
        }
    }

    private fun getImposterCountForGame(createOptions: CreateOptions): Int {
        var imposterCount = createOptions.customProps.imposterCount
        if (imposterCount == 0) {
            //TODO: Get real imposter count
            imposterCount = ceil(0.2f * createOptions.maxPlayers).roundToInt()
        }
        return imposterCount
    }
}