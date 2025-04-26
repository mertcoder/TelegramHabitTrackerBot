package handler

import AppState
import AppStrings
import AppStrings.addGoalButtonText
import AppStrings.goalsButtonText
import AppStrings.habitTrackerText
import AppStrings.sportButtonText
import AppStrings.welcomeText
import KeyboardMarkup
import KeyboardMarkup.addNewOrShowHabitMarkup
import KeyboardMarkup.backToMainMenuSingleMarkup
import KeyboardMarkup.goalsAddShowKeyboardMarkup
import KeyboardMarkup.goalsCategoryMarkup
import KeyboardMarkup.mainMenuKeyboardMarkup
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import handler.ProgressHandler


class CallbackHandler(private val goalHandler: GoalHandler,private val habitHandler: HabitHandler, private val progressHandler: ProgressHandler, private val settingsHandler: SettingsHandler,
                      private val database: MongoDatabase, private val userStates: MutableMap<Long,String>,    private val appState: AppState  // Changed to use AppState instead of String
)
{


    fun registerCallbacks(dispatcher: Dispatcher) {
        registerMenuCallbacks(dispatcher)
        registerGoalCallbacks(dispatcher)
        registerHabitCallbacks(dispatcher)
        registerProgressCallbacks(dispatcher)
        registerSettingsCallbacks(dispatcher)
    }

    private fun registerSettingsCallbacks(dispatcher: Dispatcher) {
        dispatcher.callbackQuery(
            callbackData = "settingsButton"
        ) {
            val chatId = callbackQuery.message?.chat?.id?:return@callbackQuery
            bot.sendMessage(
                chatId = ChatId.fromId(chatId),
                text = "Select an option to continue.",
                replyMarkup = KeyboardMarkup.settingsMarkup
            )
        }

        dispatcher.callbackQuery(
            callbackData = "deleteButton"
        ) {
            val chatId = callbackQuery.message?.chat?.id?: return@callbackQuery
            val userId = callbackQuery.from.id

            settingsHandler.deleteUser(userId)
            bot.sendMessage(
                chatId = ChatId.fromId(chatId),
                text = "Your chat has been cleared."
            )
        }
        dispatcher.callbackQuery(
            callbackData = "helpButton"
        ) {
            val chatId = callbackQuery.message?.chat?.id?:return@callbackQuery
            bot.sendMessage(
                chatId = ChatId.fromId(chatId),
                text = AppStrings.helpText,
                parseMode = ParseMode.MARKDOWN,
                replyMarkup = KeyboardMarkup.backToMainMenuSingleMarkup
            )

        }

    }

    private fun registerProgressCallbacks(dispatcher: Dispatcher) {
        dispatcher.callbackQuery(
            callbackData = "progressButton"
        ){
            val chatId = callbackQuery.message?.chat?.id?:return@callbackQuery
            val userId = callbackQuery.from.id

            progressHandler.showProgress(bot,userId,chatId)

        }


    }

    private fun registerHabitCallbacks(dispatcher: Dispatcher) {

        dispatcher.callbackQuery(
            callbackData = "habitTrackerButton"
        ) {
            val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery
            bot.sendMessage(
                chatId = ChatId.fromId(chatId),
                text  = habitTrackerText,
                parseMode = ParseMode.MARKDOWN,
                replyMarkup = addNewOrShowHabitMarkup
            )
        }

        dispatcher.callbackQuery(callbackData = "showHabitsButton"){
            val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery
            val userId = callbackQuery.from.id
            habitHandler.showHabits(userId,bot,chatId)

        }

        dispatcher.callbackQuery {
            val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery
            val userId = callbackQuery.from.id
            if (callbackQuery.data.startsWith("completedHabitButton_")) {
                val habitId = callbackQuery.data.substringAfter("completedHabitButton_")
                //debug print
                println("Completed habit Id: $habitId")
                CoroutineScope(Dispatchers.IO).launch {
                    habitHandler.updateHabitStatus(userId, habitId, operation = "")
                }
                bot.sendMessage(
                    ChatId.fromId(chatId),
                    text = "Habit marked as completed for today.",
                    replyMarkup = backToMainMenuSingleMarkup
                )

            }
            if (callbackQuery.data.startsWith("deleteHabitButton_")) {
                val habitId = callbackQuery.data.substringAfter("deleteHabitButton_")
                //debug print
                println("Selected to be deleted habit Id: $habitId")
                CoroutineScope(Dispatchers.IO).launch {
                    habitHandler.updateHabitStatus(userId, habitId, operation = "delete")
                }
                bot.sendMessage(
                    ChatId.fromId(chatId),
                    text = "Habit deleted.",
                    replyMarkup = backToMainMenuSingleMarkup
                )
            }
        }

        dispatcher.callbackQuery(
            callbackData = "addNewHabitButton"
        ) {
            val chatId = callbackQuery.message?.chat?.id?:return@callbackQuery
            val userId = callbackQuery.from.id
            userStates[chatId] = "waiting_for_habit"

            bot.sendMessage(
                chatId = ChatId.fromId(chatId),
                text  = "\"\uD83C\uDFAF Write your habits name to add (f.e: 'Drink 8 glass of water.')."
            )
        }
    }

    private fun registerGoalCallbacks(dispatcher: Dispatcher) {

        dispatcher.callbackQuery(
            callbackData = "showGoalsButton",
        ) {
            val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery
            val userId = callbackQuery.from?.id

            goalHandler.showGoals(userId,bot,chatId,type = "pending")


        }


        dispatcher.callbackQuery(
            callbackData = "goalsButton"
            // ,callbackAnswerText = "HelloText",
            //callbackAnswerShowAlert = false,
        ) {
            val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery
            bot.sendMessage(
                ChatId.fromId(chatId),
                text = goalsButtonText,
                parseMode = ParseMode.MARKDOWN,
                replyMarkup = goalsAddShowKeyboardMarkup
            )
        }
        dispatcher.callbackQuery(
            callbackData = "addGoalButton"
        ) {
            val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery
            bot.sendMessage(
                ChatId.fromId(chatId),
                text = addGoalButtonText,
                parseMode = ParseMode.MARKDOWN,
                replyMarkup = goalsCategoryMarkup
            )
        }
        dispatcher.callbackQuery(
            callbackData = "sportButton",
        ) {
            val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery
            userStates[chatId] = "waiting_for_goal"
            appState.goalCategory  = "sport"

            bot.sendMessage(
                ChatId.fromId(chatId),
                text = sportButtonText,
                replyMarkup = backToMainMenuSingleMarkup,
                parseMode = ParseMode.MARKDOWN
            )
        }
        dispatcher.callbackQuery(
            callbackData = "readingButton",
        ) {
            val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery
            userStates[chatId] = "waiting_for_goal"
            appState.goalCategory = "reading"

            bot.sendMessage(
                ChatId.fromId(chatId),
                text = """
                    📚 You've selected *Reading* as your goal category! 
                    Let's dive into the world of books! 📖
            
                    Please type your *reading goal* below and set your literary aspirations! 🌟
                    """.trimIndent(),
                replyMarkup = backToMainMenuSingleMarkup,
                parseMode = ParseMode.MARKDOWN
            )
        }

        dispatcher.callbackQuery(
            callbackData = "workButton",
        ) {
            val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery
            userStates[chatId] = "waiting_for_goal"
            appState.goalCategory = "work"

            bot.sendMessage(
                ChatId.fromId(chatId),
                text = """
                    💼 You've selected *Work* as your goal category! 
                    Time to boost your career! 🚀
            
                    Please write your *work goal* below and take the next step toward success! 🌟
                    """.trimIndent(),
                replyMarkup = backToMainMenuSingleMarkup,
                parseMode = ParseMode.MARKDOWN
            )
        }

        dispatcher.callbackQuery(
            callbackData = "personalButton",
        ) {
            val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery
            userStates[chatId] = "waiting_for_goal"
            appState.goalCategory = "personal"

            bot.sendMessage(
                ChatId.fromId(chatId),
                text = """
                    🎨 You've selected *Personal* goals! 
                    Time to focus on your personal growth! 🌱
            
                    Please type your *personal goal* below and let's make progress together! 🌟
                    """.trimIndent(),
                replyMarkup = backToMainMenuSingleMarkup,
                parseMode = ParseMode.MARKDOWN

            )
        }

       dispatcher.callbackQuery {
            val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery
            val callbackData = callbackQuery.data
            val userId = callbackQuery.from?.id

            if(callbackData.startsWith("finishedGoalButton_")){
                val goalId = callbackData.substringAfter("finishedGoalButton_")
                // debug print
                println("Finished marked goal Id: ${goalId}")


                CoroutineScope(Dispatchers.IO).launch {
                    goalHandler.updateGoalStatus(userId?:0,goalId,GoalStatus.FINISHED)
                }
                bot.sendMessage(
                    ChatId.fromId(chatId),
                    text = "Goal marked as finished!",
                    replyMarkup = backToMainMenuSingleMarkup // Keyboard after action
                )


            }
            if(callbackData.startsWith("failedGoalButton_")){
                val goalId = callbackData.substringAfter("failedGoalButton_")
                // debug print
                println("Failed marked goal Id: ${goalId}")


                CoroutineScope(Dispatchers.IO).launch {
                    goalHandler.updateGoalStatus(userId?:0,goalId,GoalStatus.FAILED)
                }
                bot.sendMessage(
                    ChatId.fromId(chatId),
                    text = "Goal marked as failed!",
                    replyMarkup = backToMainMenuSingleMarkup // Keyboard after action
                )

            }
            if(callbackData.startsWith("deleteGoalButton_")){
                val goalId = callbackData.substringAfter("deleteGoalButton_")
                // debug print
                println("Deleted marked goal Id: ${goalId}")


                CoroutineScope(Dispatchers.IO).launch {
                    goalHandler.updateGoalStatus(userId?:0,goalId,GoalStatus.DELETE)
                }
                bot.sendMessage(
                    ChatId.fromId(chatId),
                    text = "Goal deleted!",
                    replyMarkup = backToMainMenuSingleMarkup // Keyboard after action
                )

            }


        }

        dispatcher.callbackQuery(callbackData = "showAllHistoricalGoals") {
            val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery
            val userId = callbackQuery.from.id
            goalHandler.showGoals(userId,bot,chatId,type="historical")



        }


    }

    private fun registerMenuCallbacks(dispatcher: Dispatcher) {
        dispatcher.callbackQuery(
            callbackData = "mainMenuButton",
        ) {
            val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery
            userStates[chatId] = ""
            appState.goalCategory = ""
            bot.sendMessage(
                ChatId.fromId(chatId),
                text = welcomeText,
                replyMarkup = mainMenuKeyboardMarkup,
                parseMode = ParseMode.MARKDOWN,
            )
        }

    }

}