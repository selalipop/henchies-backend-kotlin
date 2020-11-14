package util.ktor

import io.ktor.application.*
import io.ktor.util.pipeline.*
import io.ktor.websocket.*

fun forRoute(call: suspend (ApplicationCall) -> Unit):
        PipelineInterceptor<Unit, ApplicationCall> = { call(this.call) }

fun forRoute(call: suspend (DefaultWebSocketServerSession) -> Unit):
        suspend DefaultWebSocketServerSession.() -> Unit = { call(this) }