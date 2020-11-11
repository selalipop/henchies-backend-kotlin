package models

enum class PlayerColor(val color: Int) {
    Teal(1),
    Blue(2),
    Amber(3),
    Red(4),
    Lime(5),
    Purple(6),
    Pink(7);

    companion object {
        val SelectableColors = listOf(
            Teal,
            Blue,
            Amber,
            Red,
            Lime,
            Purple,
            Pink
        )
    }
}

