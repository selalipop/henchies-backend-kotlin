package models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import getUnixTime

@JsonClass(generateAdapter = true)
data class PlayerSecrets(
    @Json(name = "isImposter")
    val isImposter: Boolean,
    @Json(name = "gameKey")
    val gameKey: SavedGameKey,
    @Json(name = "createdAt")
    val createdAt: Long = getUnixTime()
) : ClientUpdatePackable {
    override fun toUpdate() = ClientUpdate(null, this, false)
    fun withUpdatedTime() = this.copy(createdAt = getUnixTime())
}