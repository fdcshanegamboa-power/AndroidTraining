package com.learn.androidtraining.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _loginState = MutableStateFlow<Boolean?>(null)
    val loginState: StateFlow<Boolean?> = _loginState
    private val _registerState = MutableStateFlow<Boolean?>(null)
    val registerState: StateFlow<Boolean?> = _registerState
    fun login(email: String, password: String) {
        viewModelScope.launch {

            val result = repository.login(email, password)

            _loginState.value = result
        }
    }
    fun register(email: String, password: String) {
        viewModelScope.launch {

            val result = repository.register(email, password)

            _registerState.value = result
        }
    }
    fun resetRegisterState() {
        _registerState.value = null
    }

     fun logout() {
        repository.logout()
    }

     fun getCurrentUser() = repository.getCurrentUser()
}