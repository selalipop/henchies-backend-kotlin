package controller.updates

import com.squareup.moshi.Moshi
import io.javalin.http.Context
import io.javalin.http.Handler
import javalin.bindQueryParams
import javalin.scope
import javalin.sendError
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import logger
import org.eclipse.jetty.http.HttpStatus
import repository.GameKeyStore
import schema.GetGameKeyRequest
import schema.GetGameKeyResponse

class GameKeyController(
    val moshi: Moshi,
    private val gameKeyStore: GameKeyStore) {
    fun getPlayerGameKey(ctx: Context) {
        val (request, errors) = ctx.bindQueryParams<GetGameKeyRequest>(moshi)
        if (request == null || errors != null) {
            ctx.sendError("Invalid parameters: ${errors.toString()}")
            return
        }

        val (savedKey, err) = gameKeyStore.createOrRetrieveGameKey(request.gameId, request.playerId, ctx.ip())
        if (savedKey == null || err != null) {
            logger.error(err) { "Failed to retrieve game key, game ID: $request.gameId player ID: $request.playerId" }
            return ctx.res.sendError(
                HttpStatus.BAD_REQUEST_400,
                "Failed to retrieve game key"
            )
        }

        ctx.status(HttpStatus.OK_200)
            .json(GetGameKeyResponse(savedKey.key))
    }
}