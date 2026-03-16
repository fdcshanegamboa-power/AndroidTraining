package com.learn.androidtraining

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.learn.androidtraining.fragments.*
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.learn.androidtraining.databinding.ActivityMainBinding
import com.learn.androidtraining.fragments.home.HomeFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var homeFragment: HomeFragment
    private lateinit var profileFragment: ProfileFragment
    private lateinit var settingsFragment: SettingsFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if (savedInstanceState == null) {
            homeFragment = HomeFragment()
            profileFragment = ProfileFragment()
            settingsFragment = SettingsFragment()
            supportFragmentManager.beginTransaction()
                .add(binding.fragmentContainer.id, homeFragment, "HOME")
                .add(binding.fragmentContainer.id, profileFragment, "PROFILE").hide(profileFragment)
                .add(binding.fragmentContainer.id, settingsFragment, "SETTINGS").hide(settingsFragment)
                .commit()
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
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
}
