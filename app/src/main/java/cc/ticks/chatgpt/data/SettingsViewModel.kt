package cc.ticks.chatgpt.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    val appSettingData = SettingDataSingle.getInstance(application.applicationContext)
    private val chatDataBase = ChatGPTDBSingle.getInstance(application.applicationContext)
    private val chatPromptDao = chatDataBase.chatPromptDao()

    val allChatPrompt: LiveData<List<ChatPrompt>> = chatPromptDao.getAllChatPrompts()


}