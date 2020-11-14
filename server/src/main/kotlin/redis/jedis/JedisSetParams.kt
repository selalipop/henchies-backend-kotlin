package redis.jedis

import redis.clients.jedis.params.SetParams
import kotlin.time.Duration


fun ttlFromDuration(ttl: Duration): SetParams = SetParams.setParams().ex(ttl.inSeconds.toInt())