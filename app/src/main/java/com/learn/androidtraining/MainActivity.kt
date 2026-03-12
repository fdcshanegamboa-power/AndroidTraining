package com.learn.androidtraining

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.learn.androidtraining.fragments.*
import com.learn.androidtraining.ui.theme.AndroidTrainingTheme

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    // Keep references to fragments
    private val homeFragment = HomeFragment()
    private val profileFragment = ProfileFragment()
    private val settingsFragment = SettingsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottom_nav)

        // Initial add - add all fragments but hide all except Home
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, homeFragment, "HOME")
            .add(R.id.fragment_container, profileFragment, "PROFILE").hide(profileFragment)
            .add(R.id.fragment_container, settingsFragment, "SETTINGS").hide(settingsFragment)
            .commit()

        // Handle bottom nav clicks
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> showFragment(homeFragment)
                R.id.nav_profile -> showFragment(profileFragment)
                R.id.nav_settings -> showFragment(settingsFragment)
                else -> false
            }
        }
    }

    private fun showFragment(fragmentToShow: Fragment): Boolean {
        val transaction = supportFragmentManager.beginTransaction()

        // Hide all fragments first
        listOf(homeFragment, profileFragment, settingsFragment).forEach {
            if (it.isAdded && it != fragmentToShow) {
                transaction.hide(it)
            }
        }

        // Show the selected fragment
        transaction.show(fragmentToShow).commit()

        return true
    }

    // Example of remove (not used in bottom nav)
    private fun removeFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .remove(fragment)
            .commit()
    }
}

//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    AndroidTrainingTheme {
//        Greeting("Android")
//    }
//}