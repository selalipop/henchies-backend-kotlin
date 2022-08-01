package controllers.photon

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import models.EmptyPlayerSecrets
import models.GamePhase
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
    secretsStore.setPlayerSecrets(gameId, playerId, EmptyPlayerSecrets)

    gameStateStore.updateGameState(gameId) { oldState ->
        if (oldState.players.any { it.id == playerId }) {
            logger.info { "received player joined event but player was already in game, player:$playerId game: $gameId " }
            return@updateGameState oldState
        }

        val unusedColor = PlayerColor.SelectableColors.shuffled().first { color ->
            oldState.players.none { it.color == color }
        }

        val newState = oldState.copy(players = oldState.players + playerState(playerId, unusedColor))

        if (newState.players.size >= oldState.maxPlayers) {
            if (newState.players.size > oldState.maxPlayers) {
                logger.warn { "exceeded max player count for game before starting for game $gameId" }
            }
            startGame(gameId, secretsStore, gameStateStore)
        }

        return@updateGameState newState
    }
}


private suspend fun startGame(
    gameId: GameId,
    playerSecretsStore: PlayerSecretsStore,
    gameStateStore: GameStateStore
) = coroutineScope {
    delay(WaitingForLeavingPlayerDelay)

    gameStateStore.updateGameState(gameId) { state ->
        if (state.phase != GamePhase.WaitingForPlayers ||
            state.players.size < state.maxPlayers
        ) {
            //Someone left during the delay, or the game already started
            return@updateGameState state
        }

        //Update imposters
        state.players.shuffled()
            .mapIndexed { index, player ->
                async {
                    playerSecretsStore.updatePlayerSecrets(gameId, player.id) { secrets ->
                        //The first X players in the shuffled list are imposters
                        secrets.copy(isImposter = index < state.imposterCount)
                    }
                }
            }.forEach { it.await() }


        return@updateGameState state.copy(phase = GamePhase.Started)
    }

}

