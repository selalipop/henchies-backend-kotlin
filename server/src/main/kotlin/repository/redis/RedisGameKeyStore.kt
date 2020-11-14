package repository.redis

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import models.GameKey
import models.SavedGameKey
import models.id.GameId
import models.id.PlayerId
import redis.RedisClient
import redis.getJson
import redis.jedis.ttlFromDuration
import redis.setJson
import repository.GameKeyStore
import util.error.err
import java.util.*

class RedisGameKeyStore(
    private val redis: RedisClient,
) : GameKeyStore {
    override suspend fun createOrRetrieveGameKey(
        gameId: GameId,
        playerId: PlayerId,
        clientIp: String
    ): Result<SavedGameKey, Error> {
        val gameKey = SavedGameKey(UUID.randomUUID(), clientIp)

        val redisKey = RedisKeys.playerGameKey(gameId, playerId)

        val (_, setErr) = redis.setJson(
            redisKey, null, gameKey, ttlFromDuration(RedisPlayerSecretsStore.PlayerGameKeyTtl).nx()
        )

        if (setErr != null && !setErr.isMissingKey) {
            return err("Failed to set player game key", setErr)
        }

        val (retrievedKey, getErr) = redis.getJson<SavedGameKey>(redisKey)

        if (getErr != null || retrievedKey == null) {
            return err("Failed to get key from redis")
        }

        //If we created a new key, or it's the same player requesting their existing key return the retrieved key
        return if (retrievedKey == gameKey || retrievedKey.ownerIp == clientIp) {
            Ok(retrievedKey)
        } else {
            err("Player already has a game key but IP address has changed")
        }
    }

    override suspend fun verifyGameKey(
        gameId: GameId,
        playerId: PlayerId,
        expected: GameKey
    ): Result<Boolean, Error> {
        val (key, err) = redis.getJson<SavedGameKey>(RedisKeys.playerGameKey(gameId, playerId))
        if (err != null || key == null) {
            return err("Failed to get key from redis")
        }

        return Ok(key.key == expected)
    }
}