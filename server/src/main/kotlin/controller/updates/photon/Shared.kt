package controller.updates.photon

import com.github.michaelbull.result.Result
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import logger
import models.GamePhase
import models.GameState
import models.PlayerColor
import models.PlayerState
import models.id.GameId
import models.id.PlayerId
import repository.GameStateStore

const val WaitingForLeavingPlayerDelay = 5000L

suspend fun processPlayerJoined(
    gameId: GameId,
    playerId: PlayerId,
    gameStateStore: GameStateStore
) = coroutineScope {
    gameStateStore.updateGameState(gameId, fun(state: GameState): GameState {
        if (state.players.any { it.id == playerId }) {
            logger.info { "received player joined event but player was already in game, player:$playerId game: $gameId " }
        }
        val unusedColor = PlayerColor.SelectableColors.shuffled().first { color ->
            state.players.none { it.color == color }
        }

        val updatedState = state.copy(players = state.players + PlayerState(playerId, unusedColor, false))

        if (updatedState.players.size >= state.maxPlayers) {
            if (updatedState.players.size > state.maxPlayers) {
                logger.error { "exceeded max player count for game before starting for game $gameId" }
            }
            logger.info { "starting game $gameId after player $playerId joined" }
            launch {
                startGame(gameStateStore, gameId)
            }

            return updatedState.copy(phase = GamePhase.Started)
        }
        return updatedState
    })
}

private suspend fun startGame(
    gameStateStore: GameStateStore,
    gameId: GameId
): Result<Unit, Error> {

    delay(WaitingForLeavingPlayerDelay)

    return gameStateStore.updateGameState(gameId) { state ->
        if (state.phase != GamePhase.Starting) {
            return@updateGameState state
        }

        if (state.players.size < state.maxPlayers) {
            return@updateGameState state.copy(phase = GamePhase.WaitingForPlayers)
        }

        //Shuffled twice to avoid giving away imposters based on order
        val updatedPlayers = state.players.shuffled().mapIndexed { index, player ->
            player.copy(isImposter = index < state.imposterCount)
        }.shuffled()

        return@updateGameState state.copy(players = updatedPlayers, phase = GamePhase.Started)
    }
}