import controllers.GameItemController
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
import org.koin.core.KoinComponent
import org.koin.core.inject
import repository.GameStateStore
import schema.requests.photon.InternalErrorReply
import util.JSON
import util.ktor.forPhotonRoute
import util.ktor.forRoute
import util.ktor.forWsRoute
import util.logger
import java.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class Server : KoinComponent {
    private val gameStateStore: GameStateStore by inject()
    private val gameKeyController: GameKeyController by inject()
    private val gameItemController: GameItemController by inject()
    private val updateController: UpdateController by inject()

    private val roomCreatedController: RoomCreatedController by inject()
    private val roomClosedController: RoomClosedController by inject()

    private val playerJoinedController: PlayerJoinedController by inject()
    private val playerLeftController: PlayerLeftController by inject()


    fun serve(port: Int) {
        val server = embeddedServer(Netty, port = port) {
            installExtensions()
            setupRouting()
        }
        server.start(wait = true)
    }

    fun Application.installExtensions() {
        install(XForwardedHeaderSupport)
        install(ContentNegotiation) {
            json(JSON)
        }
        install(CORS) {
            method(HttpMethod.Options)
            method(HttpMethod.Get)
            method(HttpMethod.Post)
            allowNonSimpleContentTypes = true
            allowSameOrigin = true
            header(HttpHeaders.XForwardedProto)
            header(HttpHeaders.Origin)
            header(HttpHeaders.Allow)
            header(HttpHeaders.AccessControlAllowOrigin)
            header(HttpHeaders.AccessControlAllowHeaders)
            header(HttpHeaders.ContentType)
            header(HttpHeaders.AuthenticationInfo)

            //TODO: Enforce reduced list of hosts
            anyHost()
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
                if (call.request.uri.startsWith(photonWebhookRoot)) {
                    call.respond(InternalErrorReply(cause))
                } else {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        "Internal Server Error: ${cause.message}"
                    )
                }
                throw cause
            }
        }
        install(WebSockets) {
            //Track to use kotlin time classes https://youtrack.jetbrains.com/issue/KT-50516
            pingPeriod = Duration.ofSeconds(30)
            timeout = Duration.ofSeconds(40)
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }
    }


    private var photonWebhookRoot = "/photonwebhooks"

    private fun Application.setupRouting() {
        routing {

            get("/") {
                logger.info { "Hello Henchies!" }
                call.respondText("Hello Henchies!")
            }

            get("/player/{playerId}/key", forRoute(gameKeyController::getPlayerGameKey))

            post("/game/{gameId}/items", forRoute(gameItemController::createItem))

            get("/health", forRoute(gameKeyController::healthCheck))

            post("$photonWebhookRoot/playerjoined", forPhotonRoute(playerJoinedController::playerJoined))
            post("$photonWebhookRoot/playerleft", forPhotonRoute(playerLeftController::playerLeft))
            post("$photonWebhookRoot/roomcreated", forPhotonRoute(roomCreatedController::roomCreated))
            post("$photonWebhookRoot/roomclosed", forPhotonRoute(roomClosedController::roomClosed))

            webSocket("/updates") { forWsRoute(updateController::getUpdates) }
        }
    }

    suspend fun test(ctx: ApplicationCall) {
        ctx.respondText("hello world")
    }

    private fun getPlayerIdForCall(it: ApplicationCall) =
        it.request.header("henchies-player-id") ?: it.parameters["playerId"] ?: it.parameters["playerID"]

}