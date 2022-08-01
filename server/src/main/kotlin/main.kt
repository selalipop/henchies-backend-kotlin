import controllers.GameItemController
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
private const val DefaultRedisHost = "redis"
private const val DefaultRedisPort = 6379
fun main() = runBlocking {
    startKoin {
        printLogger()
    }

    val redisHost: String = System.getenv("REDIS_HOST")
        ?: DefaultRedisHost.also { logger.warn { "Using Redis $DefaultRedisHost due to missing REDIS_HOST" } }

    val redisPort: Int = System.getenv("REDIS_PORT").toIntOrNull()
        ?: DefaultRedisPort.also { logger.warn { "Using Redis $DefaultRedisPort due to missing REDIS_PORT" } }

    val port = System.getenv("PORT")?.toInt()
        ?: DefaultPort.also { logger.warn { "Server binding to port $DefaultPort due to missing PORT" } }

    loadKoinModules(listOf(appModule(redisHost,redisPort)))

    Server().serve(port)
}


fun appModule(redisHost: String, redisPort: Int) = module {
    singleBy<GameStateStore, RedisGameStateStore>()
    singleBy<PlayerSecretsStore, RedisPlayerSecretsStore>()
    singleBy<GameKeyStore, RedisGameKeyStore>()
    singleBy<RedisClient, JedisClient>()

    single<GameKeyController>()
    single<GameItemController>()
    single<UpdateController>()


    single<RoomCreatedController>()
    single<RoomClosedController>()
    single<PlayerLeftController>()
    single<PlayerJoinedController>()

    single { JedisPool(redisHost, redisPort) }
    factory<Jedis> { get<JedisPool>().resource }
}
