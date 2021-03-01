package repository.redis

import com.github.michaelbull.result.Ok
import io.mockk.*
import kotlinx.coroutines.runBlocking
import models.GameState
import models.PlayerSecrets
import models.SavedGameKey
import models.id.GameId
import models.id.PlayerId
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import redis.RedisClient
import redis.clients.jedis.Transaction
import redis.setJson

internal class RedisPlayerSecretsStoreTest {

    val redisMock = mockk<RedisClient>()
    val repo = RedisPlayerSecretsStore(redisMock)

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

    data class PlayerSecretsIgnoringTimeMatcher(
        val expected: PlayerSecrets,
        val refEq: Boolean
    ) : Matcher<PlayerSecrets> {
        override fun match(arg: PlayerSecrets?): Boolean {
            return arg?.copy(createdAt = 0) == expected.copy(createdAt = 0)
        }
    }

    fun MockKMatcherScope.matchWithoutCreatedAt(
        items: PlayerSecrets,
        refEq: Boolean = true
    ): PlayerSecrets = match(PlayerSecretsIgnoringTimeMatcher(items, refEq))

    @Test
    fun `stores initialized player secrets in Redis`() = runBlocking {
        val gameId = GameId("gameId")
        val playerId = PlayerId("playerId")
        val playerGameKey = SavedGameKey("testKey", "testIp")

        mockkStatic("redis.RedisClientJsonKt")

        val key = slot<String>()
        val publishKey = slot<String>()
        val secrets = slot<PlayerSecrets>()

        coEvery {
            redisMock.setJson(
                capture(key),
                capture(publishKey),
                capture(secrets),
                any(),
                any()
            )
        } returns Ok(Unit)


        repo.initPlayerSecrets(gameId, playerId, playerGameKey)

        assertEquals(RedisKeys.playerSecret(gameId, playerId), key.captured)
        assertEquals(RedisKeys.playerSecretPubSub(gameId, playerId), publishKey.captured)

        assertEquals(playerGameKey, secrets.captured.gameKey)
        assertFalse(secrets.captured.isImposter)
    }

    @Test
    fun `deletes player secrets from redis when clearPlayerSecrets is called`(): Unit = runBlocking {
        val gameId = GameId("gameId")
        val playerId = PlayerId("playerId")


        repo.clearPlayerSecrets(gameId, playerId)

    }

    @Test
    fun updatePlayerSecrets() = runBlocking {
        val gameId = GameId("gameId")
        val playerId = PlayerId("playerId")
        val playerGameKey = SavedGameKey("testKey", "testIp")

        val playerSecrets = PlayerSecrets(false, playerGameKey, 0)
        val modifiedPlayerSecrets = playerSecrets.copy(isImposter = true)

        val mockTransaction = mockk<Transaction>()
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