package schema.requests.photon

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import models.id.GameId

@Serializable
data class RoomClosedRequest(

    @SerialName("AppId")
    var appId: String? = null,

    @SerialName("AppVersion")
    var appVersion: String? = null,

    @SerialName("Region")
    var region: String? = null,

    @SerialName("GameId")
    var rawGameId: String? = null,

    @SerialName("Type")
    var type: String? = null,
) {
    val gameId by lazy { GameId(rawGameId ?: throw Error("Game ID was not set in Photon Args")) }
}

