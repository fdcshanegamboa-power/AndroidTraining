package com.learn.androidtraining

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.auth.FirebaseAuth
import com.learn.androidtraining.firebase.auth.AuthViewModel
import com.learn.androidtraining.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    // View binding refs
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: AuthViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = AuthViewModel()
        observeLoginState()
        setUpClickListeners()
    }

    private fun setUpClickListeners() {
        binding.buttonLogin.setOnClickListener {
            val username = binding.editTextUsername.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                showError("Please fill in all fields")
                return@setOnClickListener
            }

            loginUser(username, password)
        }

        binding.textViewRegister.setOnClickListener {
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
        binding.textViewError.visibility = View.GONE
        viewModel.login(username, password)
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
        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonLogin.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        binding.textViewError.text = message
        binding.textViewError.visibility = View.VISIBLE
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("welcome_message", "Welcome back, ${FirebaseAuth.getInstance().currentUser?.email ?: "User"}!")
        startActivity(intent)
        finish()
    }
}