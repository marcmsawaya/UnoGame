import khoury.EnabledTest
import khoury.captureResults
import khoury.fileExists
import khoury.fileReadAsList
import khoury.isAnInteger
import khoury.linesToString
import khoury.reactConsole
import khoury.runEnabledTests
import khoury.testSame

// Represents a question with its corresponding answer.
// Example: Question(query = "What is 2 + 2?", answer = "4")
data class Question(
    val query: String,
    val answer: String,
)

// Represents a collection of questions with a name.
// Example: QuestionBank(name = "Math Questions", bank = listOf(Question("What is 2 + 2?", "4")))
data class QuestionBank(
    val name: String,
    val bank: List<Question>,
)

// Represents the Question Bank state that tracks the current question index.
// Example: QuestionBankState(currentQuestionIndex = 1, isViewingAnswer = true, numCorrectAnswers = 3)
data class QuestionBankState(
    val currentQuestionIndex: Int,
    val isViewingAnswer: Boolean,
    val numCorrectAnswers: Int,
)

// Enum class representing the study stages.
// Example: StudyStage.VIEWING_QUESTION
enum class StudyStage {
    VIEWING_QUESTION,
    VIEWING_ANSWER,
}

// Calculates the cube of a given integer.
fun cubeNumber(
    number: Int,
    accumulator: Int = 3,
): Int =
    when (accumulator) {
        0 -> 1
        else -> number * cubeNumber(number, accumulator - 1)
    }

// builds the element for Question bank's question list, given index and list size
fun returnQuesForCubes(index: Int): Question =
    Question(
        query = "Q: what is ${index + 1} cubed?",
        answer = "A: ${cubeNumber(index + 1)}",
    )

// Generates a QuestionBank with questions about cubes of numbers.
fun cubes(count: Int): QuestionBank {
    val listOfQuestions = List<Question>(count, ::returnQuesForCubes)
    return QuestionBank(
        name = "Questions of Cubes",
        bank = listOfQuestions,
    )
}

// Converts a Question object to a string representation.
fun questionToString(question: Question): String = question.query + "|" + question.answer

// Converts a string representation of a question back to a Question object.
fun stringToQuestion(textLine: String): Question {
    val splitString = textLine.split("|")
    return Question(query = splitString[0], answer = splitString[1])
}

// Reads a list of questions from a file.
fun readQuestionBank(filepath: String): List<Question> =
    if (fileExists(filepath)) {
        val lines = fileReadAsList(filepath)
        lines.map { line -> stringToQuestion(line) }
    } else {
        listOf()
    }

// Checks if a word starts with the letter 'y' (case-insensitive).
fun prefixIsY(word: String): Boolean = word.length > 0 && word[0].lowercase() == "y"

// Generates the text to be displayed in the console based on the study stage.
fun toText(
    stage: StudyStage,
    question: Question,
): String =
    when (stage) {
        StudyStage.VIEWING_QUESTION -> question.query + "\nThink about the answer... (Press Enter to reveal)"
        StudyStage.VIEWING_ANSWER -> question.answer
    }

// Provides an interactive console-based study session for a single question.
// User self-report is moved to study question bank for easier integration.
fun studyQuestion(question: Question) {
    reactConsole(
        initialState = StudyStage.VIEWING_QUESTION,
        stateToText = { state -> toText(state, question) },
        nextState = { state, _ -> StudyStage.VIEWING_ANSWER },
        isTerminalState = { state -> state == StudyStage.VIEWING_ANSWER },
    )
}

// Generates the text to display for the study session state depending on Question bank state.
fun studyQuestionBankStateToText(state: QuestionBankState): String =
    when {
        state.isViewingAnswer -> "Did you get the right answer? [y/n]"
        else -> "Viewing Question ${state.currentQuestionIndex + 1}: (Press Enter to reveal)"
    }

