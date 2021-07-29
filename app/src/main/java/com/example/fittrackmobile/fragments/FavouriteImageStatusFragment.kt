package com.example.fittrackmobile.fragments


import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fittrackmobile.adaptersClasses.FavouriteImageStatusAdapter
import com.example.fittrackmobile.modelClasses.FavouriteImageStatus

import com.example.fittrackmobile.R
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * A fragment to display favourited image statuses.
 */
class FavouriteImageStatusFragment : Fragment() {

    // XML variables.
    private lateinit var parent: View
    private lateinit var recyclerView: RecyclerView

    // Class variables.
    private val TAG = "FavouriteImageStatusFragment"
    private lateinit var favouriteImageStatusAdapter: FavouriteImageStatusAdapter

    // Firebase variables.
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        parent = inflater.inflate(R.layout.fragment_favourite_image_status, container, false)
        initializeVariables()
        addStatusToRV()

        return parent
    }

    /**
     * Method called when the Fragment is visible to the user.
     * Set the RecyclerView adapter to start listening for updates to add to the RecyclerView.
     */
    override fun onStart() {
        super.onStart()

        try {
            favouriteImageStatusAdapter.startListening()
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }

    /**
     * Called when the Fragment is no longer started.
     * Set the RecyclerView adapter to STOP listening for updates to add to the RecyclerView.
     */
    override fun onStop() {
        super.onStop()

        try {
            favouriteImageStatusAdapter.stopListening()
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }

    /**
     * Populates the recycler view with favourited text statuses in order of timestamp.
     */
    private fun addStatusToRV() {
        try {
            firebaseAuth = FirebaseAuth.getInstance()

            if (firebaseAuth != null) {
                val userEmail = firebaseAuth.currentUser?.email as String
                val query = firebaseFirestore.collection("UserFavourite")
                    .document(userEmail).collection("FavouriteImageStatus")
                    .orderBy("timestamp", Query.Direction.DESCENDING)

                // Initialize the FirestoreRecyclerOptions object used
                // to load Firebase data into the RecyclerView.
                val options: FirestoreRecyclerOptions<FavouriteImageStatus> =
                    FirestoreRecyclerOptions.Builder<FavouriteImageStatus>()
                        .setQuery(query, FavouriteImageStatus::class.java).build()

                // Initialize the FavouriteImageStatusAdapter object and assign it to the adapter.
                favouriteImageStatusAdapter = FavouriteImageStatusAdapter(options)
                recyclerView.adapter = favouriteImageStatusAdapter
                recyclerView.layoutManager = LinearLayoutManager(activity)
            }
        } catch (e: Exception) {
            Log.i(TAG, Resources.getSystem().getString(R.string.logcat_no_user))
        }
    }

    /**
     * Method to initialize member variables of the class.
     */
    private fun initializeVariables() {
        try {
            recyclerView = parent.findViewById(R.id.frag_favouriteIS_RV)
            firebaseFirestore = FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }
}
