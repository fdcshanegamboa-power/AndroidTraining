package com.learn.androidtraining.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.*


class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val TAG = "AuthRepository"

    suspend fun login(email: String, password: String): Boolean{
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Log.d(TAG, "Login successful for $email")

            true
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "Login failed: ${e.message}")
            false
        }
    }
    suspend fun register(email: String, password: String): Boolean {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            Log.d(TAG, "Registration successful for $email")

            true
        } catch (e: Exception) {
            Log.d(TAG, "Registration failed: ${e.message}")
            false
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUser() = auth.currentUser

}