package com.example.fittrackmobile.adaptersClasses

import android.content.Intent
import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fittrackmobile.modelClasses.Friend
import com.example.fittrackmobile.R
import com.example.fittrackmobile.activities.ProfileActivity
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import javax.annotation.Nonnull

/**
 * Adapter to display the friends list of a user.
 */
class FriendsListAdapter(options: FirestoreRecyclerOptions<Friend>) :
    FirestoreRecyclerAdapter<Friend, FriendsListAdapter.FriendsListViewHolder>(options) {

    // Class variables.
    private val TAG = "FriendsListAdapter"

    /**
     * Called by RecyclerView to display the data at the specified position.
     * Will update the contents of the itemView to reflect the item at the
     * given position.
     *
     * @param friendsListViewHolder The RecyclerView.ViewHolder object used to describe
     * each itemView.
     * @param i The position of the item within the adapter's data set.
     * @param friend User object used to populate the itemViews with data.
     */
    override fun onBindViewHolder(friendsListViewHolder: FriendsListViewHolder, i: Int, friend: Friend) {
        val fullName = friend.firstname + " " + friend.lastname
        val profileImageURL = friend.profileimageurl

        friendsListViewHolder.emailTV.text = friend.useremail
        friendsListViewHolder.fullNameTV.text = fullName

        // Load profile picture into image view.
        Glide.with(friendsListViewHolder.profileIV.context).load(profileImageURL)
            .into(friendsListViewHolder.profileIV)

        // Set the OnClickListener for the remove friend ImageView.
        friendsListViewHolder.removeFriendBtn.setOnClickListener {
            val firebaseFirestore = FirebaseFirestore.getInstance()
            val firebaseAuth = FirebaseAuth.getInstance()
            val currentUserEmail = firebaseAuth.currentUser?.email as String

            // Delete the friendship document named: currentUserEmail + userID.
            firebaseFirestore.collection("Friendships")
                .document(currentUserEmail + friend.useremail)
                .delete()
                .addOnSuccessListener {

                    // Delete the friendship document if it is named: userID + currentUserEmail.
                    firebaseFirestore.collection("Friendships")
                        .document(friend.useremail + currentUserEmail)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(
                                friendsListViewHolder.removeFriendBtn.context,
                                Resources.getSystem().getString(R.string.friend_removed_msg),
                                Toast.LENGTH_SHORT
                            ).show()
                        }.addOnFailureListener { e ->
                            Log.e(TAG, "Unable to remove friend: " + e.message)
                        }

                    // Remove the friend from the user's individual Friends collection.
                    firebaseFirestore.collection("Users")
                        .document(currentUserEmail)
                        .collection("Friends")
                        .document(friend.useremail)
                        .delete()
                        .addOnSuccessListener {

                            // Remove the user from the friends' individual Friends collection.
                            firebaseFirestore.collection("Users")
                                .document(friend.useremail)
                                .collection("Friends")
                                .document(currentUserEmail)
                                .delete()
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        friendsListViewHolder.removeFriendBtn.context,
                                        Resources.getSystem().getString(R.string.friend_removed_msg),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }.addOnFailureListener { e -> Log.e(TAG, "" + e.message) }

                        }.addOnFailureListener { e -> Log.e(TAG, "" + e.message) }

                }.addOnFailureListener { e ->
                    Log.e(TAG, "Unable to remove friend: " + e.message)
                }


        }

        // Set the OnClickListener for the profile picture to link the user profile.
        friendsListViewHolder.profileIV.setOnClickListener {
            val profileIntent = Intent(friendsListViewHolder.profileIV.context, ProfileActivity::class.java)
            profileIntent.putExtra("userid", friend.useremail)
            profileIntent.putExtra("fragment", "profile")
            friendsListViewHolder.profileIV.context.startActivity(profileIntent)

        }
    }
    /**
     * Create a new RecyclerView.ViewHolder.
     *
     * @param parent The ViewGroup into which the new View will be added after it is
     * bound to an adapter position.
     * @param viewType The view type of the new View.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsListViewHolder {
        return FriendsListViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.friend_layout, parent, false))
    }

    /**
     * Inner RecyclerView.ViewHolder class describes an item view and metadata
     * about its place within the RecyclerView.
     *
     * @param itemView The view object that is to be added to the RecyclerView.
     */
    inner class FriendsListViewHolder(@Nonnull itemView: View): RecyclerView.ViewHolder(itemView) {
        var profileIV: ImageView = itemView.findViewById(R.id.friend_layout_profilePicIV)
        var fullNameTV: TextView = itemView.findViewById(R.id.friend_layout_fullNameTV)
        var emailTV: TextView = itemView.findViewById(R.id.friend_layout_emailTV)
        var removeFriendBtn: ImageView = itemView.findViewById(R.id.friend_layout_removeFriendBtn)
    }
}