package redis

import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow
import redis.clients.jedis.params.SetParams

interface RedisClient {

    suspend fun get(key: String): Result<String, RedisError>
    suspend fun set(key: String, publishKey: String?, value: String, params: SetParams): Result<Unit, RedisError>

    suspend fun clear(redisKey: String): Result<Unit, RedisError>

    suspend fun update(key: String, publishKey: String?, params: SetParams, update: suspend (String) -> String):
            Result<Unit, RedisError>

    fun subscribeChannel(channelName: String): Flow<String>
}