package com.example.fittrackmobile.adaptersClasses

import android.content.Intent
import android.content.res.Resources
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fittrackmobile.appClasses.AddNotifications
import com.example.fittrackmobile.modelClasses.ImageStatus
import com.example.fittrackmobile.R
import com.example.fittrackmobile.activities.ImageStatusCommentsActivity
import com.example.fittrackmobile.activities.ProfileActivity
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

/**
 * Creates a new RecyclerView paging adapter for image statuses that listens to a Firestore Query.
 */
class ImageStatusAdapter(options: FirestorePagingOptions<ImageStatus>) :
    FirestorePagingAdapter<ImageStatus, ImageStatusAdapter.ImageStatusViewHolder>(options) {

    // Class variables.
    private val TAG = "ImageStatusAdapter"

    /**
     * Called by RecyclerView to display the data at the specified position.
     * Will update the contents of the itemView to reflect the item at the
     * given position.
     *
     * @param imageStatusViewHolder The RecyclerView.ViewHolder object used to describe
     * each itemView.
     * @param i The position of the item within the adapter's data set.
     * @param imageStatus Text status object used to populate the itemViews with data.
     */
    override fun onBindViewHolder(imageStatusViewHolder: ImageStatusViewHolder, i: Int, imageStatus: ImageStatus) {

        imageStatusViewHolder.progressBar.visibility = View.VISIBLE
        imageStatusViewHolder.firstNameTV.text = imageStatus.firstname
        imageStatusViewHolder.dateTimeTV.text = getDateFormatted(imageStatus.timestamp.toDate())
        imageStatusViewHolder.imageDescriptionTV.text = imageStatus.status
        imageStatusViewHolder.loveCountTV.text = imageStatus.numberoflovereacts.toString()
        imageStatusViewHolder.laughCountTV.text = imageStatus.numberoflaughreacts.toString()
        imageStatusViewHolder.sadCountTV.text = imageStatus.numberofsadreacts.toString()
        imageStatusViewHolder.commentCountTV.text = imageStatus.numberofcomments.toString()

        val profileImageURL = imageStatus.profileurl
        val statusImageURL = imageStatus.statusimageurl

        // Load profile picture into image view.
        Glide.with(imageStatusViewHolder.profileIV.context).load(profileImageURL)
            .into(imageStatusViewHolder.profileIV)

        // Load the status image into imag view.
        Glide.with(imageStatusViewHolder.imageStatusIV.context).load(statusImageURL)
            .into(imageStatusViewHolder.imageStatusIV)

        imageStatusViewHolder.progressBar.visibility = View.INVISIBLE

        val addNotifications = AddNotifications()

        // Set the OnClickListener for the heart react ImageView.
        imageStatusViewHolder.heartIV.setOnClickListener {
            val firebaseAuth = FirebaseAuth.getInstance()

            if (firebaseAuth != null) {

                val userEmail = firebaseAuth.currentUser?.email as String
                val firebaseFirestore = FirebaseFirestore.getInstance()
                val documentID = getItem(imageStatusViewHolder.adapterPosition)!!.id
                val collectionReference = firebaseFirestore.collection("ImageStatus")

                // Get a reference to the user's reaction to the status.
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
                        if (imageStatus.useremail != userEmail) {
                            addNotifications.generateNotification(
                                userEmail,
                                "loved", "image status",
                                imageStatus.useremail
                            )
                        }

                        // If the currently selected reaction is love, update accordingly.
                        when {
                            currentFlag.equals("love") -> {
                                emotionsReference.update("currentflag", "love")

                                // If the currently selected reaction is laugh..
                            }
                            currentFlag.equals("haha") -> {
                                imageStatus.numberoflovereacts++
                                imageStatus.numberoflaughreacts--

                                // Update the status in Firebase.
                                statusReference.update("numberoflovereacts", imageStatus.numberoflovereacts)
                                statusReference.update("numberoflaughreacts", imageStatus.numberoflaughreacts)

                                // Update the current flag of the status.
                                emotionsReference.update("currentflag", "love")

                                // Update the TextViews.
                                imageStatusViewHolder.loveCountTV.text = imageStatus.numberoflovereacts.toString()
                                imageStatusViewHolder.laughCountTV.text = imageStatus.numberoflaughreacts.toString()

                                // If the currently selected reaction is sad..
                            }
                            currentFlag.equals("sad") -> {
                                imageStatus.numberoflovereacts++
                                imageStatus.numberofsadreacts--

                                // Update the status in Firebase.
                                statusReference.update("numberoflovereacts", imageStatus.numberoflovereacts)
                                statusReference.update("numberofsadreacts", imageStatus.numberofsadreacts)

                                // Update the current flag of the status.
                                emotionsReference.update("currentflag", "love")

                                // Update the TextViews.
                                imageStatusViewHolder.loveCountTV.text = imageStatus.numberoflovereacts.toString()
                                imageStatusViewHolder.sadCountTV.text = imageStatus.numberofsadreacts.toString()
                            }
                        }
                    } else {
                        val hashMap: HashMap<String, Any> = HashMap()
                        hashMap["currentflag"] = "love"

                        firebaseFirestore.collection("ImageStatus")
                            .document(documentID).collection("Emotions")
                            .document(userEmail)
                            .set(hashMap)

                        imageStatus.numberoflovereacts++

                        // Update the status in Firebase.
                        statusReference.update("numberoflovereacts", imageStatus.numberoflovereacts)

                        // Update the current flag of the status.
                        emotionsReference.update("currentflag", "love")

                        // Update the TextView.
                        imageStatusViewHolder.loveCountTV.text = imageStatus.numberoflovereacts.toString()

                        // If the user email of the text status and the current user
                        // don't match, generate a notification for the action.
                        if (imageStatus.useremail != userEmail) {
                            addNotifications.generateNotification(
                                userEmail,
                                "loved", "image status",
                                imageStatus.useremail
                            )
                        }
                    }
                }.addOnFailureListener { e -> Log.e(TAG, "" + e.message) }
            } else {
                Log.i(TAG, "User cannot be authenticated" )
            }
        }

        // Set the OnClickListener for the laugh react ImageView.
        imageStatusViewHolder.hahaIV.setOnClickListener {
            val firebaseAuth = FirebaseAuth.getInstance()

            if (firebaseAuth != null) {

                val userEmail = firebaseAuth.currentUser?.email as String
                val firebaseFirestore = FirebaseFirestore.getInstance()
                val documentID = getItem(imageStatusViewHolder.adapterPosition)!!.id
                val collectionReference = firebaseFirestore.collection("ImageStatus")

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
                        if (imageStatus.useremail != userEmail) {
                            addNotifications.generateNotification(
                                userEmail,
                                "laughed at", "image status",
                                imageStatus.useremail
                            )
                        }

                        // If the currently selected reaction is laugh, update accordingly.
                        when {
                            currentFlag.equals("haha") -> {
                                documentReference.update("currentflag", "haha")

                                // If the currently selected reaction is love..
                            }
                            currentFlag.equals("love") -> {
                                imageStatus.numberoflaughreacts++
                                imageStatus.numberoflovereacts--

                                // Update the status in Firebase.
                                statusReference.update("numberoflaughreacts", imageStatus.numberoflaughreacts)
                                statusReference.update("numberoflovereacts", imageStatus.numberoflovereacts)

                                // Update the current flag of the status.
                                documentReference.update("currentflag", "haha")

                                // Update the TextViews.
                                imageStatusViewHolder.loveCountTV.text = imageStatus.numberoflovereacts.toString()
                                imageStatusViewHolder.laughCountTV.text = imageStatus.numberoflaughreacts.toString()

                                // If the currently selected reaction is sad..
                            }
                            currentFlag.equals("sad") -> {
                                imageStatus.numberoflaughreacts++
                                imageStatus.numberofsadreacts--

                                // Update the status in Firebase.
                                statusReference.update("numberoflaughreacts", imageStatus.numberoflaughreacts)
                                statusReference.update("numberofsadreacts", imageStatus.numberofsadreacts)

                                // Update the current flag of the status.
                                documentReference.update("currentflag", "haha")

                                // Update the TextViews.
                                imageStatusViewHolder.laughCountTV.text = imageStatus.numberoflaughreacts.toString()
                                imageStatusViewHolder.sadCountTV.text = imageStatus.numberofsadreacts.toString()
                            }
                        }
                    } else {
                        val hashMap: HashMap<String, Any> = HashMap()
                        hashMap["currentflag"] = "haha"

                        firebaseFirestore.collection("ImageStatus")
                            .document(documentID).collection("Emotions")
                            .document(userEmail)
                            .set(hashMap)

                        imageStatus.numberoflaughreacts++

                        // Update the status in Firebase.
                        statusReference.update("numberoflaughreacts", imageStatus.numberoflaughreacts)

                        // Update the current flag of the status.
                        documentReference.update("currentflag", "haha")

                        // Update the TextView.
                        imageStatusViewHolder.laughCountTV.text = imageStatus.numberoflaughreacts.toString()

                        // If the user email of the text status and the current user
                        // don't match, generate a notification for the action.
                        if (imageStatus.useremail != userEmail) {
                            addNotifications.generateNotification(
                                userEmail,
                                "laughed at", "image status",
                                imageStatus.useremail
                            )
                        }
                    }
                }.addOnFailureListener { e -> Log.e(TAG, "" + e.message) }
            } else {
                Log.i(TAG, "User cannot be authenticated" )
            }
        }

        // Set the OnClickListener for the sad react ImageView.
        imageStatusViewHolder.sadIV.setOnClickListener {
            val firebaseAuth = FirebaseAuth.getInstance()

            if (firebaseAuth != null) {

                val userEmail = firebaseAuth.currentUser?.email as String
                val firebaseFirestore = FirebaseFirestore.getInstance()
                val documentID = getItem(imageStatusViewHolder.adapterPosition)!!.id
                val collectionReference = firebaseFirestore.collection("ImageStatus")

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
                        if (imageStatus.useremail != userEmail) {
                            addNotifications.generateNotification(
                                userEmail,
                                "cried at", "image status",
                                imageStatus.useremail
                            )
                        }

                        // If the currently selected reaction is sad, update accordingly.
                        when {
                            currentFlag.equals("sad") -> {
                                documentReference.update("currentflag", "sad")

                                // If the currently selected reaction is love..
                            }
                            currentFlag.equals("love") -> {
                                imageStatus.numberofsadreacts++
                                imageStatus.numberoflovereacts--

                                // Update the status in Firebase.
                                statusReference.update("numberofsadreacts", imageStatus.numberofsadreacts)
                                statusReference.update("numberoflovereacts", imageStatus.numberoflovereacts)

                                // Update the current flag of the status.
                                documentReference.update("currentflag", "sad")

                                // Update the TextViews.
                                imageStatusViewHolder.sadCountTV.text = imageStatus.numberofsadreacts.toString()
                                imageStatusViewHolder.loveCountTV.text = imageStatus.numberoflovereacts.toString()

                                // If the currently selected reaction is laugh..
                            }
                            currentFlag.equals("haha") -> {
                                imageStatus.numberofsadreacts++
                                imageStatus.numberoflaughreacts--

                                // Update the status in Firebase.
                                statusReference.update("numberofsadreacts", imageStatus.numberofsadreacts)
                                statusReference.update("numberoflaughreacts", imageStatus.numberoflaughreacts)

                                // Update the current flag of the status.
                                documentReference.update("currentflag", "sad")

                                // Update the TextViews.
                                imageStatusViewHolder.sadCountTV.text = imageStatus.numberofsadreacts.toString()
                                imageStatusViewHolder.laughCountTV.text = imageStatus.numberoflovereacts.toString()
                            }
                        }
                    } else {
                        val hashMap: HashMap<String, Any> = HashMap()
                        hashMap["currentflag"] = "sad"

                        firebaseFirestore.collection("ImageStatus")
                            .document(documentID).collection("Emotions")
                            .document(userEmail)
                            .set(hashMap)

                        imageStatus.numberofsadreacts++

                        // Update the status in Firebase.
                        statusReference.update("numberofsadreacts", imageStatus.numberofsadreacts)

                        // Update the current flag of the status.
                        documentReference.update("currentflag", "sad")

                        // Update the TextView.
                        imageStatusViewHolder.sadCountTV.text = imageStatus.numberofsadreacts.toString()

                        // If the user email of the text status and the current user
                        // don't match, generate a notification for the action.
                        if (imageStatus.useremail != userEmail) {
                            addNotifications.generateNotification(
                                userEmail,
                                "cried at", "image status",
                                imageStatus.useremail
                            )
                        }
                    }
                }.addOnFailureListener { e -> Log.e(TAG, "" + e.message) }
            } else {
                Log.i(TAG, "User cannot be authenticated" )
            }
        }

        // Set the OnClickListener for the comment ImageView.
        imageStatusViewHolder.commentIV.setOnClickListener {
            val documentID = getItem(imageStatusViewHolder.adapterPosition)?.id as String
            val objContext = imageStatusViewHolder.commentIV.context

            val intent = Intent(objContext, ImageStatusCommentsActivity::class.java)
            intent.putExtra("documentID", documentID)
            intent.putExtra("userEmailID", imageStatus.useremail)
            objContext.startActivity(intent)
        }

        // Set the OnClickListener for the delete status ImageView.
        imageStatusViewHolder.deleteIV.setOnClickListener {
            val objFirebaseAuth = FirebaseAuth.getInstance()
            val documentID = getItem(imageStatusViewHolder.adapterPosition)?.id as String

            // Delete the status if it belongs to the current user.
            if (objFirebaseAuth.currentUser?.email.equals(imageStatus.useremail)) {
                val firebaseFirestore = FirebaseFirestore.getInstance()

                firebaseFirestore.collection("ImageStatus")
                    .document(documentID)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(imageStatusViewHolder.deleteIV.context,
                            R.string.status_deleted_msg, Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener { e -> Log.e(TAG, "" + e.message) }
            } else {
                Log.i(TAG, "No user logged in")
            }
        }

        // Set the OnClickListener for the favourite status ImageView.
        imageStatusViewHolder.favouriteIV.setOnClickListener {
            val documentID = getItem(imageStatusViewHolder.adapterPosition)?.id as String
            val firebaseFirestore = FirebaseFirestore.getInstance()
            val statusDocumentReference = firebaseFirestore.collection("ImageStatus")
                .document(documentID)

            // Upload the favourited status to favourites.
            statusDocumentReference.get()
                .addOnSuccessListener { documentSnapshot ->
                    val userEmail = documentSnapshot?.getString("useremail") as String
                    val firstName = documentSnapshot.getString("firstname") as String
                    val status = documentSnapshot.getString("status") as String
                    val profileURL = documentSnapshot.getString("profileurl") as String
                    val statusImageURL = documentSnapshot.getString("statusimageurl") as String
                    val statusTimestamp = documentSnapshot.getTimestamp("timestamp") as Timestamp

                    val firebaseAuth = FirebaseAuth.getInstance()
                    val statusHashMap: HashMap<String,Any> = HashMap()

                    statusHashMap["useremail"] = userEmail
                    statusHashMap["firstname"] = firstName
                    statusHashMap["status"] = status
                    statusHashMap["profileurl"] = profileURL
                    statusHashMap["timestamp"] = statusTimestamp
                    statusHashMap["statusimageurl"] = statusImageURL

                    if (firebaseAuth != null) {
                        val currentUserEmail = firebaseAuth.currentUser?.email as String

                        // Initialize a document reference for the status in the UserFavourite collection.
                        val favouriteStatusDocumentRef = firebaseFirestore.collection("UserFavourite")
                            .document(currentUserEmail)
                            .collection("FavouriteImageStatus")
                            .document(documentID)

                        // Get the document and upload the image status to Firebase if
                        // is not already favourited by the user.
                        favouriteStatusDocumentRef.get().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val document = task.result

                                // If the document exists in favourites, show
                                // the appropriate Toast message.
                                if (document!!.exists()) {
                                    Toast.makeText(
                                        imageStatusViewHolder.favouriteIV.context,
                                        R.string.already_favourited_msg,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    // Upload the image status to the favourites collection.
                                    firebaseFirestore.collection("UserFavourite")
                                        .document(currentUserEmail)
                                        .collection("FavouriteImageStatus")
                                        .document(documentID).set(statusHashMap)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                imageStatusViewHolder.favouriteIV.context,
                                                R.string.favourite_status_added_msg,
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            // Generate the notification for the action.
                                            addNotifications.generateNotification(userEmail,
                                                "favourited", "image status",
                                                imageStatus.useremail)
                                        }.addOnFailureListener { e ->
                                            Log.i(
                                                TAG,
                                                Resources.getSystem().getString(
                                                    R.string.logcat_favourite_status_fail)
                                                        + e.message)
                                        }
                                }
                            }
                        }.addOnFailureListener { e ->
                            Log.e(TAG,
                                Resources.getSystem().getString(
                                    R.string.logcat_obtain_document_reference_fail)
                                        + e.message)
                        }
                    } else {
                        Log.i(TAG, Resources.getSystem().getString(
                            R.string.logcat_no_user))
                    }
                }.addOnFailureListener { e ->
                    Log.e(TAG,
                        Resources.getSystem().getString(
                            R.string.logcat_obtain_document_reference_fail)
                                + e.message)
                }
        }

        // Set the OnClickListener for the profile picture ImageView.
        imageStatusViewHolder.profileIV.setOnClickListener {
            val profileIntent = Intent(imageStatusViewHolder.profileIV.context, ProfileActivity::class.java)
            profileIntent.putExtra("fragment", "profile")
            profileIntent.putExtra("userid", imageStatus.useremail)
            imageStatusViewHolder.profileIV.context.startActivity(profileIntent)
        }
    }

    /**
     * Create a new RecyclerView.ViewHolder.
     *
     * @param parent The ViewGroup into which the new View will be added after it is
     * bound to an adapter position.
     * @param viewType The view type of the new View.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageStatusViewHolder {
        return ImageStatusViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.image_status_layout, parent, false))
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
    inner class ImageStatusViewHolder(@NonNull itemView: View): RecyclerView.ViewHolder(itemView) {
        var imageStatusIV: ImageView = itemView.findViewById(R.id.image_status_layout_MainImageIV)
        var profileIV: ImageView = itemView.findViewById(R.id.image_status_layout_ProfilePicIV)
        var heartIV: ImageView = itemView.findViewById(R.id.image_status_layout_heartIV)
        var hahaIV: ImageView = itemView.findViewById(R.id.image_status_layout_hahaIV)
        var sadIV: ImageView = itemView.findViewById(R.id.image_status_layout_sadIV)
        var deleteIV: ImageView = itemView.findViewById(R.id.image_status_layout_deleteIV)
        var commentIV: ImageView = itemView.findViewById(R.id.image_status_layout_commentsIV)
        var favouriteIV: ImageView = itemView.findViewById(R.id.image_status_layout_favouriteStatusIV)
        var imageDescriptionTV: TextView = itemView.findViewById(R.id.image_status_layout_imageDescriptionTV)
        var firstNameTV: TextView = itemView.findViewById(R.id.image_status_layout_userNameTV)
        var dateTimeTV: TextView = itemView.findViewById(R.id.image_status_layout_dateTV)
        var loveCountTV: TextView = itemView.findViewById(R.id.image_status_layout_heartCountTV)
        var laughCountTV: TextView = itemView.findViewById(R.id.image_status_layout_hahaCountTV)
        var sadCountTV: TextView = itemView.findViewById(R.id.image_status_layout_sadCountTV)
        var commentCountTV: TextView = itemView.findViewById(R.id.image_status_layout_commentCountTV)
        var progressBar: ProgressBar = itemView.findViewById(R.id.image_status_layout_progressBar)
    }
}