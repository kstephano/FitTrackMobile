package com.example.fittrackmobile.fragments


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.fittrackmobile.adaptersClasses.ImageStatusAdapter
import com.example.fittrackmobile.modelClasses.ImageStatus
import com.example.fittrackmobile.R
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlin.Exception

/**
 * A fragment to display image statuses.
 */
class ImageStatusesFragment : Fragment() {

    // XML variables.
    private lateinit var parent: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    // Class variables.
    private val TAG = "ImageStatuses"
    private lateinit var imageStatusAdapter: ImageStatusAdapter

    // Firebase variables.
    private lateinit var firebaseFirestore: FirebaseFirestore

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
        parent = inflater.inflate(R.layout.fragment_image_statuses, container, false)
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
            imageStatusAdapter.startListening()
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
            imageStatusAdapter.stopListening()
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }

    /**
     * Method to add statuses to the RecyclerView.
     */
    private fun addStatusToRV() {
        try {
            // Initialise the configuration for the PagedList to allow pagination.
            val config = PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(5)
                .setPageSize(10)
                .build()

            val imageStatusQuery = firebaseFirestore.collection("ImageStatus")
                .orderBy("timestamp", Query.Direction.DESCENDING)

            // Initialize the FirestoreRecyclerOptions object used
            // to load Firebase data into the RecyclerView.
            val options: FirestorePagingOptions<ImageStatus> =
                FirestorePagingOptions.Builder<ImageStatus>()
                    .setLifecycleOwner(this)
                    .setQuery(imageStatusQuery, config) { snapshot ->
                        // Modify each ImageStatus obtained from Firestore and set the ID.
                        val imageStatus = snapshot.toObject(ImageStatus::class.java) as ImageStatus
                        imageStatus.statusID = snapshot.id
                        imageStatus
                    }.build()

            // Initialize the TextStatusAdapter object and assign it to the adapter.
            imageStatusAdapter = ImageStatusAdapter(options)
            recyclerView.adapter = imageStatusAdapter
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
            recyclerView = parent.findViewById(R.id.frag_image_statuses_RV)
            swipeRefreshLayout = parent.findViewById(R.id.frag_image_statuses_SRL)
            firebaseFirestore = FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }
}
