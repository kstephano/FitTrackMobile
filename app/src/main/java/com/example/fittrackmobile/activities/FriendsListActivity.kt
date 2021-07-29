package com.example.fittrackmobile.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fittrackmobile.adaptersClasses.FriendsListAdapter
import com.example.fittrackmobile.R
import com.example.fittrackmobile.modelClasses.Friend
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.ArrayList


class FriendsListActivity : AppCompatActivity() {

    // XML variables.
    private lateinit var toolbar: Toolbar
    private lateinit var searchFriendsET: EditText
    private lateinit var searchBtn: ImageButton
    private lateinit var recyclerView: RecyclerView

    // Class variables.
    private val TAG = "FriendsListActivity"
    private var allFriends = ArrayList<String>()
    private lateinit var friendsListAdapter: FriendsListAdapter

    // Firebase variables.
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)

        // Set the tool bar.
        setSupportActionBar(findViewById(R.id.friends_page_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_friends_page)

        attachJavaToXML()
        addFriendsToRV()
    }

    /**
     * Method called when the Fragment is visible to the user.
     * Set the RecyclerView adapter to START listening for updates to add to the RecyclerView.
     */
    override fun onStart() {
        super.onStart()

        try {
            friendsListAdapter.startListening()
        } catch (e: Exception) {
            Log.e(TAG, "onStart: " + e.message)
        }
    }

    /**
     * Called when the Fragment is no longer started.
     * Set the RecyclerView adapter to STOP listening for updates to add to the RecyclerView.
     */
    override fun onStop() {
        super.onStop()
        try {
            friendsListAdapter.stopListening()
        } catch (e: Exception) {
            Log.e(TAG, "onStop: " + e.message)
        }
    }

    /**
     * Populates the recycler view with friends in alphabetical order.
     */
    private fun addFriendsToRV() {
        try {
            val userEmail = firebaseAuth.currentUser?.email.toString()

            // Query all user's to find the current user's friends.
            val friendsQuery = firebaseFirestore.collection("Users")
                .document(userEmail)
                .collection("Friends")
                .orderBy("lastname", Query.Direction.DESCENDING)

            val options: FirestoreRecyclerOptions<Friend> = FirestoreRecyclerOptions
                .Builder<Friend>().setQuery(friendsQuery, Friend::class.java).build()

            friendsListAdapter = FriendsListAdapter(options)
            recyclerView.adapter = friendsListAdapter
            recyclerView.layoutManager = LinearLayoutManager(this)
        } catch (e: Exception) {
            Log.e(TAG, "addFriendsToRV: " + e.message)
        }
    }

    /**
     * Attach Java/Kotlin objects to their respective XML counterparts.
     */
    private fun attachJavaToXML() {
        try {
            firebaseAuth = FirebaseAuth.getInstance()
            firebaseFirestore = FirebaseFirestore.getInstance()
            toolbar = findViewById(R.id.friends_page_toolbar)
            searchFriendsET = findViewById(R.id.friends_page_searchFriendsET)
            searchBtn = findViewById(R.id.friends_page_searchBtn)
            recyclerView = findViewById(R.id.friends_page_RV)

            searchBtn.setOnClickListener {
                val searchText = searchFriendsET.text.toString()

                // Search for the given email and open the profile page.
                firebaseFirestore.collection("Users")
                    .document(searchText)
                    .get()
                    .addOnSuccessListener {
                        val profileIntent = Intent(this, ProfileActivity::class.java)
                        profileIntent.putExtra("fragment", "profile")
                        profileIntent.putExtra("userid", searchText)
                        startActivity(profileIntent)
                    }.addOnFailureListener {
                        Toast.makeText(this, "User not found",
                            Toast.LENGTH_SHORT).show()
                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "attachJavaToXML: " + e.message)
        }
    }
}