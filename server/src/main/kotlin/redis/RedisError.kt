package redis


open class RedisError(message: String, val isMissingKey: Boolean, cause: Throwable?) : Error(message, cause) {
    constructor(message: String, isMissingKey: Boolean) : this(message, isMissingKey, null)
    constructor(message: String, cause: Throwable?) : this(message, false, cause)

    constructor(message: String, redisError: RedisError?) : this(
        message,
        redisError?.isMissingKey ?: false,
        redisError
    )

    constructor(isMissingKey: Boolean) : this("Caused by missing redis key", isMissingKey, null)

    object MissingKey : RedisError(true)
}
