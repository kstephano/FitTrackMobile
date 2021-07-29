package com.example.fittrackmobile.fragments


import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.bumptech.glide.Glide

import com.example.fittrackmobile.R
import com.example.fittrackmobile.appClasses.AddNotifications
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView

/**
 * A fragment for a user profile page.
 */
class ProfileFragment : Fragment() {

    // Interface to get the bio String.
    interface GetBioInfo {
        fun getBioInfo(bio: String)
    }

    // XML variables.
    private lateinit var parent: View
    private lateinit var profilePicIV: CircleImageView
    private lateinit var userNameTV: TextView
    private lateinit var scoreTV: TextView
    private lateinit var textStatusCountTV: TextView
    private lateinit var imageStatusCountTV: TextView
    private lateinit var genderTV: TextView
    private lateinit var cityTV: TextView
    private lateinit var countryTV: TextView
    private lateinit var emailCardTV: TextView
    private lateinit var exitTV: TextView
    private lateinit var bioTV: TextView
    private lateinit var bioBtn: Button
    private lateinit var sendFriendRequestBtn: Button
    private lateinit var removeFriendBtn: Button
    private lateinit var acceptRequestBtn: Button
    private lateinit var waitDialog: Dialog
    private lateinit var bioDialog: Dialog

    // Class variables.
    private var TAG = "ProfileFragment"
    private lateinit var currentUserEmail: String
    private lateinit var extractedBio: String
    private lateinit var userID: String
    private var friendshipStatus = "not friends"

    // Firebase variables.
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var documentReference: DocumentReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        parent = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize the wait dialog.
        waitDialog = Dialog(context!!)
        waitDialog.setContentView(R.layout.please_wait_dialog)

        // Initialize the bio dialog.
        bioDialog = Dialog(context!!)
        bioDialog.setContentView(R.layout.user_bio_dialog)

        initializeObjects()

        currentUserEmail = firebaseAuth.currentUser?.email as String
        userID = arguments?.getString("userid") as String


