import khoury.* // I'm importing khoury library

enum class UnoColor {
    RED, YELLOW, GREEN, BLUE, NONE;  // NONE for wild cards

    override fun toString(): String = when (this) {
        RED -> "red"
        YELLOW -> "yellow"
        GREEN -> "green"
        BLUE -> "blue"
        NONE -> "none"
    }

    companion object {
        fun fromString(s: String): UnoColor? = when (s.lowercase()) {
            "red" -> RED
            "yellow" -> YELLOW
            "green" -> GREEN
            "blue" -> BLUE
            "none" -> NONE
            else -> null
        }
    }
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

    companion object {
        fun fromString(s: String): UnoType? = when (s.lowercase()) {
            "0" -> ZERO
            "1" -> ONE
            "2" -> TWO
            "3" -> THREE
            "4" -> FOUR
            "5" -> FIVE
            "6" -> SIX
            "7" -> SEVEN
            "8" -> EIGHT
            "9" -> NINE
            "skip" -> SKIP
            "plus-two", "draw two" -> DRAW_TWO
            "reverse" -> REVERSE
            "wild" -> WILD
            "wild-draw-four", "wild draw four" -> WILD_DRAW_FOUR
            else -> null
        }
    }
}

data class UnoCard(val type: UnoType, val color: UnoColor)

// Here are the 3 example cards for testing:
val exampleCard1 = UnoCard(UnoType.THREE, UnoColor.YELLOW)
val exampleCard2 = UnoCard(UnoType.DRAW_TWO, UnoColor.GREEN)
val exampleCard3 = UnoCard(UnoType.WILD, UnoColor.NONE)

class UnoDeck {
    val cards: MutableList<UnoCard> = mutableListOf()

    init {
        // For each of the four colors, I add the 25 cards.
        val colors = listOf(UnoColor.RED, UnoColor.YELLOW, UnoColor.GREEN, UnoColor.BLUE)
        for (color in colors) {
            // One 0 card per color.
            cards.add(UnoCard(UnoType.ZERO, color))
            // Two copies of each number card 1-9.
            for (i in 1..9) {
                val type = when (i) {
                    1 -> UnoType.ONE
                    2 -> UnoType.TWO
                    3 -> UnoType.THREE
                    4 -> UnoType.FOUR
                    5 -> UnoType.FIVE
                    6 -> UnoType.SIX
                    7 -> UnoType.SEVEN
                    8 -> UnoType.EIGHT
                    9 -> UnoType.NINE
                    else -> throw IllegalStateException("Unexpected number")
                }
                cards.add(UnoCard(type, color))
                cards.add(UnoCard(type, color))
            }
            // Two copies of each action card: Skip, Draw Two, Reverse.
            cards.add(UnoCard(UnoType.SKIP, color))
            cards.add(UnoCard(UnoType.SKIP, color))
            cards.add(UnoCard(UnoType.DRAW_TWO, color))
            cards.add(UnoCard(UnoType.DRAW_TWO, color))
            cards.add(UnoCard(UnoType.REVERSE, color))
            cards.add(UnoCard(UnoType.REVERSE, color))
        }
        // I now add wild cards (with color "NONE", which basically wild cards)
        repeat(4) {
            cards.add(UnoCard(UnoType.WILD, UnoColor.NONE))
            cards.add(UnoCard(UnoType.WILD_DRAW_FOUR, UnoColor.NONE))
        }
    }
}