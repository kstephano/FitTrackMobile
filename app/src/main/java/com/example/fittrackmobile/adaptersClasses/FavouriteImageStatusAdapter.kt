package com.example.fittrackmobile.adaptersClasses

import android.content.res.Resources
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fittrackmobile.modelClasses.FavouriteImageStatus
import com.example.fittrackmobile.R
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import javax.annotation.Nonnull

/**
 * Adapter to display favourited image statuses for a user.
 */
class FavouriteImageStatusAdapter(options: FirestoreRecyclerOptions<FavouriteImageStatus>) :
    FirestoreRecyclerAdapter<FavouriteImageStatus,
            FavouriteImageStatusAdapter.FavouriteImageStatusViewHolder>(options) {

    // Class variables.
    private val TAG = "FavouriteImageStatusAdapter"

    /**
     * Called by RecyclerView to display the data at the specified position.
     * Will update the contents of the itemView to reflect the item at the
     * given position.
     *
     * @param favouriteImageStatusViewHolder The RecyclerView.ViewHolder object used to describe
     * each itemView.
     * @param i The position of the item within the adapter's data set.
     * @param favouriteImageStatus Image status object used to populate the itemViews with data.
     */
    override fun onBindViewHolder(favouriteImageStatusViewHolder: FavouriteImageStatusViewHolder,
                                  i: Int, favouriteImageStatus: FavouriteImageStatus) {

        favouriteImageStatusViewHolder.firstNameTV.text = favouriteImageStatus.firstname
        favouriteImageStatusViewHolder.dateTV.text = getDateFormatted(favouriteImageStatus.timestamp.toDate())
        favouriteImageStatusViewHolder.imageDescriptionTV.text = favouriteImageStatus.status

        val userProfileImageUrl = favouriteImageStatus.profileurl
        val statusImageUrl = favouriteImageStatus.statusimageurl

        // Load the user's profile picture into the profile ImageView.
        Glide.with(favouriteImageStatusViewHolder.profilePicIV.context)
            .load(userProfileImageUrl).into(favouriteImageStatusViewHolder.profilePicIV)

        // Load the status image into the ImageView.
        Glide.with(favouriteImageStatusViewHolder.imageIV.context)
            .load(statusImageUrl).into(favouriteImageStatusViewHolder.imageIV)

        // Set the OnClickListener for the remove favourite ImageView.
        favouriteImageStatusViewHolder.removeStatusIV.setOnClickListener {
            val firebaseAuth = FirebaseAuth.getInstance()

            if (firebaseAuth != null) {
                val userEmail = firebaseAuth.currentUser?.email as String
                val firebaseFirestore = FirebaseFirestore.getInstance()

                firebaseFirestore.collection("UserFavourite").document(userEmail)
                    .collection("FavouriteImageStatus")
                    .document(
                        snapshots.getSnapshot(favouriteImageStatusViewHolder.adapterPosition).id
                    )
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(favouriteImageStatusViewHolder.removeStatusIV.context,
                            R.string.status_deleted_msg, Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener { e ->
                        Log.e(TAG, Resources.getSystem().
                            getString(R.string.logcat_remove_favourite_fail) + e.message) }
            } else {
                Log.i(TAG, Resources.getSystem().getString(R.string.logcat_no_user))
            }
        }
    }

    /**
     * Create a new RecyclerView.ViewHolder.
     *
     * @param parent The ViewGroup into which the new View will be added after it is
     * bound to an adapter position.
     * @param viewType The view type of the new View.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouriteImageStatusViewHolder {
        return FavouriteImageStatusViewHolder(LayoutInflater.from(parent.context).inflate(
            R.layout.favourite_is_layout, parent, false))
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
    inner class FavouriteImageStatusViewHolder(@Nonnull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageIV: ImageView = itemView.findViewById(R.id.favourite_is_imageIV)
        var profilePicIV: ImageView = itemView.findViewById(R.id.favourite_is_profilePicIV)
        var removeStatusIV: ImageView = itemView.findViewById(R.id.favourite_is_removeStatusIV)
        var firstNameTV: TextView = itemView.findViewById(R.id.favourite_is_firstNameTV)
        var dateTV: TextView = itemView.findViewById(R.id.favourite_is_dateTV)
        var imageDescriptionTV: TextView = itemView.findViewById(R.id.favourite_is_imageDescriptionTV)
    }
}