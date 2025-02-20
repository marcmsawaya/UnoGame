// First and foremost, I import the khoury library.

import khoury.CapturedResult
import khoury.captureResults
import khoury.EnabledTest
import khoury.runEnabledTests
import khoury.testSame
import khoury.fileExists
import khoury.fileReadAsList
import khoury.input
import khoury.reactConsole

// Step 1 : Now, to start things off, I create the UnoColor enum class which contains all the possible colors of an UnoDeck.

enum class UnoColor { RED, YELLOW, GREEN, BLUE, NONE }

// Second, I create the UnoType enum class which contains all the possible types/signs of an UnoCard. 

enum class UnoType {
    ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE,
    SKIP, DRAW_TWO, REVERSE, WILD, WILD_DRAW_FOUR
}

// Now that we have done both these enum classes, we can set up the UnoCard data class which has a unique type and a unique color.

data class UnoCard(val type: UnoType, val color: UnoColor)

// Outside of the flow of thought here, I just knocked down the step where we must create examples for use later.

val exampleCard1 = UnoCard(UnoType.THREE, UnoColor.YELLOW)
val exampleCard2 = UnoCard(UnoType.DRAW_TWO, UnoColor.GREEN)
val exampleCard3 = UnoCard(UnoType.WILD, UnoColor.NONE)


// Step 2 : Now, it's time to create an deck.

data class UnoDeck(val cards: MutableList<UnoCard>)
/* Obviously, it's a list of uno cards. 
However, we have not done anything yet, we need to create the cards, and then add them to the deck, so let's do a function first. */

fun createUnoDeck(): UnoDeck {
    val deckOfUno = mutableListOf<UnoCard>()
    // For each color suit (red, yellow, green, blue)
    for (color in listOf(UnoColor.RED, UnoColor.YELLOW, UnoColor.GREEN, UnoColor.BLUE)) {
        // One 0 card per suit.
        deckOfUno.add(UnoCard(UnoType.ZERO, color))
        // Two copies of each number card 1–9.
        for (i in 1..9) {
            val numType = when (i) {
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
            deckOfUno.add(UnoCard(numType, color))
            deckOfUno.add(UnoCard(numType, color))
        }
        // Two copies of each action card: SKIP, DRAW_TWO, REVERSE.
        deckOfUno.add(UnoCard(UnoType.SKIP, color))
        deckOfUno.add(UnoCard(UnoType.SKIP, color))
        deckOfUno.add(UnoCard(UnoType.DRAW_TWO, color))
        deckOfUno.add(UnoCard(UnoType.DRAW_TWO, color))
        deckOfUno.add(UnoCard(UnoType.REVERSE, color))
        deckOfUno.add(UnoCard(UnoType.REVERSE, color))
    }
    // Add wild cards (with color NONE): four WILD and four WILD_DRAW_FOUR.
    for (i in 1..4) {
        deckOfUno.add(UnoCard(UnoType.WILD, UnoColor.NONE))
        deckOfUno.add(UnoCard(UnoType.WILD_DRAW_FOUR, UnoColor.NONE))
    }
    return UnoDeck(deckOfUno.toList())
}

// ================================================================
// 3. Files of Cards
// ================================================================

// (a) Convert an UnoCard to a string in the format "type|color" (using explicit mappings)
fun unoCardToString(card: UnoCard): String {
    val typeStr = when (card.type) {
        UnoType.ZERO -> "0"
        UnoType.ONE -> "1"
        UnoType.TWO -> "2"
        UnoType.THREE -> "3"
        UnoType.FOUR -> "4"
        UnoType.FIVE -> "5"
        UnoType.SIX -> "6"
        UnoType.SEVEN -> "7"
        UnoType.EIGHT -> "8"
        UnoType.NINE -> "9"
        UnoType.SKIP -> "skip"
        UnoType.DRAW_TWO -> "plus-two"
        UnoType.REVERSE -> "reverse"
        UnoType.WILD -> "wild"
        UnoType.WILD_DRAW_FOUR -> "wild-draw-four"
    }
    val colorStr = when (card.color) {
        UnoColor.RED -> "red"
        UnoColor.YELLOW -> "yellow"
        UnoColor.GREEN -> "green"
        UnoColor.BLUE -> "blue"
        UnoColor.NONE -> "none"
    }
    return typeStr + "|" + colorStr
}

// (b) Convert a string (e.g., "3|yellow" or "plus-two|green") to an UnoCard.
fun stringToUnoCard(s: String): UnoCard {
    val parts = s.split("|")
    if (parts.size != 2) {
        return UnoCard(UnoType.WILD, UnoColor.NONE)
    }
    val typePart = parts[0].trim().lowercase()
    val colorPart = parts[1].trim().lowercase()
    val typeVal = when (typePart) {
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
        "plus-two" -> UnoType.DRAW_TWO
        "reverse" -> UnoType.REVERSE
        "wild" -> UnoType.WILD
        else -> UnoType.WILD_DRAW_FOUR
    }
    val colorVal = when (colorPart) {
        "red" -> UnoColor.RED
        "yellow" -> UnoColor.YELLOW
        "green" -> UnoColor.GREEN
        "blue" -> UnoColor.BLUE
        else -> UnoColor.NONE
    }
    return UnoCard(typeVal, colorVal)
}

// (c) Read a file containing Uno cards (one per line) and return a MutableList<UnoCard>).
fun readUnoCardsFile(path: String): MutableList<UnoCard>) {
    if (!fileExists(path)) {
        return emptyList()
    }
    
    val lines = fileReadAsList(cards.txt)
    return lines.map { stringToUnoCard(it) }
}

