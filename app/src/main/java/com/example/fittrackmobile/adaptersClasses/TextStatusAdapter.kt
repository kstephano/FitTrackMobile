package com.example.fittrackmobile.adaptersClasses

import android.content.Intent
import android.content.res.Resources
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fittrackmobile.adaptersClasses.TextStatusAdapter.TextStatusViewHolder
import com.example.fittrackmobile.appClasses.AddNotifications
import com.example.fittrackmobile.modelClasses.TextStatus
import com.example.fittrackmobile.R
import com.example.fittrackmobile.activities.ProfileActivity
import com.example.fittrackmobile.activities.TextStatusCommentsActivity
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

/**
 * Creates a new RecyclerView adapter for text statuses that listens to a Firestore Query.
 */
class TextStatusAdapter(options: FirestorePagingOptions<TextStatus>) :
    FirestorePagingAdapter<TextStatus, TextStatusViewHolder>(options) {

    // Class variables.
    private val TAG = "TextStatusAdapter"

    /**
     * Called by RecyclerView to display the data at the specified position.
     * Will update the contents of the itemView to reflect the item at the
     * given position.
     *
     * @param textStatusViewHolder The RecyclerView.ViewHolder object used to describe
     * each itemView.
     * @param i The position of the item within the adapter's data set.
     * @param textStatus Text status object used to populate the itemViews with data.
     */
    override fun onBindViewHolder(textStatusViewHolder: TextStatusViewHolder,
                                  i: Int, textStatus: TextStatus) {
        textStatusViewHolder.firstNameTV.text = textStatus.firstname
        textStatusViewHolder.dateTimeTV.text = getDateFormatted(textStatus.timestamp.toDate())
        textStatusViewHolder.userStatusTV.text = textStatus.status
        textStatusViewHolder.loveCountTV.text = textStatus.numberoflovereacts.toString()
        textStatusViewHolder.laughCountTV.text = textStatus.numberoflaughreacts.toString()
        textStatusViewHolder.sadCountTV.text = textStatus.numberofsadreacts.toString()
        textStatusViewHolder.commentCountTV.text = textStatus.numberofcomments.toString()

        val userProfileImageUrl = textStatus.profileurl
        val objFirebaseAuth = FirebaseAuth.getInstance()

        // Load the user's profile picture into the profile ImageView.
        Glide.with(textStatusViewHolder.profileIV.context)
            .load(userProfileImageUrl).into(textStatusViewHolder.profileIV)

        // Hide the delete status button.
        if (!objFirebaseAuth.currentUser?.email.equals(textStatus.useremail)) {
            textStatusViewHolder.deleteIV.visibility = View.INVISIBLE
        }

        val addNotifications = AddNotifications()

        // Set OnClickListener for the love react ImageView.
        textStatusViewHolder.heartIV.setOnClickListener {
            val firebaseAuth = FirebaseAuth.getInstance()

            if (firebaseAuth != null) {

                val userEmail = firebaseAuth.currentUser?.email as String
                val firebaseFirestore = FirebaseFirestore.getInstance()
                val documentID = getItem(textStatusViewHolder.adapterPosition)!!.id
                val collectionReference = firebaseFirestore.collection("TextStatus")

                // Get a reference to the user's reactions to the status.
                val emotionsReference = collectionReference
                    .document(documentID).collection("Emotions")
                    .document(userEmail)

                // Get a reference to the status.
                val statusReference = collectionReference.document(documentID)
                statusReference.get()

                emotionsReference.get().addOnCompleteListener { task ->
                    // If there is a result obtained from the task.
                    if (task.result!!.exists()) {
                        // Obtain the currently selected reaction.
                        val currentFlag = task.result?.getString("currentflag")

                        // If the user email of the text status and the current user
                        // don't match, generate a notification for the action.
                        if (textStatus.useremail != userEmail) {
                            addNotifications.generateNotification(
                                userEmail,
                                "loved", "text status",
                                textStatus.useremail
                            )
                        }

                        // If the currently selected reaction is love, update accordingly.
                        when {
                            currentFlag.equals("love") -> {
                                emotionsReference.update("currentflag", "love")

                                // If the currently selected reaction is laugh..
                            }
                            currentFlag.equals("haha") -> {
                                textStatus.numberoflovereacts++
                                textStatus.numberoflaughreacts--

                                // Update the status in Firebase.
                                statusReference.update("numberoflovereacts", textStatus.numberoflovereacts)
                                statusReference.update("numberoflaughreacts", textStatus.numberoflaughreacts)

                                // Update the current flag of the status.
                                emotionsReference.update("currentflag", "love")

                                // Update the TextViews.
                                textStatusViewHolder.loveCountTV.text = textStatus.numberoflovereacts.toString()
                                textStatusViewHolder.laughCountTV.text = textStatus.numberoflaughreacts.toString()

                                // If the currently selected reaction is sad..
                            }
                            currentFlag.equals("sad") -> {
                                textStatus.numberoflovereacts++
                                textStatus.numberofsadreacts--

                                // Update the status in Firebase.
                                statusReference.update("numberoflovereacts", textStatus.numberoflovereacts)
                                statusReference.update("numberofsadreacts", textStatus.numberofsadreacts)

                                // Update the current flag of the status.
                                emotionsReference.update("currentflag", "love")

                                // Update the TextViews.
                                textStatusViewHolder.loveCountTV.text = textStatus.numberoflovereacts.toString()
                                textStatusViewHolder.sadCountTV.text = textStatus.numberofsadreacts.toString()
                            }
                        }
                    } else {
                        val hashMap: HashMap<String, Any> = HashMap()
                        hashMap["currentflag"] = "love"

                        firebaseFirestore.collection("TextStatus")
                            .document(documentID).collection("Emotions")
                            .document(userEmail)
                            .set(hashMap)

                        textStatus.numberoflovereacts++

                        // Update the status in Firebase.
                        statusReference.update("numberoflovereacts", textStatus.numberoflovereacts)

                        // Update the current flag of the status.
                        emotionsReference.update("currentflag", "love")

                        // Update the TextView.
                        textStatusViewHolder.loveCountTV.text = textStatus.numberoflovereacts.toString()

                        // If the user email of the text status and the current user
                        // don't match, generate a notification for the action.
                        if (textStatus.useremail != userEmail) {
                            addNotifications.generateNotification(
                                userEmail,
                                "loved", "text status",
                                textStatus.useremail
                            )
                        }
                    }
                }.addOnFailureListener { e -> Log.e(TAG, "" + e.message) }
            } else {
                Log.i(TAG, "User cannot be authenticated" )
            }
        }

        // Set the OnClickListener for the laugh react ImageView.
        textStatusViewHolder.hahaIV.setOnClickListener {
            val firebaseAuth = FirebaseAuth.getInstance()

            if (firebaseAuth != null) {

                val userEmail = firebaseAuth.currentUser?.email as String
                val firebaseFirestore = FirebaseFirestore.getInstance()
                val documentID = getItem(textStatusViewHolder.adapterPosition)!!.id
                val collectionReference = firebaseFirestore.collection("TextStatus")

                // Get a reference to the user's reaction to the status.
                val documentReference = collectionReference
                    .document(documentID).collection("Emotions")
                    .document(userEmail)

                // Get a reference to the status.
                val statusReference = collectionReference.document(documentID)
                statusReference.get()

                documentReference.get().addOnCompleteListener { task ->
                    // If there is a result obtained from the task.
                    if (task.result!!.exists()) {
                        // Obtain the currently selected reaction.
                        val currentFlag = task.result?.getString("currentflag")

                        // If the user email of the text status and the current user
                        // don't match, generate a notification for the action.
                        if (textStatus.useremail != userEmail) {
                            addNotifications.generateNotification(
                                userEmail,
                                "laughed at", "text status",
                                textStatus.useremail
                            )
                        }

                        // If the currently selected reaction is laugh, update accordingly.
                        when {
                            currentFlag.equals("haha") -> {
                                documentReference.update("currentflag", "haha")

                                // If the currently selected reaction is love..
                            }
                            currentFlag.equals("love") -> {

                                textStatus.numberoflaughreacts++
                                textStatus.numberoflovereacts--

                                // Update the status in Firebase.
                                statusReference.update("numberoflaughreacts", textStatus.numberoflaughreacts)
                                statusReference.update("numberoflovereacts", textStatus.numberoflovereacts)

                                // Update the current flag of the status.
                                documentReference.update("currentflag", "haha")

                                // Update the TextViews.
                                textStatusViewHolder.loveCountTV.text = textStatus.numberoflovereacts.toString()
                                textStatusViewHolder.laughCountTV.text = textStatus.numberoflaughreacts.toString()

                                // If the currently selected reaction is sad..
                            }
                            currentFlag.equals("sad") -> {
                                textStatus.numberoflaughreacts++
                                textStatus.numberofsadreacts--

                                // Update the status in Firebase.
                                statusReference.update("numberoflaughreacts", textStatus.numberoflaughreacts)
                                statusReference.update("numberofsadreacts", textStatus.numberofsadreacts)

                                // Update the current flag of the status.
                                documentReference.update("currentflag", "haha")

                                // Update the TextViews.
                                textStatusViewHolder.laughCountTV.text = textStatus.numberoflaughreacts.toString()
                                textStatusViewHolder.sadCountTV.text = textStatus.numberofsadreacts.toString()
                            }
                        }
                    } else {
                        val hashMap: HashMap<String, Any> = HashMap()
                        hashMap["currentflag"] = "haha"

                        firebaseFirestore.collection("TextStatus")
                            .document(documentID).collection("Emotions")
                            .document(userEmail)
                            .set(hashMap)

                        textStatus.numberoflaughreacts++

                        // Update the status in Firebase.
                        statusReference.update("numberoflaughreacts", textStatus.numberoflaughreacts)

                        // Update the current flag of the status.
                        documentReference.update("currentflag", "haha")

                        // Update the TextView.
                        textStatusViewHolder.laughCountTV.text = textStatus.numberoflaughreacts.toString()

                        // If the user email of the text status and the current user
                        // don't match, generate a notification for the action.
                        if (textStatus.useremail != userEmail) {
                            addNotifications.generateNotification(
                                userEmail,
                                "laughed at", "text status",
                                textStatus.useremail
                            )
                        }
                    }
                }.addOnFailureListener { e -> Log.e(TAG, "" + e.message) }
            } else {
                Log.i(TAG, "User cannot be authenticated" )
            }
        }

        // Set the OnClickListener for the sad react ImageView.
        textStatusViewHolder.sadIV.setOnClickListener {
            val firebaseAuth = FirebaseAuth.getInstance()

            if (firebaseAuth != null) {

                val userEmail = firebaseAuth.currentUser?.email as String
                val firebaseFirestore = FirebaseFirestore.getInstance()
                val documentID = getItem(textStatusViewHolder.adapterPosition)!!.id
                val collectionReference = firebaseFirestore.collection("TextStatus")

                // Get a reference to the user's reaction to the status.
                val documentReference = collectionReference
                    .document(documentID).collection("Emotions")
                    .document(userEmail)

                // Get a reference to the status.
                val statusReference = collectionReference.document(documentID)
                statusReference.get()

                documentReference.get().addOnCompleteListener { task ->
                    // If there is a result obtained from the task.
                    if (task.result!!.exists()) {
                        // Obtain the currently selected reaction.
                        val currentFlag = task.result?.getString("currentflag")

                        // If the user email of the text status and the current user
                        // don't match, generate a notification for the action.
                        if (textStatus.useremail != userEmail) {
                            addNotifications.generateNotification(
                                userEmail,
                                "cried at", "text status",
                                textStatus.useremail
                            )
                        }

                        // If the currently selected reaction is sad, update accordingly.
                        when {
                            currentFlag.equals("sad") -> {
                                documentReference.update("currentflag", "sad")

                                // If the currently selected reaction is love..
                            }
                            currentFlag.equals("love") -> {
                                textStatus.numberofsadreacts++
                                textStatus.numberoflovereacts--

                                // Update the status in Firebase.
                                statusReference.update("numberofsadreacts", textStatus.numberofsadreacts)
                                statusReference.update("numberoflovereacts", textStatus.numberoflovereacts)

                                // Update the current flag of the status.
                                documentReference.update("currentflag", "sad")

                                // Update the TextViews.
                                textStatusViewHolder.sadCountTV.text = textStatus.numberofsadreacts.toString()
                                textStatusViewHolder.loveCountTV.text = textStatus.numberoflovereacts.toString()

                                // If the currently selected reaction is laugh..
                            }
                            currentFlag.equals("haha") -> {
                                textStatus.numberofsadreacts++
                                textStatus.numberoflaughreacts--

                                // Update the status in Firebase.
                                statusReference.update("numberofsadreacts", textStatus.numberofsadreacts)
                                statusReference.update("numberoflaughreacts", textStatus.numberoflaughreacts)

                                // Update the current flag of the status.
                                documentReference.update("currentflag", "sad")

                                // Update the TextViews.
                                textStatusViewHolder.sadCountTV.text = textStatus.numberofsadreacts.toString()
                                textStatusViewHolder.laughCountTV.text = textStatus.numberoflaughreacts.toString()
                            }
                        }
                    } else {
                        val hashMap: HashMap<String, Any> = HashMap()
                        hashMap["currentflag"] = "sad"

                        firebaseFirestore.collection("TextStatus")
                            .document(documentID).collection("Emotions")
                            .document(userEmail)
                            .set(hashMap)

                        textStatus.numberofsadreacts++

                        // Update the status in Firebase.
                        statusReference.update("numberofsadreacts", textStatus.numberofsadreacts)

                        // Update the current flag of the status.
                        documentReference.update("currentflag", "sad")

                        // Update the TextView.
                        textStatusViewHolder.sadCountTV.text = textStatus.numberofsadreacts.toString()

                        // If the user email of the text status and the current user
                        // don't match, generate a notification for the action.
                        if (textStatus.useremail != userEmail) {
                            addNotifications.generateNotification(
                                userEmail,
                                "cried at", "text status",
                                textStatus.useremail
                            )
                        }
                    }
                }.addOnFailureListener { e -> Log.e(TAG, "" + e.message) }
            } else {
                Log.i(TAG, "User cannot be authenticated" )
            }
        }

        // Set the OnClickListener for the comments ImageView.
        textStatusViewHolder.commentIV.setOnClickListener {
            val documentID = getItem(textStatusViewHolder.adapterPosition)?.id as String
            val objContext = textStatusViewHolder.commentIV.context

            val intent = Intent(objContext, TextStatusCommentsActivity::class.java)
            intent.putExtra("documentID", documentID)
            intent.putExtra("userEmailID", textStatus.useremail)
            objContext.startActivity(intent)
        }

        // Set the OnClickListener for the delete status ImageView.
        textStatusViewHolder.deleteIV.setOnClickListener {
            val objFirebaseAuth = FirebaseAuth.getInstance()
            val documentID = getItem(textStatusViewHolder.adapterPosition)?.id as String

            // Delete the status if it belongs to the current user.
            if (objFirebaseAuth.currentUser?.email.equals(textStatus.useremail)) {
                val firebaseFirestore = FirebaseFirestore.getInstance()

                firebaseFirestore.collection("TextStatus")
                    .document(documentID)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(textStatusViewHolder.deleteIV.context,
                            R.string.status_deleted_msg, Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener { e -> Log.e(TAG, "" + e.message) }
            } else {
                Log.i(TAG, "No user logged in")
            }
        }

        // Set the OnClickListener for the favourite ImageView.
        textStatusViewHolder.favouriteIV.setOnClickListener {
            val documentID = getItem(textStatusViewHolder.adapterPosition)?.id as String
            val firebaseFirestore = FirebaseFirestore.getInstance()
            val statusDocumentReference = firebaseFirestore.collection("TextStatus")
                .document(documentID)

            // Upload the favourited status to favourites.
            statusDocumentReference.get()
                .addOnSuccessListener { documentSnapshot ->
                    val userEmail = documentSnapshot?.getString("useremail") as String
                    val firstName = documentSnapshot.getString("firstname") as String
                    val status = documentSnapshot.getString("status") as String
                    val profileURL = documentSnapshot.getString("profileurl") as String
                    val statusTimestamp = documentSnapshot.getTimestamp("timestamp") as Timestamp

                    val statusHashMap: HashMap<String,Any> = HashMap()

                    statusHashMap["useremail"] = userEmail
                    statusHashMap["firstname"] = firstName
                    statusHashMap["status"] = status
                    statusHashMap["profileurl"] = profileURL
                    statusHashMap["timestamp"] = statusTimestamp

                    if (objFirebaseAuth != null) {
                        val currentUserEmail = objFirebaseAuth.currentUser?.email as String


                        // Initialize a document reference for the status in the UserFavourite collection.
                        val favouriteStatusDocumentRef = firebaseFirestore.collection("UserFavourite")
                            .document(currentUserEmail)
                            .collection("FavouriteTextStatus")
                            .document(documentID)

                        // Get the document and upload the text status to Firebase if
                        // is not already favourited by the user.
                        favouriteStatusDocumentRef.get().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val document = task.result

                                // If the document exists in favourites, show
                                // the appropriate Toast message.
                                if (document!!.exists()) {
                                    Toast.makeText(
                                        textStatusViewHolder.favouriteIV.context,
                                        R.string.already_favourited_msg,
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // Generate the notification for the action.
                                    addNotifications.generateNotification(userEmail,
                                        "favourited", "text status",
                                        textStatus.useremail)
                                } else {
                                    // Upload the text status to the favourites collection.
                                    firebaseFirestore.collection("UserFavourite")
                                        .document(currentUserEmail)
                                        .collection("FavouriteTextStatus")
                                        .document(documentID)
                                        .set(statusHashMap)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                textStatusViewHolder.favouriteIV.context,
                                                "Status added to favourites",
                                                Toast.LENGTH_SHORT).show()
                                        }.addOnFailureListener { e ->
                                            Log.e(TAG,
                                                Resources.getSystem().getString(
                                                    R.string.logcat_favourite_status_fail)
                                                        + e.message)
                                        }
                                }
                            }
                        }.addOnFailureListener { e ->
                            Log.e(TAG,Resources.getSystem().getString(
                                R.string.logcat_obtain_document_reference_fail) + e.message)
                        }
                    } else {
                        Log.i(TAG, Resources.getSystem().getString(R.string.logcat_no_user))
                    }
                }.addOnFailureListener { e -> Log.e(TAG, "" + e.message) }
        }

        // Set the OnClickListener for the profile picture ImageView.
        textStatusViewHolder.profileIV.setOnClickListener {
            val profileIntent = Intent(textStatusViewHolder.profileIV.context, ProfileActivity::class.java)
            profileIntent.putExtra("fragment", "profile")
            profileIntent.putExtra("userid", textStatus.useremail)
            textStatusViewHolder.profileIV.context.startActivity(profileIntent)
        }
    }

    /**
     * Create a new RecyclerView.ViewHolder.
     *
     * @param parent The ViewGroup into which the new View will be added after it is
     * bound to an adapter position.
     * @param viewType The view type of the new View.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextStatusViewHolder {
        return TextStatusViewHolder(LayoutInflater.from(parent.context).inflate(
        R.layout.text_status_layout, parent, false))
    }

    /**
     * Get the current date and time.
     *
     * @return The current date and time formatted as a String.
     */
    private fun getDateFormatted(date: Date): String {
        var simpleDateFormat = SimpleDateFormat("d MMM yyyy 'at' HH:mm", Locale.UK)

        val longDate = date.time

        // Change the format if date is today.
        if (DateUtils.isToday(longDate)) {
            simpleDateFormat = SimpleDateFormat("'Today at' HH:mm", Locale.UK)
        }

        return simpleDateFormat.format(date)
    }


    /**
     * Inner RecyclerView.ViewHolder class describes an item view and metadata
     * about its place within the RecyclerView.
     *
     * @param itemView The view object that is to be added to the RecyclerView.
     */
    inner class TextStatusViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profileIV: ImageView = itemView.findViewById(R.id.text_status_layout_profileIV)
        var heartIV: ImageView = itemView.findViewById(R.id.text_status_layout_heartIV)
        var hahaIV: ImageView = itemView.findViewById(R.id.text_status_Layout_hahaIV)
        var sadIV: ImageView = itemView.findViewById(R.id.text_status_Layout_sadIV)
        var deleteIV: ImageView = itemView.findViewById(R.id.text_status_Layout_deleteIV)
        var commentIV: ImageView = itemView.findViewById(R.id.text_status_Layout_commentsIV)
        var favouriteIV: ImageView = itemView.findViewById(R.id.text_status_layout_favouriteIV)
        var firstNameTV: TextView = itemView.findViewById(R.id.text_status_layout_firstNameTV)
        var dateTimeTV: TextView = itemView.findViewById(R.id.text_status_layout_dateTV)
        var userStatusTV: TextView = itemView.findViewById(R.id.text_status_layout_textStatusTV)
        var loveCountTV: TextView = itemView.findViewById(R.id.text_status_layout_heartCountTV)
        var laughCountTV: TextView = itemView.findViewById(R.id.text_status_layout_hahaCountTV)
        var sadCountTV: TextView = itemView.findViewById(R.id.text_status_layout_sadCountTV)
        var commentCountTV: TextView = itemView.findViewById(R.id.text_status_layout_commentCountTV)
    }
}