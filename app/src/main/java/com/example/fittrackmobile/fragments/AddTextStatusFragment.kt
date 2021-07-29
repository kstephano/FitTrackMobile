package com.example.fittrackmobile.fragments


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.example.fittrackmobile.activities.HomeActivity
import com.example.fittrackmobile.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.Exception
import kotlin.collections.HashMap

/**
 * Fragment for users to create and upload text statuses.
 */
class AddTextStatusFragment : Fragment() {

    // Interface to retrieve the profile url from Firebase.
    interface GetURLInterface {
        fun getProfileUrl(profileUrlValue: String) { }
    }

    // Class variables.
    private var TAG = "AddTextStatusFragment"
    private lateinit var parent: View
    private lateinit var profileURL: String

    // XML variables.
    private lateinit var statusET: EditText
    private lateinit var publishStatusBtn: Button
    private lateinit var rewriteStatusBtn: Button
    private lateinit var goBackFAB: FloatingActionButton
    private lateinit var progressBar: ProgressBar

    // Firebase objects
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var documentReference: DocumentReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        parent = inflater.inflate(R.layout.fragment_add_text_status, container, false)
        attachJavaViewsToXMLViews()

        // Set the OnClickListener for the publish status button.
        publishStatusBtn.setOnClickListener {
            addStatus(object : GetURLInterface {
                override fun getProfileUrl(profileUrlValue: String) {
                    super.getProfileUrl(profileUrlValue)
                    profileURL = profileUrlValue
                    publishStatus()
                }
            })
        }
        return parent
    }

    /**
     * Method to upload status data to Firebase.
     */
    fun publishStatus() {
        try {
            if (statusET.text.toString().isNotEmpty()) {
                val currentUserEmail = firebaseAuth.currentUser?.email as String
                val statusData: HashMap<String, Any> = HashMap()

                val userDocumentReference = firebaseFirestore.collection("Users")
                    .document(currentUserEmail)

                userDocumentReference.get().addOnSuccessListener { documentSnapshot ->
                    val firstName = documentSnapshot?.getString("firstname") as String

                    statusData["timestamp"] = Timestamp.now()
                    statusData["firstname"] = firstName
                    statusData["useremail"] = currentUserEmail
                    statusData["profileurl"] = profileURL
                    statusData["status"] = statusET.text.toString()
                    statusData["numberoflaughreacts"] = 0
                    statusData["numberoflovereacts"] = 0
                    statusData["numberofsadreacts"] = 0
                    statusData["numberofcomments"] = 0
                    statusData["currentflag"] = "none"

                    firebaseFirestore.collection("TextStatus")
                        .document(System.currentTimeMillis().toString())
                        .set(statusData)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Status published", Toast.LENGTH_SHORT).show()
                            progressBar.visibility = View.INVISIBLE

                            // Obtain a reference of the document from "Users" belonging to the current user.
                            documentReference = firebaseFirestore.collection("Users")
                                .document(currentUserEmail)

                            // Update the user's values upon success.
                            documentReference.get()
                                .addOnSuccessListener { documentSnapshot ->
                                    var numberoftextstatus =
                                        documentSnapshot?.getLong("numberoftextstatus") ?: 0
                                    numberoftextstatus++

                                    val hashMap: HashMap<String, Any> = HashMap()
                                    hashMap["numberoftextstatus"] = numberoftextstatus

                                    // Update the document.
                                    firebaseFirestore.collection("Users")
                                        .document(currentUserEmail).update(hashMap)
                                }.addOnFailureListener { e -> Log.e(TAG, "" + e.message) }

                            publishStatusBtn.isEnabled = true
                            activity?.finish()
                        }.addOnFailureListener { e -> Log.e(TAG, "" + e.message) }
                }
            } else {
                Toast.makeText(context, resources.getString(R.string.enter_status_msg),
                    Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }

    /**
     * Method to validate the user and retrieve their profile image URL.
     */
    private fun addStatus(objectGetURLInterface: GetURLInterface) {
        try {
            firebaseAuth = FirebaseAuth.getInstance()
            firebaseFirestore = FirebaseFirestore.getInstance()

            // If the user is logged in correctly and the text field is not empty.
            if (firebaseAuth != null && statusET.text.toString().isNotEmpty()) {
                progressBar.visibility = View.VISIBLE
                publishStatusBtn.isEnabled = false

                val currentUserEmail = firebaseAuth.currentUser?.email ?: ""

                // Obtain a reference of the document from "Users" belonging to the current user.
                documentReference = firebaseFirestore.collection("Users").document(currentUserEmail)

                // Send the user's profile image URL upon success.
                documentReference.get().addOnSuccessListener { documentSnapshot ->
                    val profileImageURL = documentSnapshot.getString("profileimageurl") ?: ""
                    objectGetURLInterface.getProfileUrl(profileImageURL)
                }.addOnFailureListener { e ->
                    Log.e(TAG, "" + e.message)
                    Toast.makeText(context, "Fails to get profile url", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.INVISIBLE
                    publishStatusBtn.isEnabled = true
                }
            } else {
                Toast.makeText(context, "Status must have text", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }

    /**
     * Attach Java objects to their XML counterparts and set button OnClickListeners.
     */
    private fun attachJavaViewsToXMLViews() {
        try {
            statusET = parent.findViewById(R.id.frag_addTextStatus_textStatusET)
            publishStatusBtn = parent.findViewById(R.id.frag_addTextStatus_publishStatusBtn)
            rewriteStatusBtn = parent.findViewById(R.id.frag_addTextStatus_rewriteStatusBtn)
            goBackFAB = parent.findViewById(R.id.frag_addTextStatus_goBackFAB)
            progressBar = parent.findViewById(R.id.frag_addTextStatus_progressBar)

            // Set the OnClickListener for the rewrite status button.
            rewriteStatusBtn.setOnClickListener { statusET.setText("") }

            // Set the OnClickListener for the go back button.
            goBackFAB.setOnClickListener {
                val intent = Intent(context, HomeActivity::class.java)
                startActivity(intent)
                activity?.finish()
            }

        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }
}
