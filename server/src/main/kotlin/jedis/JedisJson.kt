package jedis

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.squareup.moshi.Moshi
import models.InlineStringClassAdapter
import redis.clients.jedis.Jedis
import redis.clients.jedis.params.SetParams
import java.lang.Error
import kotlin.time.Duration


val sharedMoshi: Moshi = Moshi.Builder().add(InlineStringClassAdapter()).build()
class JedisError : Error()
/***
 * @return Error value returns true if error was a missing key
 */
inline fun <reified T> Jedis.updateJson(
    key: String,
    publishChannel: String?,
    ttl: Duration,
    update: (T) -> T
): Result<Unit, Boolean> {
    watch(key)
    val currentValue = get(key) ?: return Err(true)

    val adapter = sharedMoshi.adapter(T::class.java)
    val decodedValue = adapter.fromJson(currentValue) ?: return Err(false)
    val updatedValue = update(decodedValue)

    val encodedValue = adapter.toJson(updatedValue)

    val tx = multi()
    tx.set(key, encodedValue, ttlFromDuration(ttl))

    publishChannel?.let {
        tx.publish(it, encodedValue)
    }

    tx.exec()
    return Ok(Unit)
}

/***
 * @return Error value returns true if error was a missing key
 */
inline fun <reified T> Jedis.getJson(
    key: String
): Result<T, Boolean> {
    val adapter = sharedMoshi.adapter(T::class.java)
    val value = get(key) ?: return Err(true)
    val decodedValue = adapter.fromJson(value) ?: return Err(false)
    return Ok(decodedValue)
}

inline fun <reified T> Jedis.setJson(
    key: String,
    publishChannel: String?,
    value: T,
    ttl: Duration,
): Result<Unit, Error> {
    return setJson(key, publishChannel, value, ttlFromDuration(ttl))
}

inline fun <reified T> Jedis.setJson(
    key: String,
    publishChannel: String?,
    value: T,
    setParams: SetParams
): Result<Unit, Error> {
    val adapter = sharedMoshi.adapter(T::class.java)
    val encodedValue = adapter.toJson(value)
    set(key, encodedValue, setParams)
    publishChannel?.let {
        publish(it, encodedValue)
    }
    return Ok(Unit)
}