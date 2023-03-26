package cc.ticks.chatgpt.fragment


import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.activityViewModels
import cc.ticks.chatgpt.data.ChatGPTViewModel
import cc.ticks.chatgpt.data.ChatPrompt
import cc.ticks.chatgpt.data.ChatRoom
import cc.ticks.chatgpt.databinding.FragmentNewChatDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class NewChatBottomSheet : BottomSheetDialogFragment() {

    private var _binding : FragmentNewChatDialogBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private val viewModel: ChatGPTViewModel by activityViewModels()
    // 模型列表
    private val modelList = mutableListOf("gpt-3.5-turbo", "gpt-3.5-turbo-0301")
    // prompt列表
    private val promptList = mutableListOf<ChatPrompt>()

    companion object {
        const val TAG = "NewChatBottomSheet"
        const val DEFAULT_PROMPT = "你是一个有礼貌的人工智能ChatGPT，旨在帮助用户解决他们的问题。"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewChatDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 初始化数据
        initData()
        // 初始化视图
        initView()
    }

    private fun initData() {
        viewModel.allChatPrompt.observe(this.viewLifecycleOwner) {
            promptList.clear()
            promptList.addAll(it)
        }
    }

    private fun initView() {
        // 设置下拉选项
        val modelInput = binding.newChatDialogModelInputLayout.editText as AutoCompleteTextView?
        if (modelList.isNotEmpty()) { modelInput?.setText(modelList[0]) }
        modelInput?.setAdapter(ArrayAdapter(
            this.requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            modelList))
        val promptInput = binding.newChatDialogPromptInputLayout.editText as AutoCompleteTextView?
        if (promptList.isNotEmpty()) { promptInput?.setText(promptList[0].tag) }
        promptInput?.setAdapter(ArrayAdapter(
            this.requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            promptList))
        // 根据布局获取单个下拉选项高度，再设置下拉框高度
        val dropItemHeight = with(TypedValue()) {
            requireContext().theme.resolveAttribute(
                android.R.attr.listPreferredItemHeight,
                this,
                true
            )
            TypedValue.complexToDimensionPixelSize(this.data, requireContext().resources.displayMetrics)
        }
        modelInput?.dropDownHeight = if (modelList.size > 4) {
            dropItemHeight * 4
        } else{
            dropItemHeight * modelList.size
        }
        promptInput?.dropDownHeight = if (promptList.size > 4) {
            dropItemHeight * 4
        } else{
            dropItemHeight * promptList.size
        }

        // 取消按钮点击事件
        binding.newChatDialogCancelBtn.setOnClickListener {
            this.dismiss()
        }
        // 开始按钮点击事件
        binding.newChatDialogStartBtn.setOnClickListener {
            checkAndGotoChatDetail()
        }
    }

    private fun checkAndGotoChatDetail() {
        val titleInput = binding.newChatDialogTitleInputLayout.editText
        val modelInput = binding.newChatDialogModelInputLayout.editText
        val promptInput = binding.newChatDialogPromptInputLayout.editText
        val title = titleInput?.text.toString()
        val model = modelInput?.text.toString()
        var prompt = promptInput?.text.toString()
        if (title.isEmpty()) {
            binding.newChatDialogTitleInputLayout.error = "标题不能为空"
            return
        }else {
            binding.newChatDialogTitleInputLayout.error = null
        }
        if (prompt.isEmpty()) {
            prompt = DEFAULT_PROMPT
        }
        if (model.isEmpty()) {
            binding.newChatDialogModelInputLayout.error = "模型不能为空"
            return
        }else {
            binding.newChatDialogModelInputLayout.error = null
        }
        // 添加聊天室
        viewModel.addChatRoom(ChatRoom.createChatRoom(title, model, prompt))
        // 关闭弹窗
        this.dismiss()
        /* TODO : 跳转到另外一个界面 */
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}