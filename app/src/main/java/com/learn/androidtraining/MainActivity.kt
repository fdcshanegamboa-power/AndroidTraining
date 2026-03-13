package com.learn.androidtraining

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.learn.androidtraining.fragments.*
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    private val homeFragment = HomeFragment()
    private val profileFragment = ProfileFragment()
    private val settingsFragment = SettingsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottom_nav)


        if (savedInstanceState == null) {
                supportFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, homeFragment, "HOME")
                    .add(R.id.fragment_container, profileFragment, "PROFILE").hide(profileFragment)
                    .add(R.id.fragment_container, settingsFragment, "SETTINGS").hide(settingsFragment)
                    .commit()
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> showFragment(homeFragment)
                R.id.nav_profile -> showFragment(profileFragment)
                R.id.nav_settings -> showFragment(settingsFragment)
                else -> false
            }
            true
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val homeContainer = supportFragmentManager.findFragmentByTag("HOME") as? HomeFragment
                val homeChildManager = homeContainer?.childFragmentManager

                if (homeChildManager != null && homeChildManager.backStackEntryCount > 0) {
                    homeChildManager.popBackStack()
                } else {
                    android.app.AlertDialog.Builder(this@MainActivity)
                        .setTitle("Exit App")
                        .setMessage("Are you sure you want to exit?")
                        .setPositiveButton("Yes") { _, _ ->
                            isEnabled = false
                            onBackPressedDispatcher.onBackPressed()
                        }
                        .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                        .show()
                }
            }
        })

    }

    private fun showFragment(target: Fragment) {
        supportFragmentManager.beginTransaction()
            .hide(settingsFragment)
            .hide(profileFragment)
            .hide(homeFragment)
            .show(target)
            .commit()
    }

//    private fun showTab(containerId: Int) {
//        listOf(R.id.container_home, R.id.container_profile, R.id.container_settings).forEach {
//            findViewById<FrameLayout>(it).visibility =
//                if (it == containerId) View.VISIBLE else View.GONE
//        }
//    }
//    override fun onBackPressed() {
//        val homeContainer = supportFragmentManager.findFragmentByTag("HOME") as? HomeFragment
//        val homeChildManager = homeContainer?.childFragmentManager
//
//        if (homeChildManager != null && homeChildManager.backStackEntryCount > 0) {
//            homeChildManager.popBackStack()
//        } else {
//            super.onBackPressed()
//        }
//    }


}

//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    AndroidTrainingTheme {
//        Greeting("Android")
//    }
//}