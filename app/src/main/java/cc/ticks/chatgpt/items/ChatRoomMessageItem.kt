package cc.ticks.chatgpt.items

import android.content.ClipboardManager
import android.content.Context
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat.getSystemService
import cc.ticks.chatgpt.R
import cc.ticks.chatgpt.data.ChatMessage
import cc.ticks.chatgpt.tools.DateTool
import com.google.android.material.textview.MaterialTextView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import java.util.Objects

class ChatRoomMessageItem(
    var msg: ChatMessage,
    val isAiError: Boolean = false
    ) : AbstractItem<ChatRoomMessageItem.ViewHolder>() {

    override val layoutRes: Int
        get() = if ("assistant" == msg.sender) { R.layout.chat_room_detail_list_item_ai }
                else { R.layout.chat_room_detail_list_item_user }

    override val type: Int
        get() = if ("assistant" == msg.sender) { R.id.chatRoomDetailMessageItem_aiLayout }
                else { R.id.chatRoomDetailMessageItem_userLayout }


    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    class ViewHolder(view: View) : FastAdapter.ViewHolder<ChatRoomMessageItem>(view) {
        private var msgContent: MaterialTextView = view.findViewById(R.id.chatRoomDetailMessageItem_msg)
        private var msgDate: MaterialTextView = view.findViewById(R.id.chatRoomDetailMessageItem_msgDate)
        private var msgBox: LinearLayout = view.findViewById(R.id.chatRoomDetailMessageItem_msgBox)

        override fun bindView(item: ChatRoomMessageItem, payloads: List<Any>) {
            msgContent.text = item.msg.content
            msgDate.text = DateTool.getRecentDateString(item.msg.time)
            if (item.isAiError) {
                msgBox.setBackgroundResource(R.drawable.chat_room_detail_ai_emessage_background)
            }
        }

        override fun unbindView(item: ChatRoomMessageItem) {
            msgContent.text = null
            msgDate.text = null
        }
    }

    override fun toString(): String {
        return "ChatRoomMessageItem{" +
                    "msg={" +
                        "messageId='${msg.messageId}'," +
                        "roomId='${msg.roomId}'," +
                        "sender='${msg.sender}'," +
                        "content='${msg.content}'," +
                        "time='${msg.time}'" +
                    "}" +
                "}"
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || javaClass != other.javaClass) return false
        if (this === other) return true
        if (!super.equals(other)) return false
        val it = other as ChatRoomMessageItem
        return msg.messageId == it.msg.messageId
                && msg.sender == it.msg.sender
                && msg.roomId == it.msg.roomId
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), msg.messageId)
    }

}