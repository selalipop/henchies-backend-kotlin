package repository

import com.github.michaelbull.result.Result
import models.GameKey
import models.SavedGameKey
import models.id.GameId
import models.id.PlayerId

interface GameKeyStore {
    /***
     * @return Returns a new game key if this is the first request for this player, or an existing one if one was already
     * created by the same client IP address
     */
    suspend fun createOrRetrieveGameKey(gameId: GameId, playerId: PlayerId, clientIp: String): Result<SavedGameKey, Error>

    /***
     * @return Returns true if the game key passed matches the existing Game Key
     */
    suspend fun verifyGameKey(gameId: GameId, playerId: PlayerId, expected: GameKey): Result<Boolean, Error>
}
