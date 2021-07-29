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
import com.example.fittrackmobile.modelClasses.FavouriteTextStatus
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import javax.annotation.Nonnull
import com.example.fittrackmobile.R
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class FavouriteTextStatusAdapter(options: FirestoreRecyclerOptions<FavouriteTextStatus>) :
    FirestoreRecyclerAdapter<FavouriteTextStatus,
            FavouriteTextStatusAdapter.FavouriteTextStatusViewHolder>(options) {

    // Class variables.
    private val TAG = "FavouriteTextStatusAdapter"

    /**
     * Called by RecyclerView to display the data at the specified position.
     * Will update the contents of the itemView to reflect the item at the
     * given position.
     *
     * @param favouriteTextStatusViewHolder The RecyclerView.ViewHolder object used to describe
     * each itemView.
     * @param i The position of the item within the adapter's data set.
     * @param favouriteTextStatus Text status object used to populate the itemViews with data.
     */
    override fun onBindViewHolder(favouriteTextStatusViewHolder: FavouriteTextStatusViewHolder,
                                  i: Int, favouriteTextStatus: FavouriteTextStatus) {
        favouriteTextStatusViewHolder.firstNameTV.text = favouriteTextStatus.firstname
        favouriteTextStatusViewHolder.dateTV.text = getDateFormatted(favouriteTextStatus.timestamp.toDate())
        favouriteTextStatusViewHolder.statusTV.text = favouriteTextStatus.status

        val userProfileImageUrl = favouriteTextStatus.profileurl

        // Load the user's profile picture into the profile ImageView.
        Glide.with(favouriteTextStatusViewHolder.profilePicIV.context)
            .load(userProfileImageUrl).into(favouriteTextStatusViewHolder.profilePicIV)

        // Set the OnClickListener for the remove favourite ImageView.
        favouriteTextStatusViewHolder.removeIV.setOnClickListener {
            val firebaseAuth = FirebaseAuth.getInstance()

            if (firebaseAuth != null) {
                val userEmail = firebaseAuth.currentUser?.email as String
                val firebaseFirestore = FirebaseFirestore.getInstance()

                firebaseFirestore.collection("UserFavourite").document(userEmail)
                    .collection("FavouriteTextStatus")
                    .document(
                        snapshots.getSnapshot(favouriteTextStatusViewHolder.adapterPosition).id
                    )
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(favouriteTextStatusViewHolder.removeIV.context,
                            R.string.status_deleted_msg, Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener { e -> Log.e(TAG, "Couldn't remove status: " + e.message) }
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
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouriteTextStatusViewHolder {
        return FavouriteTextStatusViewHolder(LayoutInflater.from(parent.context).inflate(
            R.layout.favourite_ts_layout, parent, false))
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
    inner class FavouriteTextStatusViewHolder(@Nonnull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var removeIV: ImageView = itemView.findViewById(R.id.favourite_ts_layout_removeStatusIV)
        var profilePicIV: ImageView = itemView.findViewById(R.id.favourite_ts_layout_profilePicIV)
        var firstNameTV: TextView = itemView.findViewById(R.id.favourite_ts_layout_firstNameTV)
        var dateTV: TextView = itemView.findViewById(R.id.favourite_ts_layout_dateTV)
        var statusTV: TextView = itemView.findViewById(R.id.favourite_ts_layout_statusTV)

    }
}