// Determines the next state of the study session for a question bank based on the user input.
fun studyQuestionNextStateFunction(
    state: QuestionBankState,
    input: String,
    questions: List<Question>,
): QuestionBankState {
    val nextStateIndex = state.currentQuestionIndex + 1
    return when {
        state.isViewingAnswer -> {
            if (prefixIsY(input)) {
                QuestionBankState(nextStateIndex, false, state.numCorrectAnswers + 1)
            } else {
                QuestionBankState(nextStateIndex, false, state.numCorrectAnswers)
            }
        }
        else -> {
            studyQuestion(questions[state.currentQuestionIndex])
            QuestionBankState(state.currentQuestionIndex, true, state.numCorrectAnswers)
        }
    }
}

// Provides an interactive console-based study session for a question bank.
fun studyQuestionBank(questions: List<Question>): QuestionBankState =
    reactConsole(
        initialState = QuestionBankState(0, false, 0),
        stateToText = { state -> studyQuestionBankStateToText(state) },
        nextState = { state, input -> studyQuestionNextStateFunction(state, input, questions) },
        isTerminalState = { state -> state.currentQuestionIndex >= questions.size },
        terminalStateToText = { "Congratulations on finishing all the problems for this problem set!" },
    )

// Generates the text to display when choosing a question bank.
fun chooseBankStateToText(
    state: Int,
    questionBanks: List<QuestionBank>,
): String {
    val header = listOf("\nWelcome to Question Time! You can choose from ${questionBanks.size} question banks:")
    val questionBanksLines =
        questionBanks.mapIndexed { index, questionBank ->
            "${index + 1}. ${questionBank.name}"
        }
    val prompt = listOf("Enter your choice: ")

    val screen = header + questionBanksLines + prompt

    return when (state) {
        0 -> linesToString(screen)
        else -> ""
    }
}

// Determines the next state when choosing a question bank based on user input.
fun chooseBankNextState(
    state: Int,
    userInput: String,
    questionBanks: List<QuestionBank>,
): Int {
    if (isAnInteger(userInput)) {
        val index = userInput.toInt()
        if (index in 1..questionBanks.size) {
            return index
        }
    }
    // Return the current state to avoid resetting to 0 if input is invalid
    return state
}

// Allows the user to choose a question bank from a list of question banks.
fun chooseBank(questionBanks: List<QuestionBank>): QuestionBank {
    val index =
        reactConsole(
            initialState = 0,
            stateToText = { state -> chooseBankStateToText(state, questionBanks) },
            isTerminalState = { state -> state in 1..questionBanks.size },
            nextState = { state, input -> chooseBankNextState(state, input, questionBanks) },
        )

    return questionBanks[index - 1]
}

// Runs the interactive study session by allowing the user to choose a question bank and study it.
fun play() {
    val questionBank1 = cubes(3)
    val questionBank2 =
        QuestionBank(
            name = "History",
            bank =
                listOf(
                    Question("Who painted the Mona Lisa?", "Leonardo da Vinci"),
                    Question("When did WWII end?", "1945"),
                    Question("Where is the Great Wall?", "China"),
                    Question("What is the capital of France?", "Paris"),
                ),
        )
    val questionBank3 = QuestionBank(name = "Trivia", bank = readQuestionBank("questions.txt"))

    val questionBanks = listOf(questionBank1, questionBank2, questionBank3)

    val chosenQuestionBank = chooseBank(questionBanks)

    val results = studyQuestionBank(chosenQuestionBank.bank)
    println("You've answered ${results.numCorrectAnswers} questions correctly!")
    println("Come again! Bye!")
}

// Test function for cubes.
@EnabledTest
fun testCubes() {
    testSame(cubeNumber(10), 1000, testName = "Testing cubeNumber() for input 10")
    testSame(
        cubes(1),
        QuestionBank(
            "Questions of Cubes",
            List(1) {
                Question("Q: what is ${1} cubed?", answer = "A: ${1}")
            },
        ),
        testName = "Testing cubesQuestion() for input 1",
    )
}

