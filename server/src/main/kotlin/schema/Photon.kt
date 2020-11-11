package schema

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import models.id.GameId
import models.id.PlayerId

@JsonClass(generateAdapter = true)
open class PhotonArgs(
    @Json(name = "AppId")
    val appId: String,
    @Json(name = "AppVersion")
    val appVersion: String,
    @Json(name = "Region")
    val region: String,
    @Json(name = "GameID")
    val gameId: GameId,
    @Json(name = "Type")
    val type: String

) {
    override fun toString(): String {
        return "PhotonArgs(appId='$appId', appVersion='$appVersion', region='$region', gameId=$gameId, type='$type')"
    }
}

@JsonClass(generateAdapter = true)
open class PhotonExtendedArgs(
    @Json(name = "ActorNr")
    val actorNr: Int,
    @Json(name = "UserId")
    val playerId: PlayerId,
    @Json(name = "NickName")
    val nickName: String,

    appId: String,
    appVersion: String,
    region: String,
    gameId: GameId,
    type: String
) : PhotonArgs(appId, appVersion, region, gameId, type) {
    override fun toString(): String {
        return "PhotonExtendedArgs(base =${super.toString()} actorNr=$actorNr, playerId=$playerId, nickName='$nickName')"
    }
}

@JsonClass(generateAdapter = true)
open class CustomRoomProperties(
    @Json(name = "ImposterCount")
    val imposterCount: Int,
    @Json(name = "ServerCreatedRoom")
    val serverCreatedRoom: Boolean,

    ) {
    override fun toString(): String {
        return "CustomRoomProperties(imposterCount=$imposterCount, serverCreatedRoom=$serverCreatedRoom)"
    }
}

@JsonClass(generateAdapter = true)
open class CreateOptions(
    @Json(name = "MaxPlayers")
    val maxPlayers: Int,
    @Json(name = "LobbyID")
    val lobbyId: String,
    @Json(name = "LobbyType")
    val lobbyType: Int,
    @Json(name = "CustomProperties")
    val customProps: CustomRoomProperties,
    @Json(name = "EmptyRoomTTL")
    val emptyRoomTtl: Int,
    @Json(name = "PlayerTTL")
    val playerTtl: Int,
    @Json(name = "CheckUserOnJoin")
    val checkUserOnJoin: Boolean,
    @Json(name = "DeleteCacheOnLeave")
    val deleteCacheOnLeave: Boolean,
    @Json(name = "SuppressRoomEvents")
    val suppressRoomEvents: Boolean
) {
    override fun toString(): String {
        return "CreateOptions(maxPlayers=$maxPlayers, lobbyId='$lobbyId', lobbyType=$lobbyType, customProps=$customProps, emptyRoomTtl=$emptyRoomTtl, playerTtl=$playerTtl, checkUserOnJoin=$checkUserOnJoin, deleteCacheOnLeave=$deleteCacheOnLeave, suppressRoomEvents=$suppressRoomEvents)"
    }
}

@JsonClass(generateAdapter = true)
open class RoomCreatedRequest(
    @Json(name = "CreateOptions")
    val createOptions: CreateOptions,
    actorNr: Int,
    playerId: PlayerId,
    nickName: String,
    appId: String,
    appVersion: String,
    region: String,
    gameId: GameId,
    type: String
) : PhotonExtendedArgs(actorNr, playerId, nickName, appId, appVersion, region, gameId, type) {
    override fun toString(): String {
        return "RoomCreatedRequest(base=${super.toString()} createOptions=$createOptions)"
    }
}

@JsonClass(generateAdapter = true)
open class PlayerJoinedRequest(
    actorNr: Int,
    playerId: PlayerId,
    nickName: String,
    appId: String,
    appVersion: String,
    region: String,
    gameId: GameId,
    type: String
) : PhotonExtendedArgs(actorNr, playerId, nickName, appId, appVersion, region, gameId, type){
    override fun toString(): String {
        return "PlayerLeftRequest(base=${super.toString()})"
    }
}
@JsonClass(generateAdapter = true)
open class PlayerLeftRequest(
    actorNr: Int,
    playerId: PlayerId,
    nickName: String,
    appId: String,
    appVersion: String,
    region: String,
    gameId: GameId,
    type: String
) : PhotonExtendedArgs(actorNr, playerId, nickName, appId, appVersion, region, gameId, type) {
    override fun toString(): String {
        return "PlayerLeftRequest(base=${super.toString()})"
    }
}

@JsonClass(generateAdapter = true)
open class RoomClosedRequest(appId: String, appVersion: String, region: String, gameId: GameId, type: String) :
    PhotonArgs(appId, appVersion, region, gameId, type) {
    override fun toString(): String {
        return "RoomClosedRequest(base=${super.toString()})"
    }
}