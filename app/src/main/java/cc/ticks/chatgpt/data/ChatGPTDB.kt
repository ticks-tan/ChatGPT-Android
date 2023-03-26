package cc.ticks.chatgpt.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

// 数据表，存储聊天室
@Entity(tableName = "chat_rooms")
data class ChatRoom(
    @PrimaryKey(autoGenerate = false) var roomId: Long = 0,
    var title: String = "",
    var model: String = "",
    var prompt: String = ""
) {
    companion object {
        fun createChatRoom(title: String, model: String, prompt: String): ChatRoom {
            return ChatRoom(roomId = System.currentTimeMillis(), title = title, model = model, prompt = prompt)
        }
    }
}

// 数据表，存储 prompt 数据
@Entity(tableName = "chat_prompts")
data class ChatPrompt(
    @PrimaryKey(autoGenerate = false) var promptId: Long = 0,
    var tag: String = "",
    var content: String = ""
) {
    companion object {
        fun createChatPrompt(content: String): ChatPrompt {
            return ChatPrompt(promptId = System.currentTimeMillis(), content = content)
        }

        fun createChatPrompt(tag: String, content: String): ChatPrompt {
            return ChatPrompt(promptId = System.currentTimeMillis(), content = content, tag = tag)
        }
    }
}

// 数据表，存储聊天消息
@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = false) var messageId: Long = 0,
    var roomId: Long = 0,
    var time: Long = 0,
    var sender: String = "",
    var content: String = "",
) {
    companion object {
        fun createUserChatMessage(roomId: Long, content: String): ChatMessage {
            val item = ChatMessage()
            item.time = System.currentTimeMillis()
            item.messageId = item.time
            item.roomId = roomId
            item.sender = "user"
            item.content = content
            return item
        }
        fun createAIChatMessage(roomId: Long, content: String): ChatMessage {
            val item = ChatMessage()
            item.time = System.currentTimeMillis()
            item.messageId = item.time
            item.roomId = roomId
            item.sender = "assistant"
            item.content = content
            return item
        }
    }
}

@Dao
interface ChatRoomDao {
    @Insert
    suspend fun addChatRoom(room: ChatRoom)

    @Query("select * from chat_rooms where roomId = :roomId limit 1")
    fun getChatRoomById(roomId: Long) : LiveData<ChatRoom>

    @Query("select * from chat_rooms")
    fun getAllChatRooms() : LiveData<List<ChatRoom>>

    @Query("delete from chat_rooms where roomId = :roomId")
    suspend fun removeChatRoomById(roomId: Long)

    @Delete
    suspend fun removeChatRoom(chatRoom: ChatRoom)

    @Update
    suspend fun updateChatRoom(chatRoom: ChatRoom)
}

@Dao
interface ChatPromptDao {
    @Insert
    suspend fun addChatPrompt(chatPrompt: ChatPrompt)

    @Query("select * from chat_prompts")
    fun getAllChatPrompts() : LiveData<List<ChatPrompt>>

    @Query("delete from chat_prompts where promptId = :promptId")
    suspend fun removeChatPromptById(promptId: Long)

    @Delete
    suspend fun removeChatPrompt(chatPrompt: ChatPrompt)

    @Update
    suspend fun updateChatPrompt(chatPrompt: ChatPrompt)
}

@Dao
interface ChatMessageDao {
    @Insert
    suspend fun addChatMessage(message: ChatMessage)

    @Query("select * from chat_messages where roomId = :roomId order by time asc")
    fun getChatMessagesByRoomId(roomId: Long) : LiveData<List<ChatMessage>>

    @Query("select * from chat_messages where messageId = :messageId limit 1")
    fun getChatMessageByMsgId(messageId: Long) : LiveData<ChatMessage>

    @Query("delete from chat_messages where messageId = :messageId")
    suspend fun removeChatMessageByMsgId(messageId: Long)

    @Query("delete from chat_messages where roomId = :roomId")
    suspend fun removeChatMessagesByRoomId(roomId: Long)

    @Delete
    suspend fun removeChatMessage(chatMessage: ChatMessage)

    @Update
    suspend fun updateChatMessage(chatMessage: ChatMessage)
}


@Database(version = 1, entities = [ChatRoom::class, ChatMessage::class, ChatPrompt::class])
abstract class ChatGPTDB : RoomDatabase() {
    abstract fun chatRoomDao() : ChatRoomDao
    abstract fun chatPromptDao() : ChatPromptDao
    abstract fun chatMessageDao() : ChatMessageDao

    companion object {
        const val DATABASE_NAME = "chatgpt_db"
    }
}

object ChatGPTDBSingle {
    @Volatile
    private var INSTANCE : ChatGPTDB? = null

    fun getInstance(context: Context) : ChatGPTDB {
        val tmpInstance = INSTANCE
        if (tmpInstance != null) {
            return tmpInstance
        }
        synchronized(this) {
            val ins = Room.databaseBuilder(
                context,
                ChatGPTDB::class.java,
                ChatGPTDB.DATABASE_NAME
            ).build()
            INSTANCE = ins
           return ins
        }
    }
}