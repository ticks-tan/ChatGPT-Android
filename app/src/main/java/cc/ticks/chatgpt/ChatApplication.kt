package cc.ticks.chatgpt

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.core.os.HandlerCompat
import com.google.android.material.color.DynamicColors
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ChatApplication : Application() {
    val executorService: ExecutorService = Executors.newFixedThreadPool(2)
    val mainThreadHandler: Handler = HandlerCompat.createAsync(Looper.getMainLooper())

    companion object {
        lateinit var instance: ChatApplication
        private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}