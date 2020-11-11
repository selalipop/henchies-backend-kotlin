package controller.updates

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import io.javalin.websocket.WsConnectContext
import io.javalin.websocket.WsHandler
import javalin.*
import kotlinx.coroutines.Dispatchers
import logger
import models.ClientUpdate
import models.PingUpdate
import models.id.GameId
import repository.GameStateStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import models.GameKey
import models.id.PlayerId
import repository.GameKeyStore
import repository.PlayerSecretsStore
import schema.GetUpdateRequest
import kotlin.time.seconds


class UpdateController(
    private val moshi: Moshi,
    private val gameState: GameStateStore,
    private val playerSecrets: PlayerSecretsStore,
    private val gameKeyStore: GameKeyStore
) {
    private val updateAdapter: JsonAdapter<ClientUpdate> = moshi.adapter(ClientUpdate::class.java)

    fun getUpdates(ws: WsHandler) {
        logger.trace { "Created WebSocket ${ws.hashCode()}" }

        ws.onConnect { ctx ->
            logger.trace { "Update WebSocket Connected ${ws.hashCode()}" }
            val (request, errors) = ctx.bindQueryParams<GetUpdateRequest>(moshi)
            if (request == null || errors != null) {
                ctx.sendError("Invalid parameters: ${errors.toString()}")
                return@onConnect
            }

            ws.scope().launch(Dispatchers.IO) {
                connectGameStateUpdates(request.gameId, request.playerId, request.gameKey, ctx)
            }
            ws.setupPings(ctx, 30.seconds, updateAdapter.toJson(PingUpdate))
        }
        ws.onClose {
            logger.trace { "Update WebSocket Closed ${ws.hashCode()}" }
        }
        ws.onError {
            logger.trace { "UpdateWebSocket Error ${ws.hashCode()}" }
        }
    }

    private suspend fun connectGameStateUpdates(
        gameId: GameId,
        playerId: PlayerId,
        gameKey: GameKey,
        ctx: WsConnectContext
    ) {
        val (isValidKey, gameKeyErr) = gameKeyStore.verifyGameKey(gameId, playerId, gameKey)
        if (isValidKey != true || gameKeyErr != null) {
            return ctx.sendError("Invalid player key", gameKeyErr)
        }

        val (gameStateUpdates, gameStateError) = gameState.observeGameState(gameId)
        if (gameStateError != null || gameStateUpdates == null) {
            return ctx.sendError("Failed to subscribe to game state", gameStateError)
        }

        val (playerSecretUpdates, playerSecretError) = playerSecrets.observePlayerSecrets(gameId, playerId)
        if (playerSecretError != null || playerSecretUpdates == null) {
            return ctx.sendError("Failed to subscribe to player state", playerSecretError)
        }

        flowOf(
            gameStateUpdates,
            playerSecretUpdates
        ).flattenMerge()
            .map { updateAdapter.toJson(it.toUpdate()) }
            .filterNotNull()
            .onCompletion { updateErr ->
                ctx.sendError("Exception while observing game state updates", updateErr)
            }
            .closeWsOnComplete(ctx)
            .collect { state ->
                ctx.send(state)
            }
    }

}
