package redis

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.serializer
import redis.clients.jedis.params.SetParams
import redis.jedis.ttlFromDuration
import util.JSON
import util.error.err
import util.error.resultOf
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.time.Duration



inline fun <reified T> RedisClient.subscribeJsonChannel(channelName: String) =
    subscribeChannel(channelName).map { JSON.decodeFromString<T>(it) }

suspend inline fun RedisClient.getJson(key: String, type: KType): Result<Any?, RedisError> {

    val (redisValue, err) = get(key)
    if (err != null || redisValue == null) {
        return err(RedisError("failed to get redis value for getJson", err))
    }

    val serializer = JSON.serializersModule.serializer(type)

    return resultOf { JSON.decodeFromString(serializer, redisValue) }
        .mapError { RedisError("got redis key but failed to deserialize from Json", false, it) }
}

suspend fun RedisClient.updateJson(
    key: String,
    publishChannel: String?,
    ttl: Duration,
    type: KType,
    update: suspend (Any?) -> Any?
): Result<Unit, RedisError> {

    return update(key, publishChannel, ttlFromDuration(ttl)) {
        val serializer = JSON.serializersModule.serializer(type)
        val decodedValue = JSON.decodeFromString(serializer, it)
        val updatedValue = update(decodedValue)

        JSON.encodeToString(serializer, updatedValue)
    }
}


suspend inline fun <reified T> RedisClient.setJson(
    key: String,
    publishChannel: String?,
    value: T,
    setParams: SetParams,
): Result<Unit, RedisError> {
    return setJson(key, publishChannel, value, typeOf<T>(), setParams)
}
suspend inline fun RedisClient.setJson(
    key: String,
    publishChannel: String?,
    value: Any?,
    type: KType,
    setParams: SetParams,
): Result<Unit, RedisError> {

    val serializer = JSON.serializersModule.serializer(type)
    val encoded = JSON.encodeToString(serializer, value)
    return set(key, publishChannel, encoded, setParams)

}
suspend inline fun <reified T> RedisClient.setJson(
    key: String,
    publishChannel: String?,
    value: T,
    ttl: Duration,
): Result<Unit, RedisError> {
    return setJson(key, publishChannel, value, typeOf<T>(), ttlFromDuration(ttl))
}

inline suspend fun <reified T> RedisClient.getJson(
    key: String
): Result<T, RedisError> {
    @Suppress("UNCHECKED_CAST")
    return getJson(key, typeOf<T>()) as Result<T, RedisError>
}

/***
 * @return Error value returns true if util.ktor.error was a missing key
 */
suspend inline fun <reified T> RedisClient.updateJson(
    key: String,
    publishChannel: String?,
    ttl: Duration,
    noinline update: suspend (T) -> T
): Result<Unit, RedisError> {
    return updateJson(key, publishChannel, ttl, typeOf<T>()) { update(it as T) }
}