// Test function for converting questions to and from strings.
@EnabledTest
fun testQString() {
    val exampleOne = Question("What is 2 cubed?", "8")
    val exampleTwo = Question("Who's the boss?", "I'm the boss")
    val exampleThree = Question("What is a question?", "a problem that needs an answer")

    val expectedSFormatOne = "What is 2 cubed?|8"
    val expectedSFormatTwo = "Who's the boss?|I'm the boss"
    val expectedSFormatThree = "What is a question?|a problem that needs an answer"

    testSame(questionToString(exampleOne), expectedSFormatOne, testName = "Testing questionToString for example one")
    testSame(questionToString(exampleTwo), expectedSFormatTwo, testName = "Testing questionToString for example two")
    testSame(questionToString(exampleThree), expectedSFormatThree, testName = "Testing questionToString for example three")

    testSame(stringToQuestion(expectedSFormatOne), exampleOne, testName = "Testing stringToQuestion for example one")
    testSame(stringToQuestion(expectedSFormatTwo), exampleTwo, testName = "Testing stringToQuestion for example two")
    testSame(stringToQuestion(expectedSFormatThree), exampleThree, testName = "Testing stringToQuestion for example three")
}

// Test function for reading questions from a file.
@EnabledTest
fun testReadQuestionBank() {
    val doesNotExist = ""
    val validFilePath = "questions.txt"

    val questionOne = "What do the letters S and A stand for in SAM missiles?|Surface to air"
    val questionTwo = "How many black keys are found on a traditional 88-key piano?|36"
    val questionThree = "In which decade were the first Winter Olympic Games held?|1920s"

    testSame(readQuestionBank(doesNotExist), listOf(), testName = "Testing readQuestionBank for invalid filepath. Expects empty list.")
    testSame(
        readQuestionBank(validFilePath),
        listOf(stringToQuestion(questionOne), stringToQuestion(questionTwo), stringToQuestion(questionThree)),
        testName = "Testing readQuestinBank for valid filepat. Expects list of three elements.",
    )
}

// Test function for checking if a word starts with 'y'.
@EnabledTest
fun testPrefixIsY() {
    val valid = "Yorkshire"
    val invalid = "Northeastern"
    testSame(prefixIsY(valid), true, testName = "Testing prefixIsY for valid string")
    testSame(prefixIsY(invalid), false, testName = "Testing prefixIsY for invalid string")
}

// Test function for generating text based on the study stage.
@EnabledTest
fun testToText() {
    val stateOne = StudyStage.VIEWING_ANSWER
    val stateZero = StudyStage.VIEWING_QUESTION

    val sampleQuestion = Question(query = "Who's the boss?", answer = "I'm the boss")
    testSame(
        toText(stateZero, sampleQuestion),
        "Who's the boss?\nThink about the answer... (Press Enter to reveal)",
        testName = "Testing toText() for studyQuestion when viewing question.",
    )
    testSame(toText(stateOne, sampleQuestion), "I'm the boss", testName = "Testing toText() for studyQuestion when viewing answer.")
}

// Test function for QuestionBank and Question interaction
@EnabledTest
fun testQuestionBank() {
    val question = Question("What is 2 cubed?", "8")
    val questionBank = QuestionBank("Math Questions", listOf(question))

    testSame(questionBank.name, "Math Questions", testName = "Testing QuestionBank name retrieval")
    testSame(questionBank.bank.size, 1, testName = "Testing QuestionBank bank size")
    testSame(questionBank.bank[0], question, testName = "Testing QuestionBank question retrieval")
}

// Test function for state transitions in studyQuestionBank
@EnabledTest
fun testStudyQuestionBankToState() {
    val qbStateForQues = QuestionBankState(currentQuestionIndex = 0, isViewingAnswer = false, numCorrectAnswers = 0)
    val qbStateForAns = QuestionBankState(currentQuestionIndex = 0, isViewingAnswer = true, numCorrectAnswers = 0)

    testSame(
        studyQuestionBankStateToText(qbStateForQues),
        "Viewing Question 1: (Press Enter to reveal)",
        testName = "Testing studyQuestionBankStateToText for when viewing answer",
    )

    testSame(
        studyQuestionBankStateToText(qbStateForAns),
        "Did you get the right answer? [y/n]",
        testName = "Testing studyQuestionBankStateToText for when viewing question",
    )
}

