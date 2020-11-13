package util.error

import com.github.michaelbull.result.Err

fun err(message: String) = Err(Error(message))
fun err(message: String, cause: Throwable?) = Err(Error(message, cause))