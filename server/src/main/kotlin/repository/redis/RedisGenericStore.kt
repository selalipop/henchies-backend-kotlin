package repository.redis

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flowOf
import redis.*
import util.error.err
import kotlin.time.Duration

abstract class RedisGenericStore<T>(
    val redis: RedisClient
) {

    suspend inline fun <reified T> update(
        redisKey: String,
        redisPubSubKey: String,
        redisTtl: Duration,
        noinline  stateUpdate: suspend (T) -> T
    ): Result<Unit, Error> {

        return redis.updateJson(
            redisKey,
            redisPubSubKey,
            redisTtl,
            stateUpdate
        ).mapError(this::simplifyRedisErr)
    }

    suspend inline fun <reified T> set(
        redisKey: String,
        redisPubSubKey: String,
        redisTtl: Duration,
        newValue: T
    ): Result<Unit, Error> {
        return redis.setJson(
            redisKey,
            redisPubSubKey,
            newValue,
            redisTtl
        ).mapError(this::simplifyRedisErr)
    }

    fun simplifyRedisErr(it: RedisError): Error {
        return if (it.isMissingKey) {
            Error("Could not find item with given ID: ", it)
        } else {
            Error("Internal redis error", it)
        }
    }

    suspend inline fun <reified T : Any> observe(
        redisKey: String,
        redisPubSubKey: String
    ): Result<Flow<T>, Error> {

        val (currentValue, err) = redis.getJson<T>(redisKey)
        if (err != null) {
            return err(simplifyRedisErr(err))
        }

        val flow = flowOf(
            flowOf(currentValue),
            redis.subscribeJsonChannel(redisPubSubKey)
        )
            .flattenMerge()
            .filterNotNull()
        return Ok(flow)
    }

    suspend fun clear(redisKey: String): Result<Unit, Error> {
        return redis.clear(redisKey)
    }

}

