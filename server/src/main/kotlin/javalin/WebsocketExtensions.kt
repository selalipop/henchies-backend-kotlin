package javalin

import io.javalin.http.Context
import io.javalin.http.Handler
import io.javalin.websocket.WsContext
import io.javalin.websocket.WsHandler
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import logger
import org.eclipse.jetty.http.HttpStatus
import java.util.function.Consumer
import kotlin.time.Duration



fun WsHandler.setupPings(ctx: WsContext, interval: Duration, pingMessage: String): CoroutineScope =
    this.scope().apply {
        this.launch {
            while (this.isActive) {
                delay(interval.toLongMilliseconds())
                ctx.send(pingMessage)
            }
        }
    }

fun WsContext.sendError(message: String, cause: Throwable? = null) {
    logger.error(cause) { "Websocket error" }
    session.close(HttpStatus.Code.INTERNAL_SERVER_ERROR.code, message)
}

fun WsContext.sendError(error: Throwable?) {
    logger.error(error) { "Websocket error" }
    session.close(HttpStatus.Code.INTERNAL_SERVER_ERROR.code, error?.message)
}

fun Flow<Any>.closeWsOnComplete(ctx: WsContext): Flow<Any> = this.onCompletion { err ->
    if (err != null) {
        ctx.sendError(err)
    } else {
        ctx.session.close(HttpStatus.Code.OK.code, "Finished")
    }
}
typealias WsDefinition = Consumer<WsHandler>