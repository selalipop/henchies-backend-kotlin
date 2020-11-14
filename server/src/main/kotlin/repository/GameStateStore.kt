package repository

import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow
import models.GamePhase
import models.GameState
import models.id.GameId

interface GameStateStore {
    suspend fun updateGameState(
        gameId: GameId,
        stateUpdate: suspend (GameState) -> GameState
    ): Result<Unit, Error>

    suspend fun setGameState(
        gameId: GameId,
        state: GameState
    ): Result<Unit, Error>

    suspend fun observeGameState(
        gameId: GameId
    ): Result<Flow<GameState>, Error>

    suspend fun clearGameState(
        gameId: GameId
    ): Result<Unit, Error>

    suspend fun initGameState(gameId: GameId, startingPlayerCount: Int, imposterCount: Int) =
        setGameState(
            gameId, GameState(
                startingPlayerCount,
                imposterCount,
                GamePhase.WaitingForPlayers,
                emptyList()
            )
        )
}
