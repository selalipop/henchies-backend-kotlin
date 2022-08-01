package repository

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import models.PlayerSecrets
import models.SavedGameKey
import models.id.GameId
import models.id.PlayerId

interface PlayerSecretsStore {
    suspend fun updatePlayerSecrets(
        gameId: GameId,
        playerId: PlayerId,
        secretUpdate: suspend (PlayerSecrets) -> PlayerSecrets
    ): Result<Unit, Error>

    suspend fun setPlayerSecrets(
        gameId: GameId,
        playerId: PlayerId,
        secrets: PlayerSecrets
    ): Result<Unit, Error>


    suspend fun initPlayerSecrets(gameId: GameId, playerId: PlayerId) =
        setPlayerSecrets(
            gameId, playerId, PlayerSecrets(false)
        )

    suspend fun clearPlayerSecrets(gameId: GameId, playerId: PlayerId): Result<Unit, Error>
    suspend fun observePlayerSecrets(gameId: GameId, playerId: PlayerId): Result<Flow<PlayerSecrets>, Error>
    suspend fun getPlayerSecrets(gameId: GameId, playerId: PlayerId) =
        observePlayerSecrets(gameId,playerId).map { it.first() }
}
