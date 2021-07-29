package com.example.fittrackmobile.activities

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fittrackmobile.adaptersClasses.NotificationsAdapter
import com.example.fittrackmobile.modelClasses.Notification
import com.example.fittrackmobile.R
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

class NotificationsActivity : AppCompatActivity() {

    // XML variables.
    private lateinit var recyclerView: RecyclerView
    private lateinit var toolbar: Toolbar

    // Class variables.
    val TAG = "NotificationsActivity"
    private lateinit var notificationsAdapter: NotificationsAdapter
    private lateinit var dialog: Dialog

    // Firestore variables.
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        attachJavaToXML()
        addNotificationsToRV()
    }

    /**
     * Executes after the activity is created.
     * Sets the notifications adapter to start listening for updates.
     */
    override fun onStart() {
        super.onStart()
        try {
            notificationsAdapter.startListening()
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }

    /**
     * Executes before the activity is destroyed.
     * Sets the notifications adapter to stop listening for updates.
     */
    override fun onStop() {
        super.onStop()
        try {
            notificationsAdapter.stopListening()
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }

    /**
     * Add the user's notifications to the RecyclerView.
     */
    private fun addNotificationsToRV() {
        try {
            val currentUserEmail = firebaseAuth.currentUser?.email as String

            // Make a query to obtain notifications for the current user.
            val notificationsQuery = firebaseFirestore.collection("Users")
                .document(currentUserEmail).collection("Notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)

            // Set the options for the RecyclerView with the query and
            // the Notification model class.
            val options: FirestoreRecyclerOptions<Notification> =
                FirestoreRecyclerOptions.Builder<Notification>().setQuery(notificationsQuery,
                    Notification::class.java).build()

            notificationsAdapter = NotificationsAdapter(options)
            recyclerView.adapter  = notificationsAdapter
            recyclerView.layoutManager = LinearLayoutManager(this)

        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }

    /**
     * Clear all notifications from the RecyclerView.
     */
    private fun clearAllNotifications() {
        try {
            val notificationsIdArrayList: ArrayList<String> = ArrayList()

            if (firebaseAuth != null) {
                val currentUserEmail = firebaseAuth.currentUser?.email as String

                // Get all the notifications collection for the current user.
                firebaseFirestore.collection("Users")
                    .document(currentUserEmail)
                    .collection("Notifications")
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Iterate through all notification documents in the collection.
                            for (queryDocumentSnapshot: QueryDocumentSnapshot in task.result!!) {
                                // Add each notification document ID to the String ArrayList.
                                notificationsIdArrayList.add(queryDocumentSnapshot.id)
                            }
                            // Create a batched write to execute write operations atomically.
                            val writeBatch: WriteBatch = firebaseFirestore.batch()

                            // Iterate through all notifications in the ArrayList.
                            for (i in 0 until notificationsIdArrayList.size) {
                                // Delete each notification from Firebase.
                                writeBatch.delete(
                                    firebaseFirestore.collection("Users")
                                        .document(currentUserEmail)
                                        .collection("Notifications")
                                        .document(notificationsIdArrayList[i])
                                )
                            }


                            // Commit the batch to delete all the notifications atomically.
                            writeBatch.commit().addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this@NotificationsActivity,
                                        getString(R.string.notifications_cleared_msg),
                                        Toast.LENGTH_SHORT).show()
                                } else if (!task.isSuccessful) {
                                    Log.e(TAG, getString(
                                        R.string.logcat_unable_to_clear_notifications
                                    ) +
                                            task.exception)
                                }
                            }
                        }
                    }
            } else {
                Log.i(TAG, getString(R.string.logcat_no_user))
            }
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }

    /**
     * Attach Java and Kotlin objects to their XML counterparts.
     */
    private fun attachJavaToXML() {
        try {
            dialog = Dialog(this)
            dialog.setContentView(R.layout.please_wait_dialog)
            toolbar = findViewById(R.id.notifications_toolbar)
            recyclerView = findViewById(R.id.notifications_RV)

            // Inflate the toolbar menu and set the OnClickListeners for the items.
            toolbar.inflateMenu(R.menu.notifications_menu)
            toolbar.title = getString(R.string.title_notifications_page)
            toolbar.setOnMenuItemClickListener(object : Toolbar.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem): Boolean {
                    when (item.itemId) {
                        R.id.notifications_menu_item_clear -> {
                            clearAllNotifications()
                            return true
                        }

                        R.id.notifications_menu_item_goBack -> {
                            val intent = Intent(this@NotificationsActivity,
                                HomeActivity::class.java)
                            startActivity(intent)
                            return true
                        }
                    }
                    return false
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }
}