@EnabledTest
fun testChooseBankStateToText() {
    val questionBank = listOf(QuestionBank("Trivia", listOf(Question("Who painted the Mona Lisa?", "Leonardo da Vinci"))))
    val viewingScreen = 0
    val notViewingScreen = 1

    testSame(
        chooseBankStateToText(viewingScreen, questionBank),
        "\nWelcome to Question Time! You can choose from 1 question banks:\n" +
            "1. Trivia\n" +
            "Enter your choice: ",
        testName = "Testing chooseBankStateToText for when viewing screen",
    )

    testSame(
        chooseBankStateToText(notViewingScreen, questionBank),
        "",
        testName = "Testing chooseBankStateToText for when not viewing screen",
    )
}

@EnabledTest
fun testChooseBankNextState() {
    val state = 0
    val userInputValid = "1"
    val userInputInvalid = "fdfsdfds"
    val userInputInvalidNumber = "1000"

    val questionBank = listOf(QuestionBank("Trivia", listOf(Question("Who painted the Mona Lisa?", "Leonardo da Vinci"))))

    testSame(
        chooseBankNextState(state, userInputValid, questionBank),
        1,
        testName = "Testing chooseBankNextState for when the usr input is valid",
    )

    testSame(
        chooseBankNextState(state, userInputInvalid, questionBank),
        0,
        testName = "Testing chooseBankNextState for when the user input is invalid as a string",
    )

    testSame(
        chooseBankNextState(state, userInputInvalidNumber, questionBank),
        0,
        testName = "Testing chooseBankNextState for when the user input is invalid as a number",
    )
}

// Test function for choosing a question bank
@EnabledTest
fun testChooseBank() {
    val questionBank1 = QuestionBank("Math Questions", listOf(Question("What is 3 cubed?", "27")))
    val questionBank2 = QuestionBank("Trivia", listOf(Question("Who painted the Mona Lisa?", "Leonardo da Vinci")))
    val questionBanks = listOf(questionBank1, questionBank2)

    val capturedResult = captureResults({ chooseBank(questionBanks) }, "2")

    testSame(
        capturedResult.returnedValue.name,
        "Trivia",
        testName = "Testing chooseBank selection of Trivia question bank",
    )
}

// Main function to run all tests.
// Includes required examples of Question, QuestionBank, and QuestionBankState
fun main() {
    // Example of Questions data class
    val exampleOne = Question("What is 2 cubed?", "8")
    val exampleTwo = Question("Who's the boss?", "I'm the boss")
    val exampleThree = Question("What is a question?", "a problem that needs an answer")

    // Example of Question bank data class
    val qbExampleOne = QuestionBank("Example question bank 1", bank = listOf(exampleOne, exampleTwo))
    val qbExampleTwo = QuestionBank("Example question bank 2", bank = listOf(exampleTwo, exampleThree))

    // Example of QuestionBankState
    // Example 1: Initial State (No Questions Answered)
    val initialState = QuestionBankState(currentQuestionIndex = 0, isViewingAnswer = false, numCorrectAnswers = 0)

    // Example 2: In Progress (Some Questions Answered, Mixed Correct and Incorrect)
    val mixedProgress = QuestionBankState(currentQuestionIndex = 5, isViewingAnswer = false, numCorrectAnswers = 2)

    // Example 3: All Questions Answered Correctly
    val allCorrect = QuestionBankState(currentQuestionIndex = 5, isViewingAnswer = true, numCorrectAnswers = 5)

    // Example 4: All Questions Answered Incorrectly
    val allIncorrect = QuestionBankState(currentQuestionIndex = 5, isViewingAnswer = true, numCorrectAnswers = 0)

    play()
}

runEnabledTests(this)
main()
