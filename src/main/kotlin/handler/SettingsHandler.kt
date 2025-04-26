package handler

import User
import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull

class SettingsHandler(private val database: MongoDatabase) {

    suspend fun deleteUser(userId: Long) {
        val collection = database.getCollection<User>("users")
        val filters = Filters.eq("userId", userId)
        val result = collection.deleteOne(filters)
        if (result.deletedCount>0) {
            println("User deleted successfully.")
        }else{
            println("Can't be deleted.")
        }
    }
}