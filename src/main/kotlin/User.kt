import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class User(
    @BsonId val id: ObjectId = ObjectId(),
    val username: String,
    val userId: Long,
    val messages: List<Message> = listOf(),
    val goals: List<Goal> = listOf(),
    val habits: List<Habit> = listOf()
)
