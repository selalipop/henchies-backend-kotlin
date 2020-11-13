package controllers

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import io.ktor.http.cio.websocket.*
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import models.GameKey
import models.PingUpdate
import models.id.GameId
import models.id.PlayerId
import repository.GameKeyStore
import repository.GameStateStore
import repository.PlayerSecretsStore
import util.JSON
import util.error.err
import util.logger
import kotlin.time.seconds


private val pingFrame = Frame.Text(JSON.encodeToString(PingUpdate))

class UpdateController(
    private val gameState: GameStateStore,
    private val playerSecrets: PlayerSecretsStore,
    private val gameKeyStore: GameKeyStore
) {

    suspend fun getUpdates(ctx: DefaultWebSocketServerSession) {
        logger.trace { "Created WebSocket ${hashCode()}" }

        val params = ctx.call.request.queryParameters

        val gameId = GameId(params.getOrFail("gameId"))
        val gameKey = GameKey(params.getOrFail("gameKey"))
        val playerId = PlayerId(params.getOrFail("playerId"))

        val (clientUpdates, error) = getClientUpdates(gameId, playerId, gameKey)
        if (error != null || clientUpdates == null) {
            throw Error("Failed to connect to Game State Updates", error)
        }

        clientUpdates.onCompletion { err ->
            ctx.close(CloseReason(CloseReason.Codes.INTERNAL_ERROR, "Terminated updates: $err"))
        }

        val pingFlow = ticker(10.seconds.toLongMilliseconds())
            .consumeAsFlow()
            .map { pingFrame }

        flowOf(
            clientUpdates,
            pingFlow
        )
            .flattenMerge()
            .takeWhile { !ctx.outgoing.isClosedForSend }
            .collect {
                ctx.outgoing.offer(it)
            }


        logger.trace { "Closed WebSocket ${hashCode()}" }
    }

    private fun getClientUpdates(
        gameId: GameId,
        playerId: PlayerId,
        gameKey: GameKey
    ): Result<Flow<Frame.Text>, Error> {
        val (isValidKey, gameKeyErr) = gameKeyStore.verifyGameKey(gameId, playerId, gameKey)
        if (gameKeyErr != null || isValidKey != true) {
            return err("Invalid player key", gameKeyErr)
        }

        val (gameStateUpdates, stateErr) = gameState.observeGameState(gameId)
        if (gameStateUpdates == null || stateErr != null) {
            return err("Failed to subscribe to game state", stateErr)
        }

        val (playerSecretUpdates, playerSecretError) = playerSecrets.observePlayerSecrets(gameId, playerId)
        if (playerSecretError != null || playerSecretUpdates == null) {
            return err("Failed to subscribe to player state", playerSecretError)
        }

        return flowOf(
            gameStateUpdates,
            playerSecretUpdates
        ).flattenMerge()
            .map { JSON.encodeToString(it.toUpdate()) }
            .filterNotNull()
            .map { Frame.Text(it) }
            .let { Ok(it) }
    }

}
