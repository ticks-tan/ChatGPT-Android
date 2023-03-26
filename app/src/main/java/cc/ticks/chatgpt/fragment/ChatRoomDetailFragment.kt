package cc.ticks.chatgpt.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import cc.ticks.chatgpt.data.ChatGPTViewModel
import cc.ticks.chatgpt.data.ChatMessage
import cc.ticks.chatgpt.data.ChatRoom
import cc.ticks.chatgpt.databinding.FragmentChatRoomDetailBinding
import cc.ticks.chatgpt.http.ChatGPTRequest
import cc.ticks.chatgpt.items.ChatRoomMessageItem
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import okhttp3.Response

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class ChatRoomDetailFragment : Fragment() {

    private var _binding: FragmentChatRoomDetailBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private val viewModel : ChatGPTViewModel by activityViewModels()
    private val args: ChatRoomListFragmentArgs by navArgs()
    private var chatRoom: ChatRoom? = null

    // 消息列表适配器
    private val messageAdapter = ItemAdapter<ChatRoomMessageItem>()
    private val fastAdapter = FastAdapter.with(messageAdapter)

    // 当前AI消息
    private var currentAiMessage: ChatRoomMessageItem? = null
    // 当前所有消息
    private var allMessages: MutableList<ChatRoomMessageItem>? = null
    // 是否处于请求阶段
    private var isRequesting: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatRoomDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 初始化数据
        initData(savedInstanceState)
        // 初始化视图
        initView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        chatRoom = null
        currentAiMessage = null
        allMessages = null
    }

    override fun onSaveInstanceState(_outState: Bundle) {
        var outState = _outState
        outState = fastAdapter.saveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    private fun initData(savedInstanceState: Bundle?) {
        // 消息数据改变时更新列表
        // viewModel.initAllChatRoomMessagesLiveData(args.roomId)
        // viewModel.initCurrentChatRoomById(args.roomId)
        viewModel.allChatRoomMessages.observe(this@ChatRoomDetailFragment.viewLifecycleOwner) {
            val items = it.map {item ->
                ChatRoomMessageItem(item)
            }
            if (allMessages == null) {
                allMessages = mutableListOf()
                allMessages!!.addAll(items)
                messageAdapter.add(allMessages!!)
                binding.chatRoomDetailRecyclerView.scrollToPosition(fastAdapter.itemCount - 1)
            }else {
                allMessages!!.clear()
                allMessages!!.addAll(items)
            }
        }
        viewModel.currentChatRoom.observe(this@ChatRoomDetailFragment.viewLifecycleOwner) {
            chatRoom = it
        }

        chatRoom = viewModel.currentChatRoom.value
        messageAdapter.add(viewModel.allChatRoomMessages.value?.map {
            ChatRoomMessageItem(it)
        } ?: emptyList())

        fastAdapter.withSavedInstanceState(savedInstanceState)
    }

    private fun initView() {
        binding.chatRoomDetailProgressBar.isVisible = false
        // 初始化列表
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.isSmoothScrollbarEnabled = true
        layoutManager.stackFromEnd = true
        binding.chatRoomDetailRecyclerView.layoutManager = layoutManager
        binding.chatRoomDetailRecyclerView.itemAnimator = null
        binding.chatRoomDetailRecyclerView.adapter = fastAdapter

        // 发送消息
        binding.chatRoomDetailTextInputLayout.setEndIconOnClickListener { _ ->
            if (!isRequesting && viewModel.gptEventSource == null) {
                sendMessage()
            }
        }
    }

    private fun sendMessage() {
        // 获取用户数据并加入数据库，同步更新消息列表
        val content = binding.chatRoomDetailTextInput.text.toString().trim()
        if (content.isEmpty()) {
            return
        }
        binding.chatRoomDetailTextInput.text = null
        val userMsg = ChatMessage.createUserChatMessage(args.roomId, content)
        messageAdapter.add(ChatRoomMessageItem(userMsg))
        viewModel.addChatMessage(userMsg)
        // 发送请求
        if (chatRoom != null) {
            viewModel.sendChatGPTChat(chatRoom!!, userMsg, object : ChatGPTRequest.SendChatCallBack {
                override fun onConnect() {}
                override fun onReceiveMsg(chatMsg: String, exception: java.lang.Exception?) {
                    if (currentAiMessage == null) {
                        // 当前AI消息为空
                        currentAiMessage = ChatRoomMessageItem(
                            ChatMessage.createAIChatMessage(args.roomId, chatMsg)
                        )
                        messageAdapter.add(fastAdapter.itemCount, currentAiMessage!!)
                    }else {
                        currentAiMessage!!.msg.content += chatMsg
                    }
                    if (exception != null) {
                        currentAiMessage!!.msg.content += "\n\nerror: <$exception> "
                    }
                    // 更新消息
                    fastAdapter.notifyItemChanged(fastAdapter.itemCount - 1)
                    scrollToBottom()
                }
                override fun onClose() {
                    if (currentAiMessage != null) {
                        viewModel.addChatMessage(currentAiMessage!!.msg)
                    }
                    viewModel.gptEventSource = null
                    stopSendMessage()
                }
                override fun onError(response: Response?) {
                    Log.e("HttpSSE", "---- 错误: ${response?.message.toString()} ----")
                    messageAdapter.add(fastAdapter.itemCount,
                        ChatRoomMessageItem(
                            ChatMessage.createAIChatMessage(
                                args.roomId,
                                "请求失败，请联系开发我的垃圾工程师-_-"
                            ),
                            true
                        )
                    )
                    viewModel.gptEventSource = null
                    stopSendMessage()
                }
            })
            startSendMessage()
        }else {
            /* TODO : 未知错误 */
            stopSendMessage()
        }
    }

    private fun startSendMessage() {
        isRequesting = true
        binding.chatRoomDetailProgressBar.isVisible = true
        binding.chatRoomDetailTextInputLayout.setEndIconActivated(false)
        // 消息列表滚动到底部
        scrollToBottom()
    }
    private fun stopSendMessage() {
        isRequesting = false
        binding.chatRoomDetailProgressBar.isVisible = false
        binding.chatRoomDetailTextInputLayout.setEndIconActivated(true)
        currentAiMessage = null
        scrollToBottom()
    }
    private fun updateMessageList() {
        if (allMessages != null) {
            FastAdapterDiffUtil.set(messageAdapter, allMessages!!)
        }
    }
    private fun scrollToBottom() {
        val view = binding.chatRoomDetailRecyclerView
        val layoutManager = view.layoutManager as LinearLayoutManager
        val itemCount = layoutManager.itemCount
        if (itemCount != 0) {
            layoutManager.scrollToPositionWithOffset(itemCount - 1, -10)
        }
    }
}