package redis.jedis

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.*
import redis.RedisClient
import redis.RedisError
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPubSub
import redis.clients.jedis.params.SetParams
import util.error.err
import util.error.resultOf
import util.logger
import kotlin.concurrent.thread
import kotlin.math.pow
import kotlin.math.roundToInt

class JedisClient(private val jedisPool: JedisPool) : RedisClient {

    val subscriptions: MutableMap<String, BroadcastChannel<String>> = mutableMapOf()

    init {
        GlobalScope.launch(Dispatchers.IO) {
           flow<Unit> {
               jedisPool.resource.use { jedis ->
                   jedis.psubscribe(listener, "*")
               }
           }.retryWhen { error, attemptCount ->
               logger.error(error) { "error while listening for Jedis messages" }
               //Exponential backoff + random jitter
               val backoffMs = (2.0.pow(attemptCount.toInt()) +
                       (1000 * Math.random())).toLong()
               delay(backoffMs)
               !jedisPool.isClosed
           }.collect()
       }
    }

    private val listener = object : JedisPubSub() {
        override fun onPMessage(pattern: String, channel: String?, message: String?) {
            subscriptions[channel]?.apply {
                if (this.isClosedForSend) {
                    subscriptions.remove(channel)
                } else {
                    message?.let { offer(it) }
                }
            }
        }

        override fun onUnsubscribe(channel: String?, subscribedChannels: Int) {
            subscriptions[channel]?.close()
            subscriptions.remove(channel)
        }
    }


    override suspend fun clear(redisKey: String): Result<Unit, RedisError> = jedis {
        it.del(redisKey)
        return@jedis Ok(Unit)
    }

    override suspend fun get(key: String): Result<String, RedisError> = jedis {
        val value = it.get(key) ?: return@jedis err(RedisError.MissingKey)
        return@jedis Ok(value)
    }

    override suspend fun set(
        key: String, publishKey: String?, value: String, params: SetParams
    ): Result<Unit, RedisError> = jedis { j ->
        j.set(key, value, params)
        publishKey?.let {
            j.publish(it, value)
        }
        return@jedis Ok(Unit)
    }

    override suspend fun update(
        key: String, publishKey: String?, params: SetParams, update: suspend (String) -> String
    ): Result<Unit, RedisError> = jedis { j ->
        j.watch(key)
        val currentValue = j.get(key) ?: return@jedis err(RedisError.MissingKey)

        val (updatedValue, err) = resultOf {
            update(currentValue)
        }

        if (err != null) {
            return@jedis err(RedisError("failed to run update function", err))
        }
        val tx = j.multi()
        tx.set(key, updatedValue, params)

        publishKey?.let {
            tx.publish(it, updatedValue)
        }

        tx.exec()
        return@jedis Ok(Unit)
    }

    override fun subscribeChannel(channelName: String): Flow<String> {
        val channel = (subscriptions[channelName] ?: createChannel(channelName))
        return channel.asFlow()
    }

    private fun createChannel(channelName: String): BroadcastChannel<String> {
        val channel = BroadcastChannel<String>(DEFAULT_BUFFER_SIZE)
        subscriptions[channelName] = channel
        return channel
    }

    private suspend fun <T> jedis(block: suspend (Jedis) -> T): T = withContext(Dispatchers.IO) {
        jedisPool.resource.use { j ->
            return@withContext block(j)
        }
    }
}