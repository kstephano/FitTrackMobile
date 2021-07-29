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
import com.example.fittrackmobile.modelClasses.Comment
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.example.fittrackmobile.R
import com.example.fittrackmobile.activities.ProfileActivity
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import java.text.SimpleDateFormat
import java.util.*

/**
 * Creates a new RecyclerView adapter for comments that listens to a Firestore Query.
 */
class CommentAdapter(options: FirestoreRecyclerOptions<Comment>) :
    FirestoreRecyclerAdapter<Comment, CommentAdapter.TextCommentsViewHolder>(options) {

    /**
     * Create a new RecyclerView.ViewHolder.
     *
     * @param parent The ViewGroup into which the new View will be added after it is
     * bound to an adapter position.
     * @param viewType The view type of the new View.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextCommentsViewHolder {
        return TextCommentsViewHolder(LayoutInflater.from(parent.context)
        .inflate(R.layout.comment_layout, parent, false))
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * Will update the contents of the itemView to reflect the item at the
     * given position.
     *
     * @param textCommentsViewHolder The RecyclerView.ViewHolder object used to describe
     * each itemView.
     * @param i The position of the item within the adapter's data set.
     * @param comment Text comment object used to populate the itemViews with data.
     */
    override fun onBindViewHolder(textCommentsViewHolder: TextCommentsViewHolder,
                                  i: Int, comment: Comment) {
        textCommentsViewHolder.userEmailTV.text = comment.commenter
        textCommentsViewHolder.commentDateTV.text = getDateFormatted(comment.timestamp.toDate())
        textCommentsViewHolder.commentTV.text = comment.comment
        val profileImageURL = comment.profilepicurl

        Glide.with(textCommentsViewHolder.userProfileIV.context)
            .load(profileImageURL).into(textCommentsViewHolder.userProfileIV)

        // Set the OnClickListener for the profile picture ImageView.
        textCommentsViewHolder.userProfileIV.setOnClickListener {
            val profileIntent = Intent(textCommentsViewHolder.userProfileIV.context, ProfileActivity::class.java)
            profileIntent.putExtra("fragment", "profile")
            profileIntent.putExtra("userid", comment.useremail)
            textCommentsViewHolder.userProfileIV.context.startActivity(profileIntent)
        }
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
    inner class TextCommentsViewHolder(@NonNull itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var userProfileIV: ImageView = itemView.findViewById(R.id.comment_layout_profilePicIV)
        var userEmailTV: TextView = itemView.findViewById(R.id.comment_layout_userNameTV)
        var commentDateTV: TextView = itemView.findViewById(R.id.comment_layout_currentDateTimeTV)
        var commentTV: TextView = itemView.findViewById(R.id.comment_layout_commentTV)
    }
}