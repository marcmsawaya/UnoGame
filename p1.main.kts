// First and foremost, I import the khoury library.

import khoury.fileExists
import khoury.fileReadAsList
import khoury.testSame
import khoury.EnabledTest
import khoury.runEnabledTests

// Step 1 : Now, to start things off, I create the UnoColor enum class which contains all the possible colors of an UnoDeck.

enum class UnoColor { RED, YELLOW, GREEN, BLUE, NONE }

// Second, I create the UnoType enum class which contains all the possible types/signs of an UnoCard.

enum class UnoType {
    ZERO,
    ONE,
    TWO,
    THREE,
    FOUR,
    FIVE,
    SIX,
    SEVEN,
    EIGHT,
    NINE,
    SKIP,
    DRAW_TWO,
    REVERSE,
    WILD,
    WILD_DRAW_FOUR,
}

// Now that we have done both these enum classes, we can set up the UnoCard data class which has a unique type and a unique color.

data class UnoCard(
    val type: UnoType,
    val color: UnoColor,
)

// Outside of the flow of thought here, I just knocked down the step where we must create examples for use later.

val exampleCard1 = UnoCard(UnoType.THREE, UnoColor.YELLOW)
val exampleCard2 = UnoCard(UnoType.DRAW_TWO, UnoColor.GREEN)
val exampleCard3 = UnoCard(UnoType.WILD, UnoColor.NONE)

// Step 2 : Now, it's time to create an deck.

data class UnoDeck(
    val cards: MutableList<UnoCard>,
)
/* Obviously, it's a list of uno cards.
However, we have not done anything yet, we need to create the cards, and then add them to the deck, so let's do a function first. */

