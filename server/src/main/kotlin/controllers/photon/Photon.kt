package controllers.photon

import io.ktor.application.*
import io.ktor.response.*
import schema.requests.photon.WebhookReply
import util.logger

const val WaitingForLeavingPlayerDelay = 500L
const val GameLobbyId = "GameLobby"

suspend fun ApplicationCall.respondPhoton(reply: WebhookReply){

    logger.info("Sending message to Photon ${reply}")
    respond(reply)
}