package util.ktor

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import io.ktor.websocket.*
import kotlinx.serialization.decodeFromString
import schema.requests.photon.InternalErrorReply
import util.JSON
import util.logger

fun forRoute(call: suspend (ApplicationCall) -> Unit):
        PipelineInterceptor<Unit, ApplicationCall> = { call(this.call) }

fun forPhotonRoute(call: suspend (ApplicationCall) -> Unit): PipelineInterceptor<Unit, ApplicationCall> = {
    try {
        logger.debug("Photon Request: ${this.call.request}")
        call(this.call)
    } catch (e: Exception) {
        this.call.respond(InternalErrorReply(e))
    }
}

suspend fun DefaultWebSocketServerSession.forWsRoute(call: suspend (DefaultWebSocketServerSession) -> Unit){
    try {
        call(this)
    } catch (e: Exception) {
        logger.error(e) { "Failed to build WebSocket" }
    }
}

inline fun <reified T> Parameters.getJsonOrFail(name : String): T {
   return JSON.decodeFromString<T>(getOrFail(name))
}