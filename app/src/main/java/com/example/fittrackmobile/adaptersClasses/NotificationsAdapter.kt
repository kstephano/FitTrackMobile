package com.example.fittrackmobile.adaptersClasses

import android.content.Intent
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fittrackmobile.modelClasses.Notification
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.example.fittrackmobile.R
import com.example.fittrackmobile.activities.ProfileActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class NotificationsAdapter(options: FirestoreRecyclerOptions<Notification>) :
    FirestoreRecyclerAdapter<Notification, NotificationsAdapter.NotificationsViewHolder>(options) {

    val firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * Called by RecyclerView to display the data at the specified position.
     * Will update the contents of the itemView to reflect the item at the
     * given position.
     *
     * @param notificationsViewHolder The RecyclerView.ViewHolder object used to describe
     * each itemView.
     * @param i The position of the item within the adapter's data set.
     * @param notification Notification object used to populate the itemViews with data.
     */
    override fun onBindViewHolder(notificationsViewHolder: NotificationsViewHolder, i: Int, notification: Notification) {
        val action: String = notification.action
        val type: String = notification.type
        var notificationMessage = "$action your $type"

        if (action == "friend request" && type == "sent") {
            notificationMessage = "sent you a friend request"
        } else if (action == "friend request" && type == "accepted") {
            notificationMessage = "accepted your friend request"
        }

        notificationsViewHolder.userEmailTV.text = notification.email
        notificationsViewHolder.userActionTV.text = notificationMessage
        notificationsViewHolder.dateTV.text = getDateFormatted(notification.timestamp.toDate())

        // Set the appropriate action icon.
        when (action) {
            "loved" -> {
                notificationsViewHolder.actionIconIV.setImageResource(R.drawable.ic_heart)
            }
            "laughed at" -> {
                notificationsViewHolder.actionIconIV.setImageResource(R.drawable.haha)
            }
            "cried at" -> {
                notificationsViewHolder.actionIconIV.setImageResource(R.drawable.crying)
            }
            "commented on" -> {
                notificationsViewHolder.actionIconIV.setImageResource(R.drawable.comments)
            }
            "friend request" -> {
                notificationsViewHolder.actionIconIV.setImageResource(R.drawable.ic_friend_request)
            }
        }

        // Load in the user profile picture of the user who sent the notification.
        firebaseFirestore.collection("Users")
            .document(notification.email)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val profilePicUrl = documentSnapshot.getString("profileimageurl")

                // Load the user profile picture into the ImageView.
                Glide.with(notificationsViewHolder.profilePicIV.context)
                    .load(profilePicUrl).into(notificationsViewHolder.profilePicIV)
            }

        // Set the OnClickListener for the profile picture to link the user's profile.
        notificationsViewHolder.profilePicIV.setOnClickListener {
            val profileIntent = Intent(notificationsViewHolder.profilePicIV.context, ProfileActivity::class.java)
            profileIntent.putExtra("fragment", "profile")
            profileIntent.putExtra("userid", notification.email)
            notificationsViewHolder.profilePicIV.context.startActivity(profileIntent)
        }
    }

    /**
     * Create a new RecyclerView.ViewHolder.
     *
     * @param parent The ViewGroup into which the new View will be added after it is
     * bound to an adapter position.
     * @param viewType The view type of the new View.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationsViewHolder {
        return NotificationsViewHolder(LayoutInflater.from(parent.context).inflate(
                R.layout.notification_layout, parent, false))
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
    inner class NotificationsViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePicIV: ImageView = itemView.findViewById(R.id.notification_layout_profilePicIV)
        val userEmailTV: TextView = itemView.findViewById(R.id.notification_layout_userEmailTV)
        val userActionTV: TextView = itemView.findViewById(R.id.notification_layout_actionTV)
        val actionIconIV: ImageView = itemView.findViewById(R.id.notification_layout_actionIconIV)
        val dateTV: TextView = itemView.findViewById(R.id.notification_layout_dateTV)
    }
}