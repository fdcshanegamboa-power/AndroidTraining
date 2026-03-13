package com.learn.androidtraining

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.*
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.auth.FirebaseAuth
import com.learn.androidtraining.auth.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    // View binding refs
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvError: TextView
    private lateinit var loadingOverlay: FrameLayout
    private lateinit var tvRegister: TextView
    private val viewModel: AuthViewModel = AuthViewModel()

    // Static credentials — swap these out when you have real auth
    private val VALID_USERNAME = "admin"
    private val VALID_PASSWORD = "1234"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        observeLoginState()

        // Wire up views
        etUsername    = findViewById(R.id.etUsername)
        etPassword    = findViewById(R.id.etPassword)
        btnLogin      = findViewById(R.id.btnLogin)
        tvError       = findViewById(R.id.tvError)
        loadingOverlay = findViewById(R.id.loadingOverlay)
        tvRegister = findViewById(R.id.tvRegister)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                showError("Please fill in all fields")
                return@setOnClickListener
            }

            // Kick off the coroutine
            loginUser(username, password)
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun observeLoginState() {
        lifecycleScope.launch {
            // repeatOnLifecycle ensures collection stops when UI is not visible
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginState.collect { success ->
                    if (success == null) return@collect

                    // Hide loading when we get a result
                    showLoading(false)

                    if (success) {
                        goToMain()
                    } else {
                        showError("Invalid username or password")
                    }
                }
            }
        }
    }


    private fun loginUser(username: String, password: String) {
        viewModel.resetRegisterState()
        showLoading(true)
        tvError.visibility = View.GONE
        viewModel.register(username, password)
    }

    override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            goToMain()
        }
    }

    // Runs on Dispatchers.IO
    // When you have real auth (Room or API), replace the body — signature stays the same
//    private suspend fun simulateAuthCheck(username: String, password: String): Boolean {
//        delay(2000) // simulates a 2 second network call
//        return username == VALID_USERNAME && password == VALID_PASSWORD
//    }

    private fun showLoading(isLoading: Boolean) {
        loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !isLoading
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