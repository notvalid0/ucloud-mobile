package com.youxam.ucloudhomework

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.youxam.ucloudhomework.adapter.HomeworkAdapter
import com.youxam.ucloudhomework.databinding.ActivityMainBinding
import com.youxam.ucloudhomework.model.HomeworkItem
import com.youxam.ucloudhomework.viewmodel.HomeworkState
import com.youxam.ucloudhomework.viewmodel.MainViewModel
import kotlinx.coroutines.launch

/**
 * 主页面 - 显示未完成作业列表
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: HomeworkAdapter
    
    // FAB菜单是否展开
    private var isFabMenuOpen = false
    
    private val loginLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.loadHomeworkList()
        } else {
            finish()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initViews()
        setupObservers()
        checkLoginAndLoad()
    }
    
    private fun initViews() {
        // 设置 RecyclerView
        adapter = HomeworkAdapter { item ->
            onHomeworkItemClick(item)
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
            setHasFixedSize(true)
        }
        
        // 设置下拉刷新
        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.primary,
            R.color.secondary
        )
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }
        
        // 重试按钮
        binding.btnRetry.setOnClickListener {
            viewModel.loadHomeworkList(forceRefresh = true)
        }
        
        // FAB菜单按钮
        binding.fabMenu.setOnClickListener {
            toggleFabMenu()
        }
        
        // FAB刷新按钮
        binding.fabRefresh.setOnClickListener {
            viewModel.refresh()
            closeFabMenu()
        }
        
        // FAB退出按钮
        binding.fabLogout.setOnClickListener {
            showLogoutConfirmDialog()
            closeFabMenu()
        }
    }
    
    private fun toggleFabMenu() {
        if (isFabMenuOpen) {
            closeFabMenu()
        } else {
            openFabMenu()
        }
    }
    
    private fun openFabMenu() {
        isFabMenuOpen = true
        
        binding.fabMenu.animate()
            .rotation(45f)
            .setDuration(200)
            .start()
        
        binding.fabRefresh.visibility = View.VISIBLE
        binding.fabRefresh.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(200)
            .start()
        
        binding.fabLogout.visibility = View.VISIBLE
        binding.fabLogout.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(200)
            .start()
    }
    
    private fun closeFabMenu() {
        isFabMenuOpen = false
        
        binding.fabMenu.animate()
            .rotation(0f)
            .setDuration(200)
            .start()
        
        binding.fabRefresh.animate()
            .translationY(40f)
            .alpha(0f)
            .setDuration(150)
            .withEndAction {
                binding.fabRefresh.visibility = View.GONE
            }
            .start()
        
        binding.fabLogout.animate()
            .translationY(80f)
            .alpha(0f)
            .setDuration(150)
            .withEndAction {
                binding.fabLogout.visibility = View.GONE
            }
            .start()
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.homeworkState.collect { state ->
                    when (state) {
                        is HomeworkState.Idle -> {
                            showContent(false)
                            showLoading(false)
                            showError(false)
                        }
                        is HomeworkState.Loading -> {
                            showLoading(true)
                            showError(false)
                        }
                        is HomeworkState.Success -> {
                            showContent(true)
                            showLoading(false)
                            showError(false)
                        }
                        is HomeworkState.Empty -> {
                            showEmpty(true)
                            showLoading(false)
                            showError(false)
                        }
                        is HomeworkState.Error -> {
                            showError(true, state.message)
                            showLoading(false)
                        }
                    }
                }
            }
        }
        
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.homeworkList.collect { list ->
                    adapter.submitList(list)
                }
            }
        }
        
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isRefreshing.collect { isRefreshing ->
                    binding.swipeRefreshLayout.isRefreshing = isRefreshing
                }
            }
        }
    }
    
    private fun checkLoginAndLoad() {
        if (viewModel.checkLoginStatus()) {
            viewModel.loadHomeworkList()
        } else {
            val intent = Intent(this, LoginActivity::class.java)
            loginLauncher.launch(intent)
        }
    }
    
    private fun onHomeworkItemClick(item: HomeworkItem) {
        val intent = Intent(this, HomeworkDetailActivity::class.java).apply {
            putExtra(HomeworkDetailActivity.EXTRA_ACTIVITY_ID, item.activityId)
            putExtra(HomeworkDetailActivity.EXTRA_TITLE, item.getTitle())
            putExtra(HomeworkDetailActivity.EXTRA_COURSE_NAME, item.getCourseName())
        }
        startActivity(intent)
    }
    
    private fun showLogoutConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.main_logout)
            .setMessage(R.string.main_logout_confirm)
            .setPositiveButton(R.string.ok) { _, _ ->
                performLogout()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun performLogout() {
        viewModel.logout()
        val intent = Intent(this, LoginActivity::class.java)
        loginLauncher.launch(intent)
    }
    
    private fun showContent(show: Boolean) {
        binding.recyclerView.isVisible = show
        binding.emptyView.isVisible = false
    }
    
    private fun showEmpty(show: Boolean) {
        binding.recyclerView.isVisible = !show
        binding.emptyView.isVisible = show
    }
    
    private fun showLoading(show: Boolean) {
        binding.loadingView.isVisible = show
        binding.recyclerView.isVisible = !show
        binding.emptyView.isVisible = false
        binding.errorView.isVisible = false
    }
    
    private fun showError(show: Boolean, message: String? = null) {
        binding.errorView.isVisible = show
        binding.recyclerView.isVisible = !show
        binding.emptyView.isVisible = false
        
        if (show && message != null) {
            binding.tvErrorMessage.text = message
        }
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (isFabMenuOpen) {
            closeFabMenu()
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }
}
