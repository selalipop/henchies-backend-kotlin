package repository.redis

import models.id.GameId
import models.id.PlayerId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class RedisKeysTest {

    val gameId = GameId("gameId")
    val playerId = PlayerId("playerId")

    @Test
    fun `returns expected format for player's game key`() {
        assertEquals("player:playerId:game:gameId:key", RedisKeys.playerGameKey(gameId, playerId))
    }

    @Test
    fun `returns expected format for storing player's game secrets`() {
        assertEquals("player:playerId:game:gameId:secret", RedisKeys.playerSecret(gameId, playerId))
    }

    @Test
    fun `returns expected format for publishing player's game secrets`() {
        assertEquals("player:playerId:game:gameId:secret:pubSub", RedisKeys.playerSecretPubSub(gameId, playerId))
    }

    @Test
    fun `returns expected format for storing game's state`() {
        assertEquals("game:gameId:state", RedisKeys.gameState(gameId))
    }

    @Test
    fun `returns expected format for publishing game's state`() {
        assertEquals("game:gameId:state:pubSub", RedisKeys.gameStatePubSub(gameId))
    }
}