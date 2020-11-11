package controller.updates.photon

import com.squareup.moshi.Moshi
import io.javalin.http.Context
import javalin.bindQueryParams
import javalin.scope
import javalin.sendError
import kotlinx.coroutines.launch
import models.GamePhase
import models.byId
import repository.GameStateStore
import schema.CreateOptions
import schema.PlayerJoinedRequest
import schema.PlayerLeftRequest
import kotlin.math.ceil
import kotlin.math.roundToInt


class PlayerLeftController(
    val moshi: Moshi,
    private val gameStateStore: GameStateStore
) {

    fun playerLeft(ctx: Context) {
        val (request, errors) = ctx.bindQueryParams<PlayerLeftRequest>(moshi)
        if (request == null || errors != null) {
            ctx.sendError("Invalid parameters: ${errors.toString()}")
            return
        }

        ctx.scope().launch {
            gameStateStore.updateGameState(request.gameId) { state ->
                val gonePlayer = state.players.byId(request.playerId)

                if (gonePlayer != null && state.phase < GamePhase.Started) {
                    return@updateGameState state.copy(players = state.players - gonePlayer)
                }

                return@updateGameState state
            }
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