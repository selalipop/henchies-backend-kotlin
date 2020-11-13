package repository.redis

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import models.GameKey
import models.SavedGameKey
import models.id.GameId
import models.id.PlayerId
import redis.clients.jedis.Jedis
import repository.GameKeyStore
import util.error.err
import util.jedis.getJson
import util.jedis.setJson
import util.jedis.ttlFromDuration
import java.util.*

class RedisGameKeyStore(
    private val jedis: Jedis,
) : GameKeyStore {
    override fun createOrRetrieveGameKey(
        gameId: GameId,
        playerId: PlayerId,
        clientIp: String
    ): Result<SavedGameKey, Error> {
        val newKey = SavedGameKey(UUID.randomUUID().toString(), clientIp)
        val redisKey = RedisKeys.playerGameKey(gameId, playerId)
        jedis.setJson(
            redisKey, null, newKey, ttlFromDuration(
                RedisPlayerSecretsStore.PlayerGameKeyTtl
            ).nx()
        )


        val (retrievedKey, error) = jedis.getJson<SavedGameKey>(redisKey)

        if (retrievedKey == null || error != null) {
            return err("Failed to get key from redis")
        }

        //If we created a new key, or it's the same player requesting their existing key return the retrieved key
        return if (retrievedKey == newKey || retrievedKey.ownerIp == clientIp) {
            Ok(retrievedKey)
        } else {
            err("Player already has a game key but IP address has changed")
        }
    }

    override fun verifyGameKey(
        gameId: GameId,
        playerId: PlayerId,
        key: GameKey
    ): Result<Boolean, Error> {
        val (retrievedKey, error) = jedis.getJson<SavedGameKey>(RedisKeys.playerGameKey(gameId, playerId))
        if (retrievedKey == null || error != null) {
            return err("Failed to get key from redis")
        }
        return Ok(retrievedKey.key == key)
    }
}