package controllers.photon

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import models.GamePhase
import models.GameState
import models.PlayerColor
import models.PlayerState
import models.id.GameId
import models.id.PlayerId
import repository.GameStateStore
import util.logger


suspend fun processPlayerJoined(gameId: GameId, playerId: PlayerId, gameStateStore: GameStateStore) =
    coroutineScope {
        gameStateStore.updateGameState(gameId) { oldState ->
            if (oldState.players.any { it.id == playerId }) {
                logger.info { "received player joined event but player was already in game, player:$playerId game: $gameId " }
            }

            val unusedColor = PlayerColor.SelectableColors.shuffled().first { color ->
                oldState.players.none { it.color == color }
            }

            val newState = oldState.copy(players = oldState.players + PlayerState(playerId, unusedColor))

            if (newState.players.size < oldState.maxPlayers) {
                return@updateGameState newState
            }

            if (newState.players.size > oldState.maxPlayers) {
                logger.warn { "exceeded max player count for game before starting for game $gameId" }
            }

            logger.info { "starting game $gameId after player $playerId joined" }
            return@updateGameState startGame(newState)
        }
    }


private suspend fun startGame(state: GameState): GameState {
    delay(WaitingForLeavingPlayerDelay)

    //Shuffled twice to avoid giving away imposters based on order
    val updatedPlayers = state.players.shuffled().mapIndexed { index, player ->
        player.copy(isImposter = index < state.imposterCount)
    }.shuffled()

    return state.copy(players = updatedPlayers, phase = GamePhase.Started)
}
