package repository.redis

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import kotlinx.coroutines.flow.Flow
import models.PlayerSecrets
import models.id.GameId
import models.id.PlayerId
import redis.RedisClient
import repository.PlayerSecretsStore
import kotlin.time.hours

class RedisPlayerSecretsStore(
    redis: RedisClient
) : RedisGenericStore<PlayerSecrets>(redis), PlayerSecretsStore {
    companion object {
        val PlayerGameKeyTtl = 6.hours
    }


    override suspend fun observePlayerSecrets(gameId: GameId, playerId: PlayerId): Result<Flow<PlayerSecrets>, Error> {
        return observe(
            RedisKeys.playerSecret(gameId, playerId),
            RedisKeys.playerSecretPubSub(gameId, playerId)
        )
    }

    override suspend fun clearPlayerSecrets(gameId: GameId, playerId: PlayerId): Result<Unit, Error> {
        return clear(RedisKeys.playerSecret(gameId, playerId))
            .andThen { clear( RedisKeys.playerSecretPubSub(gameId, playerId)) }
    }

    override suspend fun updatePlayerSecrets(
        gameId: GameId,
        playerId: PlayerId,
        secretUpdate: suspend (PlayerSecrets) -> PlayerSecrets
    ): Result<Unit, Error> {
        return update<PlayerSecrets>(
            RedisKeys.playerSecret(gameId, playerId),
            RedisKeys.playerSecretPubSub(gameId, playerId),
            PlayerGameKeyTtl
        ) {
            //Ensure createdAt is updated when called
            secretUpdate(it).withUpdatedTime()
        }
    }

    override suspend fun setPlayerSecrets(gameId: GameId, playerId: PlayerId, secrets: PlayerSecrets): Result<Unit, Error> {
        return set(
            RedisKeys.playerSecret(gameId, playerId),
            RedisKeys.playerSecretPubSub(gameId, playerId),
            PlayerGameKeyTtl, secrets
        )
    }
}