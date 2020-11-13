package util.jedis

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import util.JSON
import redis.clients.jedis.Jedis
import redis.clients.jedis.params.SetParams
import kotlin.time.Duration


class JedisError(val isMissingKey: Boolean) : Error()

/***
 * @return Error value returns true if util.ktor.error was a missing key
 */
inline fun <reified T> Jedis.updateJson(
    key: String,
    publishChannel: String?,
    ttl: Duration,
    update: (T) -> T
): Result<Unit, Boolean> {
    watch(key)
    val currentValue = get(key) ?: return Err(true)

    val decodedValue = JSON.decodeFromString<T>(currentValue) ?: return Err(false)
    val updatedValue = update(decodedValue)

    val encodedValue = JSON.encodeToString(updatedValue)

    val tx = multi()
    tx.set(key, encodedValue, ttlFromDuration(ttl))

    publishChannel?.let {
        tx.publish(it, encodedValue)
    }

    tx.exec()
    return Ok(Unit)
}

/***
 * @return Error value returns true if util.ktor.error was a missing key
 */
inline fun <reified T> Jedis.getJson(
    key: String
): Result<T, JedisError> {
    val value = get(key) ?: return Err(JedisError(true))
    val decodedValue = JSON.decodeFromString<T>(value) ?: return Err(JedisError(false))
    return Ok(decodedValue)
}

inline fun <reified T> Jedis.setJson(
    key: String,
    publishChannel: String?,
    value: T,
    ttl: Duration,
): Result<Unit, JedisError> {
    return setJson(key, publishChannel, value, ttlFromDuration(ttl))
}

inline fun <reified T> Jedis.setJson(
    key: String,
    publishChannel: String?,
    value: T,
    setParams: SetParams
): Result<Unit, JedisError> {
    val encodedValue = JSON.encodeToString(value)
    set(key, encodedValue, setParams)
    publishChannel?.let {
        publish(it, encodedValue)
    }
    return Ok(Unit)
}