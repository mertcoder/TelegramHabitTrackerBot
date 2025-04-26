package handler

import Goal
import GoalStatus
import User
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import com.mongodb.client.result.UpdateResult
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import createGoalMessage
import kotlinx.coroutines.flow.firstOrNull
import org.bson.Document
import org.bson.types.ObjectId

class GoalHandler(private val database: MongoDatabase)  {

    suspend fun showGoals(userId: Long?, bot: Bot, chatId: Long, type: String?){
        val userCollection = database.getCollection<User>("users")
        val doc = userCollection.find(Filters.eq("userId", userId)).firstOrNull()

        val allGoals : List<Goal>? = if(type=="historical"){
            doc?.goals?.filter {
                it.status != GoalStatus.PENDING
            }?.toList()
        }else{
            doc?.goals?.filter {
                it.status == GoalStatus.PENDING
            }?.toList()
        }
        allGoals?.forEach{
            val goalText = it.goal
            val goalId = it.id
            val goalCategory = it.category
            val goalStatus = it.status.name.lowercase().replaceFirstChar { it.uppercase() }

            val singleGoalStatusMarkeup = InlineKeyboardMarkup.create(
                listOf(
                    InlineKeyboardButton.CallbackData(text = "✅ Finished.", callbackData = "finishedGoalButton_${goalId}"),
                    InlineKeyboardButton.CallbackData(text = "❌ Failed.", callbackData = "failedGoalButton_${goalId}"),
                    InlineKeyboardButton.CallbackData(text = "\uD83D\uDDD1\uFE0F Delete.", callbackData = "deleteGoalButton_${goalId}")
                )
            )


            val goalMessage = createGoalMessage(goalCategory,goalText,type)
            bot.sendMessage(
                ChatId.fromId(chatId),
                text =goalMessage,
                replyMarkup = if (type == "historical") null else singleGoalStatusMarkeup,
                parseMode = ParseMode.MARKDOWN
            )
        }
        val listFinishedFailedGoalsMarkup = InlineKeyboardMarkup.create(
            listOf(InlineKeyboardButton.CallbackData(text = "List Finished/Failed Goals.", callbackData = "showAllHistoricalGoals")),
            listOf(InlineKeyboardButton.CallbackData(text = "Main Menu.", callbackData = "mainMenuButton"))
        )

        bot.sendMessage(
            ChatId.fromId(chatId),
            text = "You can also see your all finished/failed goals.",
            replyMarkup = listFinishedFailedGoalsMarkup
        )
    }
    suspend fun addGoalToDb(user: User, newGoal: Goal) {
        val updateGoals = Updates.push("goals", newGoal)
        val userCollection = database.getCollection<User>("users")
        val query = Filters.eq(User::userId.name, user.userId)
        val updateResultGoals = userCollection.updateOne(query, updateGoals)
        if (updateResultGoals.modifiedCount > 0) {
            println("Goal added successfully to user.")
        } else {
            println("User not found or no changes made.")
        }}

    suspend fun updateGoalStatus(userId: Long,goalId: String, status: GoalStatus) {
        val userCollection = database.getCollection<User>("users")
        var  updateResult : UpdateResult
        val filter = Filters.and(
            Filters.eq("userId", userId),
            Filters.eq("goals._id", ObjectId(goalId))
        )
        if(status==GoalStatus.DELETE){
            val update = Updates.pull("goals", Filters.eq("_id", ObjectId(goalId)))
            updateResult = userCollection.updateOne(filter, update)

        }else{
            val update = Updates.set("goals.$[elem].status", "$status")
            updateResult = userCollection.updateOne(
                filter,
                update,
                UpdateOptions().arrayFilters(listOf(Document("elem._id", ObjectId(goalId))))
            )
        }
        if (updateResult.modifiedCount > 0) {
            println("✅ Goal updated/deleted successfully!")
        } else {
            println("⚠️ Goal? Where is it?")
        }
    }

}