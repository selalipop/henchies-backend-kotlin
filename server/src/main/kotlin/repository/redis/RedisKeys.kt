package repository.redis

import models.id.GameId
import models.id.PlayerId

object RedisKeys {
    fun playerGameKey(gameId: GameId, playerId: PlayerId): String {
        return "player:${playerId.value}:game:${gameId.value}:key"
    }

    fun playerSecret(gameId: GameId, playerId: PlayerId): String {
        return "player:${playerId.value}:game:${gameId.value}:secret"
    }

    fun playerSecretPubSub(gameId: GameId, playerId: PlayerId): String {
        return "player:${playerId.value}:game:${gameId.value}:secret:pubSub"
    }

    fun gameState(gameId: GameId): String {
        return "game:${gameId.value}:state"
    }

    fun gameStatePubSub(gameId: GameId): String {
        return "game:${gameId.value}:state:pubSub"
    }
}