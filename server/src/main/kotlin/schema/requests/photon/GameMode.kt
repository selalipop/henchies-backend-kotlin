package schema.requests.photon

enum class GameMode(val Id: String, val playerCount: Int, val imposterCount: Int) {
    TwoImposterSixPlayer("twoImposterSixPlayer",2,6)
}