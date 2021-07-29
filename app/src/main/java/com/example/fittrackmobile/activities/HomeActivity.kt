package com.example.fittrackmobile.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.MenuItemCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.fittrackmobile.fragments.FavouritesFragment
import com.example.fittrackmobile.fragments.ImageStatusesFragment
import com.example.fittrackmobile.fragments.TextStatusesFragment
import com.example.fittrackmobile.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import de.hdodenhof.circleimageview.CircleImageView
import java.util.ArrayList

/**
 * Main home page activity.
 */
class HomeActivity: AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    // XML variables.
    private lateinit var toolbar: Toolbar
    private lateinit var navigationView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var headerProfilePicIV: CircleImageView
    private lateinit var headerUserNameTV: TextView
    private lateinit var headerUserEmailTV: TextView
    private lateinit var headerProgressBar: ProgressBar
    private lateinit var fragmentsBNV: BottomNavigationView
    private lateinit var appBNV: BottomNavigationView
    private lateinit var updateStatusFAB: FloatingActionButton
    private lateinit var notificationTV: TextView

    // Firebase variables.
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var documentReference: DocumentReference
    private lateinit var collectionReference: CollectionReference
    private lateinit var notificationFirebaseFirestore: FirebaseFirestore
    private lateinit var listenerRegistration: ListenerRegistration

    // Fragment objects.
    private lateinit var textStatusesFragment: TextStatusesFragment
    private lateinit var imageStatusesFragment: ImageStatusesFragment
    private lateinit var favouritesFragment: FavouritesFragment

    // Class variables.
    private lateinit var currentUserEmail: String
    var TAG: String = "HomeActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get instances for Firebase variables.
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        notificationFirebaseFirestore = FirebaseFirestore.getInstance()
        collectionReference = notificationFirebaseFirestore.collection("Users")
            .document(firebaseAuth.currentUser?.email.toString())
            .collection("Notifications")

        // Initialize fragment objects.
        textStatusesFragment = TextStatusesFragment()
        imageStatusesFragment = ImageStatusesFragment()
        favouritesFragment = FavouritesFragment()

        // Set the default fragment and attach Java/Kotlin objects to XML variables.
        changeFragment(textStatusesFragment)
        attachJavaToXML()

        // Inflate the toolbar menu and set the OnClickListener for the menu items.
        toolbar.inflateMenu(R.menu.main_content_menu_bar)
        toolbar.setOnMenuItemClickListener(object : Toolbar.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem): Boolean {
                when (item.itemId) {
                    // Open the notifications activity.
                    R.id.mainContentMenuBar_item_notificationsIcon -> {
                        val intent = Intent(this@HomeActivity,
                            NotificationsActivity::class.java)
                        startActivity(intent)
                        return true
                    }
                }
                return false
            }
        })
        setUpDrawerMenu()
        getCurrentUserDetails()
        navigationView.setNavigationItemSelectedListener(this)

        // Set the listener for the bottom navigation bar.
        fragmentsBNV.setOnNavigationItemSelectedListener(object : BottomNavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                try {
                    when (item.itemId) {
                        R.id.item_textStatuses -> {
                            changeFragment(textStatusesFragment)
                            return true
                        }
                        R.id.item_imageStatuses -> {
                            changeFragment(imageStatusesFragment)
                            return true
                        }
                        R.id.item_favourites -> {
                            changeFragment(favouritesFragment)
                            return true
                        }
                        else -> {
                            return false
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "" + e.message)
                }
                return false
            }
        })

        // Set the listener for the main app navigation bar.
        appBNV.setOnNavigationItemSelectedListener { item ->
            try {
                when (item.itemId) {
                    R.id.app_menu_item_home -> {
                        val intent = Intent(
                            this@HomeActivity, HomeActivity::class.java)
                        startActivity(intent)
                    }
                    R.id.app_menu_item_diary -> {
                        val intent = Intent(
                            this@HomeActivity, DiaryActivity::class.java)
                        startActivity(intent)
                    }
                    R.id.app_menu_item_progress -> {
                        val intent = Intent(
                            this@HomeActivity, ProgressActivity::class.java)
                        startActivity(intent)
                    }
                    R.id.app_menu_item_settings -> {
                        val intent = Intent(
                            this@HomeActivity, SettingsActivity::class.java)
                        startActivity(intent)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "" + e.message)
            }
            false
        }

        // Set the listener for the update status floating action button.
        updateStatusFAB.setOnClickListener {
            val intent = Intent(this@HomeActivity, AddStatusesActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Change the current fragment displaying statuses.
     *
     * @param fragment The new fragment to display.
     */
    private fun changeFragment(fragment: Fragment) {
        try {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.container, fragment)
            fragmentTransaction.commit()
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }

    /**
     * Attach the Java/Kotlin objects to their XML counterparts.
     */
    private fun attachJavaToXML() {
        try {
            toolbar = findViewById(R.id.friends_page_toolbar)
            drawerLayout = findViewById(R.id.drawerLayout)
            navigationView = findViewById(R.id.main_page_navigationView)
            val headerXMLFile = navigationView.getHeaderView(0)
            headerProfilePicIV = headerXMLFile.findViewById(R.id.header_profilePic)
            headerUserNameTV = headerXMLFile.findViewById(R.id.header_userName_tv)
            headerUserEmailTV = headerXMLFile.findViewById(R.id.header_userEmail_tv)
            headerProgressBar = headerXMLFile.findViewById(R.id.header_progressBar)
            fragmentsBNV = findViewById(R.id.main_page_fragmentsBNV)
            appBNV = findViewById(R.id.main_page_mainBNV)
            updateStatusFAB = findViewById(R.id.main_page_addStatus_floatingBtn)
            notificationTV = MenuItemCompat.getActionView(navigationView.menu.findItem(
                R.id.item_notifications
            )) as TextView


        } catch (e: Exception) {
            Log.e(TAG, "HomeActivity:" + e.message)
        }
    }

    /**
     * Gets the current user details and updates the UI accordingly.
     */
    private fun getCurrentUserDetails() {
        try {
            currentUserEmail = getCurrentLoggedInUser()

            if(currentUserEmail == resources.getString(R.string.nobody_logged_in_msg)) {
                Toast.makeText(this, resources.getString(R.string.nobody_logged_in_msg),
                    Toast.LENGTH_SHORT).show()

                // Open the login activity.
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // Make the header progress bar visible.
                headerProgressBar.visibility = View.VISIBLE

                // Obtain a document reference for the current user.
                documentReference = firebaseFirestore.collection("Users").
                    document(currentUserEmail)

                // Assign user data to variables upon successful document acquisition.
                documentReference.get().addOnSuccessListener { documentSnapshot ->
                    headerUserNameTV.text = documentSnapshot.getString("username")
                    headerUserNameTV.isAllCaps = true
                    headerUserEmailTV.text = currentUserEmail
                    val profileImageLink = documentSnapshot.getString("profileimageurl")

                    // Load the user image.
                    Glide.with(this@HomeActivity).load(profileImageLink).into(headerProfilePicIV)

                    headerProgressBar.visibility = View.INVISIBLE
                }.addOnFailureListener { e -> Log.e(TAG, ""+e.message) }
            }
        } catch (e: Exception) {

        }
    }

    /**
     * Method to retrieve the email of the currently logged in user.
     *
     * @return userEmail The email of the currently logged in user. If this is null,
     * return instead a string that says nobody is logged in. Returns an empty string
     * if an exception occurs.
     */
    private fun getCurrentLoggedInUser(): String {
        return try {
            // If a user is currently logged in, return their email.
            if (firebaseAuth != null) {
                val userEmail = firebaseAuth.currentUser?.email.toString()
                userEmail
                // Otherwise, return an error string.
            } else {
                resources.getString(R.string.nobody_logged_in_msg)
            }
        } catch (e: Exception) {
            Log.e(TAG, ""+e.message)
            ""
        }
    }


    /**
     * Method to set up the drawer menu.
     */
    private fun setUpDrawerMenu() {
        try {
            val actionBarDrawerToggle = ActionBarDrawerToggle(this,
                drawerLayout, toolbar,
                R.string.open,
                R.string.close
            )

            drawerLayout.addDrawerListener(actionBarDrawerToggle)
            actionBarDrawerToggle.syncState()
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }

    /**
     * Opens the drawer.
     */
    private fun openDrawer() {
        try {
            drawerLayout.openDrawer(GravityCompat.START)
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }

    /**
     * Closes the drawer.
     */
    private fun closeDrawer() {
        try {
            drawerLayout.closeDrawer(GravityCompat.START)
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }

    /**
     * Set the functionality for the drawer buttons.
     *
     * @param item The button clicked on by the user.
     * @return A Boolean value, true if an item is clicked, false otherwise.
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        try{
            when (item.itemId) {
                R.id.item_profile -> {
                    val profileIntent = Intent(this, ProfileActivity::class.java)
                    profileIntent.putExtra("fragment", "profile")
                    profileIntent.putExtra("userid", "default")
                    startActivity(profileIntent)
                    return true
                }
                R.id.item_notifications -> {
                    val notificationsIntent = Intent(this, NotificationsActivity::class.java)
                    startActivity(notificationsIntent)
                    return true
                }
                R.id.item_settings -> {
                    val settingsIntent = Intent(this, ProfileActivity::class.java)
                    settingsIntent.putExtra("fragment", "settings")
                    settingsIntent.putExtra("userid", "default")
                    startActivity(settingsIntent)
                    return true
                }
                R.id.item_favourites -> {
                    changeFragment(favouritesFragment)
                    return true
                }
                R.id.item_friends -> {
                    val friendsIntent = Intent(this, FriendsListActivity::class.java)
                    startActivity(friendsIntent)
                    return true
                }
                R.id.item_signout -> {
                    Log.i(TAG, getString(R.string.logcat_signed_out))
                    signOutUser()
                    return true
                }
                else -> {
                    return false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
        return false
    }

    /**
     *  Sign out the current user and send them back to the login activity.
     */
    private fun signOutUser() {
        try {
            if (firebaseAuth != null) {
                firebaseAuth.signOut()
                Toast.makeText(this, "You have successfully been logged out",
                    Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                closeDrawer()
                finish()
            }
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }

    /**
     * Executes after the activity is created.
     * Starts listening for notifications.
     */
    override fun onStart() {
        super.onStart()
        listenerRegistration = collectionReference.addSnapshotListener(this
        ) { querySnapshot, e ->
            if (e != null) {
                Log.e(TAG, getString(R.string.logcat_notifications_retrieval_error))
            } else if (!querySnapshot!!.isEmpty) {
                val size = querySnapshot.size()
                notificationTV.gravity = Gravity.CENTER_VERTICAL
                notificationTV.setTypeface(null, Typeface.BOLD)
                notificationTV.setTextColor(resources.getColor(R.color.colorBlack))
                //notificationTV.text = size as String

                Toast.makeText(this@HomeActivity, "You have $size notifications.",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Executes before the activity is destroyed.
     * Stops listening for notifications.
     */
    override fun onStop() {
        super.onStop()
        listenerRegistration.remove()
    }
}