package repository.redis

import com.github.michaelbull.result.Result
import com.squareup.moshi.Moshi
import jedis.JedisFlowPubSub
import kotlinx.coroutines.flow.*
import models.GameState
import models.id.GameId
import redis.clients.jedis.Jedis
import repository.GameStateStore
import kotlin.time.hours

class RedisGameStateStore(
    moshi: Moshi,
    jedis: Jedis,
    jedisPubSub: JedisFlowPubSub
) : RedisGenericStore<GameState>(moshi, jedis, jedisPubSub), GameStateStore {
    companion object {
        val GameStateTtl = 6.hours
    }

    override fun updateGameState(
        gameId: GameId,
        gameStateUpdate: (GameState) -> GameState
    ): Result<Unit, Error> {
        return update<GameState>(
            RedisKeys.gameState(gameId),
            RedisKeys.gameStatePubSub(gameId),
            GameStateTtl
        ) {
            //Ensure createdAt is updated when called
            gameStateUpdate(it).withUpdatedTime()
        }
    }

    override fun setGameState(gameId: GameId, state: GameState): Result<Unit, Error> {
        return set(
            RedisKeys.gameState(gameId),
            RedisKeys.gameStatePubSub(gameId),
            GameStateTtl,
            state
        )
    }

    override fun observeGameState(gameId: GameId): Result<Flow<GameState>, Error> {
        return observe(
            RedisKeys.gameState(gameId),
            RedisKeys.gameStatePubSub(gameId)
        )
    }

    override fun clearGameState(gameId: GameId): Result<Unit, Error> {
        return clear(
            RedisKeys.gameState(gameId),
            RedisKeys.gameStatePubSub(gameId)
        )
    }

}