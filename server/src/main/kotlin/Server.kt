import controllers.GameKeyController
import controllers.UpdateController
import controllers.photon.PlayerJoinedController
import controllers.photon.PlayerLeftController
import controllers.photon.RoomClosedController
import controllers.photon.RoomCreatedController
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import models.id.GameId
import org.koin.core.KoinComponent
import org.koin.core.inject
import repository.GameStateStore
import util.JSON
import util.ktor.forPhotonRoute
import util.ktor.forRoute
import util.logger
import kotlin.time.seconds
import kotlin.time.toJavaDuration

class Server : KoinComponent {
    private val gameStateStore: GameStateStore by inject()
    private val gameKeyController: GameKeyController by inject()
    private val updateController: UpdateController by inject()

    private val roomCreatedController: RoomCreatedController by inject()
    private val roomClosedController: RoomClosedController by inject()

    private val playerJoinedController: PlayerJoinedController by inject()
    private val playerLeftController: PlayerLeftController by inject()


    suspend fun serve(port: Int) {
        val gameId = GameId("test")
        gameStateStore.initGameState(gameId, 10, 10)

        val server = embeddedServer(Netty, port = port) {
            installExtensions()
            setupRouting()
        }
        server.start(wait = true)
    }

    private fun Application.installExtensions() {
        install(XForwardedHeaderSupport)
        install(ContentNegotiation) {
            json(JSON)
        }

        install(CallLogging) {
            mdc("playerId") { getPlayerIdForCall(it) }
            mdc("method") { it.request.httpMethod.value }
            mdc("path") { it.request.path() }
            mdc("client") { it.request.host() }
        }

        install(StatusPages) {
            exception<Throwable> { cause ->
                logger.error(cause) { "Internal Server Error" }
                call.respond(
                    HttpStatusCode.InternalServerError,
                    "Internal Server Error: ${cause.message}"
                )
                throw cause
            }
        }
        install(WebSockets) {
            pingPeriod = 30.seconds.toJavaDuration()
            timeout = 40.seconds.toJavaDuration()
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }
    }


    private fun Application.setupRouting() {
        routing {

            get("/") {
                logger.info { "Hello Henchies!" }
                call.respondText("Hello Henchies!")
            }

            get("/player/{playerId}/key", forRoute(gameKeyController::getPlayerGameKey))

            post("/photonwebhooks/playerjoined", forPhotonRoute(playerJoinedController::playerJoined))
            post("/photonwebhooks/playerleft", forPhotonRoute(playerLeftController::playerLeft))
            post("/photonwebhooks/roomcreated", forPhotonRoute(roomCreatedController::roomCreated))
            post("/photonwebhooks/roomclosed", forPhotonRoute(roomClosedController::roomClosed))

            webSocket("/updates") { forRoute(updateController::getUpdates) }
        }
    }

    suspend fun test(ctx: ApplicationCall) {
        ctx.respondText("hello world")
    }

    private fun getPlayerIdForCall(it: ApplicationCall) =
        it.request.header("henchies-player-id") ?: it.parameters["playerId"] ?: it.parameters["playerID"]

}