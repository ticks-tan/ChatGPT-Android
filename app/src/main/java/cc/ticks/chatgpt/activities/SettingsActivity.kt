package cc.ticks.chatgpt.activities

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import cc.ticks.chatgpt.data.SettingsViewModel
import cc.ticks.chatgpt.databinding.ActivitySettingsBinding
import com.zackratos.ultimatebarx.ultimatebarx.java.UltimateBarX

class SettingsActivity : AppCompatActivity() {

    private var _binding : ActivitySettingsBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private val viewMode : SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        // 初始化状态栏
        initStatusBar()

        // 绑定UI
        _binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initData()
        initView()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun initData() {

    }

    private fun initView() {

    }

    // 初始化设置状态栏
    private fun initStatusBar() {
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                // 夜间模式
                UltimateBarX.statusBar(this)
                    .fitWindow(false)
                    .color(Color.TRANSPARENT)
                    .light(false)
                    .lvlColor(Color.TRANSPARENT)
                    .apply()
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                // 日间模式
                UltimateBarX.statusBar(this)
                    .fitWindow(false)
                    .color(Color.TRANSPARENT)
                    .light(true)
                    .lvlColor(Color.TRANSPARENT)
                    .apply()
            }
        }
    }

}