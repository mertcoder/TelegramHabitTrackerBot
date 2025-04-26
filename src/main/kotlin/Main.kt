import AppStrings.addedNewGoalCongratsText
import AppStrings.welcomeText
import KeyboardMarkup.mainMenuKeyboardMarkup
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import handler.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.sql.Timestamp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

fun main(args: Array<String>) {
    // to hold goal category
    val appState = AppState()

    val userStates = mutableMapOf<Long,String>()

    val uri = System.getenv("MONGO_URI")?: error("MONGO_URI not set.")
    val token = System.getenv("BOT_TOKEN")?: error("BOT_TOKEN not set.")

    val mongoClient = try {
        MongoClient.create(uri)
    } catch (e: Exception) {
        error("Failed to connect to MongoDB: ${e.message}")
    }
    val database = mongoClient.getDatabase("telegrambot")
    val ioScope = CoroutineScope(Dispatchers.IO)

    val goalHandler = GoalHandler(database)
    val habitHandler =  HabitHandler(database)
    val progressHandler = ProgressHandler(database)
    val settingsHandler = SettingsHandler(database)

    val bot = bot{
        this.token = token
        dispatch {

            val callBackHandler = CallbackHandler(goalHandler,habitHandler,progressHandler,settingsHandler,database,userStates,appState)
            callBackHandler.registerCallbacks(this)

            message {
                val userId = message.from?.id
                val userName = message.from?.username
                val textMessage = message.text

                val timeStamp = Timestamp(message.date*1000)
                val messageLocalTimeDate = convertTelegramDateToLocal(timeStamp.time, "Europe/Istanbul")
                val messageDate = Date(timeStamp.time)
                val messageId = message.messageId

                val chatId = message.chat.id

                //Debug message prints
                println("usernameId: $userId")
                println("usernameName: $userName")
                println("message: $textMessage")
                println("Message date: ${messageDate}")
                println("Message local time date : $messageLocalTimeDate")

                val user = User(username = userName?:"", userId = userId?:0)
                val newMessage = Message(
                    text = textMessage?:"",
                    messageId = messageId,
                    date = messageDate )

                ioScope.launch {
                    checkUserExistenceAndUpdateMessage(database,user,newMessage)
                }

                when(userStates[chatId]){
                    "waiting_for_goal"-> {
                        //debug prints for goal
                        println("Goal is $textMessage")
                        println("Category: ${appState.goalCategory}")
                        //reset categorization

                        userStates[chatId] = ""
                        //create an instance
                        val newGoal = Goal(goal= textMessage?:"", category = appState.goalCategory, status =  GoalStatus.PENDING)

                        ioScope.launch {
                            try {
                                goalHandler.addGoalToDb(user, newGoal)
                            } catch (e: Exception) {
                                println("Error adding goal: ${e.message}")
                            }
                        }

                        bot.sendMessage(
                            ChatId.fromId(chatId),
                            text = addedNewGoalCongratsText,
                            replyMarkup = mainMenuKeyboardMarkup,
                            parseMode = ParseMode.MARKDOWN
                        )
                        appState.goalCategory=""
                    }
                    "waiting_for_habit"->{
                        userStates[chatId] = ""
                        println("Habit is $textMessage")
                        val newHabit = Habit(habit=textMessage?:"")
                        ioScope.launch {
                            ioScope.launch {
                                try {
                                    habitHandler.addHabitToDb(user, newHabit)
                                } catch (e: Exception) {
                                    println("Error adding habit: ${e.message}")
                                }
                            }
                        }

                        bot.sendMessage(
                            ChatId.fromId(chatId),
                            text = addedNewGoalCongratsText,
                            replyMarkup = mainMenuKeyboardMarkup,
                            parseMode = ParseMode.MARKDOWN
                        )
                    }
                    else->{}
                }


            }
            command("start"){
                bot.sendMessage(
                    ChatId.fromId(message.chat.id),
                    text = welcomeText,
                    replyMarkup = mainMenuKeyboardMarkup
                )
            }
            command("menu"){
                bot.sendMessage(
                    ChatId.fromId(message.chat.id),
                    text = welcomeText,
                    parseMode = ParseMode.MARKDOWN,
                    replyMarkup = mainMenuKeyboardMarkup
                )
            }
            command("goals") {
                val chatId = message.chat.id
                bot.sendMessage(
                    ChatId.fromId(chatId),
                    text = AppStrings.goalsButtonText,
                    parseMode = ParseMode.MARKDOWN,
                    replyMarkup = KeyboardMarkup.goalsAddShowKeyboardMarkup
                )
            }
        }


    }

    try {
        bot.startPolling()
    } catch (e: Exception) {
        println("Bot polling failed to start: ${e.message}")
    }
}

// for showGoals, creates aesthetic goal message if there any.
fun createGoalMessage(goalCategory: String, goalText: String, type: String?): String {
    val categoryEmoji = when(goalCategory) {
        "sport" -> "🏋️‍♂️"
        "reading" -> "📚"
        "work" -> "💼"
        "personal" -> "🎨"
        else -> "🏠" // Default to Home if no match
    }


    val goalMessage = if(type == "historical") {
        """
            🌟 *Goal Progress* 🌟
            🎯 *Goal:* $goalText
            ${categoryEmoji} *Category:* $goalCategory
            """.trimIndent()
    } else {
        """
            🌟 *Goal Progress* 🌟
            🎯 *Goal:* $goalText
            ${categoryEmoji} *Category:* $goalCategory
            """.trimIndent()
    }

    return goalMessage

}

suspend fun checkUserExistenceAndUpdateMessage(database: MongoDatabase, user: User , newMessage: Message) {
    val userCollection = database.getCollection<User>("users")
    try{

        val doc = userCollection.find(eq("userId", user.userId)).firstOrNull()
        if(doc==null){
            //create user, there is no existing user with userId
            val result = database.getCollection<User>("users").insertOne(user)
            val insertedId = result.insertedId?.asObjectId()?.value
            println("Insert a doc with the following id: $insertedId")
        }
        val query = eq(User::userId.name,user.userId)
        val updates = Updates.push("messages", newMessage)
        val updateResult = userCollection.updateOne(query, updates)
        if (updateResult.modifiedCount > 0) {
            println("Message added successfully to user.")
        } else {
            println("User not found or no changes made.")
        }
    }catch (e: Exception){
        println("MongoDB error in user check-update : ${e.message}")
    }
}

fun convertTelegramDateToLocal(timestamp: Long, timeZone: String): String {
    val instant = Instant.ofEpochMilli(timestamp)
    val zonedDateTime = instant.atZone(ZoneId.of(timeZone))
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return zonedDateTime.format(formatter)
}
fun areDatesOnSameDay(date1: Date, date2: Date): Boolean{
    val instant1 = date1.toInstant()
    val instant2 = date2.toInstant()

    val localDate1 = instant1.atZone(ZoneId.of("UTC")).toLocalDate()
    val localDate2 = instant2.atZone(ZoneId.of("UTC")).toLocalDate()

    return localDate1.isEqual(localDate2)
}
