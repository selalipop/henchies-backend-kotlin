package repository

import com.github.michaelbull.result.Result
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import models.GamePhase
import models.GameState
import models.id.GameId
import java.io.Closeable
import java.time.LocalDateTime

interface GameStateStore {
    fun updateGameState(
        gameId: GameId,
        update: (GameState) -> GameState
    ): Result<Unit, Error>

    fun setGameState(
        gameId: GameId,
        state: GameState
    ): Result<Unit, Error>

    fun observeGameState(
        gameId: GameId
    ): Result<Flow<GameState>, Error>

    fun clearGameState(
        gameId: GameId
    ): Result<Unit, Error>

    fun initGameState(gameId: GameId, startingPlayerCount: Int, imposterCount: Int) =
        setGameState(
            gameId, GameState(
                startingPlayerCount,
                imposterCount,
                GamePhase.WaitingForPlayers,
                emptyList()
            )
        )
}
