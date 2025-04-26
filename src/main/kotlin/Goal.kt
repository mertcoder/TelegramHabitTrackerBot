import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Goal(
    @BsonId val id: ObjectId = ObjectId(),
    val goal: String,
    val category: String,
    var status: GoalStatus
)
enum class GoalStatus {
    PENDING,
    FINISHED,  
    FAILED,
    DELETE
}