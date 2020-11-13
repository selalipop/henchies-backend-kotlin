package schema.requests.photon

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import models.id.GameId
import models.id.PlayerId


@Serializable
data class RoomCreatedRequest(
    @SerialName("CreateOptions")
    val createOptions: CreateOptions,

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

    @SerialName("ActorNr")
    var actorNr: Int? = null,

    @SerialName("UserId")
    var rawPlayerId: String? = null,

    @SerialName("NickName")
    var nickName: String? = null
) {
    val gameId by lazy { GameId(rawGameId ?: throw Error("Game ID was not set in Photon Args")) }
    val playerId by lazy { PlayerId(rawPlayerId ?: throw Error("Player ID was not set in Photon Args")) }
}


