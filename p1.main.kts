import khoury.* // I'm importing khoury library

enum class UnoColor {
    RED, YELLOW, GREEN, BLUE, NONE;  // NB : The NONE color is for wild cards

    override fun toString(): String = when (this) {
        RED -> "red"
        YELLOW -> "yellow"
        GREEN -> "green"
        BLUE -> "blue"
        NONE -> "none"
    }
}

fun UnoColor.fromString(s: String): UnoColor? = when (s.lowercase()) {
    "red" -> UnoColor.RED
    "yellow" -> UnoColor.YELLOW
    "green" -> UnoColor.GREEN
    "blue" -> UnoColor.BLUE
    "none" -> UnoColor.NONE
    else -> null
}

enum class UnoType {
    // Number cards
    ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE,
    // Action cards (per colour)
    SKIP, DRAW_TWO, REVERSE,
    // Wild cards (no colour)
    WILD, WILD_DRAW_FOUR;

    override fun toString(): String = when (this) {
        ZERO -> "0"
        ONE -> "1"
        TWO -> "2"
        THREE -> "3"
        FOUR -> "4"
        FIVE -> "5"
        SIX -> "6"
        SEVEN -> "7"
        EIGHT -> "8"
        NINE -> "9"
        SKIP -> "skip"
        DRAW_TWO -> "plus-two"  // as in "plus-two|green"
        REVERSE -> "reverse"
        WILD -> "wild"
        WILD_DRAW_FOUR -> "wild-draw-four"
    }
}

fun UnoType.fromString(s: String): UnoType? = when (s.lowercase()) {
    "0" -> UnoType.ZERO
    "1" -> UnoType.ONE
    "2" -> UnoType.TWO
    "3" -> UnoType.THREE
    "4" -> UnoType.FOUR
    "5" -> UnoType.FIVE
    "6" -> UnoType.SIX
    "7" -> UnoType.SEVEN
    "8" -> UnoType.EIGHT
    "9" -> UnoType.NINE
    "skip" -> UnoType.SKIP
    "plus-two", "draw two" -> UnoType.DRAW_TWO
    "reverse" -> UnoType.REVERSE
    "wild" -> UnoType.WILD
    "wild-draw-four", "wild draw four" -> UnoType.WILD_DRAW_FOUR
    else -> null
}

data class UnoCard(val type: UnoType, val color: UnoColor)

// Here are the 3 example cards for testing:
val exampleCard1 = UnoCard(UnoType.THREE, UnoColor.YELLOW)
val exampleCard2 = UnoCard(UnoType.DRAW_TWO, UnoColor.GREEN)
val exampleCard3 = UnoCard(UnoType.WILD, UnoColor.NONE)

fun createUnoDeck(): MutableList<UnoCard> {
    val deck = mutableListOf<UnoCard>()
    val colors = listOf(UnoColor.RED, UnoColor.YELLOW, UnoColor.GREEN, UnoColor.BLUE)

    for (color in colors) {
        for (i in 0..9) {
            val type = when (i) {
                0 -> UnoType.ZERO
                1 -> UnoType.ONE
                2 -> UnoType.TWO
                3 -> UnoType.THREE
                4 -> UnoType.FOUR
                5 -> UnoType.FIVE
                6 -> UnoType.SIX
                7 -> UnoType.SEVEN
                8 -> UnoType.EIGHT
                else -> UnoType.NINE
            }
            deck.add(UnoCard(type, color))
        }

        deck.add(UnoCard(UnoType.SKIP, color))
        deck.add(UnoCard(UnoType.SKIP, color))
        deck.add(UnoCard(UnoType.DRAW_TWO, color))
        deck.add(UnoCard(UnoType.DRAW_TWO, color))
        deck.add(UnoCard(UnoType.REVERSE, color))
        deck.add(UnoCard(UnoType.REVERSE, color))
    }

    val wildCards = listOf(
        UnoCard(UnoType.WILD, UnoColor.NONE),
        UnoCard(UnoType.WILD, UnoColor.NONE),
        UnoCard(UnoType.WILD, UnoColor.NONE),
        UnoCard(UnoType.WILD, UnoColor.NONE),
        UnoCard(UnoType.WILD_DRAW_FOUR, UnoColor.NONE),
        UnoCard(UnoType.WILD_DRAW_FOUR, UnoColor.NONE),
        UnoCard(UnoType.WILD_DRAW_FOUR, UnoColor.NONE),
        UnoCard(UnoType.WILD_DRAW_FOUR, UnoColor.NONE)
    )
    deck.addAll(wildCards)

    return deck
}

data class UnoDeck(var cards: MutableListOf = createUnoDeck())

fun unoCardToString(card: UnoCard): String {
    return if (card.color == UnoColor.NONE) {
        card.type.toString()
    } else {
        "${card.type}|${card.color}"
    }
}

fun stringToUnoCard(s: String): UnoCard? {
    val parts = s.split("|")
    return if (parts.size == 2) {
        val type = UnoType.fromString(parts[0])
        val color = UnoColor.fromString(parts[1])
        if (type != null && color != null) {
            return UnoCard(type, color)
        } else {
            return null
        }
    } else {
        val type = UnoType.fromString(parts[0])
        if (type != null) {
            return UnoCard(type, UnoColor.NONE)
        } else{
            return null
        }
    }
}

fun readUnoCardsFile(path: String): List<UnoCard> {
    return if (fileExists(path)) {
        fileReadAsList(path).map { stringToUnoCard(it) }
    } else {
        emptyList()
    }
}

fun isCompleteUnoDeck(deck: List<UnoCard>): Boolean {
    if (deck.size != 108) return false

    val expected = mutableMapOf<UnoCard, Int>()
    val colors = listOf("Red", "Blue", "Green", "Yellow")

    for (color in colors) {
        expected[Card(color, "0")] = 1
        for (n in 1..9) {
            expected[Card(color, n.toString())] = 2
        }
        for (action in listOf("Skip", "Reverse", "Draw Two")) {
            expected[Card(color, action)] = 2
        }
    }

    expected[Card(null, "Wild")] = 4
    expected[Card(null, "Wild Draw Four")] = 4

    val actual = deck.groupingBy { it }.eachCount()

    return expected == actual
}

fun shuffleUnoDeck(deck: MutableList<UnoCard>) {
    deck.shuffle()
}

fun dealUnoCards(deck: UnoDeck, n: Int): List<UnoCard> {
    if (n < 1 || n > deck.cards.size) {
        return emptyList()
    }

    val hand = deck.cards.take(n)
    deck.cards = deck.cards.drop(n).toMutableList()
    return hand
}

fun isValidPlay(playerCard: UnoCard, topCard: UnoCard): Boolean {
    return (playerCard.color == topCard.color) ||
           (playerCard.type == topCard.type) ||
           (playerCard.type == UnoType.WILD) ||
           (playerCard.type == UnoType.WILD_DRAW_FOUR)
}

fun main() {
    val deck = UnoDeck()
    shuffleUnoDeck(deck.cards)
    val hand = dealUnoCards(deck, 7)
    val topCard = deck.cards.removeAt(0)

    println("Top card: ${unoCardToString(topCard)}")
    println("Hand: ${hand.map { unoCardToString(it) }}")
    println("Deck size: ${deck.cards.size}")
}