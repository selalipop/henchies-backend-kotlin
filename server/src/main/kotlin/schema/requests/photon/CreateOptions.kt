package schema.requests.photon

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateOptions(
    @SerialName("MaxPlayers")
    val maxPlayers: Int? = null,

    @SerialName("LobbyId")
    val lobbyId: String? = null,

    @SerialName("LobbyType")
    val lobbyType: Int? = null,

    @SerialName("CustomProperties")
    val customProps: CustomRoomProperties? = null,

    @SerialName("EmptyRoomTTL")
    val emptyRoomTtl: Int? = null,

    @SerialName("PlayerTTL")
    val playerTtl: Int? = null,

    @SerialName("CheckUserOnJoin")
    val checkUserOnJoin: Boolean? = null,

    @SerialName("DeleteCacheOnLeave")
    val deleteCacheOnLeave: Boolean? = null,

    @SerialName("SuppressRoomEvents")
    val suppressRoomEvents: Boolean? = null,
)