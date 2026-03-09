package com.youxam.ucloudhomework

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.youxam.ucloudhomework.databinding.ActivityLoginBinding
import com.youxam.ucloudhomework.viewmodel.LoginState
import com.youxam.ucloudhomework.viewmodel.LoginViewModel
import kotlinx.coroutines.launch

/**
 * 登录页面
 */
class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    
    private lateinit var tilStudentId: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etStudentId: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var cbRememberMe: MaterialCheckBox
    private lateinit var btnLogin: MaterialButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initViews()
        setupObservers()
        loadSavedCredentials()
    }
    
    private fun initViews() {
        tilStudentId = binding.tilStudentId
        tilPassword = binding.tilPassword
        etStudentId = binding.etStudentId
        etPassword = binding.etPassword
        cbRememberMe = binding.cbRememberMe
        btnLogin = binding.btnLogin
        
        // 输入框文本变化监听
        etStudentId.addTextChangedListener {
            tilStudentId.error = null
        }
        
        etPassword.addTextChangedListener {
            tilPassword.error = null
        }
        
        // 登录按钮点击
        btnLogin.setOnClickListener {
            attemptLogin()
        }
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginState.collect { state ->
                    when (state) {
                        is LoginState.Idle -> {
                            showLoading(false)
                        }
                        is LoginState.Loading -> {
                            showLoading(true)
                        }
                        is LoginState.Success -> {
                            showLoading(false)
                            Toast.makeText(this@LoginActivity, R.string.login_success, Toast.LENGTH_SHORT).show()
                            // 返回主页面
                            setResult(RESULT_OK)
                            finish()
                        }
                        is LoginState.Error -> {
                            showLoading(false)
                            showError(state.message)
                        }
                    }
                }
            }
        }
        
        // 观察保存的学号
        lifecycleScope.launch {
            viewModel.savedStudentId.collect { studentId ->
                if (!studentId.isNullOrBlank()) {
                    etStudentId.setText(studentId)
                }
            }
        }
        
        // 观察记住账号选项
        lifecycleScope.launch {
            viewModel.rememberMe.collect { rememberMe ->
                cbRememberMe.isChecked = rememberMe
            }
        }
    }
    
    private fun loadSavedCredentials() {
        // ViewModel 会自动加载保存的凭证
    }
    
    private fun attemptLogin() {
        val studentId = etStudentId.text.toString().trim()
        val password = etPassword.text.toString()
        val rememberMe = cbRememberMe.isChecked
        
        // 验证输入
        var hasError = false
        
        if (studentId.isBlank()) {
            tilStudentId.error = getString(R.string.login_empty_fields)
            hasError = true
        }
        
        if (password.isBlank()) {
            tilPassword.error = getString(R.string.login_empty_fields)
            hasError = true
        }
        
        if (hasError) return
        
        // 执行登录
        viewModel.login(studentId, password, rememberMe)
    }
    
    private fun showLoading(show: Boolean) {
        binding.loadingLayout.visibility = if (show) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !show
        btnLogin.text = if (show) getString(R.string.login_ing) else getString(R.string.login_button)
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, getString(R.string.login_failed, message), Toast.LENGTH_LONG).show()
    }
    
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, LoginActivity::class.java)
            context.startActivity(intent)
        }
    }
}
