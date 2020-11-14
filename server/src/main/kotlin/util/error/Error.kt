package util.error

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result

fun err(message: String) = Err(Error(message))
fun err(message: String, cause: Throwable?) = Err(Error(message, cause))
fun <T> err(exception: T) = Err<T>(exception)

inline fun <T> resultOf(block: ()->T): Result<T, Throwable> {
    return try {
        Ok(block())
    } catch (e: Throwable) {
        Err(e)
    }
}
val Ok = Ok(Unit)