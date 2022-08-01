package controllers

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import io.ktor.http.cio.websocket.*
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import models.*
import models.id.GameId
import models.id.PlayerId
import repository.GameKeyStore
import repository.GameStateStore
import repository.PlayerSecretsStore
import util.JSON
import util.error.err
import util.logger
import kotlin.time.seconds


class UpdateController(
    private val gameState: GameStateStore,
    private val playerSecrets: PlayerSecretsStore,
    private val gameKeyStore: GameKeyStore
) {

    suspend fun getUpdates(ctx: DefaultWebSocketServerSession) {
        logger.trace { "Created WebSocket ${hashCode()}" }

        val params = ctx.call.parameters

        val gameId = GameId(params.getOrFail("gameId"))
        val gameKey = GameKey(params.getOrFail("playerKey"))
        val playerId = PlayerId(params.getOrFail("playerId"))

        val (clientUpdates, updateErr) = getClientUpdates(gameId, playerId, gameKey)

        if (updateErr != null || clientUpdates == null) {
            logger.error(updateErr) { "Failed to connect to Game State Updates" }
            throw Error("Failed to connect to Game State Updates", updateErr)
        }

        clientUpdates.onCompletion { err ->
            logger.error(err) { "Terminated Client Updates" }
            ctx.close(CloseReason(CloseReason.Codes.INTERNAL_ERROR, "Terminated updates: $err"))
        }

        val pingFlow = ticker(10.seconds.toLongMilliseconds())
            .consumeAsFlow()
            .map { ClientUpdate.Ping }

        flowOf(
            clientUpdates,
            pingFlow
        )
            .flattenMerge()
            .takeWhile { !ctx.outgoing.isClosedForSend }
            .collect {
                val encodedValue = JSON.encodeToString(it)
                logger.info("Sending value '$encodedValue' to player '$playerId'")
                ctx.outgoing.trySend(Frame.Text(encodedValue))
            }


        logger.trace { "Closed WebSocket ${hashCode()}" }
    }

    private suspend fun getClientUpdates(
        gameId: GameId,
        playerId: PlayerId,
        gameKey: GameKey
    ): Result<Flow<ClientUpdate>, Error> {
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

        return Ok(
            merge(
                gameStateUpdates,
                playerSecretUpdates
            )
                .transform {
                    when (it) {
                        is GameState -> emit(ClientUpdate(gameState = it))
                        is PlayerSecrets -> emit(ClientUpdate(playerSecrets = it))
                    }
                }
        )
    }

}
