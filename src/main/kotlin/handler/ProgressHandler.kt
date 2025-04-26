package handler

import AppStrings.noGoalsYetText
import Goal
import Habit
import User
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import java.util.*

class ProgressHandler(private val database: MongoDatabase) {


    suspend fun showProgress( bot: Bot, userId: Long, chatId: Long) {
        val collection = database.getCollection<User>("users")
        val filters = Filters.eq("userId", userId)
        val user = collection.find(filters,User::class.java).firstOrNull()
        if(user!=null){

            val userGoals = user.goals
            val userHabits = user.habits
            calculateGoalProgress(userGoals,bot,chatId)
            calculateHabitProgress(userHabits,bot,chatId)
        }
    }

    fun calculateHabitProgress(userHabits: List<Habit>, bot: Bot, chatId: Long) {
        if (userHabits.isNotEmpty()) {
            userHabits.forEach {
                println(it)
                val streak = calculateStreak(it)
                println("Streak for ${it.habit}: $streak")

                val message = """
                🌟 *Habit Progress* 🌟
                ✅ *Habit:* ${it.habit}
                🔥 *Current Streak:* $streak ${if (streak > 0) "🔥" else "❄️"}
                
                ${if (streak > 0) "Keep going! You're doing great! 🚀" else "Let's start fresh! You got this! 💪"}
            """.trimIndent()

                bot.sendMessage(ChatId.fromId(chatId), text = message, parseMode = ParseMode.MARKDOWN)
            }
        }
    }

    fun calculateStreak(it: Habit): Int {

        val calendar = Calendar.getInstance()


        val completedDaysList = it.completedDates
        // If no completed dates exist, streak is zero
        if (completedDaysList.isEmpty()) {
            return 0
        }
        val lastCompletedDate = completedDaysList.last()
        calendar.time = lastCompletedDate

        // Last completed day, max days in that month, and month index (0-based)
        val lastCompletedDateAsDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val lastCompletedDateActualMaximum = calendar.getActualMaximum(Calendar.DATE)
        val lastCompletedDateMonthIndex = calendar.get(Calendar.MONTH)

        val currentDate = Date(System.currentTimeMillis())
        calendar.time = currentDate

        // Current day, current month's max days, and month index (0-based)
        val currentDateAsDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val currentDateMonthIndex = calendar.get(Calendar.MONTH)


        // If the last completed date is in the same month as today
        if (currentDateMonthIndex == lastCompletedDateMonthIndex) {
            // If the gap between the last completed day and today is greater than 1, streak resets
            if (currentDateAsDayOfMonth - lastCompletedDateAsDayOfMonth > 1) {
                println("Current date: $currentDateAsDayOfMonth, Last completed date: $lastCompletedDateAsDayOfMonth, Difference: ${currentDateAsDayOfMonth - lastCompletedDateAsDayOfMonth}")
                return 0
            } else {
                // Otherwise, calculate the streak
                return calculateStreakFromList(completedDaysList)
            }
        }
        // If the last completed date is in the previous month
        else {
            // Check if the last completed day was the last day of its month and today is the 1st of the new month
            if (lastCompletedDateAsDayOfMonth == lastCompletedDateActualMaximum && currentDateAsDayOfMonth == 1) {
                return calculateStreakFromList(completedDaysList)
            } else {
                // If they are not consecutive, streak resets
                return 0
            }
        }

    }

    fun calculateStreakFromList(completedDaysList: List<Date>): Int {
        var streak = 1
        val calendar = Calendar.getInstance()
        for (index in completedDaysList.size-1 downTo 1){
            val currentIndexDate = completedDaysList[index]
            val previousIndexDate = completedDaysList[index-1]


            calendar.time = currentIndexDate
            val currentIndexDay = calendar.get(Calendar.DAY_OF_MONTH)

            calendar.time = previousIndexDate
            val previousIndexDay = calendar.get(Calendar.DAY_OF_MONTH)

            if(currentIndexDay-previousIndexDay>1){
                break
            }else{
                streak++
            }
        }
        return streak
    }
    fun calculateGoalProgress(userGoals: List<Goal>, bot: Bot, chatId: Long) {
        if(userGoals.isNotEmpty()){
            val finishedGoals = userGoals.filter { it.status== GoalStatus.FINISHED }.count()
            val failedGoals = userGoals.filter { it.status== GoalStatus.FAILED }.count()
            val activeGoals = userGoals.filter { it.status== GoalStatus.PENDING }.count()
            val totalGoals = finishedGoals+failedGoals+activeGoals
            val successRatio = (finishedGoals/totalGoals.toDouble())*100
            bot.sendMessage(
                ChatId.fromId(chatId),
                text = """
                    📊 *Goal Summary* 📊
            
                    You have attempted *$totalGoals* goals in total.
                    
                    ✅ You have successfully finished *$finishedGoals* goals.
                    
                    ❌ Unfortunately, you have failed *$failedGoals* goals.
                    
                    ⏳ You currently have *$activeGoals* active goals.
            
                    🎯 Your success ratio is *$successRatio*%.
                    
                    Keep up the great work! 💪 Let's achieve more! 🌟
                """.trimIndent(),
                parseMode = ParseMode.MARKDOWN
            )
        }else{
            bot.sendMessage(
                ChatId.fromId(chatId),
                text = noGoalsYetText,
                parseMode = ParseMode.MARKDOWN
            )
        }
    }

}