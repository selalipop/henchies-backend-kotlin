package controllers.photon

import kotlinx.coroutines.delay
import models.GamePhase
import models.GameState
import models.PlayerColor
import models.id.GameId
import models.id.PlayerId
import models.playerState
import repository.GameStateStore
import repository.PlayerSecretsStore
import util.logger


suspend fun processPlayerJoined(
    gameId: GameId,
    playerId: PlayerId,
    gameStateStore: GameStateStore,
    secretsStore: PlayerSecretsStore,
) {
    gameStateStore.updateGameState(gameId) { oldState ->
        if (oldState.players.any { it.id == playerId }) {
            logger.info { "received player joined event but player was already in game, player:$playerId game: $gameId " }
            return@updateGameState oldState
        }

        val unusedColor = PlayerColor.SelectableColors.shuffled().first { color ->
            oldState.players.none { it.color == color }
        }

        val newState = oldState.copy(players = oldState.players + playerState(playerId, unusedColor))

        if (newState.players.size < oldState.maxPlayers) {
            return@updateGameState newState
        }

        if (newState.players.size > oldState.maxPlayers) {
            logger.warn { "exceeded max player count for game before starting for game $gameId" }
        }

        logger.info { "starting game $gameId after player $playerId joined" }
        return@updateGameState startGame(gameId, newState, secretsStore)
    }
}


private suspend fun startGame(gameId: GameId, state: GameState, playerSecretsStore: PlayerSecretsStore): GameState {
    delay(WaitingForLeavingPlayerDelay)

    //Update imposters
    state.players.shuffled().forEachIndexed { index, player ->
        if (index < state.imposterCount) {
            playerSecretsStore.updatePlayerSecrets(gameId, player.id) { secrets ->
                secrets.copy(isImposter = true)
            }
        }
    }

    return state.copy(phase = GamePhase.Started)
}
