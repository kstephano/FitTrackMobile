package com.example.fittrackmobile.fragments


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.fittrackmobile.adaptersClasses.TextStatusAdapter
import com.example.fittrackmobile.modelClasses.TextStatus

import com.example.fittrackmobile.R
import com.example.fittrackmobile.activities.FriendsListActivity
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import java.util.ArrayList
import kotlin.Exception

/**
 * A fragment to display text statuses.
 */
class TextStatusesFragment : Fragment() {

    // XML variables.
    private lateinit var parent: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    // Class variables.
    private val TAG = "TextStatuses"
    private lateinit var textStatusAdapter: TextStatusAdapter

    // Firebase variables.
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth

    /**
     * Method to assign View variables and set up the main UI view.
     *
     * @param inflater Object used to instantiate a layout XML file into its
     * corresponding View objects.
     * @param container The ViewGroup used to inflate the layout for the fragment.
     * @param savedInstanceState Reference to a Bundle object that saves data.
     */
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        parent = inflater.inflate(R.layout.fragment_text_statuses, container, false)
        initializeVariables()
        addStatusToRV()

        return parent
    }

    /**
     * Method called when the Fragment is visible to the user.
     * Set the RecyclerView adapter to START listening for updates to add to the RecyclerView.
     */
    override fun onStart() {
        super.onStart()

        try {
            textStatusAdapter.startListening()
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
            textStatusAdapter.stopListening()
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }

    /**
     * Populates the recycler view with text statuses in order of timestamp.
     */
    private fun addStatusToRV() {
        try {
            // Initialise the configuration for the PagedList to allow pagination.
            val config = PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(10)
                .build()

            val textStatusQuery: Query = firebaseFirestore.collection("TextStatus")
                .orderBy("timestamp", Query.Direction.DESCENDING)


            // Initialize the FirestoreRecyclerOptions object used
            // to load Firebase data into the RecyclerView.
            val options: FirestorePagingOptions<TextStatus> =
                FirestorePagingOptions.Builder<TextStatus>()
                    .setLifecycleOwner(this)
                    .setQuery(textStatusQuery, config) { snapshot ->
                        val textStatus = snapshot.toObject(TextStatus::class.java) as TextStatus
                        textStatus.statusID = snapshot.id
                        textStatus
                    }.build()

            // Initialize the TextStatusAdapter object and assign it to the adapter.
            textStatusAdapter = TextStatusAdapter(options)
            recyclerView.adapter = textStatusAdapter
            recyclerView.layoutManager = LinearLayoutManager(activity)
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }

    /**
     * Initialize member variables of the class.
     */
    private fun initializeVariables() {
        try {
            recyclerView = parent.findViewById(R.id.textStatus_RV)
            swipeRefreshLayout = parent.findViewById(R.id.frag_text_statuses_SRL)
            firebaseFirestore = FirebaseFirestore.getInstance()
            firebaseAuth = FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }
}
