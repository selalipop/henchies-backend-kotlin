package controller.updates.photon

import com.squareup.moshi.Moshi
import io.javalin.http.Context
import javalin.bindQueryParams
import javalin.scope
import javalin.sendError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import repository.GameStateStore
import repository.PlayerSecretsStore
import schema.CreateOptions
import schema.PlayerJoinedRequest
import schema.RoomClosedRequest
import kotlin.math.ceil
import kotlin.math.roundToInt


class PlayerJoinedController(
    val moshi: Moshi,
    val gameStateStore: GameStateStore
) {

    fun playerJoined(ctx: Context) {
        val (request, errors) = ctx.bindQueryParams<PlayerJoinedRequest>(moshi)
        if (request == null || errors != null) {
            ctx.sendError("Invalid parameters: ${errors.toString()}")
            return
        }

        ctx.scope().launch {
            processPlayerJoined(request.gameId, request.playerId, gameStateStore)
        }
    }

    private fun getImposterCountForGame(createOptions: CreateOptions): Int {
        var imposterCount = createOptions.customProps.imposterCount
        if (imposterCount == 0) {
            //TODO: Get real imposter count
            imposterCount = ceil(0.2f * createOptions.maxPlayers).roundToInt()
        }
        return imposterCount
    }
}