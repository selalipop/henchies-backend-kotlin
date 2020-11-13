package repository

import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow
import models.PlayerSecrets
import models.SavedGameKey
import models.id.GameId
import models.id.PlayerId

interface PlayerSecretsStore {
    fun updatePlayerSecrets(
        gameId: GameId,
        playerId: PlayerId,
        update: (PlayerSecrets) -> PlayerSecrets
    ): Result<Unit, Error>

    fun setPlayerSecrets(
        gameId: GameId,
        playerId: PlayerId,
        secrets: PlayerSecrets
    ): Result<Unit, Error>


    fun initPlayerSecrets(gameId: GameId, playerId: PlayerId, key: SavedGameKey) =
        setPlayerSecrets(
            gameId, playerId, PlayerSecrets(false, key)
        )

    fun clearPlayerSecrets(gameId: GameId, playerId: PlayerId): Result<Unit, Error>
    fun observePlayerSecrets(gameId: GameId, playerId: PlayerId): Result<Flow<PlayerSecrets>, Error>
}
