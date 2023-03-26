package cc.ticks.chatgpt.activities

import android.Manifest
import android.app.ActivityManager
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import cc.ticks.chatgpt.R
import cc.ticks.chatgpt.data.ChatGPTViewModel
import cc.ticks.chatgpt.databinding.ActivityMainBinding
import cc.ticks.chatgpt.fragment.ChatRoomListFragment
import com.zackratos.ultimatebarx.ultimatebarx.java.UltimateBarX

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    // ViewMode
    private val viewModel : ChatGPTViewModel by viewModels()

    companion object {
        const val NET_PERMISSION_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        // 初始化状态栏
        initStatusBar()

        // 绑定UI
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置工具栏
        setSupportActionBar(binding.topAppBar)

        // 设置导航
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        // 请求权限
        requestPermission()
    }

    // 创建工具栏菜单
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // 工具栏菜单点击事件
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // 在夜间模式改变后修改状态栏
        initStatusBar()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            NET_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    /* TODO : 权限请求成功 */
                }else {
                    Toast.makeText(this, "网络权限获取失败，应用可能无法正常运行！", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
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
    // 请求权限
    private fun requestPermission() {
        if (
            ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
            != PackageManager.PERMISSION_GRANTED
        )
        {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.INTERNET),
                NET_PERMISSION_CODE
            )
        }
    }

}