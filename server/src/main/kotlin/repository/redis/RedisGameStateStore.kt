package repository.redis

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import kotlinx.coroutines.flow.Flow
import models.GameState
import models.id.GameId
import redis.RedisClient
import repository.GameStateStore
import util.error.err
import kotlin.time.hours

class RedisGameStateStore(
    redis: RedisClient,
) : RedisGenericStore<GameState>(redis), GameStateStore {
    companion object {
        val GameStateTtl = 24.hours
    }

     override suspend fun updateGameState(
         gameId: GameId,
         stateUpdate: suspend (GameState) -> GameState
    ): Result<Unit, Error> {
        return update<GameState>(
            RedisKeys.gameState(gameId),
            RedisKeys.gameStatePubSub(gameId),
            GameStateTtl
        ) {
            //Ensure createdAt is updated when called
            stateUpdate(it).withUpdatedTime()
        }
    }

    override suspend fun setGameState(gameId: GameId, state: GameState): Result<Unit, Error> {
        return set(
            RedisKeys.gameState(gameId),
            RedisKeys.gameStatePubSub(gameId),
            GameStateTtl,
            state
        )
    }

    override suspend fun observeGameState(gameId: GameId): Result<Flow<GameState>, Error> {
        return observe(
            RedisKeys.gameState(gameId),
            RedisKeys.gameStatePubSub(gameId)
        )
    }

    override suspend  fun clearGameState(gameId: GameId): Result<Unit, Error> {
        val (_, redisError) = redis.clear(RedisKeys.gameState(gameId))
            .andThen { redis.clear(RedisKeys.gameStatePubSub(gameId)) }

        return if (redisError != null && !redisError.isMissingKey) {
            err(redisError)
        } else {
            Ok(Unit)
        }
    }

}