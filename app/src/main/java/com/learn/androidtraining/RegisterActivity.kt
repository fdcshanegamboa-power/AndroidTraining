package com.learn.androidtraining

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.learn.androidtraining.auth.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvError: TextView
    private lateinit var loadingOverlay: FrameLayout
    private lateinit var tvLogin: TextView

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        observeRegistrationState()

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvError = findViewById(R.id.tvError)
        loadingOverlay = findViewById(R.id.loadingOverlay)
        tvLogin = findViewById(R.id.tvLogin)

        btnRegister.setOnClickListener {

            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirm = etConfirmPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                showError("Please fill in all fields")
                return@setOnClickListener
            }

            if (password != confirm) {
                showError("Passwords do not match")
                return@setOnClickListener
            }

            registerUser(email, password)
        }

        tvLogin.setOnClickListener {
            finish()
        }

    }
    private fun observeRegistrationState() {
        lifecycleScope.launch {
            // Use repeatOnLifecycle to respect lifecycle
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registerState.collect { result ->
                    result?.let {
                        showLoading(false)
                        if (it) {
                            Toast.makeText(this@RegisterActivity, "Account created!", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            showError("Registration failed")
                        }
                    }
                }
            }
        }
    }

    private fun registerUser(username: String, password: String) {
        viewModel.resetRegisterState()
        showLoading(true)
        tvError.visibility = View.GONE

        viewModel.register(username, password)
    }

    private fun showLoading(isLoading: Boolean) {
        loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnRegister.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }
    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}