// ================================================================
// 4. Playing with Cards
// ================================================================

// (a) Determine if a given list of cards represents a complete Uno deck.
fun isCompleteUnoDeck(cards: MutableList<UnoCard>): Boolean {
    if (cards.size != 108) return false
    for (color in listOf(UnoColor.RED, UnoColor.YELLOW, UnoColor.GREEN, UnoColor.BLUE)) {
        if (cards.filter { it.color == color }.filter { it.type == UnoType.ZERO }.size != 1) return false
        if (cards.filter { it.color == color }.filter { it.type == UnoType.ONE }.size != 2) return false
        if (cards.filter { it.color == color }.filter { it.type == UnoType.TWO }.size != 2) return false
        if (cards.filter { it.color == color }.filter { it.type == UnoType.THREE }.size != 2) return false
        if (cards.filter { it.color == color }.filter { it.type == UnoType.FOUR }.size != 2) return false
        if (cards.filter { it.color == color }.filter { it.type == UnoType.FIVE }.size != 2) return false
        if (cards.filter { it.color == color }.filter { it.type == UnoType.SIX }.size != 2) return false
        if (cards.filter { it.color == color }.filter { it.type == UnoType.SEVEN }.size != 2) return false
        if (cards.filter { it.color == color }.filter { it.type == UnoType.EIGHT }.size != 2) return false
        if (cards.filter { it.color == color }.filter { it.type == UnoType.NINE }.size != 2) return false
        if (cards.filter { it.color == color }.filter { it.type == UnoType.SKIP }.size != 2) return false
        if (cards.filter { it.color == color }.filter { it.type == UnoType.DRAW_TWO }.size != 2) return false
        if (cards.filter { it.color == color }.filter { it.type == UnoType.REVERSE }.size != 2) return false
    }
    if (cards.filter { it.color == UnoColor.NONE }.filter { it.type == UnoType.WILD }.size != 4) return false
    if (cards.filter { it.color == UnoColor.NONE }.filter { it.type == UnoType.WILD_DRAW_FOUR }.size != 4) return false
    return true
}


