package repository.redis

import com.squareup.moshi.Moshi
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jedis.JedisFlowPubSub
import models.GameKey
import models.InlineStringClassAdapter
import models.SavedGameKey
import models.PlayerSecrets
import models.id.GameId
import models.id.PlayerId
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import redis.clients.jedis.*

internal class RedisPlayerSecretsStoreTest {

    val moshi = Moshi.Builder().add(InlineStringClassAdapter()).build()
    val jedis = mockk<Jedis>()
    val pubSub = mockk<JedisFlowPubSub>()
    val repo = RedisPlayerSecretsStore(moshi, jedis, pubSub)

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun `stores initialized player secrets in Redis`() {
        val gameId = GameId("gameId")
        val playerId = PlayerId("playerId")
        val playerGameKey = SavedGameKey(GameKey("testKey"), "testIp")


        every { jedis.set(RedisKeys.playerSecret(gameId, playerId), any(), any()) } returns "Ok."
        every { jedis.publish(RedisKeys.playerSecretPubSub(gameId, playerId), any()) } returns 1

        repo.initPlayerSecrets(gameId, playerId, playerGameKey)

        verify {
            jedis.set(RedisKeys.playerSecret(gameId, playerId), any(), any())
            jedis.publish(RedisKeys.playerSecretPubSub(gameId, playerId), any())
        }
    }

    @Test
    fun `deletes player secrets from redis when clearPlayerSecrets is called`() {
        val gameId = GameId("gameId")
        val playerId = PlayerId("playerId")


        every { jedis.del(RedisKeys.playerSecret(gameId, playerId)) } returns 1
        every { jedis.del(RedisKeys.playerSecretPubSub(gameId, playerId)) } returns 1

        repo.clearPlayerSecrets(gameId, playerId)

        verify {
            jedis.del(RedisKeys.playerSecret(gameId, playerId))
            jedis.del(RedisKeys.playerSecretPubSub(gameId, playerId))
        }
    }

    @Test
    fun updatePlayerSecrets() {
        val gameId = GameId("gameId")
        val playerId = PlayerId("playerId")
        val playerGameKey = SavedGameKey(GameKey("testKey"), "testIp")

        val playerSecrets = PlayerSecrets(false, playerGameKey, 0)
        val modifiedPlayerSecrets = playerSecrets.copy(isImposter = true)
        val expectedString = moshi.adapter(PlayerSecrets::class.java).toJson(playerSecrets)

        every { jedis.watch(RedisKeys.playerSecret(gameId, playerId)) } returns "Ok."
        every { jedis.get(RedisKeys.playerSecret(gameId, playerId)) } returns expectedString

        val mockTransaction = mockk<Transaction>()
        every { jedis.multi() } returns mockTransaction
        every { mockTransaction.set(RedisKeys.playerSecret(gameId, playerId), any(), any()) } returns mockk()
         every {
            mockTransaction.publish(
                RedisKeys.playerSecretPubSub(gameId, playerId),
                any()
            )
        } returns mockk()

        every { mockTransaction.exec() } returns mockk()

        repo.updatePlayerSecrets(gameId, playerId) {
            assertEquals(it, playerSecrets)
            modifiedPlayerSecrets
        }

        verify {
            jedis.watch(RedisKeys.playerSecret(gameId, playerId))
            jedis.get(RedisKeys.playerSecret(gameId, playerId))
            jedis.multi()
            mockTransaction.set(RedisKeys.playerSecret(gameId, playerId), any(), any())
            mockTransaction.publish(RedisKeys.playerSecretPubSub(gameId, playerId), any())
            mockTransaction.exec()
        }
    }

    @Test
    fun setPlayerSecrets() {
    }

    @Test
    fun getOrCreatePlayerGameKey() {
    }
}