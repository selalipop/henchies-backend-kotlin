package repository.redis

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.squareup.moshi.Moshi
import error.ErrResult
import jedis.JedisFlowPubSub
import jedis.getJson
import jedis.setJson
import jedis.updateJson
import kotlinx.coroutines.flow.*
import redis.clients.jedis.Jedis
import kotlin.time.Duration

abstract class RedisGenericStore<T>(
    val moshi: Moshi,
    val jedis: Jedis,
    val jedisPubSub: JedisFlowPubSub
) {

    inline fun <reified T> update(
        redisKey: String,
        redisPubSubKey: String,
        redisTtl: Duration,
        update: (T) -> T
    ): Result<Unit, Error> {

        val (_, error) = jedis.updateJson(
            redisKey,
            redisPubSubKey,
            redisTtl,
            update
        )

        if (error != null) {
            return ErrResult(
                if (error) {
                    "Could not find item with given ID"
                } else {
                    "Internal Redis error"
                }
            )
        }

        return Ok(Unit)
    }

    inline fun <reified T> set(
        redisKey: String,
        redisPubSubKey: String,
        redisTtl: Duration,
        newValue: T
    ): Result<Unit, Error> {
        val (_, error) = jedis.setJson(
            redisKey,
            redisPubSubKey,
            newValue,
            redisTtl
        )
        if (error != null) {
            return ErrResult("Internal Redis error")
        }
        return Ok(Unit)
    }

    inline fun <reified T : Any> observe(
        redisKey: String,
        redisPubSubKey: String
    ): Result<Flow<T>, Error> {

        val (currentValue, _) = jedis.getJson<T>(redisKey)
        val mappedFlow = flowOf(
            flowOf(currentValue),
            jedisPubSub.subscribeJsonFlow<T>(redisPubSubKey, moshi),
        )
            .flattenMerge()
            .filterNotNull()

        return Ok(mappedFlow)
    }

    fun clear(redisKey: String, redisPubSubKey: String): Result<Unit, Error> {
        jedis.del(redisKey)
        jedis.del(redisPubSubKey)
        return Ok(Unit)
    }

}