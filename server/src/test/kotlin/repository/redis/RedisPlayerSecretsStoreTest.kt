package repository.redis

import io.mockk.Matcher
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.GameState
import models.PlayerSecrets
import models.SavedGameKey
import models.id.GameId
import models.id.PlayerId
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import redis.clients.jedis.Jedis
import redis.clients.jedis.Transaction
import util.jedis.JedisFlowPubSub

internal class RedisPlayerSecretsStoreTest {

    val jedis = mockk<Jedis>()
    val pubSub = mockk<JedisFlowPubSub>()
    val repo = RedisPlayerSecretsStore(jedis, pubSub)

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    data class GameStateIgnoringTimeMatcher(
        val expected: GameState,
        val refEq: Boolean
    ) : Matcher<GameState> {
        override fun match(arg: GameState?): Boolean {
            return arg?.copy(createdAt = 0) == expected.copy(createdAt = 0)
        }
    }

    @Test
    fun `stores initialized player secrets in Redis`() {
        val gameId = GameId("gameId")
        val playerId = PlayerId("playerId")
        val playerGameKey = SavedGameKey("testKey", "testIp")


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
        val playerGameKey = SavedGameKey("testKey", "testIp")

        val playerSecrets = PlayerSecrets(false, playerGameKey, 0)
        val modifiedPlayerSecrets = playerSecrets.copy(isImposter = true)
        val expectedString = Json.encodeToString(playerSecrets)

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