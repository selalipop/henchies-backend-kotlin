import com.squareup.moshi.Moshi
import controller.updates.GameKeyController
import controller.updates.UpdateController
import controller.updates.photon.PlayerJoinedController
import controller.updates.photon.PlayerLeftController
import controller.updates.photon.RoomClosedController
import controller.updates.photon.RoomCreatedController
import io.javalin.Javalin
import io.javalin.plugin.json.FromJsonMapper
import io.javalin.plugin.json.JavalinJson
import io.javalin.plugin.json.ToJsonMapper
import javalin.withScopesEnabled
import jedis.JedisFlowPubSub
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import models.InlineStringClassAdapter
import models.id.GameId
import org.koin.core.KoinComponent
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.inject
import org.koin.dsl.module
import redis.clients.jedis.JedisPool
import repository.GameKeyStore
import repository.GameStateStore
import repository.PlayerSecretsStore
import repository.redis.RedisGameKeyStore
import repository.redis.RedisGameStateStore
import repository.redis.RedisPlayerSecretsStore

val appModule = module {
    single {
        Moshi.Builder().add(InlineStringClassAdapter()).build()
    }

    single {
        val redisUrl: String = System.getenv("REDIS_URL") ?: "localhost"
        JedisPool(redisUrl)
    }


    single { RedisGameStateStore(get(), get(), get()) as GameStateStore }
    single { RedisPlayerSecretsStore(get(), get(), get()) as PlayerSecretsStore }
    single { RedisGameKeyStore(get()) as GameKeyStore }

    single { GameKeyController(get(), get()) }
    single { UpdateController(get(), get(), get(), get()) }

    single { RoomCreatedController(get(), get()) }
    single { RoomClosedController(get(), get(), get()) }
    single { PlayerLeftController(get(), get()) }
    single { PlayerJoinedController(get(), get()) }

    factory { get<JedisPool>().resource }
    factory { JedisFlowPubSub(get()) }
}

fun main(args: Array<String>) {
    startKoin {
        printLogger()
    }
    loadKoinModules(listOf(appModule))
    Server().serve(args)
}

class Server : KoinComponent {
    private val gameStateStore: GameStateStore by inject()
    private val playerSecretsStore: RedisPlayerSecretsStore by inject()
    private val gameKeyController: GameKeyController by inject()
    private val updateController: UpdateController by inject()

    private val roomCreatedController: RoomCreatedController by inject()
    private val roomClosedController: RoomClosedController by inject()

    private val playerJoinedController : PlayerJoinedController by inject()
    private val playerLeftController : PlayerLeftController by inject()

    fun serve(args: Array<String>) {


        val port = System.getenv("PORT")?.toInt() ?: 23567

        val moshi = Moshi.Builder().add(InlineStringClassAdapter()).build()
        JavalinJson.fromJsonMapper = object : FromJsonMapper {
            override fun <T> map(json: String, targetClass: Class<T>) = moshi.adapter(targetClass).fromJson(json) as T
        }

        JavalinJson.toJsonMapper = object : ToJsonMapper {
            override fun map(obj: Any): String = moshi.adapter(obj.javaClass).toJson(obj)
        }

        val gameId = GameId("test")
        gameStateStore.initGameState(gameId, 10, 10);

        val app = Javalin.create().withScopesEnabled().start(port)
        app.get("/") { ctx -> ctx.result("Hello World") }
        app.get("/key", gameKeyController::getPlayerGameKey)

        app.post("/photonwebhooks/roomcreated", roomCreatedController::roomCreated)
        app.post("/photonwebhooks/roomclosed", roomClosedController::roomClosed)
        app.post("/photonwebhooks/playerjoined", playerJoinedController::playerJoined)
        app.post("/photonwebhooks/playerleft", playerLeftController::playerLeft)

        app.ws("/updates", updateController::getUpdates)
    }

}