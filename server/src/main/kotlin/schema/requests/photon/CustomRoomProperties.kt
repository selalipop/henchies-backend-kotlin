package schema.requests.photon

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CustomRoomProperties(
    @SerialName("ImposterCount")
    val imposterCount: Int = 0,
    @SerialName("ServerCreatedRoom")
    val serverCreatedRoom: Boolean = false,
)