package error

import com.github.michaelbull.result.Err

fun ErrResult(message: String) = Err(Error(message))
fun ErrResult(message: String, cause: Throwable) = Err(Error(message, cause))