        // Load profile data depending on the supplied userID.
        if (userID != "default" && userID != currentUserEmail) {
            loadOtherUserProfile(object : GetBioInfo {
                override fun getBioInfo(bio: String) {
                    extractedBio = bio
                }
            })
        } else {
            // Load the user's profile information and extract the bio using the interface.
            loadUserProfileData(object : GetBioInfo {
                override fun getBioInfo(bio: String) {
                    extractedBio = bio
                }
            })
        }
        return parent
    }

    /**
     * Method to load the user's data into the activity.
     */
    @SuppressLint("SetTextI18n")
    private fun loadUserProfileData(getBioInfo: GetBioInfo) {
        try {
            if (firebaseAuth != null) {
                waitDialog.show()
                waitDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                currentUserEmail = firebaseAuth.currentUser?.email as String

                documentReference = firebaseFirestore.collection("Users")
                    .document(currentUserEmail)
                documentReference.get().addOnSuccessListener { documentSnapshot ->
                    // Load the user profile picture into the ImageView.
                    val profilePicUrl = documentSnapshot?.getString("profileimageurl")
                    Glide.with(context!!).load(profilePicUrl).into(profilePicIV)

                    emailCardTV.text = currentUserEmail
                    userNameTV.text = documentSnapshot?.getString("username")
                    userNameTV.isAllCaps = true

                    val textStatusCount = documentSnapshot?.getLong("numberoftextstatus") as Long
                    val imageStatusCount = documentSnapshot.getLong("numberofimagestatus") as Long
                    val score = textStatusCount + imageStatusCount
                    val userBio = documentSnapshot.getString("bio") ?: getString(R.string.user_bio_main_tv)
                    getBioInfo.getBioInfo(userBio)

                    // Set the text in the score TextView.
                    if (score < 10) {
                        scoreTV.text = "0$score"
                    } else {
                        scoreTV.text = score.toString()
                    }

                    // Set the text in the textStatusCount TextView.
                    if (textStatusCount < 10) {
                        textStatusCountTV.text = "0$textStatusCount"
                    } else {
                        textStatusCountTV.text = textStatusCount.toString()
                    }

                    // Set the text in the imageStatusCount TextView.
                    if (imageStatusCount < 10) {
                        imageStatusCountTV.text = "0$imageStatusCount"
                    } else {
                        imageStatusCountTV.text = imageStatusCount.toString()
                    }

                    genderTV.text = documentSnapshot.getString("gender")
                    cityTV.text = documentSnapshot.getString("city") ?: "N/A"
                    countryTV.text = documentSnapshot.getString("country") ?: "N/A"
                    waitDialog.dismiss()
                }.addOnFailureListener { e -> Log.e(TAG, "" + e.message) }

            } else {
                Log.i(TAG, getString(R.string.nobody_logged_in_msg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        }
    }

    /**
     * Method to load another user's profile.
     */
    private fun loadOtherUserProfile(getBioInfo: GetBioInfo) {
        try {
            waitDialog.show()
            waitDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            sendFriendRequestBtn.isEnabled = true
            sendFriendRequestBtn.isVisible = true

            // Try and get a friendship reference of the current user and the other user.
            firebaseFirestore.collection("Friendships")
                .whereIn("recipient", listOf(userID, currentUserEmail))
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (!task.result!!.isEmpty) {
                            val friendshipDocument = task.result!!.documents[0]
                            val friendship = friendshipDocument.getString("friendship")
                            val recipient = friendshipDocument.getString("recipient")
                            sendFriendRequestBtn.isEnabled = false
                            sendFriendRequestBtn.isVisible = false

                            if (friendship == "pending" && recipient == userID) {
                                sendFriendRequestBtn.isEnabled = false
                                sendFriendRequestBtn.isVisible = true
                                sendFriendRequestBtn.text = "request sent"
                            } else if (friendship == "pending" && recipient == currentUserEmail) {
                                acceptRequestBtn.isEnabled = true
                                acceptRequestBtn.isVisible = true
                            } else if (friendship == "friends") {
                                removeFriendBtn.isEnabled = true
                                removeFriendBtn.isVisible = true
                            }
                        } else {
                            Log.i(TAG, "Empty task")
                        }
                    }
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Couldn't find friendship" + e.message)
                    sendFriendRequestBtn.isEnabled = true
                    sendFriendRequestBtn.isVisible = true
                }

            // Get a reference to the user from the Users collection.
            val otherUserReference = firebaseFirestore.collection("Users")
                .document(userID)

            // Load in the other user's profile data.
            otherUserReference.get().addOnSuccessListener { documentSnapshot ->
                // Load the user profile picture into the ImageView.
                val profilePicUrl = documentSnapshot?.getString("profileimageurl")
                Glide.with(context!!).load(profilePicUrl).into(profilePicIV)

                emailCardTV.text = userID
                userNameTV.text = documentSnapshot?.getString("username")
                userNameTV.isAllCaps = true

                val textStatusCount = documentSnapshot?.getLong("numberoftextstatus") as Long
                val imageStatusCount = documentSnapshot.getLong("numberofimagestatus") as Long
                val score = textStatusCount + imageStatusCount
                val userBio = documentSnapshot.getString("bio") ?: getString(R.string.user_bio_main_tv)
                getBioInfo.getBioInfo(userBio)

                // Set the text in the score TextView.
                if (score < 10) {
                    scoreTV.text = "0$score"
                } else {
                    scoreTV.text = score.toString()
                }

                // Set the text in the textStatusCount TextView.
                if (textStatusCount < 10) {
                    textStatusCountTV.text = "0$textStatusCount"
                } else {
                    textStatusCountTV.text = textStatusCount.toString()
                }

                // Set the text in the imageStatusCount TextView.
                if (imageStatusCount < 10) {
                    imageStatusCountTV.text = "0$imageStatusCount"
                } else {
                    imageStatusCountTV.text = imageStatusCount.toString()
                }

                genderTV.text = documentSnapshot.getString("gender")
                cityTV.text = documentSnapshot.getString("city") ?: "N/A"
                countryTV.text = documentSnapshot.getString("country") ?: "N/A"
                waitDialog.dismiss()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed sending request: " + e.message)
        }
    }

    /**
     * Initialize objects and map Java/Kotlin objects to XML objects.
     */
    private fun initializeObjects() {
        try {
            firebaseAuth = FirebaseAuth.getInstance()
            firebaseFirestore = FirebaseFirestore.getInstance()
            profilePicIV = parent.findViewById(R.id.frag_profile_profilePicIV)
            userNameTV = parent.findViewById(R.id.frag_profile_userNameTV)
            scoreTV = parent.findViewById(R.id.frag_profile_scoreTV)
            textStatusCountTV = parent.findViewById(R.id.frag_profile_textStatusTV)
            imageStatusCountTV = parent.findViewById(R.id.frag_profile_imageStatusTV)
            genderTV = parent.findViewById(R.id.frag_profile_genderCardTV)
            cityTV = parent.findViewById(R.id.frag_profile_addressCardTV)
            countryTV = parent.findViewById(R.id.frag_profile_countryCardTV)
            emailCardTV = parent.findViewById(R.id.frag_profile_emailCardTV)
            bioBtn = parent.findViewById(R.id.frag_profile_bioBtn)
            sendFriendRequestBtn = parent.findViewById(R.id.frag_profile_send_friend_requestBtn)
            removeFriendBtn = parent.findViewById(R.id.frag_profile_remove_friendBtn)
            acceptRequestBtn = parent.findViewById(R.id.frag_profile_accept_requestBtn)
            exitTV = bioDialog.findViewById(R.id.user_bio_exitTV)
            bioTV = bioDialog.findViewById(R.id.user_bio_bioTV)

            // Set the bio button functionality.
            bioBtn.setOnClickListener {
                bioDialog.show()
                bioTV.text = extractedBio
            }

            // Set the exit TextView functionality.
            exitTV.setOnClickListener { bioDialog.dismiss() }

            // Set the send friend request button OnClickListener
            sendFriendRequestBtn.setOnClickListener {
                val friendshipData: HashMap<String, Any> = HashMap()
                val addNotifications = AddNotifications()

                friendshipData["friendship"] = "pending"
                friendshipData["sender"] = currentUserEmail
                friendshipData["recipient"] = userID

                // Update the friendship in Firebase.
                firebaseFirestore.collection("Friendships")
                    .document(currentUserEmail + userID)
                    .set(friendshipData)
                    .addOnSuccessListener {
                        Toast.makeText(context, getString(R.string.friend_request_sent_msg),
                            Toast.LENGTH_SHORT).show()

                        addNotifications.generateNotification(
                            currentUserEmail,
                            "friend request", "sent",
                            userID)
                    }.addOnFailureListener { e -> Log.e(TAG, "" + e.message) }


                sendFriendRequestBtn.isEnabled = false
                sendFriendRequestBtn.text = "request sent"
            }

            // Set the remove friends button OnClickListener.
            removeFriendBtn.setOnClickListener {

                // Delete the friendship document named: currentUserEmail + userID.
                firebaseFirestore.collection("Friendships")
                    .document(currentUserEmail + userID)
                    .delete()
                    .addOnSuccessListener {

                        // Delete the friendship document if it is named: userID + currentUserEmail.
                        firebaseFirestore.collection("Friendships")
                            .document(userID + currentUserEmail)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(context, getString(R.string.friend_removed_msg),
                                    Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener { e ->
                                Log.e(TAG, "Unable to remove friend: " + e.message)
                            }

                        // Remove the friend from the user's individual Friends collection.
                        firebaseFirestore.collection("Users")
                            .document(currentUserEmail)
                            .collection("Friends")
                            .document(userID)
                            .delete()
                            .addOnSuccessListener {

                                // Remove the user from the friends' individual Friends collection.
                                firebaseFirestore.collection("Users")
                                    .document(userID)
                                    .collection("Friends")
                                    .document(currentUserEmail)
                                    .delete()
                                    .addOnSuccessListener {
                                        Toast.makeText(context, getString(R.string.friend_removed_msg),
                                            Toast.LENGTH_SHORT).show()
                                    }.addOnFailureListener { e -> Log.e(TAG, "" + e.message) }

                            }.addOnFailureListener { e -> Log.e(TAG, "" + e.message) }

                    }.addOnFailureListener {e ->
                        Log.e(TAG, "Unable to remove friend: " + e.message)
                    }
                removeFriendBtn.isEnabled = false
                removeFriendBtn.isVisible = false
                sendFriendRequestBtn.isEnabled = true
                sendFriendRequestBtn.isVisible = true
            }

            // Set the accept friends button OnClickListener.
            acceptRequestBtn.setOnClickListener {
                val addNotifications = AddNotifications()
                val userData: HashMap<String, Any> = HashMap()
                val otherUserData: HashMap<String, Any> = HashMap()
                userData["useremail"] = currentUserEmail
                otherUserData["useremail"] = userID

                // Get a document reference of the current user to retrieve data.
                firebaseFirestore.collection("Users")
                    .document(currentUserEmail)
                    .get()
                    .addOnSuccessListener { documentSnapshot1 ->
                        userData["profileimageurl"] = documentSnapshot1?.getString("profileimageurl") as String
                        userData["firstname"] = documentSnapshot1.getString("firstname") as String
                        userData["lastname"] = documentSnapshot1.getString("lastname") as String

                        // Get a document reference of the other user to retrieve data.
                        firebaseFirestore.collection("Users")
                            .document(userID)
                            .get()
                            .addOnSuccessListener { documentSnapshot2 ->
                                otherUserData["profileimageurl"] = documentSnapshot2?.getString(
                                    "profileimageurl") as String
                                otherUserData["firstname"] = documentSnapshot2.getString(
                                    "firstname") as String
                                otherUserData["lastname"] = documentSnapshot2.getString(
                                    "lastname") as String

                                // Add the friend to the individual Friends collection for the user.
                                firebaseFirestore.collection("Users")
                                    .document(currentUserEmail)
                                    .collection("Friends")
                                    .document(userID)
                                    .set(otherUserData)
                                    .addOnSuccessListener {
                                        Log.i(TAG, "Added to individual Friends collection (1)")
                                    }.addOnFailureListener { e -> Log.e(TAG, "" + e.message) }

                                // Add the friend to the individual Friends collection for the other user.
                                firebaseFirestore.collection("Users")
                                    .document(userID)
                                    .collection("Friends")
                                    .document(currentUserEmail)
                                    .set(userData)
                                    .addOnSuccessListener {
                                        Log.i(TAG, "Added to individual Friends collection (2)")
                                    }.addOnFailureListener { e -> Log.e(TAG, "" + e.message) }

                                // Update the friendship status in Firestore.
                                firebaseFirestore.collection("Friendships")
                                    .document(userID + currentUserEmail)
                                    .update("friendship", "friends")
                                firebaseFirestore.collection("Friendships")
                                    .document(currentUserEmail + userID)
                                    .update("friendship", "friends")

                                // Generate a notification for the accepted request.
                                addNotifications.generateNotification(
                                    userID,
                                    "friend request", "accepted",
                                    currentUserEmail)
                            }.addOnFailureListener { e ->
                                Log.e(TAG, "" + e.message)
                            }
                    }.addOnFailureListener { e ->
                        Log.e(TAG, "" + e.message)
                    }
                acceptRequestBtn.isEnabled = false
                acceptRequestBtn.isVisible = false
                removeFriendBtn.isEnabled = true
                removeFriendBtn.isVisible = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize member variables: " + e.message)
        }
    }
}
