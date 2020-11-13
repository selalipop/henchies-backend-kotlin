package util.jedis

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPubSub
import util.JSON
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

    inline fun <reified T> subscribeJsonFlow(channelName: String) = subscribeFlow(channelName).map {
        JSON.decodeFromString<T>(it)
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
