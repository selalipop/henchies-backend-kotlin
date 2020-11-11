package controller.updates.photon

import com.squareup.moshi.Moshi
import io.javalin.http.Context
import io.javalin.http.Handler
import javalin.bindQueryParams
import javalin.scope
import javalin.sendError
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import logger
import models.id.GameId
import models.id.PlayerId
import org.eclipse.jetty.http.HttpStatus
import org.koin.ext.scope
import repository.GameKeyStore
import repository.GameStateStore
import repository.PlayerSecretsStore
import schema.CreateOptions
import schema.GetGameKeyResponse
import schema.RoomClosedRequest
import schema.RoomCreatedRequest
import kotlin.math.ceil
import kotlin.math.roundToInt

class RoomClosedController(val moshi: Moshi, val gameStateStore: GameStateStore, val playerSecretsStore: PlayerSecretsStore) {
    companion object {
        const val GameLobbyId = "GameLobby"
    }

    fun roomClosed(ctx: Context){
        val (request, errors) = ctx.bindQueryParams<RoomClosedRequest>(moshi)
        if (request == null || errors != null) {
            ctx.sendError("Invalid parameters: ${errors.toString()}")
            return
        }
        val (stateUpdates, error) = gameStateStore.observeGameState(request.gameId)

        gameStateStore.clearGameState(request.gameId)
        if (error != null) {
            return ctx.sendError("Failed to initialize game state", error)
        }
        ctx.scope().launch{
            stateUpdates?.first()?.players?.forEach {
                playerSecretsStore.clearPlayerSecrets(request.gameId, it.id)
            }
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