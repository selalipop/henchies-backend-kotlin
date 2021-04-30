package controllers

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.util.*
import kotlinx.coroutines.flow.first
import models.*
import models.game.item.CreateRules
import models.game.item.GameItem
import models.game.item.GameItemDefinition
import models.id.GameId
import models.id.PlayerId
import repository.GameStateStore
import repository.PlayerSecretsStore
import util.ktor.getJsonOrFail
import util.logger

class GameItemController(
    private val gameStateStore: GameStateStore,
    private val playerSecretsStore: PlayerSecretsStore
) {
    suspend fun createItem(ctx: ApplicationCall) {

        val gameId = GameId(ctx.parameters.getOrFail("gameId"))
        val playerId = PlayerId(ctx.parameters.getOrFail("playerId"))
        val resourceId = ctx.parameters.getOrFail("resourceId")

        val position = ctx.parameters.getJsonOrFail<Vector3>("position")
        val rotation = ctx.parameters.getJsonOrFail<Vector4>("rotation")

        val definition = GameItemDefinition.getByResourceId(resourceId)
            ?: throw Error("Failed to get game item definition, game ID: $gameId player ID: $playerId resourceId: $resourceId")

        val (secrets, error) = playerSecretsStore.observePlayerSecrets(gameId, playerId)

       if(secrets == null || error != null){
            throw Error("Failed to observer player secrets when creating item", error)
        }

        val playerSecrets = secrets.first()

        val canCreate = when(definition.createRules){
            CreateRules.Anyone -> true
            CreateRules.ImposterOnly -> playerSecrets.isImposter
            CreateRules.HenchieOnly -> !playerSecrets.isImposter
        }

        if(!canCreate){
            throw Error("Player $playerId cannot create item $definition based on CreateRules")
        }

        val newItem = GameItem(definition, position, rotation)

        gameStateStore.updateGameState(gameId){
            it.copy(spawnedItems = it.spawnedItems + newItem)
        }
        logger.info { "Created game item $newItem in game $gameId for player $playerId" }
        ctx.respond(newItem)
    }
}