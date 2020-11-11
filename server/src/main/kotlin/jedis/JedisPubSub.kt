@file:Suppress("EXPERIMENTAL_API_USAGE")

package jedis

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import models.GameState
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPubSub
import java.io.Closeable
import java.lang.Exception
import kotlin.concurrent.thread

class JedisFlowPubSub(private val jedisPool: JedisPool) {
    val subscriptions: MutableMap<String, BroadcastChannel<String>> = mutableMapOf()
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

    init {
        thread {
            jedisPool.resource.use { jedis ->
                jedis.psubscribe(listener, "*")
            }
        }
    }
    inline fun <reified T>subscribeJsonFlow(channelName: String, moshi: Moshi) = subscribeFlow(channelName).map {
        withContext(Dispatchers.IO) {
            moshi.adapter(T::class.java).fromJson(it)
        }
    }

    fun subscribeFlow(channelName: String): Flow<String> {
        val channel = (subscriptions[channelName] ?: createChannel(channelName))
        return channel.asFlow()
    }

    private fun createChannel(channelName: String): BroadcastChannel<String> {
        val channel = BroadcastChannel<String>(DEFAULT_BUFFER_SIZE)
        subscriptions[channelName] = channel
        return channel
    }
}
