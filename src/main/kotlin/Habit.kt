import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.util.*

data class Habit(
    @BsonId val id: ObjectId = ObjectId(),
    val habit: String,
    val completedDates: List<Date> = listOf()
)
