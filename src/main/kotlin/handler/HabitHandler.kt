package handler

import AppStrings.noGoalsYetText
import AppStrings.showHabitsMessage
import Goal
import Habit
import User
import areDatesOnSameDay
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import java.util.*

class HabitHandler(private val database: MongoDatabase) {


    suspend fun updateHabitStatus(userId: Long, habitId: String, operation: String?) {
        val userCollection = database.getCollection<User>("users")

        val filter = Filters.and(
            Filters.eq("userId",userId),
            Filters.eq("habits._id", ObjectId(habitId))
        )
        val currentEpochTime = System.currentTimeMillis()

        if(operation == "delete"){
            val update = Updates.pull("habits", Filters.eq("_id", ObjectId(habitId)))
            val updateResult = userCollection.updateOne(filter, update)
            if (updateResult?.modifiedCount!! > 0) {
                println("✅ Habit deleted successfully!")
            } else {
                println("⚠️ Habit? Where is it?")
            }

        }else{
            val currentDate =  Date(currentEpochTime)

            val doc = userCollection.find(filter).firstOrNull()
            val completedDates = doc?.habits?.firstOrNull{it.id == ObjectId(habitId) }?.completedDates
            completedDates?.forEach {
                if (!areDatesOnSameDay(currentDate,it)){
                    addCompletedDateToHabitIfNotSameDay(currentDate,habitId,filter)

                }
            }
            if (completedDates.isNullOrEmpty()) {
                addCompletedDateToHabitIfNotSameDay(currentDate,habitId,filter)
            }

        }

    }

    suspend fun addCompletedDateToHabitIfNotSameDay(currentDate : Date, habitId: String, filter: Bson) {
        val userCollection = database.getCollection<User>("users")

        val update = Updates.addToSet("habits.$[habit].completedDates", currentDate)

        val updateResult = userCollection.updateOne(
            filter,
            update,
            com.mongodb.client.model.UpdateOptions().arrayFilters(listOf(Document("habit._id", ObjectId(habitId))))
        )
        if(updateResult.modifiedCount>0){
            println("Habit updated with completed new day.")
        }else{
            println("I can't make this happen, sorry.")
        }
    }


    suspend fun showHabits(userId: Long, bot: Bot, chatId: Long) {
        bot.sendMessage(
            ChatId.fromId(chatId),
            text=showHabitsMessage,
            parseMode = ParseMode.MARKDOWN
        )

        val userCollection = database.getCollection<User>("users")
        val doc = userCollection.find(Filters.eq("userId", userId)).firstOrNull()

        doc?.habits?.forEachIndexed{index,it->
            var showCompleteButton = true
            val habitText = it.habit
            val habitId = it.id
            val habitCompletedDate = it.completedDates
            val currentDate = Date(System.currentTimeMillis())

            habitCompletedDate.forEach { date->
                if(areDatesOnSameDay(currentDate,date)){
                    showCompleteButton=false
                }
            }


            val onlyDeleteHabitStatusMarkUp = InlineKeyboardMarkup.create(
                listOf(InlineKeyboardButton.CallbackData(text="❌Delete", callbackData = "deleteHabitButton_${habitId}"))
            )
            val deleteAndCompleteHabitStatusMarkUp = InlineKeyboardMarkup.create(
                listOf(
                    InlineKeyboardButton.CallbackData(text="✅ Completed", callbackData = "completedHabitButton_${habitId}"),
                    (InlineKeyboardButton.CallbackData(text="❌Delete", callbackData = "deleteHabitButton_${habitId}"))
                )

            )
            bot.sendMessage(
                ChatId.fromId(chatId),
                text= if(showCompleteButton) "${index+1}: $habitText" else "${index+1}: $habitText (completed for today)",
                replyMarkup = if(showCompleteButton) deleteAndCompleteHabitStatusMarkUp else onlyDeleteHabitStatusMarkUp
            )
        }

    }

    suspend fun addHabitToDb(user: User, newHabit: Habit) {
        val updateHabits = Updates.push("habits",newHabit)
        val userCollection = database.getCollection<User>("users")
        val query = Filters.eq(User::userId.name, user.userId)
        val updateResultHabits = userCollection.updateOne(query,updateHabits)
        if(updateResultHabits.modifiedCount>0){
            println("Habit added successfully to the user.")
        }else{
            println("Are you sure that you are exist?")
        }
    }

}