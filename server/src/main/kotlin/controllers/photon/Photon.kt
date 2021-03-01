package controllers.photon

import io.ktor.application.*
import io.ktor.response.*
import schema.requests.photon.WebhookReply

const val WaitingForLeavingPlayerDelay = 500L
const val GameLobbyId = "GameLobby"

suspend fun ApplicationCall.respondPhoton(reply: WebhookReply){
    respond(reply)
}