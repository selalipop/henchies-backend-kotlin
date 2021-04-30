package schema.requests.photon

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WebhookReply(
    @SerialName("ResultCode")
    val resultCode: Int,

    @SerialName("State")
    val state: String?,

    @SerialName("Message")
    val message: String?,
) {
    constructor(resultCode: ResultCode, state: String?, message: String?) :
            this(resultCode.value, state, message)
}

val OkReply = WebhookReply(ResultCode.Ok, null, "Ok")
val AllowCreateReply = WebhookReply(ResultCode.Ok, "", "Room Create Allowed")
fun InternalErrorReply(e: Throwable) =
    WebhookReply(ResultCode.InternalError, null, e.message)
fun InternalErrorReply(err: String) =
    WebhookReply(ResultCode.InternalError, null, err)

fun InvalidRoomParametersReply(err: String) =
    WebhookReply(ResultCode.InvalidRoomParameters, null, err)