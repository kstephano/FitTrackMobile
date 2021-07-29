package com.example.fittrackmobile.activities

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fittrackmobile.adaptersClasses.CommentAdapter
import com.example.fittrackmobile.appClasses.AddNotifications
import com.example.fittrackmobile.modelClasses.Comment
import com.example.fittrackmobile.R
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlin.Exception
import kotlin.collections.HashMap

class TextStatusCommentsActivity : AppCompatActivity() {

    // XML variables.
    private lateinit var recyclerView: RecyclerView
    private lateinit var addCommentBtn: ImageButton
    private lateinit var commentET: EditText

    // Class variables.
    private var TAG = "TextStatusCommentsActivity"
    private lateinit var bundle: Bundle
    private lateinit var documentID: String
    private lateinit var currentUser: String
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var addNotifications: AddNotifications
    private lateinit var receivedUserEmail: String

    // Firebase variables.
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var documentReference: DocumentReference
    private lateinit var collectionReference: CollectionReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_text_status_comments)
        attachJavaToXML()
        bundle = intent.extras as Bundle
        documentID = bundle.get("documentID") as String
        receivedUserEmail = bundle.getString("userEmailID") as String
        addCommentToRecyclerView()
        addNotifications = AddNotifications()

        // Set the tool bar.
        setSupportActionBar(findViewById(R.id.text_status_comments_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_comments_page)

        addCommentBtn.setOnClickListener {
            Toast.makeText(this@TextStatusCommentsActivity, "Pressed", Toast.LENGTH_SHORT).show()
            addCommentToFirebase()
        }

    }

    /**
     * Add comments to the recyclerView.
     */
    private fun addCommentToRecyclerView() {
        try {
            val queryComments = firebaseFirestore.collection("TextStatus")
                .document(documentID).collection("Comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)

            val options: FirestoreRecyclerOptions<Comment> = FirestoreRecyclerOptions
                .Builder<Comment>().setQuery(queryComments, Comment::class.java).build()

            commentAdapter = CommentAdapter(options)
            recyclerView.adapter = commentAdapter
            recyclerView.layoutManager = LinearLayoutManager(this)

        } catch (e: Exception) {
            Log.e(TAG, "addCommentToRecyclerView: " + e.message)
        }
    }

    /**
     * Method called when activity starts.
     * Start listening for comments to be added to the comment adapter.
     */
    override fun onStart() {
        super.onStart()

        try {
            commentAdapter.startListening()
        } catch (e: Exception) {
            Log.e(TAG, "onStart: " + e.message)
        }
    }

    /**
     * Method called when activity stops.
     * Stop listening for comments to add to the adapter.
     */
    override fun onStop() {
        super.onStop()

        try {
            commentAdapter.stopListening()
        } catch (e: Exception) {
            Log.e(TAG, "onStop: " + e.message)
        }
    }

    /**
     * Finish the activity upon the back button being pressed.
     */
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    /**
     * Method to upload the comment to Firebase Firestore.
     */
    private fun addCommentToFirebase() {
        try {
            when {
                commentET.text.toString().isNotEmpty() -> {
                    currentUser = firebaseAuth.currentUser?.email as String
                    firebaseFirestore = FirebaseFirestore.getInstance()

                    // Get a document reference of the current user.
                    documentReference = firebaseFirestore.collection("Users")
                        .document(currentUser)

                    documentReference.get()
                        .addOnSuccessListener { documentSnapshot ->
                            val profileURL = documentSnapshot?.getString("profileimageurl") as String
                            val firstName = documentSnapshot.getString("firstname") as String

                            val hashMap: HashMap<String, Any> = HashMap()
                            hashMap["useremail"] = currentUser
                            hashMap["commenter"] = firstName
                            hashMap["comment"] = commentET.text.toString()
                            hashMap["profilepicurl"] = profileURL
                            hashMap["timestamp"] = Timestamp.now()

                            // Upload the the comment to Firebase.
                            firebaseFirestore.collection("TextStatus")
                                .document(documentID).collection("Comments")
                                .add(hashMap)
                                .addOnSuccessListener {
                                    Toast.makeText(this@TextStatusCommentsActivity,
                                        R.string.commented_added_msg, Toast.LENGTH_SHORT).show()
                                    // Generate the notification for the action.
                                    addNotifications.generateNotification(currentUser,
                                        "commented on", "text status",
                                        receivedUserEmail)
                                    commentET.setText("")

                                    // Get a collection reference of the comments of the current status.
                                    collectionReference = firebaseFirestore.collection("TextStatus")
                                        .document(documentID)
                                        .collection("Comments")

                                    // On success,
                                    collectionReference.get().addOnSuccessListener { querySnapshot ->
                                        // Get the number of comments on the status.
                                        val commentCount = querySnapshot?.size() as Int

                                        // Get a document reference to the text status.
                                        val textStatusDocumentReference = firebaseFirestore.collection("TextStatus")
                                            .document(documentID)

                                        textStatusDocumentReference.update("numberofcomments", commentCount)
                                    }.addOnFailureListener { e -> Log.e(TAG, "" + e.message) }
                                }.addOnFailureListener { e -> Log.e(TAG, "" + e.message) }
                        }
                }
                firebaseAuth == null -> {
                    Toast.makeText(this,
                        R.string.nobody_logged_in_msg, Toast.LENGTH_SHORT).show()
                }
                commentET.text.toString().isEmpty() -> {
                    Toast.makeText(this,
                        R.string.enter_comment_msg, Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Couldn't upload comment", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "addCommentToFirebase: " + e.message)
        }
    }

    /**
     * Attach Java/Kotlin objects to their respective XML counterparts.
     */
    private fun attachJavaToXML() {
        try {
            firebaseAuth = FirebaseAuth.getInstance()
            firebaseFirestore = FirebaseFirestore.getInstance()
            recyclerView = findViewById(R.id.text_status_comments_RV)
            commentET = findViewById(R.id.text_status_comments_commentET)
            addCommentBtn = findViewById(R.id.text_status_comments_AddCommentBtn)
        } catch (e: Exception) {
            Log.e(TAG, "attachJavaToXML: " + e.message)
        }
    }
}