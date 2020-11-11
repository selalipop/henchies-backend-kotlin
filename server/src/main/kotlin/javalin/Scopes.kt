package javalin

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.websocket.WsHandler
import kotlinx.coroutines.*
import java.lang.Exception


private val handlerScopes = mutableMapOf<String, CompletableJob>()
private val wsScopes = mutableMapOf<WsHandler, CompletableJob>()

fun Javalin.withScopesEnabled(): Javalin {
    before {
        handlerScopes[it.req.session.id] = SupervisorJob()
    }

    after {
        waitForFinish(handlerScopes[it.req.session.id])
    }

    wsBefore {
        wsScopes[it] = SupervisorJob()
    }

    wsAfter {
        waitForFinish(wsScopes[it])
    }
    return this
}

private fun waitForFinish(job : CompletableJob?) {
    runBlocking {
        job?.children?.forEach {
            it.join()
        }
    }
}

fun Context.scope(): CoroutineScope =
    handlerScopes[req.session.id]?.let {
        CoroutineScope(it + Dispatchers.IO)
    } ?: throw Exception("Forgot to call Javalin.withScopesEnabled")

fun WsHandler.scope(): CoroutineScope =
    wsScopes[this]?.let {
        CoroutineScope(it + Dispatchers.IO)
    } ?: throw Exception("Forgot to call Javalin.withScopesEnabled")

