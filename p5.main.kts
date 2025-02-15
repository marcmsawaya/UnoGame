// First and foremost, I import the khoury library.

import khoury.CapturedResult
import khoury.EnabledTest
import khoury.captureResults
import khoury.fileExists
import khoury.fileReadAsList
import khoury.input
import khoury.reactConsole
import khoury.runEnabledTests
import khoury.testSame

// Step 1 : Now, to start things off, I create the UnoColor enum class which contains all the possible colors of an UnoDeck.

enum class UnoColor { RED, YELLOW, GREEN, BLUE, NONE }

// Second, I create the UnoType enum class which contains all the possible types/signs of an UnoCard. 

enum class UnoType {
    ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE,
    SKIP, DRAW_TWO, REVERSE,
    WILD, WILD_DRAW_FOUR
}

// Now that we have done both these enum classes, we can set up the UnoCard data class which has a unique type and a unique color.

data class UnoCard(val type: UnoType, val color: UnoColor)

// Outside of the flow of thought here, I just knocked down the step where we must create examples for use later.

val exampleCard1 = UnoCard(UnoType.THREE, UnoColor.YELLOW)
val exampleCard2 = UnoCard(UnoType.DRAW_TWO, UnoColor.GREEN)
val exampleCard3 = UnoCard(UnoType.WILD, UnoColor.NONE)


// Step 2 : Now, it's time to create an deck.

data class UnoDeck(val cards: List<UnoCard>)
/* Obviously, it's a list of uno cards. 
However, we have not done anything yet, we need to create the cards, and then add them to the deck, so let's do a function first. */

