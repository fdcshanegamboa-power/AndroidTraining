package com.learn.androidtraining

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.learn.androidtraining.firebase.auth.AuthViewModel
import com.learn.androidtraining.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {



    private val viewModel: AuthViewModel by viewModels()
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpClickListeners()
        observeRegistrationState()
    }
    private fun setUpClickListeners(){
        binding.buttonRegister.setOnClickListener {

            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            val confirm = binding.editTextConfirmPassword.text.toString().trim()

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

        binding.textViewLogin.setOnClickListener {
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
        binding.textViewError.visibility = View.GONE

        viewModel.register(username, password)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonRegister.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        binding.textViewError.text = message
        binding.textViewError.visibility = View.VISIBLE
    }
    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}