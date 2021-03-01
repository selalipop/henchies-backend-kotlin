package util.ktor

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import io.ktor.websocket.*
import schema.requests.photon.InternalErrorReply

fun forRoute(call: suspend (ApplicationCall) -> Unit):
        PipelineInterceptor<Unit, ApplicationCall> = { call(this.call) }

fun forPhotonRoute(call: suspend (ApplicationCall) -> Unit): PipelineInterceptor<Unit, ApplicationCall> = {
    try {
        call(this.call)
    } catch (e: Exception) {
        this.call.respond(InternalErrorReply(e))
    }
}

fun forRoute(call: suspend (DefaultWebSocketServerSession) -> Unit):
        suspend DefaultWebSocketServerSession.() -> Unit = { call(this) }