fun createUnoDeck(): UnoDeck {
    val deckOfUno = mutableListOf<UnoCard>()
    // For each color suit (red, yellow, green, blue)
    for (color in listOf(UnoColor.RED, UnoColor.YELLOW, UnoColor.GREEN, UnoColor.BLUE)) {
        // One 0 card per suit.
        deckOfUno.add(UnoCard(UnoType.ZERO, color))
        // Two copies of each number card 1–9.
        for (i in 0..9) {
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
        throw IllegalArgumentException("Invalid card format: " + s)
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
        "plus-two", "draw two" -> UnoType.DRAW_TWO
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

// (c) Read a file containing Uno cards (one per line) and return a List<UnoCard>.
fun readUnoCardsFile(path: String): List<UnoCard> {
    if (!fileExists(path)) {
        return emptyList()
    }
    
    val lines = fileReadAsList(filepath)
    return lines.map { stringToUnoCard(it) }
}

// ================================================================
// 4. Playing with Cards
// ================================================================

// (a) Determine if a given list of cards represents a complete Uno deck.
fun isCompleteUnoDeck(cards: List<UnoCard>): Boolean {
    if (cards.size != 108) return false
    for (color in listOf(UnoColor.RED, UnoColor.YELLOW, UnoColor.GREEN, UnoColor.BLUE)) {
        if (cards.filter { it.color == color && it.type == UnoType.ZERO }.size != 1) return false
        if (cards.filter { it.color == color && it.type == UnoType.ONE }.size != 2) return false
        if (cards.filter { it.color == color && it.type == UnoType.TWO }.size != 2) return false
        if (cards.filter { it.color == color && it.type == UnoType.THREE }.size != 2) return false
        if (cards.filter { it.color == color && it.type == UnoType.FOUR }.size != 2) return false
        if (cards.filter { it.color == color && it.type == UnoType.FIVE }.size != 2) return false
        if (cards.filter { it.color == color && it.type == UnoType.SIX }.size != 2) return false
        if (cards.filter { it.color == color && it.type == UnoType.SEVEN }.size != 2) return false
        if (cards.filter { it.color == color && it.type == UnoType.EIGHT }.size != 2) return false
        if (cards.filter { it.color == color && it.type == UnoType.NINE }.size != 2) return false
        if (cards.filter { it.color == color && it.type == UnoType.SKIP }.size != 2) return false
        if (cards.filter { it.color == color && it.type == UnoType.DRAW_TWO }.size != 2) return false
        if (cards.filter { it.color == color && it.type == UnoType.REVERSE }.size != 2) return false
    }
    if (cards.filter { it.color == UnoColor.NONE && it.type == UnoType.WILD }.size != 4) return false
    if (cards.filter { it.color == UnoColor.NONE && it.type == UnoType.WILD_DRAW_FOUR }.size != 4) return false
    return true
}

// A function to “shuffle” a deck; returns a new deck with shuffled cards.
fun shuffleUnoDeck(deck: MutableList<UnoCard>) {
    deck.shuffle()
    return deck
}

// (b) Deal N cards from the top of the deck without replacement.
// Returns a Pair: the dealt hand and the new deck.
fun dealUnoCards(deck: UnoDeck, n: Int): Pair<List<UnoCard>, UnoDeck> {
    if (n < 1) throw IllegalArgumentException("Must deal at least one card")
    if (n > deck.cards.size) throw IllegalStateException("Not enough cards in the deck")
    val hand = deck.cards.take(n)
    val newDeck = UnoDeck(deck.cards.drop(n))
    return Pair(hand, newDeck)
}

// A helper function to deal one card.
fun dealOneCard(deck: UnoDeck): Pair<UnoCard, UnoDeck> {
    val (hand, newDeck) = dealUnoCards(deck, 1)
    return Pair(hand[0], newDeck)
}

// A helper function to remove one occurrence of a card from a hand.
fun removeOneCard(hand: List<UnoCard>, card: UnoCard): List<UnoCard> {
    val index = hand.indexOf(card)
    return if (index == -1) hand else hand.take(index) + hand.drop(index + 1)
}

// (c) Determine if a play is valid.
// A player's card is playable if it has the same color as the top card,
// the same type, or if it is a wild card.
fun isValidPlay(playerCard: UnoCard, topCard: UnoCard): Boolean {
    return (playerCard.color == topCard.color) ||
           (playerCard.type == topCard.type) ||
           (playerCard.type == UnoType.WILD) ||
           (playerCard.type == UnoType.WILD_DRAW_FOUR)
}

// (d) A helper function to draw cards until a valid card is found.
// Returns a Triple: (the drawn card to play, the (unchanged) hand, and the new deck).
fun drawUntilValid(hand: List<UnoCard>, deck: UnoDeck, topCard: UnoCard): Triple<UnoCard, List<UnoCard>, UnoDeck> {
    if (deck.cards.isEmpty()) {
        throw IllegalStateException("Deck exhausted: no valid play possible")
    }
    val (card, newDeck) = dealOneCard(deck)
    return if (isValidPlay(card, topCard)) {
        Triple(card, hand, newDeck)
    } else {
        // Add drawn card to hand and try again.
        drawUntilValid(hand + card, newDeck, topCard)
    }
}


// ================================================================
// 5. Two-Player Uno Game Simulation
// ================================================================

// In our simulation we represent each player's hand as an immutable List<UnoCard>.
// All state updates (hands, deck, top card, current player) are returned as new values.
// We use tail recursion to simulate the game.

tailrec fun playTurn(
    p1: List<UnoCard>,
    p2: List<UnoCard>,
    deck: UnoDeck,
    topCard: UnoCard,
    currentPlayer: Int
): Int {
    // Select the current player's hand.
    val currentHand = if (currentPlayer == 1) p1 else p2

    // If current hand is empty, that player wins.
    if (currentHand.isEmpty()) return currentPlayer

    // Try to find a valid card in the current hand.
    val candidate = currentHand.firstOrNull { isValidPlay(it, topCard) }

    // Determine the played card and update the current hand.
    val playedCard: UnoCard
    val newCurrentHand: List<UnoCard>
    val deckAfter: UnoDeck

    if (candidate != null) {
        playedCard = candidate
        newCurrentHand = removeOneCard(currentHand, candidate)
        deckAfter = deck
    } else {
        // Draw cards until a valid card is found.
        val (drawn, handAfter, newDeck) = drawUntilValid(currentHand, deck, topCard)
        playedCard = drawn
        newCurrentHand = handAfter  // The drawn card is played immediately.
        deckAfter = newDeck
    }

    // The played card becomes the new top-of-pile.
    val newTop = playedCard

    // Update the current player's hand.
    val updatedP1: List<UnoCard>
    val updatedP2: List<UnoCard>
    if (currentPlayer == 1) {
        updatedP1 = newCurrentHand
        updatedP2 = p2
    } else {
        updatedP1 = p1
        updatedP2 = newCurrentHand
    }

    // Check for win: if the current player's hand is now empty.
    if (newCurrentHand.isEmpty()) return currentPlayer

    // Process action cards.
    when (playedCard.type) {
        UnoType.SKIP, UnoType.REVERSE -> {
            // Same player goes again.
            return playTurn(updatedP1, updatedP2, deckAfter, newTop, currentPlayer)
        }
        UnoType.DRAW_TWO -> {
            // Opponent draws 2 cards.
            if (currentPlayer == 1) {
                val (drawnCards, newDeck2) = dealUnoCards(deckAfter, 2)
                return playTurn(updatedP1, updatedP2 + drawnCards, newDeck2, newTop, 2)
            } else {
                val (drawnCards, newDeck2) = dealUnoCards(deckAfter, 2)
                return playTurn(updatedP1 + drawnCards, updatedP2, newDeck2, newTop, 1)
            }
        }
        UnoType.WILD -> {
            // Turn passes to opponent.
            val nextPlayer = if (currentPlayer == 1) 2 else 1
            return playTurn(updatedP1, updatedP2, deckAfter, newTop, nextPlayer)
        }
        UnoType.WILD_DRAW_FOUR -> {
            // Opponent draws 4 cards.
            if (currentPlayer == 1) {
                val (drawnCards, newDeck2) = dealUnoCards(deckAfter, 4)
                return playTurn(updatedP1, updatedP2 + drawnCards, newDeck2, newTop, 2)
            } else {
                val (drawnCards, newDeck2) = dealUnoCards(deckAfter, 4)
                return playTurn(updatedP1 + drawnCards, updatedP2, newDeck2, newTop, 1)
            }
        }
        else -> {
            // For number cards, simply pass turn.
            val nextPlayer = if (currentPlayer == 1) 2 else 1
            return playTurn(updatedP1, updatedP2, deckAfter, newTop, nextPlayer)
        }
    }
}

// playOneGame sets up one game of Uno.
fun playOneGame(): Int {
    val deck0 = shuffleUnoDeck(createUnoDeck())
    val (hand1, deck1) = dealUnoCards(deck0, 7)
    val (hand2, deck2) = dealUnoCards(deck1, 7)
    val (topList, deck3) = dealUnoCards(deck2, 1)
    val top = topList[0]
    // Player 1 starts.
    return playTurn(hand1, hand2, deck3, top, 1)
}

// play simulates numGames games and returns the player (1 or 2) with the most wins.
// In case of a tie, Player 1 is returned.
fun play(numGames: Int): Int {
    if (numGames < 1) throw IllegalArgumentException("Must play at least one game")
    val results = (1..numGames).map { playOneGame() }
    val winsPlayer1 = results.filter { it == 1 }.size
    val winsPlayer2 = results.filter { it == 2 }.size
    return if (winsPlayer1 >= winsPlayer2) 1 else 2
}


// ================================================================
// Tests (using the Khoury testing library)
// ================================================================

@EnabledTest
fun testUnoCardToString() {
    testSame(unoCardToString(exampleCard1), "3|yellow", "UnoCardToString Example")
}

@EnabledTest
fun testStringToUnoCard() {
    val card = stringToUnoCard("plus-two|green")
    testSame(card, UnoCard(UnoType.DRAW_TWO, UnoColor.GREEN), "StringToUnoCard Example")
}

@EnabledTest
fun testIsCompleteUnoDeck() {
    val deck = createUnoDeck().cards
    testSame(isCompleteUnoDeck(deck), true, "CompleteDeck Check")
}

@EnabledTest
fun testDealUnoCards() {
    val deck = createUnoDeck()
    val initSize = deck.cards.size
    val (hand, newDeck) = dealUnoCards(deck, 7)
    testSame(hand.size, 7, "Deal 7 cards")
    testSame(newDeck.cards.size, initSize - 7, "Deck size reduced")
}


// ================================================================
// Main Function
// ================================================================

fun main() {
    // Play one game of Uno and print the overall winner.
    val overallWinner = play(1)
    println("Overall winner: Player " + overallWinner)
    
    // Run enabled tests.
    runEnabledTests(object {
        @EnabledTest fun testUnoCardToString() = testUnoCardToString()
        @EnabledTest fun testStringToUnoCard() = testStringToUnoCard()
        @EnabledTest fun testIsCompleteUnoDeck() = testIsCompleteUnoDeck()
        @EnabledTest fun testDealUnoCards() = testDealUnoCards()
    })
}
