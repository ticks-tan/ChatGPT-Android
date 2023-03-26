package cc.ticks.chatgpt.fragment

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import cc.ticks.chatgpt.R
import cc.ticks.chatgpt.data.ChatGPTViewModel
import cc.ticks.chatgpt.databinding.FragmentChatRoomBinding
import cc.ticks.chatgpt.items.ChatRoomListItem
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import com.mikepenz.fastadapter.helpers.ActionModeHelper
import com.mikepenz.fastadapter.helpers.UndoHelper
import com.mikepenz.fastadapter.select.SelectExtension
import com.mikepenz.fastadapter.select.getSelectExtension

class ChatRoomListFragment : Fragment() {

    private var _binding: FragmentChatRoomBinding? = null
    private val binding get() = _binding!!

    // ViewMode
    private val viewModel: ChatGPTViewModel by activityViewModels()
    // 底部弹出框
    private var newChatBottomSheet: NewChatBottomSheet? = null

    // 列表适配器
    private val roomItemAdapter = ItemAdapter<ChatRoomListItem>()
    private val fastAdapter = FastAdapter.with(roomItemAdapter)

    // actionMode相关
    private lateinit var actionModeHelper: ActionModeHelper<ChatRoomListItem>
    private lateinit var selected : SelectExtension<ChatRoomListItem>
    // undo 工具
    private lateinit var undoHelper: UndoHelper<ChatRoomListItem>
    // actionMode改变回调
    private var onActionChangeListener : OnActionModeChangeListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatRoomBinding.inflate(inflater, container, false)
        // 初始化底部弹窗
        newChatBottomSheet = NewChatBottomSheet()
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
        // 进行资源销毁
        super.onDestroyView()
        _binding = null
        newChatBottomSheet = null
        onActionChangeListener = null
    }

    override fun onSaveInstanceState(_outState: Bundle) {
        // 保存数据和状态
        var outState = _outState
        outState = fastAdapter.saveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    // 初始化数据
    private fun initData(savedInstanceState: Bundle?) {
        // 聊天室列表改变后更新列表
        viewModel.allChatRooms.observe(this@ChatRoomListFragment.viewLifecycleOwner) { rooms ->
            /* TODO: 更新列表 */
            val items = rooms.map {
                ChatRoomListItem(it)
            }
            // 使用Diff工具更新不同项目
            FastAdapterDiffUtil[roomItemAdapter] = items
        }

        // 设置列表数据
        roomItemAdapter.add(viewModel.allChatRooms.value?.map {
            ChatRoomListItem(it)
        } ?: emptyList())

        fastAdapter.withSavedInstanceState(savedInstanceState)
        // 设置列表选择
        selected = fastAdapter.getSelectExtension().apply {
            isSelectable = true
            multiSelect = true
            selectOnLongClick = false
        }
//        selectExtension = fastAdapter.getSelectExtension()
//        selectExtension.apply {
//            isSelectable = true
//            multiSelect = true
//        }
        actionModeHelper = ActionModeHelper(
            fastAdapter,
            R.menu.menu_chat_room_action_mode,
            RoomActionCallBack()
        )
        undoHelper = UndoHelper(fastAdapter, object : UndoHelper.UndoListener<ChatRoomListItem> {
            override fun commitRemove(
                positions: Set<Int>,
                removed: ArrayList<FastAdapter.RelativeInfo<ChatRoomListItem>>
            ) {
                removed.forEach {
                    if (it.item != null) {
                        viewModel.removeChatRoom(it.item!!.chatRoom)
                    }
                }
            }
        })
    }

    private fun initView() {
        // 设置列表
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.isSmoothScrollbarEnabled = true
        binding.chatRoomRecyclerView.layoutManager = layoutManager
        binding.chatRoomRecyclerView.itemAnimator = null
        binding.chatRoomRecyclerView.adapter = fastAdapter

        fastAdapter.onPreClickListener = { _: View?, _: IAdapter<ChatRoomListItem>, item: ChatRoomListItem, _: Int ->
            val res = actionModeHelper.onClick(requireActivity() as AppCompatActivity, item)
            res ?: false
        }
        fastAdapter.onClickListener = { v: View?, _: IAdapter<ChatRoomListItem>, item: ChatRoomListItem, pos: Int ->
            if (actionModeHelper.actionMode != null && selected.selectedItems.isNotEmpty()) {
                if (item.isSelected) {
                    selected.select(pos)
                }else {
                    selected.deselect(item, pos)
                }
                actionModeHelper.actionMode!!.invalidate()
            }else if (v != null) {
                /* TODO 跳转 */
                viewModel.initAllChatRoomMessagesLiveData(item.chatRoom.roomId)
                viewModel.initCurrentChatRoomById(item.chatRoom.roomId)
                val bundle = ChatRoomListFragmentArgs(item.chatRoom.roomId).toBundle()
                findNavController().navigate(R.id.action_ChatRoomList_to_ChatRoomDetail, bundle)
            }
            false
        }
        fastAdapter.onPreLongClickListener = { _: View, _: IAdapter<ChatRoomListItem>, _: ChatRoomListItem, pos: Int ->
            val actionMode = actionModeHelper.onLongClick(this@ChatRoomListFragment.requireActivity() as AppCompatActivity, pos)
            if (actionMode != null) {
                actionModeHelper.actionMode?.invalidate()
            }
            actionMode != null
        }

        // 新建聊天点击回调
        binding.chatRoomActionButton.setOnClickListener {
            if (selected.selectedItems.isNotEmpty()) {
                selected.deselect()
                actionModeHelper.checkActionMode(this@ChatRoomListFragment.requireActivity() as AppCompatActivity)
            }
            newChatBottomSheet?.show(childFragmentManager, NewChatBottomSheet.TAG)
        }
    }

    interface OnActionModeChangeListener{
        fun onCreateActionMode()
        fun onDestroyActionMode()
    }

    internal inner class RoomActionCallBack: androidx.appcompat.view.ActionMode.Callback {
        override fun onCreateActionMode(action: ActionMode, menu: Menu): Boolean {
            this@ChatRoomListFragment.onActionChangeListener?.onCreateActionMode()
            return true
        }
        override fun onPrepareActionMode(action: ActionMode, menu: Menu): Boolean {
            menu.findItem(R.id.action_rename)?.isVisible = (
                    fastAdapter.getSelectExtension().selectedItems.size == 1
                    )
            action.title = "已选择 ${selected.selectedItems.size} 项"
            return true
        }
        override fun onActionItemClicked(action: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.action_rename -> {
                    Toast.makeText(
                        this@ChatRoomListFragment.requireContext(),
                        "重命名",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                R.id.action_delete -> {
                    // 处理删除事件
                    undoHelper.remove(
                        binding.chatRoomRecyclerView, "选中项已删除", "取消",
                            Snackbar.LENGTH_LONG, selected.selections)
                        .addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                super.onDismissed(transientBottomBar, event)
                                if (event != Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                                    selected.deselect()
                                }
                            }
                        })
                    actionModeHelper.checkActionMode(this@ChatRoomListFragment.requireActivity() as AppCompatActivity)
                }
            }
            return true
        }
        override fun onDestroyActionMode(action: ActionMode) {
            this@ChatRoomListFragment.onActionChangeListener?.onDestroyActionMode()
            selected.deselect()
        }
    }
}