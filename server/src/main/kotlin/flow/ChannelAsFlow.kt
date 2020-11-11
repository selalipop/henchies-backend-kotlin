package flow

import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import java.io.Closeable

fun <T> ReceiveChannel<T>.consumeAsCloseableFlow(): Pair<Flow<T>, Closeable> {
    return consumeAsFlow() to Closeable {
        cancel()
    }
}