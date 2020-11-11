package repository.redis

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.squareup.moshi.Moshi
import jedis.*
import kotlinx.coroutines.flow.*
import models.PlayerSecrets
import models.id.GameId
import models.id.PlayerId
import redis.clients.jedis.Jedis
import repository.PlayerSecretsStore
import java.lang.Error
import kotlin.time.hours

class RedisPlayerSecretsStore(
    moshi: Moshi, jedis: Jedis, jedisPubSub: JedisFlowPubSub
) : RedisGenericStore<PlayerSecrets>(moshi, jedis, jedisPubSub), PlayerSecretsStore {
    companion object {
        val PlayerGameKeyTtl = 6.hours
    }


    override fun observePlayerSecrets(gameId: GameId, playerId: PlayerId): Result<Flow<PlayerSecrets>, Error> {
        return observe(
            RedisKeys.playerSecret(gameId, playerId),
            RedisKeys.playerSecretPubSub(gameId, playerId)
        )
    }

    override fun clearPlayerSecrets(gameId: GameId, playerId: PlayerId): Result<Unit, Error> {
        clear(RedisKeys.playerSecret(gameId, playerId), RedisKeys.playerSecretPubSub(gameId, playerId))
        return Ok(Unit)
    }

    override fun updatePlayerSecrets(
        gameId: GameId,
        playerId: PlayerId,
        playerSecretUpdate: (PlayerSecrets) -> PlayerSecrets
    ): Result<Unit, Error> {
        return update<PlayerSecrets>(
            RedisKeys.playerSecret(gameId, playerId),
            RedisKeys.playerSecretPubSub(gameId, playerId),
            PlayerGameKeyTtl
        ) {
            //Ensure createdAt is updated when called
            playerSecretUpdate(it).withUpdatedTime()
        }
    }

    override fun setPlayerSecrets(gameId: GameId, playerId: PlayerId, secrets: PlayerSecrets): Result<Unit, Error> {
        return set(
            RedisKeys.playerSecret(gameId, playerId),
            RedisKeys.playerSecretPubSub(gameId, playerId),
            PlayerGameKeyTtl, secrets
        )
    }
}