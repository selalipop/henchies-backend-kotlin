package javalin

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.squareup.moshi.Moshi
import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.websocket.WsContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import logger
import org.eclipse.jetty.http.HttpStatus
import java.lang.Error
import java.lang.Exception


inline fun <reified T> Context.bindQueryParams(moshi: Moshi): Result<T, Map<String, Throwable>> {
    val params = queryParamMap()
    return bindQueryParams(params, moshi)
}

inline fun <reified T> WsContext.bindQueryParams(moshi: Moshi): Result<T, Map<String, Throwable>> {
    val params = queryParamMap()
    return bindQueryParams(params, moshi)
}

inline fun <reified T> bindQueryParams(
    params: Map<String, List<String>>,
    moshi: Moshi
): Result<T, Map<String, Throwable>> {
    val errors = mutableMapOf<String, Throwable>()
    val values = mutableListOf<Any?>()


    val constructor = T::class.java.declaredConstructors
        .firstOrNull { it.parameterCount == params.size }
        ?: return Err(mutableMapOf(("na" to Error("wrong number of parameters"))))

    T::class.java.declaredFields.forEach { field ->
        val fieldName = field.name
        params[fieldName]?.also {
            try {
                val paramValue = it[0]
                val deserializedValue = moshi.adapter(field.type).fromJson(paramValue)
                values.add(deserializedValue)
            } catch (err: Throwable) {
                errors[fieldName] = err
            }
        } ?: errors.put(fieldName, Error("Missing field $fieldName"))
    }

    return if (errors.isEmpty()) {
        constructor.isAccessible = true
        Ok(constructor.newInstance(*values.toTypedArray()) as T)
    } else {
        Err(errors)
    }
}


fun Context.sendError(message: String, cause: Throwable? = null) {
    logger.error(cause) { "Websocket error" }
    res.sendError(HttpStatus.Code.INTERNAL_SERVER_ERROR.code, message)
}

fun Context.sendError(error: Throwable?) {
    logger.error(error) { "Websocket error" }
    res.sendError(HttpStatus.Code.INTERNAL_SERVER_ERROR.code, error?.message)
}

