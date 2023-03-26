package cc.ticks.chatgpt.items

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import cc.ticks.chatgpt.R
import cc.ticks.chatgpt.data.ChatRoom
import cc.ticks.chatgpt.databinding.ChatRoomListItemBinding
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import java.util.*


open class ChatRoomListItem(val chatRoom: ChatRoom) : AbstractBindingItem<ChatRoomListItemBinding>() {

    override val type: Int
        get() = R.id.chatRoomListItem_Layout

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ChatRoomListItemBinding {
        return ChatRoomListItemBinding.inflate(inflater, parent, false)
    }

    @SuppressLint("ResourceAsColor")
    override fun bindView(binding: ChatRoomListItemBinding, payloads: List<Any>) {
        binding.chatRoomListItemTitleView.text = chatRoom.title
        binding.chatRoomListItemPromptView.text = chatRoom.prompt
        binding.chatRoomListItemTitleView.isEnabled = true
        binding.chatRoomListItemPromptView.isEnabled = true
        if (isSelected) {
            binding.chatRoomListItemBackLayout.setBackgroundResource(R.drawable.list_item_radius_background)
        }else {
            binding.chatRoomListItemBackLayout.setBackgroundResource(android.R.color.transparent)
        }
    }

    override fun toString(): String {
        return "ChatRoomListItem{" +
                    "chatRoom={" +
                        "roomId='${chatRoom.roomId}'," +
                        "title='${chatRoom.title}'," +
                        "model='${chatRoom.model}'," +
                        "prompt='${chatRoom.prompt}'" +
                    "}" +
                "}"
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || javaClass != other.javaClass) return false
        if (this === other) return true
        if (!super.equals(other)) return false
        val it = other as ChatRoomListItem
        return chatRoom.roomId == it.chatRoom.roomId
                && chatRoom.title == it.chatRoom.title
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), chatRoom.roomId)
    }

}