fun createunodeck(): UnoDeck {
    val deckOfUno = mutableListOf<UnoCard>()
    // For each color suit (red, yellow, green, blue)
    for (color in listOf(UnoColor.RED, UnoColor.YELLOW, UnoColor.GREEN, UnoColor.BLUE)) {
        // One 0 card per suit.
        deckOfUno.add(UnoCard(UnoType.ZERO, color))
        // Two copies of each number card 1–9.
        for (i in 1..9) {
            val numType =
                when (i) {
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
    return UnoDeck(deckOfUno)
}

// ================================================================
// 3. Files of Cards
// ================================================================

// (a) Convert an UnoCard to a string in the format "type|color" (using explicit mappings)
fun unoCardToString(card: UnoCard): String {
    val typeStr =
        when (card.type) {
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
    val colorStr =
        when (card.color) {
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
    val typeVal =
        when (typePart) {
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
    val colorVal =
        when (colorPart) {
            "red" -> UnoColor.RED
            "yellow" -> UnoColor.YELLOW
            "green" -> UnoColor.GREEN
            "blue" -> UnoColor.BLUE
            else -> UnoColor.NONE
        }
    return UnoCard(typeVal, colorVal)
}

// (c) Read a file containing Uno cards (one per line) and return a MutableList<UnoCard>).
fun readUnoCardsFile(path: String): MutableList<UnoCard> {
    if (!fileExists(path)) {
        return emptyList<UnoCard>().toMutableList()
    }

    val lines = fileReadAsList("cards.txt")
    return lines.map { stringToUnoCard(it) }.toMutableList()
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

fun dealUnoCards(
    deck: UnoDeck,
    n: Int,
): MutableList<UnoCard> {
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
fun isValidPlay(
    card: UnoCard,
    topCard: UnoCard,
): Boolean {
    if (card.color == topCard.color || card.type == topCard.type || card.type == UnoType.WILD || card.type == UnoType.WILD_DRAW_FOUR) {
        return true
    } else {
        return false
    }
}

// Step 5:
fun SimulatePlay(): String {
    val deck = createunodeck()
    shuffleUnoDeck(deck.cards)
    print("Total cards in deck: ")
    println(deck.cards.size)

    val player1 = dealUnoCards(deck, 7)
    val player2 = dealUnoCards(deck, 7)

    val stack = mutableListOf<UnoCard>()
    stack.add(dealUnoCards(deck, 1)[0])

    var currentPlayer = player1
    var opponent = player2

    while (!deck.cards.isEmpty() && player1.isNotEmpty() && player2.isNotEmpty()) {
        val topCard = stack.last()
        while (!currentPlayer.any { isValidPlay(it, topCard) } && deck.cards.isNotEmpty()) {
            currentPlayer.add(dealUnoCards(deck, 1)[0])
        }

        if (deck.cards.isEmpty()) {
            break
        }

        val playableCard = currentPlayer.first { isValidPlay(it, topCard) }

        currentPlayer.remove(playableCard)
        stack.add(playableCard)
        
        // Check first playable card type. If Special card, do the special action.
        when (playableCard.type) {
            UnoType.SKIP -> {
                println("played skip...")
            }
            UnoType.REVERSE -> {
                println("played reverse...")
            }
            UnoType.DRAW_TWO -> {
                println("played draw-two...")
                // If there are less than 2 cards in the deck, break the loop and claim draw
                if(deck.cards.size < 2) {
                    break
                }
                val drawnCards = dealUnoCards(deck, 2)
                opponent.addAll(drawnCards)
                stack.add(UnoCard(UnoType.DRAW_TWO, UnoColor.NONE))
            }
            // If the card is a wild, add it to the stack
            UnoType.WILD -> {
                println("played wild...")
                stack.add(UnoCard(UnoType.WILD, UnoColor.NONE))
            }

            // If the card is a wild-draw-four, draw 4 cards and add wild-draw-four to the stack
            UnoType.WILD_DRAW_FOUR -> {
                println("played wild-draw-four...")
                // If there are less than 4 cards in the deck, break the loop and claim draw
                if(deck.cards.size < 4) {
                    break
                }
                val drawnCards = dealUnoCards(deck, 4)
                opponent.addAll(drawnCards)
                stack.add(UnoCard(UnoType.WILD_DRAW_FOUR, UnoColor.NONE))
            }
            else -> {
                // If the card is not a special card, print the card played
                val stringOfCard = unoCardToString(playableCard)
                println("played " + stringOfCard + "...")
            }
        }

        if (playableCard.type != UnoType.SKIP && playableCard.type != UnoType.REVERSE) {
            val temp = currentPlayer
            currentPlayer = opponent
            opponent = temp
        }

        print("Total cards in deck: ")
        println(deck.cards.size)

        if (player1.isEmpty() || player2.isEmpty()) {
            break
        }
    }

    when {
        player1.isEmpty() -> return "Player 1 wins!"
        player2.isEmpty() -> return "Player 2 wins!"
        else -> return "It's a draw!"
    }
}

fun play(n: Int){
    // keep track of wins for each player
    val player1 = mutableListOf<String>()
    val player2 = mutableListOf<String>()
    for (i in 1..n) {
        val result = SimulatePlay()
        if (result == "Player 1 wins!") {
            player1.add("w")
        } else if (result == "Player 2 wins!") {
            player2.add("w")
        }
    }

    println("Player 1 wins: " + player1.size)
    println("Player 2 wins: " + player2.size)
    println("Draws: " + (n - player1.size - player2.size))

    // Compare the size of the two lists to determine the winner
    if (player1.size > player2.size) {
        println("Player 1 wins the game!")
    } else if (player2.size > player1.size) {
        println("Player 2 wins the game!")
    } else {
        println("It's a draw!")
    }
}

@EnabledTest
fun testUnoDeck(){
    val deck = createunodeck()
    testSame(
        deck.cards.size, 
        108, 
        "The deck should have 108 cards"
    )
}

@EnabledTest
fun testUnoCardToString(){
    val card = UnoCard(UnoType.THREE, UnoColor.YELLOW)
    testSame(
        unoCardToString(card), 
        "3|yellow", 
        "The string representation of the card should be '3|yellow'"
    )

    val card2 = UnoCard(UnoType.DRAW_TWO, UnoColor.GREEN)
    testSame(
        unoCardToString(card2), 
        "plus-two|green", 
        "The string representation of the card should be 'plus-two|green'"
    )
}

@EnabledTest
fun testStringToUnoCard(){
    val card = stringToUnoCard("3|yellow")
    testSame(
        card, 
        UnoCard(UnoType.THREE, UnoColor.YELLOW), 
        "The card should be UnoCard(UnoType.THREE, UnoColor.YELLOW)"
    )

    val card2 = stringToUnoCard("plus-two|green")
    testSame(
        card2, 
        UnoCard(UnoType.DRAW_TWO, UnoColor.GREEN), 
        "The card should be UnoCard(UnoType.DRAW_TWO, UnoColor.GREEN)"
    )
}

@EnabledTest
fun testIsCompleteUnoDeck(){
    val deck = createunodeck()
    testSame(
        isCompleteUnoDeck(deck.cards), 
        true, 
        "The deck should be a complete Uno deck"
    )
}

@EnabledTest
fun testShuffleUnoDeck(){
    val deck = createunodeck()
    val shuffledDeck = shuffleUnoDeck(deck.cards)
    testSame(
        deck.cards.size, 
        shuffledDeck.size, 
        "The size of the deck should remain the same after shuffling"
    )
}

@EnabledTest
fun testIsValidPlay(){
    val card = UnoCard(UnoType.THREE, UnoColor.YELLOW)
    val topCard = UnoCard(UnoType.THREE, UnoColor.YELLOW)
    testSame(
        isValidPlay(card, topCard), 
        true, 
        "The card should be a valid play"
    )

    val card2 = UnoCard(UnoType.DRAW_TWO, UnoColor.GREEN)
    val topCard2 = UnoCard(UnoType.THREE, UnoColor.YELLOW)
    testSame(
        isValidPlay(card2, topCard2), 
        false, 
        "The card should not be a valid play"
    )
}

@EnabledTest
fun testDealUnoCards(){
    val deck = createunodeck()
    val hand = dealUnoCards(deck, 7)
    testSame(
        hand.size, 
        7, 
        "The hand should have 7 cards"
    )

    testSame(
        deck.cards.size, 
        101, 
        "The deck should have 101 cards"
    )

    val hand2 = dealUnoCards(deck, 7)
    testSame(
        hand2.size, 
        7, 
        "The hand should have 7 cards"
    )

    testSame(
        deck.cards.size, 
        94, 
        "The deck should have 94 cards"
    )
}

runEnabledTests(this)
play(1)