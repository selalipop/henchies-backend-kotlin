package repository.redis

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flowOf
import redis.clients.jedis.Jedis
import util.error.err
import util.jedis.JedisFlowPubSub
import util.jedis.getJson
import util.jedis.setJson
import util.jedis.updateJson
import kotlin.time.Duration

abstract class RedisGenericStore<T>(
    val jedis: Jedis,
    val jedisPubSub: JedisFlowPubSub
) {

    inline fun <reified T> update(
        redisKey: String,
        redisPubSubKey: String,
        redisTtl: Duration,
        stateUpdate: (T) -> T
    ): Result<Unit, Error> {

        val (_, error) = jedis.updateJson(
            redisKey,
            redisPubSubKey,
            redisTtl,
            stateUpdate
        )

        if (error != null) {
            return err(
                if (error) {
                    "Could not find item with given ID"
                } else {
                    "Internal Redis util.ktor.error"
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
            return err("Internal Redis util.ktor.error")
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
            jedisPubSub.subscribeJsonFlow<T>(redisPubSubKey)
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