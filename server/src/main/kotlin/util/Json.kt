package util

import kotlinx.serialization.json.Json

val JSON = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
}