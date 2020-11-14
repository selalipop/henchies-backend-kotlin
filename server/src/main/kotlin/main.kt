import controllers.GameKeyController
import controllers.UpdateController
import controllers.photon.PlayerJoinedController
import controllers.photon.PlayerLeftController
import controllers.photon.RoomClosedController
import controllers.photon.RoomCreatedController
import kotlinx.coroutines.runBlocking
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.experimental.builder.single
import org.koin.experimental.builder.singleBy
import redis.RedisClient
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import redis.jedis.JedisClient
import repository.GameKeyStore
import repository.GameStateStore
import repository.PlayerSecretsStore
import repository.redis.RedisGameKeyStore
import repository.redis.RedisGameStateStore
import repository.redis.RedisPlayerSecretsStore
import util.logger

private const val DefaultPort = 23567
private const val DefaultRedisUrl = "localhost"
fun main() = runBlocking {
    startKoin {
        printLogger()
    }

    val redisUrl: String = System.getenv("HENCHIES_REDISCONNECTURL")
        ?: DefaultRedisUrl.also { logger.warn { "Using Redis $DefaultRedisUrl due to missing HENCHIES_REDISCONNECTURL" } }

    val port = System.getenv("PORT")?.toInt()
        ?: DefaultPort.also { logger.warn { "Using port $DefaultPort due to missing PORT" } }

    loadKoinModules(listOf(appModule(redisUrl)))

    Server().serve(port)
}


fun appModule(redisUrl: String) = module {
    singleBy<GameStateStore, RedisGameStateStore>()
    singleBy<PlayerSecretsStore, RedisPlayerSecretsStore>()
    singleBy<GameKeyStore, RedisGameKeyStore>()
    singleBy<RedisClient, JedisClient>()

    single<GameKeyController>()
    single<UpdateController>()


    single<RoomCreatedController>()
    single<RoomClosedController>()
    single<PlayerLeftController>()
    single<PlayerJoinedController>()

    single { JedisPool(redisUrl) }
    factory<Jedis> { get<JedisPool>().resource }
}
