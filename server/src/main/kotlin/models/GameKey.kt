package models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@InlineStringJson
inline class GameKey(val value: String)