// A function to “shuffle” a deck; returns a new deck with shuffled cards.
fun shuffleUnoDeck(deck: MutableList<UnoCard>): MutableList<UnoCard> {
    deck.shuffle()
    return deck
}

fun dealUnoCards(deck : UnoDeck, n:Int): MutableList<UnoCard> {
    val hand = mutableListOf<UnoCard>()
    for (i in 1..n) {
        val card = deck.cards.first()
        deck.cards.remove(card)
        hand.add(card)
    }
    return hand
}

// (c) Determine if a play is valid.
// A player's card is playable if it has the same color as the top card,
// the same type, or if it is a wild card.
fun isValidPlay(hand: MutableList<UnoCard>, deck: UnoDeck, topCard: UnoCard): Triple<UnoCard, MutableList<UnoCard>, UnoDeck> {
    val (card, newDeck) = dealOneCard(deck)
    if ((card.color == topCard.color) ||
        (card.type == topCard.type) ||
        (card.type == UnoType.WILD) ||
        (card.type == UnoType.WILD_DRAW_FOUR)) {
        return Triple(card, hand, newDeck)
    } else {
        // Add the drawn card to the hand and try again.
        return drawUntilValid(hand + card, newDeck, topCard)
    }
}


fun SimulatePlayHand (active_player : UnoDeck, passive_player : UnoDeck, stack : UnoDeck) : Boolean {
    if (isValidPlay(active_player, stack)){
        stack.add(active_player)
        return true
    }
    else {
        return false
    }
}

// Step 5:

fun play(): UnoDeck {
    val deck = createUnoDeck()  // Create and shuffle the deck
    shuffleUnoDeck(deck.cards)

    // Deal 7 cards to each player
    val player1 = dealUnoCards(deck, 7).toMutableList()
    val player2 = dealUnoCards(deck, 7).toMutableList()

    // Place the first card on the discard pile
    val topCard = dealUnoCards(deck, 1)[0]
    val stack = mutableListOf(topCard)

    var currentPlayer = player1
    var opponent = player2

    while (deck.cards.isNotEmpty() && player1.isNotEmpty() && player2.isNotEmpty()) {
        val playableCard = currentPlayer.find { isValidPlay(it, stack.last()) }

        if (playableCard != null) {
            // Play the valid card
            currentPlayer.remove(playableCard)
            stack.add(playableCard)

            // Handle special cards
            when (playableCard.type) {
                UnoType.SKIP -> {}  // Skip turn (no need to change logic)
                UnoType.REVERSE -> {
                    val turn = currentPlayer
                    currentPlayer = opponent
                    opponent = turn
                }
                UnoType.DRAW_TWO -> {
                    opponent.addAll(dealUnoCards(deck, 2))
                }
                UnoType.WILD -> {}  // Wild color change not needed in simulation
                UnoType.WILD_DRAW_FOUR -> {
                    opponent.addAll(dealUnoCards(deck, 4))
                }
                else -> {}
            }
        } else {
            // No valid card, draw until a valid one is found
            while (deck.cards.isNotEmpty()) {
                // No valid card; draw until a valid one is found, without using break.
                var validCardFound = false
                while (deck.cards.isNotEmpty() && !validCardFound) {
                    val drawnCard = dealUnoCards(deck, 1)[0]
                    currentPlayer.add(drawnCard)
    
                    if (isValidPlay(drawnCard, stack.last())) {
                        currentPlayer.remove(drawnCard)
                        stack.add(drawnCard)
                        validCardFound = true
                    }
                }
    }
}


        // Check win condition
        if (currentPlayer.isEmpty()) {
            if (currentPlayer == player1) {
                return UnoDeck(player1)
            } else {
                return UnoDeck(player2)
        }
    }


        // Swap turns
        val turn = currentPlayer
        currentPlayer = opponent
        opponent = turn
    }

    return UnoDeck(emptyList<UnoCard>().toMutableList())  // Return empty deck if no winner (draw)
}
