package cc.ticks.chatgpt.data

import android.content.Context
import cc.ticks.chatgpt.tools.FileTool
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONException
import com.alibaba.fastjson.JSONObject

data class ThirdApi(
    var apiUrl: String = "",
    val headerMap: MutableMap<String, String> = HashMap()
) {
    fun addHeader(key: String, value: String) {
        headerMap[key] = value
    }
    fun toJsonObject() : JSONObject {
        val obj = JSONObject()
        obj["apiUrl"] = apiUrl
        headerMap.forEach { (key, value) ->
            obj[key] = value
        }
        return obj
    }
    companion object {
        fun newThirdApi(api: String) : ThirdApi {
            return ThirdApi(api, HashMap())
        }
    }
}

data class SettingData(
    var isError: Boolean = false,
    // 是否使用第三方接口
    var useThirdApi: Boolean = false,
    // 官方接口token
    var apiToken: String = "",
    // 第三方接口
    var thirdApiMap: MutableMap<String, ThirdApi> = HashMap(),
    // 聊天使用流式传输
    var useStream: Boolean = true
) {
    fun addThirdApi(api: ThirdApi) {
        thirdApiMap.putIfAbsent(api.apiUrl, api)
    }
    fun toJsonObject() : JSONObject {
        val obj: JSONObject = JSONObject()
        obj["useThirdApi"] = useThirdApi
        obj["apiToken"] = apiToken
        obj["useStream"] = useStream
        val ary: JSONArray = JSONArray()
        thirdApiMap.forEach { (_, thirdApi) ->
            ary.add(thirdApi.toJsonObject())
        }
        obj["thirdApiMap"] = ary
        return obj
    }
    companion object {
        fun parseFromJsonString(str: String) : SettingData {
            val obj = SettingData()
            try {
                val jsonObj = JSON.parseObject(str)
                obj.useThirdApi = jsonObj.getBoolean("useThirdApi")
                obj.apiToken = jsonObj.getString("apiToken")
                obj.useStream = jsonObj.getBoolean("useStream")
                val ary = jsonObj.getJSONArray("thirdApiMap")
                val size = ary.size
                var i = 0
                while (i < size) {
                    val tmp = ary.getJSONObject(i).innerMap
                    val thirdApi = ThirdApi()
                    tmp.forEach { (key, any) ->
                        if (key == "apiUrl") thirdApi.apiUrl = any as String
                        else thirdApi.addHeader(key, any as String)
                    }
                    obj.addThirdApi(thirdApi)
                    i += 1
                }
            }catch (exp: JSONException) {
                exp.printStackTrace()
                obj.isError = true
            }
            return obj
        }
    }
}

object SettingDataSingle {
    @Volatile
    private var INSTANCE : SettingData? = null
    private const val SETTINGS_NAME : String = "app_setting_db.db"

    fun getInstance(context: Context) : SettingData {
        val tmpInstance = INSTANCE
        if (tmpInstance != null) {
            return tmpInstance
        }
        synchronized(this) {
            val fileData = FileTool.readPrivateFile(context, SETTINGS_NAME)
            val settingData = SettingData.parseFromJsonString(fileData)
            INSTANCE = settingData
            return settingData
        }
    }

    fun saveData(context: Context, settingData: SettingData) {
        FileTool.writePrivateFile(context, SETTINGS_NAME, settingData.toJsonObject().toJSONString())
    }
}

