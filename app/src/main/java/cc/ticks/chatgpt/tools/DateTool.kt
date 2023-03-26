package cc.ticks.chatgpt.tools

import java.text.SimpleDateFormat
import java.util.*

class DateTool {

    companion object {

        fun getRecentDateString(timestamp: Long) : String {
            val date = Date(timestamp)

            val currentTime = System.currentTimeMillis()
            val timeDiff = currentTime - timestamp

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timestamp

            return if (timeDiff < 60 * 1000) {
                "刚刚"
            } else if (timeDiff < 60 * 60 * 1000) {
                val minutes = timeDiff / (60 * 1000)
                "$minutes 分钟前"
            } else if (timeDiff < 24 * 60 * 60 * 1000) {
                val hours = timeDiff / (60 * 60 * 1000)
                "$hours 小时前"
            } else if (timeDiff < 3 * 24 * 60 * 60 * 1000) {
                val days = timeDiff / (24 * 60 * 60 * 1000)
                "$days 天前"
            } else {
                val format = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
                format.format(date)
            }
        }
    }
}