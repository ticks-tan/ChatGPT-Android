package cc.ticks.chatgpt.http

import android.app.Application
import cc.ticks.chatgpt.ChatApplication
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONException
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.time.Duration
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

class ChatGPTRequest() {
    private val application: ChatApplication = ChatApplication.instance

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.MINUTES)
            .readTimeout(60, TimeUnit.MINUTES)
            .writeTimeout(60, TimeUnit.MINUTES)
            .build()
    }
    private val sseFactory: EventSource.Factory = EventSources.createFactory(httpClient)

    fun sendChatGPTChat(
        request: Request,
        callBack: SendChatCallBack
    ) : EventSource {
        return sseFactory.newEventSource(request, object : EventSourceListener(){
            override fun onOpen(eventSource: EventSource, response: Response) {
                // 主线程执行回调并更新UI
                application.mainThreadHandler.post {
                    callBack.onConnect()
                }
                super.onOpen(eventSource, response)
            }
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                /**
                 * {
                 *  "id":"chatcmpl-6w9wCrROTHzc0iIrV59Gcvhe2u5ma",
                 *  "object":"chat.completion.chunk",
                 *  "created":1679319244,
                 *  "model":"gpt-3.5-turbo-0301",
                 *  "choices":[
                 *      {
                 *          "delta":{
                 *              "role":"assistant"
                 *          },
                 *          "index":0,
                 *          "finish_reason":null
                 *      }
                 *  ]}
                 **/
                /**
                 * {
                 *  "id":"chatcmpl-6w9wCrROTHzc0iIrV59Gcvhe2u5ma",
                 *  "object":"chat.completion.chunk",
                 *  "created":1679319244,
                 *  "model":"gpt-3.5-turbo-0301",
                 *  "choices":[
                 *      {
                 *          "delta":{
                 *              "content":"内容"
                 *          },
                 *          "index":0,
                 *          "finish_reason":null
                 *      }
                 *  ]}
                 **/
                var content: String = String()
                var exception: java.lang.Exception? = null
                if (data != "[DONE]") {
                    try{
                        val resp = JSON.parseObject(data)
                        val choices = resp.getJSONArray("choices")
                        var i = 0
                        while (i < choices.size) {
                            val obj = choices.getJSONObject(i)
                            val str = obj.getJSONObject("delta").getString("content")
                            if (str != null && str.isNotEmpty()) {
                                content += str
                            }
                            i += 1
                        }
                    }catch (_: JSONException) {
                        exception = Exception("Json parse error!")
                    }
                }
                application.mainThreadHandler.post {
                    callBack.onReceiveMsg(content, exception)
                }
                super.onEvent(eventSource, id, type, data)
            }
            override fun onClosed(eventSource: EventSource) {
                application.mainThreadHandler.post {
                    callBack.onClose()
                }
                super.onClosed(eventSource)
            }
            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                eventSource.cancel()
                application.mainThreadHandler.post {
                    callBack.onError(response)
                }
                super.onFailure(eventSource, t, response)
            }
        })
    }

    interface SendChatCallBack {
        fun onConnect()
        fun onReceiveMsg(chatMsg: String, exception: java.lang.Exception?)
        fun onClose()
        fun onError(response: Response?)
    }
}