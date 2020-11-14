package schema.requests.photon

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateOptions(
    @SerialName("MaxPlayers")
    val maxPlayers: Int,

    @SerialName("LobbyId")
    val lobbyId: String,

    @SerialName("LobbyType")
    val lobbyType: Int,

    @SerialName("CustomProperties")
    val customProps: CustomRoomProperties,

    @SerialName("EmptyRoomTTL")
    val emptyRoomTtl: Int,

    @SerialName("PlayerTTL")
    val playerTtl: Int,

    @SerialName("CheckUserOnJoin")
    val checkUserOnJoin: Boolean,

    @SerialName("DeleteCacheOnLeave")
    val deleteCacheOnLeave: Boolean,

    @SerialName("SuppressRoomEvents")
    val suppressRoomEvents: Boolean
)