package models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Vector3(
    @SerialName("x")
    val x: Double,
    @SerialName("y")
    val y: Double,
    @SerialName("z")
    val z: Double
)
@Serializable
data class Vector4(
    @SerialName("w")
    val w: Double,
    @SerialName("x")
    val x: Double,
    @SerialName("y")
    val y: Double,
    @SerialName("z")
    val z: Double
)
