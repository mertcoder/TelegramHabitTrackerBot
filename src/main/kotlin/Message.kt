import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.util.Date

data class Message(
    @BsonId val id: ObjectId = ObjectId(),
    val messageId: Long,
    val text: String,
    val date : Date
    )
