package com.example.fittrackmobile.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.fittrackmobile.fragments.ProfileFragment
import com.example.fittrackmobile.fragments.SettingsFragment
import com.example.fittrackmobile.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileActivity : AppCompatActivity() {

    // XML variables.
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var profileFragment: ProfileFragment
    private lateinit var settingsFragment: SettingsFragment

    // Class variables.
    private val TAG = "ProfileActivity"
    private lateinit var userID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initializeObjects()

        userID = intent.getStringExtra("userid") as String

        // Change to the correct fragment.
        when (intent.getStringExtra("fragment")) {
            "profile" -> {
                val bundle = Bundle()
                bundle.putString("userid", userID)
                profileFragment.arguments = bundle
                changeFragment(profileFragment)
            }
            "settings" -> {
                changeFragment(settingsFragment)
            }
        }

        // Set the functionality for the bottom navigation view buttons.
        bottomNavigationView.setOnNavigationItemSelectedListener(
            object : BottomNavigationView.OnNavigationItemSelectedListener {
                override fun onNavigationItemSelected(item: MenuItem): Boolean {
                    when (item.itemId) {
                        R.id.profile_page_item_profile -> {
                            changeFragment(profileFragment)
                            return true
                        }
                        R.id.profile_page_item_settings -> {
                            changeFragment(settingsFragment)
                            return true
                        }
                    }

                    return false
                }
            })

        // Set the tool bar.
        setSupportActionBar(findViewById(R.id.profile_page_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_profile_page)
    }

    /**
     * Method to change the view from the current fragment to the given fragment.
     *
     * @param fragment The new fragment to switch the view to.
     */
    private fun changeFragment(fragment: Fragment) {
        val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        val arguments = Bundle()
        arguments.putString("userid", userID)
        fragment.arguments = arguments
        fragmentTransaction.replace(R.id.profile_page_container, fragment)
        fragmentTransaction.commit()
    }

    /**
     * Method to initialize class objects and attach Java/Kotlin objects to XML.
     */
    private fun initializeObjects() {
        try {
            bottomNavigationView = findViewById(R.id.profile_page_bottomNV)
            profileFragment = ProfileFragment()
            settingsFragment = SettingsFragment()
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }
}
