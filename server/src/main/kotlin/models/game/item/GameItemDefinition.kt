package models.game.item

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameItemDefinition(
    @SerialName("name")
    val name: String,

    @SerialName("resourceId")
    val resourceId: String,

    @SerialName("imposterOnly")
    val createRules: CreateRules
) {
    companion object {
        private val Definitions = listOf(
            GameItemDefinition("Ice Trap Test", "ice-trap", CreateRules.Anyone),
            GameItemDefinition("Ice Trap", "ice-trap", CreateRules.ImposterOnly)
        )


        fun getByResourceId(id: String): GameItemDefinition? {
            return Definitions.firstOrNull { it.resourceId == id }
        }
    }
}


