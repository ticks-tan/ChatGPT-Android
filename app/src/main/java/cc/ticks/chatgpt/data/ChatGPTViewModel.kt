package cc.ticks.chatgpt.data

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import cc.ticks.chatgpt.http.ChatGPTRequest
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.sse.EventSource

class ChatGPTViewModel(application: Application) : AndroidViewModel(application) {
    private val dataBase = ChatGPTDBSingle.getInstance(application.applicationContext)
    private val chatRoomDao = dataBase.chatRoomDao()
    private val chatPromptDao = dataBase.chatPromptDao()
    private val chatMessageDao = dataBase.chatMessageDao()

    val allChatRooms: LiveData<List<ChatRoom>> = chatRoomDao.getAllChatRooms()
    val allChatPrompt: LiveData<List<ChatPrompt>> = chatPromptDao.getAllChatPrompts()
    lateinit var allChatRoomMessages: LiveData<List<ChatMessage>>
    lateinit var currentChatRoom: LiveData<ChatRoom>

    private val gptRequest: ChatGPTRequest = ChatGPTRequest()
    var gptEventSource: EventSource? = null

    fun initAllChatRoomMessagesLiveData(roomId: Long) {
        allChatRoomMessages = chatMessageDao.getChatMessagesByRoomId(roomId)
    }

    fun initCurrentChatRoomById(roomId: Long) {
        currentChatRoom = chatRoomDao.getChatRoomById(roomId)
    }

    fun addChatRoom(room: ChatRoom) {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                try {
                    chatRoomDao.addChatRoom(room)
                }catch (exp: java.lang.Exception){
                    exp.printStackTrace()
                }
            }
        }
    }

    fun removeChatRoomById(roomId: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    chatRoomDao.removeChatRoomById(roomId)
                }catch (exp: java.lang.Exception) {
                    exp.printStackTrace()
                }
            }
        }
    }
    fun removeChatRoom(chatRoom: ChatRoom) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    chatRoomDao.removeChatRoom(chatRoom)
                }catch (exp: java.lang.Exception) {
                    exp.printStackTrace()
                }
            }
        }
    }
    fun updateChatRoom(chatRoom: ChatRoom) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    chatRoomDao.updateChatRoom(chatRoom)
                }catch (exp: java.lang.Exception) {
                    exp.printStackTrace()
                }
            }
        }
    }

    fun addChatPrompt(chatPrompt: ChatPrompt) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    chatPromptDao.addChatPrompt(chatPrompt)
                }catch (exp: java.lang.Exception) {
                    exp.printStackTrace()
                }
            }
        }
    }
    fun removeChatPromptById(promptId: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    chatPromptDao.removeChatPromptById(promptId)
                }catch (exp: java.lang.Exception) {
                    exp.printStackTrace()
                }
            }
        }
    }
    fun removeChatPrompt(chatPrompt: ChatPrompt) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    chatPromptDao.removeChatPrompt(chatPrompt)
                }catch (exp: java.lang.Exception) {
                    exp.printStackTrace()
                }
            }
        }
    }
    fun updateChatPrompt(chatPrompt: ChatPrompt) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    chatPromptDao.updateChatPrompt(chatPrompt)
                }catch (exp: java.lang.Exception) {
                    exp.printStackTrace()
                }
            }
        }
    }

    fun getChatMessageByMsgId(messageId: Long) : LiveData<ChatMessage> {
        return chatMessageDao.getChatMessageByMsgId(messageId)
    }
    fun addChatMessage(message: ChatMessage) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    chatMessageDao.addChatMessage(message)
                }catch (exp: java.lang.Exception) {
                    exp.printStackTrace()
                }
            }
        }
    }
    fun removeChatMessageByMsgId(messageId: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    chatMessageDao.removeChatMessageByMsgId(messageId)
                }catch (exp: java.lang.Exception) {
                    exp.printStackTrace()
                }
            }
        }
    }
    fun removeChatMessage(chatMessage: ChatMessage) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    chatMessageDao.removeChatMessage(chatMessage)
                }catch (exp: java.lang.Exception) {
                    exp.printStackTrace()
                }
            }
        }
    }
    fun removeChatMessagesByRoomId(roomId: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    chatMessageDao.removeChatMessagesByRoomId(roomId)
                }catch (exp: java.lang.Exception) {
                    exp.printStackTrace()
                }
            }
        }
    }

    fun updateChatMessage(chatMessage: ChatMessage) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    chatMessageDao.updateChatMessage(chatMessage)
                }catch (exp: java.lang.Exception) {
                    exp.printStackTrace()
                }
            }
        }
    }

    // 发送聊天消息
    fun sendChatGPTChat(room: ChatRoom, msg: ChatMessage, callBack: ChatGPTRequest.SendChatCallBack) {
        /* TODO : 设置数据库设计，暂时使用固定设置 */
        val request = Request.Builder()
            .url("https://openai.ticks.cc/v1/chat/completions")
            .addHeader("authId", "auth20230301")
            .post(createRequestChatJsonBody(room, msg))
            .build()
        gptEventSource = gptRequest.sendChatGPTChat(request, callBack)
    }
    // 停止请求
    fun stopSendChatGPTChat() {
        if (gptEventSource != null) {
            gptEventSource!!.cancel()
        }
    }

    private fun createRequestChatJsonBody(room: ChatRoom, msg: ChatMessage): RequestBody {
        val obj = JSONObject()
        val array = JSONArray()
        obj["model"] = room.model
        obj["stream"] = true
        obj["max_tokens"] = 2048
        allChatRoomMessages.value?.forEach {
            val item = JSONObject()
            item["role"] = it.sender
            item["content"] = it.content
            array.add(item)
        }
        val tmp = JSONObject()
        tmp["role"] = "system"
        tmp["content"] = currentChatRoom.value?.prompt
        array.add(0, tmp)
        val tmp1 = JSONObject()
        tmp1["role"] = msg.sender
        tmp1["content"] = msg.content
        array.add(array.size, tmp1)

        obj["messages"] = array
        return obj.toJSONString()
            .toRequestBody(
                contentType = "application/json;charset=utf-8".toMediaTypeOrNull()
            )
    }

}