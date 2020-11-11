package models

import com.squareup.moshi.*
import java.lang.reflect.Type
import kotlin.reflect.KClass

@Target(
    AnnotationTarget.CLASS
)
@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
@MustBeDocumented
annotation class InlineStringJson

class InlineStringClassAdapter : JsonAdapter.Factory {

    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
        val clazz = type as? Class<*> ?: return null

        if (!clazz.annotations.any { it is InlineStringJson }) {
            return null
        }

        return object : JsonAdapter<Any>() {
            override fun fromJson(reader: JsonReader): Any? {
                var value = reader.readJsonValue()
                var castValue = (value as? Map<*, *>)?.values?.first() ?: value as String
                return clazz.declaredConstructors.first()
                    .also { it.isAccessible = true }
                    .newInstance(castValue)
            }

            override fun toJson(writer: JsonWriter, value: Any?) {
                if (value == null) {
                    writer.value(null as String?)
                    return
                }

                val inlineValue =
                    value.javaClass.declaredMethods.first { it.name == "getValue" }
                        .invoke(value) as String

                writer.value(inlineValue)
            }

        }
    }
}
